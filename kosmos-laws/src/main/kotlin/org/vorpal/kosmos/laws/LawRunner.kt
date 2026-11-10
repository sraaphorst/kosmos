package org.vorpal.kosmos.laws

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.time.TimeSource

/**
 * Settings for one run of a law graph.
 *
 * @param failFast if true, once a leaf fails or errors, every later leaf is reported as
 *   [LeafOutcome.Skipped] instead of being executed. The default collects all results.
 * @param timeSource the clock used for elapsed times (injectable so tests can control it).
 * @param seed master seed for the run. Each leaf's seed is derived from the master seed and the leaf's position
 *   (see [LawSeeds.leaf]), so the same master seed reproduces the whole run, whatever order leaves run in.
 *   Null picks a fresh random master seed; the one used is recorded in [LawReport.seed].
 */
data class LawRunConfig(
    val failFast: Boolean = false,
    val timeSource: TimeSource = TimeSource.Monotonic,
    val seed: Long? = null
)

/**
 * Executes [LawNode] graphs and produces [LawReport]s.
 *
 * Every call to [run] uses fresh state, so nothing carries over from one run to the next.
 *
 * Within a run, a node reached a second time (the same instance, or a node with an equal [LawNode.key]) is not
 * executed again: its first report is shown at the new path too, marked with [NodeReport.sharedFrom].
 * Cancellation of the calling coroutine is never swallowed: a [CancellationException] thrown
 * by a law propagates out of [run].
 */
object LawRunner {

    suspend fun run(root: LawNode, config: LawRunConfig = LawRunConfig()): LawReport {
        LawGraphs.requireAcyclic(root)
        val run = Run(config)
        return LawReport(run.visit(root, LawPath.root(root.label)), run.master, config)
    }

    /**
     * Run a single leaf again with a given seed, typically one recorded in a [LeafDiagnostics].
     *
     * [indices] is the leaf's [LawPath.indices] in the graph rooted at [root]. With the same code, generator,
     * settings and library versions, a law that uses [lawCheckAll] produces the same counterexample again.
     */
    suspend fun replay(root: LawNode, indices: List<Int>, seed: Long, config: LawRunConfig = LawRunConfig()): LawReport {
        LawGraphs.requireAcyclic(root)
        val node = LawGraphs.select(root, indices)
        require(node is LawNode.Leaf) { "Path $indices selects a branch (${node.label}), not a leaf." }
        val mark = config.timeSource.markNow()
        val outcome = execute(node.law, seed)
        return LawReport(NodeReport.LeafReport(node, LawPath.root(node.label), outcome, mark.elapsedNow()), null, config)
    }

    /**
     * Classify how a leaf ended.
     *
     * - The final failure is a [LawViolation]: the law is false ([LeafOutcome.Failed]).
     * - The law signals falsity explicitly (it uses [lawCheckAll]) and something else was thrown:
     *   [LeafOutcome.Errored] (the operation, generator, equality or harness failed).
     * - Legacy laws (no explicit signal): an [AssertionError] is [LeafOutcome.Failed], anything else
     *   [LeafOutcome.Errored]. Kotest's `checkAll` wraps every exception thrown inside a property in an
     *   [AssertionError], so for legacy laws a throwing operation inside a property reads as failed.
     */
    internal fun classify(thrown: Throwable, capture: PropertyCapture, seed: Long): LeafOutcome {
        val cause = capture.lastCause ?: unwrapToViolation(thrown) ?: thrown
        val diagnostics = LeafDiagnostics(
            seed = if (capture.explicit) seed else null,
            counterexample = capture.counterexample,
            cause = cause
        )
        return when {
            cause is LawViolation -> LeafOutcome.Failed(thrown, diagnostics)
            capture.explicit -> LeafOutcome.Errored(thrown, diagnostics)
            thrown is AssertionError -> LeafOutcome.Failed(thrown, diagnostics)
            else -> LeafOutcome.Errored(thrown, diagnostics)
        }
    }

    private fun unwrapToViolation(t: Throwable): Throwable? =
        generateSequence(t) { it.cause.takeIf { c -> c !== it } }.firstOrNull { it is LawViolation }

