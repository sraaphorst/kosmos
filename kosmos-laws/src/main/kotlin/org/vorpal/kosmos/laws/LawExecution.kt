package org.vorpal.kosmos.laws

import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.PropertyContext
import io.kotest.property.checkAll
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Thrown by a law when its predicate is false.
 *
 * This is the explicit "the law does not hold" signal. The runner reports a leaf whose final failure is a
 * [LawViolation] as [LeafOutcome.Failed]; any other exception from a migrated law (the operation, the
 * generator, the equality, ...) is reported as [LeafOutcome.Errored].
 *
 * It extends [AssertionError] so that legacy entry points (`runLaws`, `test()`, Kotest specs) keep treating
 * it as an ordinary test failure.
 */
class LawViolation(message: String) : AssertionError(message)

/** Throw a [LawViolation] with the given (lazily built) message if [holds] is false. */
inline fun lawCheck(holds: Boolean, message: () -> String) {
    if (!holds) throw LawViolation(message())
}

/**
 * What a property law reports about its own execution.
 *
 * Filled in by [lawCheckAll]; read by [LawRunner] after the leaf finishes.
 */
class PropertyCapture internal constructor() {
    /** True once the law has used [lawCheckAll], i.e. it signals falsity explicitly. */
    var explicit: Boolean = false
        internal set

    /** The rendered input of the last failing evaluation (after shrinking, the reported counterexample). */
    var counterexample: String? = null
        internal set

    /** The throwable of the last failing evaluation. */
    var lastCause: Throwable? = null
        internal set
}

/**
 * Coroutine context element the runner installs around one leaf execution.
 *
 * Laws never construct it; they read it through [lawCheckAll].
 */
internal class LawExecution(
    val seed: Long,
    val capture: PropertyCapture
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<LawExecution>
}

/**
 * Kotest's `checkAll` for laws: same generator, same default iteration budget and shrinking, plus
 *
 * - when run by [LawRunner], the leaf's seed is injected (`PropTestConfig(seed = ...)`), so the run can be replayed;
 * - every failing evaluation's input is rendered with [render] and recorded, so the final (shrunk)
 *   counterexample is available in the report without parsing Kotest's message.
 *
 * Outside [LawRunner] (legacy `test()`, `runLaws`, direct calls) this is exactly `checkAll(arb, property)`.
 */
suspend fun <T> lawCheckAll(
    arb: Arb<T>,
    render: (T) -> String,
    property: suspend PropertyContext.(T) -> Unit
): PropertyContext {
    val execution = currentCoroutineContext()[LawExecution]
    val config = if (execution == null) PropTestConfig() else PropTestConfig(seed = execution.seed)
    execution?.capture?.explicit = true

    return checkAll(config, arb) { value ->
        try {
            property(value)
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            // Kotest's assumption mechanism skips an iteration with an exception; that is not a failure.
            if (t::class.simpleName != "IterationSkippedException") {
                execution?.capture?.let {
                    it.counterexample = runCatching { render(value) }.getOrElse { "<render failed: $it>" }
                    it.lastCause = t
                }
            }
            throw t
        }
    }
}
