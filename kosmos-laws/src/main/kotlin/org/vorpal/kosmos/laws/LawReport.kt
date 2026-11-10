package org.vorpal.kosmos.laws

import kotlin.time.Duration

/**
 * Outcome of a node.
 *
 * Branches take the most severe state among their children, in the order
 * [ERRORED] > [FAILED] > [PASSED] > [SKIPPED]. A branch whose leaves were all skipped
 * (or that has no leaves at all) is [SKIPPED]: it provides no evidence.
 */
enum class LawStatus(val severity: Int) {
    SKIPPED(0),
    PASSED(1),
    FAILED(2),
    ERRORED(3);

    val isProblem: Boolean
        get() = this == FAILED || this == ERRORED

    companion object {
        fun worst(statuses: Iterable<LawStatus>): LawStatus =
            statuses.maxByOrNull { it.severity } ?: SKIPPED
    }
}

/**
 * What is known about one failed or errored leaf execution, for diagnosis and replay.
 *
 * @property seed the seed injected into the law's property checks, or null if the law does not use
 *   [lawCheckAll] (legacy laws pick their own random seed, which the runner cannot see).
 * @property counterexample the final (shrunk) failing input, rendered with the law's `Printable`, or null
 *   if the failure happened outside a property evaluation (a deterministic check, the generator, setup).
 * @property cause the throwable of the final failing evaluation (for a property, after shrinking);
 *   the throwable the leaf threw (often Kotest's wrapper) is kept in [LeafOutcome.Failed.error] /
 *   [LeafOutcome.Errored.error].
 */
data class LeafDiagnostics(
    val seed: Long?,
    val counterexample: String?,
    val cause: Throwable
) {
    /** Human-readable notes on which fields are missing and why. */
    val unavailable: List<String>
        get() = buildList {
            if (seed == null) add("seed: the law does not use lawCheckAll, so its seed is chosen by Kotest")
            if (counterexample == null) add("counterexample: the failure did not happen inside a property evaluation")
        }
}

/** How a single leaf ended. */
sealed interface LeafOutcome {
    val status: LawStatus

    data object Passed : LeafOutcome {
        override val status = LawStatus.PASSED
    }

    /** The law is false. [error] is the original throwable. */
    data class Failed(val error: Throwable, val diagnostics: LeafDiagnostics? = null) : LeafOutcome {
        override val status = LawStatus.FAILED
    }

    /** The law could not be evaluated: something other than the law's own falsity signal was thrown. */
    data class Errored(val error: Throwable, val diagnostics: LeafDiagnostics? = null) : LeafOutcome {
        override val status = LawStatus.ERRORED
    }

    /** The leaf was deliberately not executed. */
    data class Skipped(val reason: String) : LeafOutcome {
        override val status = LawStatus.SKIPPED
    }
}

/** Number of leaves in each state below a node (occurrences, counted once per path). */
data class LawCounts(
    val passed: Int = 0,
    val failed: Int = 0,
    val errored: Int = 0,
    val skipped: Int = 0
) {
    val total: Int get() = passed + failed + errored + skipped
    val executed: Int get() = passed + failed + errored

    operator fun plus(other: LawCounts) = LawCounts(
        passed + other.passed,
        failed + other.failed,
        errored + other.errored,
        skipped + other.skipped
    )

    companion object {
        val ZERO = LawCounts()

        fun of(status: LawStatus) = when (status) {
            LawStatus.PASSED -> LawCounts(passed = 1)
            LawStatus.FAILED -> LawCounts(failed = 1)
            LawStatus.ERRORED -> LawCounts(errored = 1)
            LawStatus.SKIPPED -> LawCounts(skipped = 1)
        }
    }
}

/**
 * Where a node sits in a run: the labels from the root down, and the child index taken at each step.
 *
 * Labels are for display. [indices] is the stable selector: sibling labels may repeat, indices do not.
 */
data class LawPath(val labels: List<String>, val indices: List<Int>) {
    init {
        require(labels.size == indices.size + 1) { "A path has one more label than indices (the root has none)." }
    }

    fun child(label: String, index: Int) = LawPath(labels + label, indices + index)

    override fun toString(): String = labels.joinToString(" > ")

    companion object {
        fun root(label: String) = LawPath(listOf(label), emptyList())
    }
}