    internal suspend fun execute(law: TestingLaw, seed: Long): LeafOutcome {
        val capture = PropertyCapture()
        // Catch inside withContext and return the throwable as a value: an exception crossing the
        // withContext boundary can be replaced by a copy (kotlinx.coroutines stack-trace recovery),
        // and the report must keep the original instance.
        val thrown: Throwable? = withContext(LawExecution(seed, capture)) {
            try {
                law.test()
                null
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                t
            }
        }
        if (thrown == null) return LeafOutcome.Passed

        // Kotest's checkAll catches every Throwable from a property, including a CancellationException,
        // and rethrows it wrapped in an AssertionError. Never turn a cancellation into a result.
        currentCoroutineContext().ensureActive()
        generateSequence(thrown) { it.cause.takeIf { c -> c !== it } }
            .firstOrNull { it is CancellationException }
            ?.let { throw it }
        return classify(thrown, capture, seed)
    }

    private class Run(val config: LawRunConfig) {
        var stopped: String? = null
        val master: Long = config.seed ?: Random.nextLong()

        /**
         * First report of every node reached so far in this run, by [LawNode.key] (or by the node instance when
         * it has no key). Lives only as long as the run.
         */
        val done = HashMap<Any, NodeReport>()

        suspend fun visit(node: LawNode, path: LawPath): NodeReport {
            val cacheKey: Any = node.key ?: node
            done[cacheKey]?.let { return it.sharedAt(path) }
            return fresh(node, path).also { done[cacheKey] = it }
        }

        private suspend fun fresh(node: LawNode, path: LawPath): NodeReport {
            val mark = config.timeSource.markNow()
            return when (node) {
                is LawNode.Leaf -> {
                    val seed = LawSeeds.leaf(master, path.indices)
                    val outcome = node.skip?.let { LeafOutcome.Skipped(it) }
                        ?: stopped?.let { LeafOutcome.Skipped(it) }
                        ?: execute(node.law, seed)
                    if (config.failFast && outcome.status.isProblem && stopped == null) {
                        stopped = "fail-fast: stopped after $path"
                    }
                    NodeReport.LeafReport(node, path, outcome, mark.elapsedNow())
                }

                is LawNode.Branch -> {
                    val children = node.children.mapIndexed { i, child ->
                        visit(child, path.child(child.label, i))
                    }
                    NodeReport.BranchReport(node, path, children, mark.elapsedNow())
                }
            }
        }
    }
}

/** Run this node with [LawRunner]. */
suspend fun LawNode.verify(config: LawRunConfig = LawRunConfig()): LawReport =
    LawRunner.run(this, config)

/** Run this suite's graph in the given view with [LawRunner]. */
suspend fun LawSuite.verify(mode: LawMode = LawMode.COMPACT, config: LawRunConfig = LawRunConfig()): LawReport =
    LawRunner.run(node(mode), config)

/**
 * Build an ad hoc law graph mixing suites, standalone laws and other nodes.
 *
 * ```
 * lawGraph("IntegerCommutativeRing") {
 *     suite(CommutativeRingLaws(ring, arb, eq, pr), LawMode.FULL)
 *     law(NormSqMultiplicativeLaw(...))
 *     branch("norm") { law(...); law(...) }
 * }.verify().throwIfFailed()
 * ```
 */
fun lawGraph(label: String, build: LawGraphBuilder.() -> Unit): LawNode =
    LawGraphBuilder().apply(build).build(label)

class LawGraphBuilder {
    private val children = mutableListOf<LawNode>()

    fun law(law: TestingLaw) { children += LawNode.Leaf(law) }
    fun law(label: String, law: TestingLaw) { children += LawNode.Leaf(law, label) }
    fun skipped(law: TestingLaw, reason: String) { children += LawNode.Leaf(law, skip = reason) }
    fun suite(suite: LawSuite, mode: LawMode = LawMode.COMPACT) { children += suite.node(mode) }
    fun node(node: LawNode) { children += node }
    fun branch(label: String, build: LawGraphBuilder.() -> Unit) { children += lawGraph(label, build) }

    internal fun build(label: String): LawNode = LawNode.Branch(label, children.toList())
}

/** Seed derivation shared by [LawRunner] and [LawSession]. */
object LawSeeds {
    /**
     * The seed of the leaf at [indices] in a run with the given [master] seed: a SplitMix64 hash of both.
     * It depends only on the master seed and the position, never on execution order.
     */
    fun leaf(master: Long, indices: List<Int>): Long =
        mix(indices.fold(mix(master)) { h, i -> mix(h xor (i + 1L) * GOLDEN) })

    private const val GOLDEN = -0x61c8864680b583ebL

    private fun mix(z0: Long): Long {
        var z = z0 + GOLDEN
        z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
        z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L
        return z xor (z ushr 31)
    }
}
