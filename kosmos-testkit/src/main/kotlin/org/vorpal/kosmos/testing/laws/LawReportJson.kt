package org.vorpal.kosmos.testing.laws

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.vorpal.kosmos.laws.LawReport
import org.vorpal.kosmos.laws.LeafOutcome
import org.vorpal.kosmos.laws.NodeReport

/**
 * JSON export of a [LawReport].
 *
 * The report model (with live throwables and law objects) stays in `kosmos-laws`; this file converts it to plain
 * data transfer objects at the export boundary. Schema (version [VERSION]):
 *
 * ```
 * {
 *   "schema": "kosmos-law-report", "version": 1,
 *   "root": "n0", "status": "FAILED", "seed": 42, "failFast": false,
 *   "counts": {"passed": 12, "failed": 1, "errored": 0, "skipped": 0}, "executions": 9,
 *   "nodes": [
 *     {"id": "n0", "kind": "branch", "label": "Field (+, *)", "path": ["Field (+, *)"], "indices": [],
 *      "status": "FAILED", "counts": {...}, "elapsedMillis": 120, "children": ["n1", "n7"]},
 *     {"id": "n5", "kind": "leaf", "label": "associativity (*)", "path": [...], "indices": [0, 0, 1, 0],
 *      "status": "FAILED", "counts": {...}, "elapsedMillis": 30, "sharedFrom": null,
 *      "outcome": {"status": "FAILED", "exception": {"type": "java.lang.AssertionError", "message": "..."},
 *                  "cause": {"type": "org.vorpal.kosmos.laws.LawViolation", "message": "..."},
 *                  "seed": -123, "counterexample": "a = 1, b = 2, c = 3", "unavailable": []}}
 *   ]
 * }
 * ```
 *
 * - `nodes` lists every node occurrence depth-first; `children` refers to them by `id`.
 * - A repeat of a shared node has `sharedFrom` set to the `id` of the occurrence that executed.
 * - Replay a property failure with `LawRunner.replay(root, indices, outcome.seed)`.
 */
object LawReportJson {
    const val SCHEMA = "kosmos-law-report"
    const val VERSION = 1

    private val json = Json { prettyPrint = true; explicitNulls = true }

    /** The report as a JSON string. */
    fun encode(report: LawReport): String = json.encodeToString(ReportDto.serializer(), toDto(report))

    /** The report as DTOs, for callers that want to add to them or encode them differently. */
    fun toDto(report: LawReport): ReportDto {
        val nodes = mutableListOf<NodeDto>()
        val idOf = HashMap<List<Int>, String>()

        fun visit(node: NodeReport): String {
            val id = "n${nodes.size}"
            idOf[node.path.indices] = id
            val slot = nodes.size
            nodes += placeholder
            val children = (node as? NodeReport.BranchReport)?.children?.map(::visit) ?: emptyList()
            nodes[slot] = NodeDto(
                id = id,
                kind = if (node is NodeReport.LeafReport) "leaf" else "branch",
                label = node.label,
                path = node.path.labels,
                indices = node.path.indices,
                status = node.status.name,
                counts = CountsDto(node.counts.passed, node.counts.failed, node.counts.errored, node.counts.skipped),
                elapsedMillis = node.elapsed.inWholeMilliseconds,
                children = children,
                sharedFrom = node.sharedFrom?.let { idOf[it.indices] },
                outcome = (node as? NodeReport.LeafReport)?.let(::outcome)
            )
            return id
        }

        val root = visit(report.root)
        val c = report.counts
        return ReportDto(
            root = root,
            status = report.status.name,
            seed = report.seed,
            failFast = report.config.failFast,
            counts = CountsDto(c.passed, c.failed, c.errored, c.skipped),
            executions = report.executions,
            nodes = nodes
        )
    }

    private val placeholder = NodeDto("", "", "", emptyList(), emptyList(), "", CountsDto(0, 0, 0, 0), 0, emptyList())

    private fun outcome(leaf: NodeReport.LeafReport): OutcomeDto = when (val o = leaf.outcome) {
        LeafOutcome.Passed -> OutcomeDto(status = "PASSED")
        is LeafOutcome.Skipped -> OutcomeDto(status = "SKIPPED", skipReason = o.reason)
        is LeafOutcome.Failed, is LeafOutcome.Errored -> {
            val d = leaf.diagnostics
            val error = (o as? LeafOutcome.Failed)?.error ?: (o as LeafOutcome.Errored).error
            OutcomeDto(
                status = o.status.name,
                exception = ThrowableDto.of(error),
                cause = d?.cause?.let(ThrowableDto::of),
                seed = d?.seed,
                counterexample = d?.counterexample,
                unavailable = d?.unavailable ?: emptyList()
            )
        }
    }
}

/** The whole report. `nodes[i].id` is referenced by `root`, `children` and `sharedFrom`. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ReportDto(
    @EncodeDefault val schema: String = LawReportJson.SCHEMA,
    @EncodeDefault val version: Int = LawReportJson.VERSION,
    val root: String,
    val status: String,
    val seed: Long?,
    val failFast: Boolean,
    val counts: CountsDto,
    val executions: Int,
    val nodes: List<NodeDto>
)

@Serializable
data class CountsDto(val passed: Int, val failed: Int, val errored: Int, val skipped: Int)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class NodeDto(
    val id: String,
    val kind: String,
    val label: String,
    val path: List<String>,
    val indices: List<Int>,
    val status: String,
    val counts: CountsDto,
    val elapsedMillis: Long,
    val children: List<String>,
    @EncodeDefault val sharedFrom: String? = null,
    @EncodeDefault val outcome: OutcomeDto? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OutcomeDto(
    val status: String,
    @EncodeDefault val exception: ThrowableDto? = null,
    @EncodeDefault val cause: ThrowableDto? = null,
    @EncodeDefault val seed: Long? = null,
    @EncodeDefault val counterexample: String? = null,
    @EncodeDefault val unavailable: List<String> = emptyList(),
    @EncodeDefault val skipReason: String? = null
)

/** An exception's type and message (live throwables are not exported). */
@Serializable
data class ThrowableDto(val type: String, val message: String?) {
    companion object {
        fun of(t: Throwable) = ThrowableDto(t::class.qualifiedName ?: t.javaClass.name, t.message)
    }
}

/** Shorthand for [LawReportJson.encode]. */
fun LawReport.toJson(): String = LawReportJson.encode(this)