/** The result of one node in one run. Mirrors the shape of the [LawNode] graph that was run. */
sealed interface NodeReport {
    val node: LawNode
    val path: LawPath
    val status: LawStatus
    val counts: LawCounts
    val elapsed: Duration

    val label: String get() = node.label

    /**
     * Null if this node was executed at [path]. Otherwise the path at which the same check was executed
     * earlier in the run; this report repeats that result without running it again (and its [elapsed] is zero).
     */
    val sharedFrom: LawPath?

    /** This report, shown at another [path] as a repeat of the original (see [sharedFrom]). */
    fun sharedAt(path: LawPath): NodeReport

    data class LeafReport(
        override val node: LawNode.Leaf,
        override val path: LawPath,
        val outcome: LeafOutcome,
        override val elapsed: Duration,
        override val sharedFrom: LawPath? = null
    ) : NodeReport {
        override fun sharedAt(path: LawPath): LeafReport =
            LeafReport(node, path, outcome, Duration.ZERO, sharedFrom ?: this.path)

        override val status: LawStatus get() = outcome.status
        override val counts: LawCounts get() = LawCounts.of(status)

        /** Diagnostics of a failed or errored leaf, if any. */
        val diagnostics: LeafDiagnostics?
            get() = when (outcome) {
                is LeafOutcome.Failed -> outcome.diagnostics
                is LeafOutcome.Errored -> outcome.diagnostics
                else -> null
            }
    }

    data class BranchReport(
        override val node: LawNode.Branch,
        override val path: LawPath,
        val children: List<NodeReport>,
        override val elapsed: Duration,
        override val sharedFrom: LawPath? = null
    ) : NodeReport {
        override fun sharedAt(path: LawPath): BranchReport =
            BranchReport(
                node,
                path,
                children.mapIndexed { i, c -> c.sharedAt(path.child(c.label, i)) },
                Duration.ZERO,
                sharedFrom ?: this.path
            )

        override val status: LawStatus = LawStatus.worst(children.map { it.status })
        override val counts: LawCounts = children.fold(LawCounts.ZERO) { acc, c -> acc + c.counts }
    }
}

/** All leaf reports below (and including) this node, depth-first and left to right. */
fun NodeReport.leaves(): List<NodeReport.LeafReport> = when (this) {
    is NodeReport.LeafReport -> listOf(this)
    is NodeReport.BranchReport -> children.flatMap { it.leaves() }
}

/**
 * Result of running a law graph with [LawRunner].
 *
 * A report with no executed leaves is valid but is not evidence of anything:
 * see [hasEvidence].
 */
data class LawReport(
    val root: NodeReport,
    /** The master seed the run used (for a single-leaf replay: null, since the leaf seed was given). */
    val seed: Long? = null,
    /** The settings of the run. */
    val config: LawRunConfig = LawRunConfig()
) {
    val status: LawStatus get() = root.status
    val counts: LawCounts get() = root.counts
    val elapsed: Duration get() = root.elapsed

    val success: Boolean get() = !status.isProblem

    /**
     * Number of leaf executions in this run: leaves that ran, counted once however many paths reach them.
     * [counts] counts occurrences (one per path), like the flat `laws()` / `fullLaws()` views.
     */
    val executions: Int get() = root.leaves().count { it.sharedFrom == null && it.status != LawStatus.SKIPPED }

    /** True if at least one leaf actually ran and passed. */
    val hasEvidence: Boolean get() = counts.passed > 0

    /** Leaves that failed or errored, with their full paths. */
    val problems: List<NodeReport.LeafReport>
        get() = root.leaves().filter { it.status.isProblem }

    /**
     * Throws an [AssertionError] whose message is [renderFailures]: the failing paths from the root, then each
     * problem's counterexample, seed (with the replay call) and cause chain. The first problem's original
     * throwable is the error's cause.
     *
     * @param requireEvidence if true (the default), a run in which no leaf passed also throws,
     *   so an empty or fully skipped graph cannot pass silently.
     */
    fun throwIfFailed(requireEvidence: Boolean = true) {
        if (success && (!requireEvidence || hasEvidence)) return

        val msg = "Law check failed.\n" + renderFailures()
        val cause = problems.firstOrNull()?.outcome.let {
            (it as? LeafOutcome.Failed)?.error ?: (it as? LeafOutcome.Errored)?.error
        }
        throw AssertionError(msg, cause)
    }
}
