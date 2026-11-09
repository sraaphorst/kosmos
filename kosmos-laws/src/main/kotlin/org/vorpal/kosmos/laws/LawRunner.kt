package org.vorpal.kosmos.laws

import kotlinx.coroutines.CancellationException
import kotlin.time.TimeSource

/**
 * Settings for one run of a law graph.
 *
 * @param failFast if true, once a leaf fails or errors, every later leaf is reported as
 *   [LeafOutcome.Skipped] instead of being executed. The default collects all results.
 * @param timeSource the clock used for elapsed times (injectable so tests can control it).
 */
data class LawRunConfig(
    val failFast: Boolean = false,
    val timeSource: TimeSource = TimeSource.Monotonic
)

/**
 * Executes [LawNode] graphs and produces [LawReport]s.
 *
 * Every call to [run] uses fresh state, so nothing carries over from one run to the next.
 * Cancellation of the calling coroutine is never swallowed: a [CancellationException] thrown
 * by a law propagates out of [run].
 */
object LawRunner {

    suspend fun run(root: LawNode, config: LawRunConfig = LawRunConfig()): LawReport {
        LawGraphs.requireAcyclic(root)
        return LawReport(Run(config).visit(root, LawPath.root(root.label)))
    }

    /**
     * Classify a throwable from a law that has no finer-grained signal.
     *
     * An [AssertionError] means the law was false ([LeafOutcome.Failed]); anything else means it
     * could not be evaluated ([LeafOutcome.Errored]).
     *
     * Caveat: Kotest's `checkAll` reports *any* exception thrown inside a property as an
     * [AssertionError], so until property laws signal their failures explicitly, an exception
     * thrown by the operation under test inside `checkAll` is classified as failed.
     */
    fun classify(t: Throwable): LeafOutcome =
        if (t is AssertionError) LeafOutcome.Failed(t) else LeafOutcome.Errored(t)

    private class Run(val config: LawRunConfig) {
        var stopped: String? = null

        suspend fun visit(node: LawNode, path: LawPath): NodeReport {
            val mark = config.timeSource.markNow()
            return when (node) {
                is LawNode.Leaf -> {
                    val outcome = stopped?.let { LeafOutcome.Skipped(it) } ?: execute(node.law)
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

        suspend fun execute(law: TestingLaw): LeafOutcome =
            try {
                law.test()
                LeafOutcome.Passed
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                classify(t)
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
    fun suite(suite: LawSuite, mode: LawMode = LawMode.COMPACT) { children += suite.node(mode) }
    fun node(node: LawNode) { children += node }
    fun branch(label: String, build: LawGraphBuilder.() -> Unit) { children += lawGraph(label, build) }

    internal fun build(label: String): LawNode = LawNode.Branch(label, children.toList())
}
