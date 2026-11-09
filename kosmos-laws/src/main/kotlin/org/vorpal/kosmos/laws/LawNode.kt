package org.vorpal.kosmos.laws

/**
 * Which view of a [LawSuite] to use.
 *
 * - [COMPACT] is what [LawSuite.laws] returns: the axioms a suite adds, plus whatever
 *   each suite has historically chosen to include from its constituents.
 * - [FULL] is what [LawSuite.fullLaws] returns: the compact laws together with the
 *   full checks of the constituent structures (e.g. a ring includes its additive group).
 *
 * The two views are defined by each suite: a suite may pick a different mode for a child
 * than its own (for example, the full view of a hemiring uses the full view of its
 * additive commutative monoid).
 */
enum class LawMode { COMPACT, FULL }

/**
 * One node of a law graph: either a single law ([Leaf]) or an ordered group of nodes ([Branch]).
 *
 * Law graphs are directed acyclic graphs: the same node object may appear under several
 * parents. Nodes are definitions only; running them is the job of [LawRunner], which keeps
 * all execution state and results outside the nodes.
 */
sealed interface LawNode {

    /** Display label. Labels are for people; they never establish that two nodes are the same check. */
    val label: String

    /** A single [TestingLaw]. */
    class Leaf(
        val law: TestingLaw,
        override val label: String = law.name
    ) : LawNode {
        override fun toString(): String = "Leaf($label)"
    }

    /** An ordered group of child nodes, such as a law suite or an ad hoc composition. */
    class Branch(
        override val label: String,
        val children: List<LawNode>
    ) : LawNode {
        override fun toString(): String = "Branch($label, ${children.size} children)"
    }

    companion object {
        /** Lift a single law into the graph. */
        fun of(law: TestingLaw): LawNode = Leaf(law)

        /** Lift a suite into the graph, using the given view. */
        fun of(suite: LawSuite, mode: LawMode = LawMode.COMPACT): LawNode = suite.node(mode)

        /** Group nodes under one label. */
        fun branch(label: String, vararg children: LawNode): LawNode = Branch(label, children.toList())

        /**
         * Compatibility adapter: wrap a suite's selected flat law list as a branch of leaves.
         *
         * This is the default [LawSuite.node] for suites that do not declare their hierarchy.
         */
        fun flat(label: String, laws: List<TestingLaw>): LawNode = Branch(label, laws.map(::Leaf))
    }
}

/**
 * Flatten a node to its leaves in depth-first, left-to-right order.
 *
 * This is occurrence-based: a node reachable along several paths contributes its laws once per path,
 * which is exactly how the legacy [LawSuite.laws] / [LawSuite.fullLaws] lists behave.
 */
fun LawNode.flatten(): List<TestingLaw> {
    LawGraphs.requireAcyclic(this)
    return buildList { collectLaws(this@flatten, this) }
}

private fun collectLaws(node: LawNode, out: MutableList<TestingLaw>) {
    when (node) {
        is LawNode.Leaf -> out.add(node.law)
        is LawNode.Branch -> node.children.forEach { collectLaws(it, out) }
    }
}

/** Thrown when a law graph contains a cycle. */
class LawGraphCycleException(val cycle: List<String>) :
    IllegalArgumentException("Law graph contains a cycle: ${cycle.joinToString(" > ")}")

object LawGraphs {
    /**
     * Throws [LawGraphCycleException] if [root] reaches itself.
     *
     * Node identity is reference identity: a node shared by two parents is fine (that is a DAG),
     * but a node that is its own descendant is not.
     */
    fun requireAcyclic(root: LawNode) {
        val done = java.util.Collections.newSetFromMap(java.util.IdentityHashMap<LawNode, Boolean>())
        val onStack = java.util.IdentityHashMap<LawNode, Int>()
        val stack = ArrayList<LawNode>()

        fun visit(node: LawNode) {
            if (node in done) return
            onStack[node]?.let { start ->
                throw LawGraphCycleException((stack.subList(start, stack.size) + node).map { it.label })
            }
            if (node is LawNode.Branch) {
                onStack[node] = stack.size
                stack.add(node)
                node.children.forEach(::visit)
                stack.removeAt(stack.lastIndex)
                onStack.remove(node)
            }
            done.add(node)
        }

        visit(root)
    }
}
