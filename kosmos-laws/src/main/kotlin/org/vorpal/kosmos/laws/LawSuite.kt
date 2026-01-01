package org.vorpal.kosmos.laws

import kotlinx.coroutines.CancellationException

/**
 * A bundle of [TestingLaw]s that collectively describe the axioms expected
 * from some algebraic structure implementation.
 */
interface LawSuite {

    /** Human-readable label for this suite. */
    val name: String
        get() = this::class.simpleName ?: "UnnamedLawSuite"

    /** The core laws to check for this structure. */
    fun laws(): List<TestingLaw>

    /**
     * Full laws: include constituent structure checks (e.g. "Ring" includes "AbelianGroup" on +).
     * Default is just the core laws.
     */
    fun fullLaws(): List<TestingLaw> = laws()

    /**
     * Run all laws and collect results instead of failing fast.
     *
     * If you want fail-fast behaviour inside Kotest, prefer:
     * `runLaws(*laws().toTypedArray())`
     */
    suspend fun test(): LawSuiteReport = runLawSuite(name, laws())
    suspend fun fullTest(): LawSuiteReport = runLawSuite(name, fullLaws())
}

suspend fun List<LawSuite>.testAll(): List<LawSuiteReport> =
    map { it.test() }

suspend fun List<LawSuite>.fullTestAll(): List<LawSuiteReport> =
    map { it.fullTest() }

fun List<LawSuite>.flatten(full: Boolean): List<TestingLaw> =
    flatMap { if (full) it.fullLaws() else it.laws() }

/**
 * Result of running an entire [LawSuite].
 */
data class LawSuiteReport(
    val suiteName: String,
    val results: List<LawResult>
) {
    val failures: List<LawResult.Failure>
        get() = results.filterIsInstance<LawResult.Failure>()

    val successes: List<LawResult.Success>
        get() = results.filterIsInstance<LawResult.Success>()

    val failedCount: Int
        get() = failures.size

    val passedCount: Int
        get() = results.size - failedCount

    val totalCount: Int
        get() = results.size

    val failure: Boolean
        get() = failures.isNotEmpty()

    val success: Boolean
        get() = !failure

    /**
     * Throws an [AssertionError] summarizing failures.
     * Useful when you ran in collecting mode but still want the test to fail.
     */
    fun throwIfFailed() {
        if (success) return

        val msg = buildString {
            appendLine("Law suite failed: $suiteName")
            appendLine("Passed: $passedCount / $totalCount")
            appendLine("Failed: $failedCount")
            appendLine()

            failures.forEachIndexed { i, f ->
                appendLine("#${i + 1}: ${f.name}")
                val m = f.error.message
                if (!m.isNullOrBlank()) {
                    appendLine(m)
                } else {
                    appendLine("(no error message)")
                }
                appendLine()
            }
        }

        // Keep the first failure as the cause so IDEs show a useful stacktrace.
        throw AssertionError(msg, failures.first().error)
    }
}

/**
 * Result of running a single [TestingLaw].
 */
sealed interface LawResult {
    val name: String
    val success: Boolean

    data class Success(override val name: String) : LawResult {
      override val success = true
    }
    data class Failure(
        override val name: String,
        val error: Throwable): LawResult {
        override val success = false
    }
}

/**
 * Run laws, collecting pass/fail results rather than throwing immediately.
 */
suspend fun runLawSuite(
    suiteName: String,
    laws: List<TestingLaw>
): LawSuiteReport {
    val results = laws.map { law ->
        try {
            law.test()
            LawResult.Success(name = law.name)
        } catch (t: CancellationException) {
            throw t
        } catch (t: Throwable) {
            LawResult.Failure(name = law.name, error = t)
        }
    }

    return LawSuiteReport(suiteName = suiteName, results = results)
}

/**
 * Derive a name for the [LawSuite] based on the object being tested and its operator symbols.
 */
fun suiteName(objName: String, vararg opNames: String): String {
    require(objName.isNotBlank()) { "In suiteName, the object name cannot be blank." }

    val ops = opNames
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    return buildString {
        append(objName)
        if (ops.isNotEmpty()) {
            append(" (")
            append(ops.joinToString(", "))
            append(")")
        }
    }
}
