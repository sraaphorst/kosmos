package org.vorpal.kosmos.laws

/**
 * Anything that can be placed in a law graph: a single [TestingLaw] (a leaf) or a [LawSuite] (a branch).
 */
interface LawComponent {
    /** This component as a node of a law graph. */
    fun node(): LawNode
}

/**
 * One node of a law graph: either a single law ([Leaf]) or an ordered group of nodes ([Branch]).
 *
 * Law graphs are directed acyclic graphs: the same node object may appear under several
 * parents, and so may nodes with equal [key]s. Nodes are definitions only; running them is the job of [LawRunner], which keeps
 * all execution state and results outside the nodes.
 */
sealed interface LawNode {

    /** Display label. Labels are for people; they never establish that two nodes are the same check. */
    val label: String

    /**
     * Identity of the check this node performs, if known (see [LawBinding]). Within one run, nodes with equal
     * keys are executed once and the result is shown under every parent. The same node *instance* reached twice
     * is always shared; a null key never matches a different instance, and neither does a skipped leaf.
     */
    val key: LawBinding?

    /**
     * A single [TestingLaw].
     *
     * @param skip if non-null, the leaf is deliberately not executed and is reported as
     *   [LeafOutcome.Skipped] with this reason.
     */
    class Leaf(
        val law: TestingLaw,
        override val label: String = law.name,
        val skip: String? = null,
        override val key: LawBinding? = law.binding
    ) : LawNode {
        override fun toString(): String = "Leaf($label)"
    }

    /** An ordered group of child nodes, such as a law suite or an ad hoc composition. */
    class Branch(
        override val label: String,
        val children: List<LawNode>,
        override val key: LawBinding? = null
    ) : LawNode {
        override fun toString(): String = "Branch($label, ${children.size} children)"
    }

    companion object {
        /** Lift a single law or a suite into the graph. */
        fun of(component: LawComponent): LawNode = component.node()

        /** Group nodes under one label. */
        fun branch(label: String, vararg children: LawNode): LawNode = Branch(label, children.toList())
    }
}

/**
 * Flatten a node to its leaves in depth-first, left-to-right order.
 *
 * This is occurrence-based: a node reachable along several paths contributes its laws once per path.
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
    /** The [LawPath] of the node at [indices] below [root]: labels from the root down, and the indices. */
    fun path(root: LawNode, indices: List<Int>): LawPath {
        var node = root
        var path = LawPath.root(root.label)
        indices.forEach { i ->
            val children = (node as? LawNode.Branch)?.children
            require(children != null && i in children.indices) { "No node at $indices below ${root.label}." }
            node = children[i]
            path = path.child(node.label, i)
        }
        return path
    }

    /** The node reached from [root] by taking child [indices] in turn (a [LawPath.indices] selector). */
    fun select(root: LawNode, indices: List<Int>): LawNode =
        indices.fold(root) { node, i ->
            require(node is LawNode.Branch) { "Cannot descend into leaf ${node.label} with index $i." }
            require(i in node.children.indices) { "Index $i is out of range for ${node.label}." }
            node.children[i]
        }

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

/** Lift this law or suite into a graph node. */
fun LawComponent.asNode(): LawNode = node()

/** Lift these laws and suites into graph nodes, in order. */
fun List<LawComponent>.asNodes(): List<LawNode> = map { it.node() }
