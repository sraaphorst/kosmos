package org.vorpal.kosmos.testing.laws

import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.core.spec.style.scopes.FunSpecRootScope
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawReportText
import org.vorpal.kosmos.laws.LawRunConfig
import org.vorpal.kosmos.laws.LawSession
import org.vorpal.kosmos.laws.LawStatus
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.LeafOutcome

/**
 * Nested Kotest registration of law graphs: every branch becomes a Kotest container and every leaf a test, so
 * Gradle and the IDE show the law tree and can run a whole root or a single law.
 *
 * ```
 * class RationalFieldSpec : FunSpec({
 *     lawTests(FieldLaws(RationalField, ArbRational.small, eqRational, printableRationalPretty), LawMode.FULL)
 *     test("zero is Rational.ZERO") { ... }
 * })
 * ```
 *
 * - One [LawSession] per execution of the root container: shared nodes execute once within it, and a new run of
 *   the spec starts a new session, so nothing leaks between runs.
 * - Selecting a single leaf runs that law even if the other paths to the same shared node are filtered out.
 * - A retried test asks the session for the same path again, which executes the law again.
 * - Sibling labels that repeat get a ` [2]`, ` [3]`, ... suffix, since Kotest names must be unique among siblings.
 * - A leaf with an explicit skip reason is registered as disabled, with the reason in its name.
 *
 * The aggregated style (`suite.fullTest().throwIfFailed()` in one test, or `suite.verify(...).throwIfFailed()`)
 * keeps working; use whichever reads better for the spec.
 */
fun FunSpecRootScope.lawTests(root: LawNode, config: LawRunConfig = LawRunConfig()) {
    val node = root as? LawNode.Branch ?: LawNode.Branch(root.label, listOf(root))
    context(node.label) {
        registerChildren(LawSession(node, config), node, emptyList())
    }
}

/** Register a suite's graph in the given view (see the root-scope [lawTests]). */
fun FunSpecRootScope.lawTests(suite: LawSuite, mode: LawMode = LawMode.FULL, config: LawRunConfig = LawRunConfig()) =
    lawTests(suite.node(mode), config)

/** Register a law graph inside an existing container. */
suspend fun FunSpecContainerScope.lawTests(root: LawNode, config: LawRunConfig = LawRunConfig()) {
    val node = root as? LawNode.Branch ?: LawNode.Branch(root.label, listOf(root))
    context(node.label) {
        registerChildren(LawSession(node, config), node, emptyList())
    }
}

/** Register a suite's graph inside an existing container. */
suspend fun FunSpecContainerScope.lawTests(suite: LawSuite, mode: LawMode = LawMode.FULL, config: LawRunConfig = LawRunConfig()) =
    lawTests(suite.node(mode), config)

/** Kotest display names for siblings: labels, with ` [k]` added to the k-th repeat of a label (k ≥ 2). */
internal fun displayNames(labels: List<String>): List<String> {
    val seen = HashMap<String, Int>()
    return labels.map { label ->
        val k = seen.merge(label, 1, Int::plus)!!
        if (k == 1) label else "$label [$k]"
    }
}

private suspend fun FunSpecContainerScope.registerChildren(session: LawSession, branch: LawNode.Branch, at: List<Int>) {
    val names = displayNames(branch.children.map { it.label })
    branch.children.forEachIndexed { i, child ->
        val indices = at + i
        when (child) {
            is LawNode.Branch -> context(names[i]) { registerChildren(session, child, indices) }
            is LawNode.Leaf -> {
                val skip = child.skip
                if (skip != null) xtest("${names[i]} (skipped: $skip)") { }
                else test(names[i]) { checkLeaf(session, indices) }
            }
        }
    }
}

private suspend fun checkLeaf(session: LawSession, indices: List<Int>) {
    val leaf = session.leaf(indices)
    if (!leaf.status.isProblem) return
    val error = when (val o = leaf.outcome) {
        is LeafOutcome.Failed -> o.error
        is LeafOutcome.Errored -> o.error
        else -> null
    }
    val message = buildString {
        appendLine("${leaf.status}: ${leaf.path}")
        LawReportText.details(leaf).forEach { appendLine("  $it") }
        if (leaf.status == LawStatus.ERRORED) append("  (errored: the law could not be evaluated; see the cause)")
    }
    throw AssertionError(message, error)
}
