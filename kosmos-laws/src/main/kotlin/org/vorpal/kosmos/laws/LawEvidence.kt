package org.vorpal.kosmos.laws

/**
 * What a passing [LawReport] establishes, in a form that can later back a `Lawful<T>` claim.
 *
 * It records the scope (which graph, in which view, and every law path that passed) and the settings needed to
 * reproduce it. It is a record of a test run, not a proof: it holds for the generator, budget and seed used.
 * Wiring evidence into a registry is separate work.
 *
 * @property scope the root label, e.g. `Field (+, *)`.
 * @property mode the view, when the root is a suite node (compact or full); null for an ad hoc graph.
 * @property seed the master seed of the run.
 * @property laws the path of every law occurrence that passed, from the root.
 * @property skipped the path of every skipped occurrence, with its reason.
 * @property executions how many leaves actually ran (shared repeats run once).
 * @property config the run settings.
 */
data class LawEvidence(
    val scope: String,
    val mode: LawMode?,
    val seed: Long?,
    val laws: List<String>,
    val skipped: List<Pair<String, String>>,
    val executions: Int,
    val config: LawRunConfig
)

/**
 * The evidence this report provides.
 *
 * @throws IllegalStateException if the report failed, errored, or has no passing leaf: such a report is not evidence.
 */
fun LawReport.evidence(): LawEvidence {
    check(success && hasEvidence) {
        "A report is evidence only if nothing failed or errored and at least one law passed (${root.label}: $status)."
    }
    val leaves = root.leaves()
    return LawEvidence(
        scope = root.label,
        mode = root.node.key?.mode,
        seed = seed,
        laws = leaves.filter { it.status == LawStatus.PASSED }.map { it.path.toString() },
        skipped = leaves.mapNotNull { l -> (l.outcome as? LeafOutcome.Skipped)?.let { l.path.toString() to it.reason } },
        executions = executions,
        config = config
    )
}
