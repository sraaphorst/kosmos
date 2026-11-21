package org.vorpal.kosmos.testing.laws

import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.core.spec.style.scopes.FunSpecRootScope
import org.vorpal.kosmos.laws.LawComponent
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawPath
import org.vorpal.kosmos.laws.LawReportText
import org.vorpal.kosmos.laws.LawRunConfig
import org.vorpal.kosmos.laws.LawSession
import org.vorpal.kosmos.laws.LawStatus
import org.vorpal.kosmos.laws.LeafOutcome
import org.vorpal.kosmos.laws.NodeReport

/**
 * Nested Kotest registration of law graphs: branches become Kotest containers and leaves tests, so Gradle and the
 * IDE show the law tree and can run a whole root or a single law.
 *
 * **One test per distinct check.** A law reached along several inheritance paths (say `associativity (*)` under
 * both `Ring` and `StarAlgebra`) is registered once, at its first path in depth-first order, so Kotest's count is
 * the number of checks rather than the number of paths. Containers left with no test are not registered. The
 * other paths are not lost: a failing test lists them, and `verify()` / [LawReportText] still show every path
 * (the repeats marked as shared).
 *
 * ```
 * class RationalFieldSpec : FunSpec({
 *     lawTests(FieldLaws(RationalField, ArbRational.small, eqRational, printableRationalPretty))
 *     test("zero is Rational.ZERO") { ... }
 * })
 * ```
 *
 * - One [LawSession] per execution of the root container: each check executes once within it, and a new run of
 *   the spec starts a new session, so nothing leaks between runs.
 * - A retried test asks the session for the same path again, which executes the law again.
 * - Sibling labels that repeat get a ` [2]`, ` [3]`, ... suffix, since Kotest names must be unique among siblings.
 * - A leaf with an explicit skip reason is registered as disabled, with the reason in its name.
 *
 * The aggregated style (`suite.verify().throwIfFailed()` in one test) keeps working; use whichever reads better
 * for the spec.
 */
fun FunSpecRootScope.lawTests(root: LawNode, config: LawRunConfig = LawRunConfig()) {
    val node = root as? LawNode.Branch ?: LawNode.Branch(root.label, listOf(root))
    context(node.label) {
        registerChildren(LawSession(node, config), node, emptyList())
    }
}

/** Register a suite's or law's graph (see the root-scope [lawTests]). */
fun FunSpecRootScope.lawTests(component: LawComponent, config: LawRunConfig = LawRunConfig()) =
    lawTests(component.node(), config)

/** Register a law graph inside an existing container. */
suspend fun FunSpecContainerScope.lawTests(root: LawNode, config: LawRunConfig = LawRunConfig()) {
    val node = root as? LawNode.Branch ?: LawNode.Branch(root.label, listOf(root))
    context(node.label) {
        registerChildren(LawSession(node, config), node, emptyList())
    }
}

/** Register a suite's or law's graph inside an existing container. */
suspend fun FunSpecContainerScope.lawTests(component: LawComponent, config: LawRunConfig = LawRunConfig()) =
    lawTests(component.node(), config)

/**
 * Kotest display names for siblings: each label, with ` [k]` added to a repeat, and every final name unique.
 *
 * Labels are reserved first, so a suffixed name never takes a name that another sibling already has as its label
 * (labels `a`, `a`, `a [2]` become `a`, `a [3]`, `a [2]`).
 */
internal fun displayNames(labels: List<String>): List<String> {
    val used = HashSet<String>()
    val firstOf = HashMap<String, Int>()
    labels.forEachIndexed { i, label -> if (firstOf.putIfAbsent(label, i) == null) used += label }
    return labels.mapIndexed { i, label ->
        if (firstOf[label] == i) label
        else generateSequence(2) { it + 1 }.map { "$label [$it]" }.first { it !in used }.also { used += it }
    }
}

/** The name a child is shown under before uniquifying: its label, plus the skip reason for a skipped leaf. */
internal fun baseName(node: LawNode): String =
    (node as? LawNode.Leaf)?.skip?.let { "${node.label} (skipped: $it)" } ?: node.label

/**
 * The positions the adapter registers: each distinct check (its first occurrence, see
 * [LawSession.uniqueLeafIndices]) and every branch above one. Repeated occurrences, and branches holding only
 * repeats, are left out.
 */
internal fun registeredPositions(session: LawSession): Set<List<Int>> =
    session.uniqueLeafIndices.flatMapTo(HashSet()) { leaf -> (0..leaf.size).map { leaf.subList(0, it) } }

private suspend fun FunSpecContainerScope.registerChildren(session: LawSession, branch: LawNode.Branch, at: List<Int>) =
    registerChildren(session, registeredPositions(session), branch, at)

private suspend fun FunSpecContainerScope.registerChildren(
    session: LawSession,
    registered: Set<List<Int>>,
    branch: LawNode.Branch,
    at: List<Int>
) {
    // Uniquify the final names (skip annotation included), so no two siblings can collide. Names are computed over
    // all children, so a check's name does not depend on which of its siblings are repeats.
    val names = displayNames(branch.children.map(::baseName))
    branch.children.forEachIndexed { i, child ->
        val indices = at + i
        if (indices !in registered) return@forEachIndexed
        when (child) {
            is LawNode.Branch -> context(names[i]) { registerChildren(session, registered, child, indices) }
            is LawNode.Leaf ->
                if (child.skip != null) xtest(names[i]) { }
                else test(names[i]) { checkLeaf(session, indices) }
        }
    }
}

/** How many of the other paths to a failing check its message lists. */
private const val MAX_SHARED_PATHS = 10

private suspend fun checkLeaf(session: LawSession, indices: List<Int>) {
    val leaf = session.leaf(indices)
    if (!leaf.status.isProblem) return
    val error = when (val o = leaf.outcome) {
        is LeafOutcome.Failed -> o.error
        is LeafOutcome.Errored -> o.error
        else -> null
    }
    throw AssertionError(failureMessage(leaf, session.sharedPaths(indices)), error)
}

/** The message of a failing test: the leaf's status, path and details, then the other paths sharing the check. */
internal fun failureMessage(leaf: NodeReport.LeafReport, shared: List<LawPath>): String = buildString {
    appendLine("${leaf.status}: ${leaf.path}")
    LawReportText.details(leaf).forEach { appendLine("  $it") }
    if (shared.isNotEmpty()) {
        appendLine("  the same check is also reached via ${shared.size} other path(s):")
        shared.take(MAX_SHARED_PATHS).forEach { appendLine("    $it") }
        if (shared.size > MAX_SHARED_PATHS) appendLine("    ... and ${shared.size - MAX_SHARED_PATHS} more")
    }
    if (leaf.status == LawStatus.ERRORED) append("  (errored: the law could not be evaluated; see the cause)")
}

