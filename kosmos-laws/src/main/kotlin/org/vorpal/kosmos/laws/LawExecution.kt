package org.vorpal.kosmos.laws

import io.kotest.common.ExperimentalKotest
import io.kotest.core.Tuple4
import io.kotest.core.Tuple5
import io.kotest.property.Arb
import io.kotest.property.Gen
import io.kotest.property.PropTestConfig
import io.kotest.property.PropertyContext
import io.kotest.property.checkAll
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import org.vorpal.kosmos.core.render.Printable
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
class LawViolation(message: String, cause: Throwable? = null) : AssertionError(message, cause)

/**
 * The law's predicate: throw a [LawViolation] with the given (lazily built) message if [holds] is false.
 *
 * This replaces `withClue(message) { check(holds) }`: Kotest's `withClue` does not attach its clue to
 * exceptions it did not create, so the message goes here.
 */
inline fun lawCheck(holds: Boolean, message: () -> Any? = { "Law violated." }) {
    if (!holds) throw LawViolation(message().toString())
}

/** As [lawCheck], with an eagerly built message (the counterpart of `withClue("...")`). */
fun lawCheck(holds: Boolean, message: Any?) {
    if (!holds) throw LawViolation(message.toString())
}

/**
 * A renderer for generated samples that prints with [pr], looking inside tuples:
 * `Pair`, `Triple`, Kotest's `Tuple4`/`Tuple5` and lists are rendered element by element.
 *
 * Only for samples whose elements all have the printable's type. A mismatch is caught by [lawCheckAll],
 * which then records a note instead of the counterexample.
 */
fun <A> renderWith(pr: Printable<A>): (Any?) -> String = { sample -> renderSample(sample, pr) }

/** The default sample renderer: `toString`, looking inside tuples like [renderWith]. */
fun renderSample(sample: Any?): String = renderSample(sample, Printable.default<Any?>())

@Suppress("UNCHECKED_CAST")
private fun <A> renderSample(sample: Any?, pr: Printable<A>): String {
    fun one(x: Any?) = pr(x as A)
    return when (sample) {
        is Pair<*, *> -> "(${one(sample.first)}, ${one(sample.second)})"
        is Triple<*, *, *> -> "(${one(sample.first)}, ${one(sample.second)}, ${one(sample.third)})"
        is Tuple4<*, *, *, *> -> "(${one(sample.a)}, ${one(sample.b)}, ${one(sample.c)}, ${one(sample.d)})"
        is Tuple5<*, *, *, *, *> -> "(${one(sample.a)}, ${one(sample.b)}, ${one(sample.c)}, ${one(sample.d)}, ${one(sample.e)})"
        is List<*> -> sample.joinToString(", ", "[", "]") { one(it) }
        else -> one(sample)
    }
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
    gen: Gen<T>,
    render: (T) -> String = ::renderSample,
    property: suspend PropertyContext.(T) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution), gen) { a -> recording(execution, { render(a) }) { property(a) } }
}

/** Two-generator form of [lawCheckAll]; [render] receives both values. */
suspend fun <A, B> lawCheckAll(
    genA: Gen<A>,
    genB: Gen<B>,
    render: (A, B) -> String = { a, b -> "${renderSample(a)}, ${renderSample(b)}" },
    property: suspend PropertyContext.(A, B) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution), genA, genB) { a, b ->
        recording(execution, { render(a, b) }) { property(a, b) }
    }
}

/** Three-generator form of [lawCheckAll]; [render] receives all three values. */
suspend fun <A, B, C> lawCheckAll(
    genA: Gen<A>,
    genB: Gen<B>,
    genC: Gen<C>,
    render: (A, B, C) -> String = { a, b, c -> "${renderSample(a)}, ${renderSample(b)}, ${renderSample(c)}" },
    property: suspend PropertyContext.(A, B, C) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution), genA, genB, genC) { a, b, c ->
        recording(execution, { render(a, b, c) }) { property(a, b, c) }
    }
}

private suspend fun enter(): LawExecution? =
    currentCoroutineContext()[LawExecution]?.also { it.capture.explicit = true }

// PropTestConfig(seed = ...) is marked experimental in Kotest 6.0.5; it is the supported way to inject a seed.
@OptIn(ExperimentalKotest::class)
private fun config(execution: LawExecution?): PropTestConfig =
    if (execution == null) PropTestConfig() else PropTestConfig(seed = execution.seed)

private suspend inline fun recording(
    execution: LawExecution?,
    crossinline render: () -> String,
    body: () -> Unit
) {
    try {
        body()
    } catch (c: CancellationException) {
        throw c
    } catch (t: Throwable) {
        // Kotest's assumption mechanism skips an iteration with an exception; that is not a failure.
        if (t::class.simpleName != "IterationSkippedException") {
            execution?.capture?.let {
                it.counterexample = runCatching { render() }.getOrElse { e -> "<could not render input: $e>" }
                it.lastCause = t
            }
        }
        throw t
    }
}
