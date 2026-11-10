package org.vorpal.kosmos.laws

/**
 * Plain-text views of a [LawReport].
 *
 * - [tree]: the whole graph, one line per node, with each node's status.
 * - [failures]: only the branches that lead to a failed or errored leaf, followed by the details of each problem
 *   (counterexample, seed and replay call, cause chain).
 *
 * Timing is left out unless asked for, so the output is stable and can be compared in tests.
 * Labels carry the operation roles (`Semigroup (*)`, `associativity (*)`), so paths say which operation failed.
 */
object LawReportText {

    /** The whole report as a tree. A shared repeat is shown as one line pointing at the original. */
    fun tree(report: LawReport, timing: Boolean = false): String = buildString {
        appendLine(summary(report))
        render(report.root, "", "", timing) { true }
    }

    /** The failing paths only, then the details of every failed or errored leaf. */
    fun failures(report: LawReport, timing: Boolean = false): String = buildString {
        appendLine(summary(report))
        val problems = report.problems
        if (problems.isEmpty()) {
            if (!report.hasEvidence) appendLine("No leaf passed, so this run provides no evidence.")
            else appendLine("No failures.")
            return@buildString
        }
        render(report.root, "", "", timing) { it.status.isProblem }
        problems.forEachIndexed { i, leaf ->
            appendLine()
            appendLine("#${i + 1} ${leaf.status}: ${leaf.path}")
            details(leaf).forEach { appendLine("  $it") }
        }
    }

    private fun summary(report: LawReport): String = with(report.counts) {
        buildString {
            append("${report.root.label}: ${report.status}")
            append(" (passed $passed, failed $failed, errored $errored, skipped $skipped")
            append("; ${report.executions} executions")
            report.seed?.let { append("; seed $it") }
            append(")")
        }
    }

    private fun StringBuilder.render(
        node: NodeReport,
        lead: String,
        childLead: String,
        timing: Boolean,
        show: (NodeReport) -> Boolean
    ) {
        append(lead).append(line(node, timing)).append('\n')
        if (node !is NodeReport.BranchReport || node.sharedFrom != null) return
        val shown = node.children.filter(show)
        shown.forEachIndexed { i, child ->
            val last = i == shown.lastIndex
            render(child, childLead + if (last) "└─ " else "├─ ", childLead + if (last) "   " else "│  ", timing, show)
        }
    }

    private fun line(node: NodeReport, timing: Boolean): String = buildString {
        append(node.label).append("  ").append(node.status)
        if (node is NodeReport.BranchReport) {
            val c = node.counts
            if (c.total > 0 && node.status != LawStatus.PASSED) append(" [${c.passed}/${c.total} passed]")
        }
        (node as? NodeReport.LeafReport)?.outcome?.let { if (it is LeafOutcome.Skipped) append(" (${it.reason})") }
        node.sharedFrom?.let { append("  (shared: same check as $it)") }
        if (timing && node.sharedFrom == null) append("  ${node.elapsed.inWholeMilliseconds} ms")
    }

    /** Detail lines for one problem leaf. */
    fun details(leaf: NodeReport.LeafReport): List<String> = buildList {
        leaf.sharedFrom?.let {
            add("same check as $it (executed once, shown under both paths)")
            return@buildList
        }
        val error = when (val o = leaf.outcome) {
            is LeafOutcome.Failed -> o.error
            is LeafOutcome.Errored -> o.error
            else -> null
        }
        val d = leaf.diagnostics
        d?.counterexample?.let { add("counterexample: $it") }
        d?.seed?.let { add("seed: $it (replay: ${replayCall(leaf.path.indices, it)})") }
        val cause = d?.cause ?: error
        if (cause != null) causeChain(cause).forEachIndexed { i, t ->
            val head = if (i == 0) "cause: " else "caused by: "
            val msg = t.message?.lines()?.filter { it.isNotBlank() }.orEmpty()
            add(head + t::class.qualifiedName + (msg.firstOrNull()?.let { ": $it" } ?: ""))
            msg.drop(1).forEach { add("  $it") }
        }
    }

    /** The replay call as valid Kotlin, e.g. `LawRunner.replay(root, listOf(1, 0), -42L)`. */
    fun replayCall(indices: List<Int>, seed: Long): String =
        "LawRunner.replay(root, listOf(${indices.joinToString(", ")}), ${seed}L)"

    private fun causeChain(t: Throwable): List<Throwable> =
        generateSequence(t) { it.cause.takeIf { c -> c !== it } }.take(5).toList()
}

/** The whole report as a text tree (see [LawReportText.tree]). */
fun LawReport.renderTree(timing: Boolean = false): String = LawReportText.tree(this, timing)

/** The failing paths and their details (see [LawReportText.failures]). */
fun LawReport.renderFailures(timing: Boolean = false): String = LawReportText.failures(this, timing)
