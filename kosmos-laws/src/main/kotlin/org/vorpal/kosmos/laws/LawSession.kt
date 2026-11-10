package org.vorpal.kosmos.laws

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

/**
 * One run of a law graph whose leaves are requested one at a time, in any order, possibly only some of them.
 *
 * This is what a test framework adapter needs: each displayed leaf asks for its own result, the framework may
 * filter leaves out, and shared nodes must still execute once. [LawRunner.run] is the all-at-once form.
 *
 * - Sharing: leaves are mapped to a *canonical* position (the first occurrence of the same node instance, or of
 *   a node with an equal [LawNode.key]). Asking for any occurrence executes the canonical leaf if nothing has
 *   executed it yet in this session, so selecting one path works even when the other paths are filtered out.
 * - Seeds: [LawSeeds.leaf] of the session seed and the canonical position, the same seed [LawRunner.run] gives
 *   that leaf with the same master seed.
 * - Fresh results: asking for the same displayed path a second time (a retry, a rerun) executes it again and
 *   replaces the stored result, so a retry never sees a previous attempt's outcome. A cancelled execution stores
 *   nothing.
 *
 * Fail-fast does not apply: every requested leaf is executed.
 */
class LawSession(val root: LawNode, val config: LawRunConfig = LawRunConfig()) {

    /** The master seed of this session. */
    val seed: Long = config.seed ?: Random.nextLong()

    private val leaves = LinkedHashMap<List<Int>, LawNode.Leaf>()
    private val canonical = HashMap<List<Int>, List<Int>>()
    private val results = HashMap<List<Int>, Pair<LawPath, NodeReport.LeafReport>>()
    private val served = HashSet<List<Int>>()
    private val mutex = Mutex()

    init {
        LawGraphs.requireAcyclic(root)
        val firstAt = HashMap<Any, List<Int>>()
        fun walk(node: LawNode, indices: List<Int>) {
            val key: Any = node.key ?: node
            val first = firstAt[key]
            if (first != null) {
                // A repeat: every leaf below maps to the leaf at the same relative position in the first occurrence.
                mapRepeat(node, indices, first)
                return
            }
            firstAt[key] = indices
            when (node) {
                is LawNode.Leaf -> { leaves[indices] = node; canonical[indices] = indices }
                is LawNode.Branch -> node.children.forEachIndexed { i, c -> walk(c, indices + i) }
            }
        }
        walk(root, emptyList())
    }

    private fun mapRepeat(node: LawNode, at: List<Int>, first: List<Int>) {
        when (node) {
            is LawNode.Leaf -> { leaves[at] = node; canonical[at] = canonical.getValue(first) }
            is LawNode.Branch -> node.children.forEachIndexed { i, c -> mapRepeat(c, at + i, first + i) }
        }
    }

    /** Positions of all leaf occurrences, depth-first. */
    val leafIndices: List<List<Int>> get() = leaves.keys.toList()

    /** Number of leaves executed so far in this session. */
    var executions: Int = 0
        private set

    /** The path of the node at [indices], with labels from the root. */
    fun pathOf(indices: List<Int>): LawPath = LawGraphs.path(root, indices)

    /**
     * The result of the leaf at [indices], executing its canonical leaf if needed (see the class notes).
     *
     * @throws IllegalArgumentException if [indices] does not select a leaf.
     */
    suspend fun leaf(indices: List<Int>): NodeReport.LeafReport = mutex.withLock {
        val node = leaves[indices] ?: throw IllegalArgumentException("No leaf at $indices in ${root.label}.")
        val target = canonical.getValue(indices)
        val path = pathOf(indices)
        val again = !served.add(indices)
        val stored = results[target]?.takeUnless { again }
        if (stored != null) {
            val (executedAt, report) = stored
            return@withLock if (executedAt == path) report else report.sharedAt(path) as NodeReport.LeafReport
        }
        val mark = config.timeSource.markNow()
        val outcome = node.skip?.let { LeafOutcome.Skipped(it) }
            ?: LawRunner.execute(leaves.getValue(target).law, LawSeeds.leaf(seed, target)).also { executions++ }
        val report = NodeReport.LeafReport(node, path, outcome, mark.elapsedNow())
        results[target] = path to report
        report
    }
}
