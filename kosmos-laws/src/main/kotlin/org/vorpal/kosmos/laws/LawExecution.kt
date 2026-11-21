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

    /**
     * A cancellation seen inside a property evaluation (including during shrinking). Kotest catches it and may
     * report a failure instead, so the runner rethrows it from here.
     */
    internal var cancellation: CancellationException? = null

    /** Number of property checks the law has started; the k-th gets its own seed (see [lawCheckAll]). */
    internal var checks: Int = 0

    /** Copied from [TestingLaw.assertionsAreViolations] of the law being executed. */
    internal var assertionsAreViolations: Boolean = false
}

/**
 * An [AssertionError] raised by the law's own assertion rather than wrapped around another exception (Kotest's
 * `checkAll` wraps whatever a property throws in an [AssertionError]): nothing in its cause chain is anything else.
 */
internal fun isOwnAssertion(t: Throwable): Boolean =
    t is AssertionError && generateSequence(t.cause) { c -> c.cause.takeIf { it !== c } }.all { it is AssertionError }

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
 * Kotest's `checkAll` for laws: same generators, same default iteration budget and shrinking, plus
 *
 * - when run by [LawRunner], a seed is injected (`PropTestConfig(seed = ...)`), so the run can be replayed: the first
 *   check of a leaf uses the leaf's seed, and the k-th further check in the same leaf a seed derived from it, so
 *   several checks in one law draw independent samples;
 * - every failing evaluation's input is rendered with [render] and recorded, so the final (shrunk)
 *   counterexample is available in the report without parsing Kotest's message.
 *
 * There are forms for one to six generators, each also with a leading `iterations` count (as Kotest's
 * `checkAll(iterations, ...)`). Outside [LawRunner] (legacy `test()`, `runLaws`, direct calls) this is exactly
 * `checkAll(...)`.
 */
suspend fun <A> lawCheckAll(
    gen: Gen<A>,
    render: (A) -> String = ::renderSample,
    property: suspend PropertyContext.(A) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution, null), gen) { a ->
        recording(execution, { render(a) }) { property(a) }
    }
}

/** One-generator form of [lawCheckAll], with an iteration count. */
suspend fun <A> lawCheckAll(
    iterations: Int,
    gen: Gen<A>,
    render: (A) -> String = ::renderSample,
    property: suspend PropertyContext.(A) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution, iterations), gen) { a ->
        recording(execution, { render(a) }) { property(a) }
    }
}

/** Two-generator form of [lawCheckAll]. */
suspend fun <A, B> lawCheckAll(
    genA: Gen<A>,
    genB: Gen<B>,
    render: (A, B) -> String = { a, b -> listOf<Any?>(a, b).joinToString(", ") { renderSample(it) } },
    property: suspend PropertyContext.(A, B) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution, null), genA, genB) { a, b ->
        recording(execution, { render(a, b) }) { property(a, b) }
    }
}

/** Two-generator form of [lawCheckAll], with an iteration count. */
suspend fun <A, B> lawCheckAll(
    iterations: Int,
    genA: Gen<A>,
    genB: Gen<B>,
    render: (A, B) -> String = { a, b -> listOf<Any?>(a, b).joinToString(", ") { renderSample(it) } },
    property: suspend PropertyContext.(A, B) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution, iterations), genA, genB) { a, b ->
        recording(execution, { render(a, b) }) { property(a, b) }
    }
}

/** Three-generator form of [lawCheckAll]. */
suspend fun <A, B, C> lawCheckAll(
    genA: Gen<A>,
    genB: Gen<B>,
    genC: Gen<C>,
    render: (A, B, C) -> String = { a, b, c -> listOf<Any?>(a, b, c).joinToString(", ") { renderSample(it) } },
    property: suspend PropertyContext.(A, B, C) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution, null), genA, genB, genC) { a, b, c ->
        recording(execution, { render(a, b, c) }) { property(a, b, c) }
    }
}

/** Three-generator form of [lawCheckAll], with an iteration count. */
suspend fun <A, B, C> lawCheckAll(
    iterations: Int,
    genA: Gen<A>,
    genB: Gen<B>,
    genC: Gen<C>,
    render: (A, B, C) -> String = { a, b, c -> listOf<Any?>(a, b, c).joinToString(", ") { renderSample(it) } },
    property: suspend PropertyContext.(A, B, C) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution, iterations), genA, genB, genC) { a, b, c ->
        recording(execution, { render(a, b, c) }) { property(a, b, c) }
    }
}

/** Four-generator form of [lawCheckAll]. */
suspend fun <A, B, C, D> lawCheckAll(
    genA: Gen<A>,
    genB: Gen<B>,
    genC: Gen<C>,
    genD: Gen<D>,
    render: (A, B, C, D) -> String = { a, b, c, d -> listOf<Any?>(a, b, c, d).joinToString(", ") { renderSample(it) } },
    property: suspend PropertyContext.(A, B, C, D) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution, null), genA, genB, genC, genD) { a, b, c, d ->
        recording(execution, { render(a, b, c, d) }) { property(a, b, c, d) }
    }
}

/** Four-generator form of [lawCheckAll], with an iteration count. */
suspend fun <A, B, C, D> lawCheckAll(
    iterations: Int,
    genA: Gen<A>,
    genB: Gen<B>,
    genC: Gen<C>,
    genD: Gen<D>,
    render: (A, B, C, D) -> String = { a, b, c, d -> listOf<Any?>(a, b, c, d).joinToString(", ") { renderSample(it) } },
    property: suspend PropertyContext.(A, B, C, D) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution, iterations), genA, genB, genC, genD) { a, b, c, d ->
        recording(execution, { render(a, b, c, d) }) { property(a, b, c, d) }
    }
}

/** Five-generator form of [lawCheckAll]. */
suspend fun <A, B, C, D, E> lawCheckAll(
    genA: Gen<A>,
    genB: Gen<B>,
    genC: Gen<C>,
    genD: Gen<D>,
    genE: Gen<E>,
    render: (A, B, C, D, E) -> String = { a, b, c, d, e -> listOf<Any?>(a, b, c, d, e).joinToString(", ") { renderSample(it) } },
    property: suspend PropertyContext.(A, B, C, D, E) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution, null), genA, genB, genC, genD, genE) { a, b, c, d, e ->
        recording(execution, { render(a, b, c, d, e) }) { property(a, b, c, d, e) }
    }
}

/** Five-generator form of [lawCheckAll], with an iteration count. */
suspend fun <A, B, C, D, E> lawCheckAll(
    iterations: Int,
    genA: Gen<A>,
    genB: Gen<B>,
    genC: Gen<C>,
    genD: Gen<D>,
    genE: Gen<E>,
    render: (A, B, C, D, E) -> String = { a, b, c, d, e -> listOf<Any?>(a, b, c, d, e).joinToString(", ") { renderSample(it) } },
    property: suspend PropertyContext.(A, B, C, D, E) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution, iterations), genA, genB, genC, genD, genE) { a, b, c, d, e ->
        recording(execution, { render(a, b, c, d, e) }) { property(a, b, c, d, e) }
    }
}

/** Six-generator form of [lawCheckAll]. */
suspend fun <A, B, C, D, E, F> lawCheckAll(
    genA: Gen<A>,
    genB: Gen<B>,
    genC: Gen<C>,
    genD: Gen<D>,
    genE: Gen<E>,
    genF: Gen<F>,
    render: (A, B, C, D, E, F) -> String = { a, b, c, d, e, f -> listOf<Any?>(a, b, c, d, e, f).joinToString(", ") { renderSample(it) } },
    property: suspend PropertyContext.(A, B, C, D, E, F) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution, null), genA, genB, genC, genD, genE, genF) { a, b, c, d, e, f ->
        recording(execution, { render(a, b, c, d, e, f) }) { property(a, b, c, d, e, f) }
    }
}

/** Six-generator form of [lawCheckAll], with an iteration count. */
suspend fun <A, B, C, D, E, F> lawCheckAll(
    iterations: Int,
    genA: Gen<A>,
    genB: Gen<B>,
    genC: Gen<C>,
    genD: Gen<D>,
    genE: Gen<E>,
    genF: Gen<F>,
    render: (A, B, C, D, E, F) -> String = { a, b, c, d, e, f -> listOf<Any?>(a, b, c, d, e, f).joinToString(", ") { renderSample(it) } },
    property: suspend PropertyContext.(A, B, C, D, E, F) -> Unit
): PropertyContext {
    val execution = enter()
    return checkAll(config(execution, iterations), genA, genB, genC, genD, genE, genF) { a, b, c, d, e, f ->
        recording(execution, { render(a, b, c, d, e, f) }) { property(a, b, c, d, e, f) }
    }
}

private suspend fun enter(): LawExecution? =
    currentCoroutineContext()[LawExecution]?.also { it.capture.explicit = true }

/** The seed of the next property check in this leaf: the leaf seed first, then seeds derived from it. */
private fun nextSeed(execution: LawExecution): Long {
    val k = execution.capture.checks++
    return if (k == 0) execution.seed else LawSeeds.leaf(execution.seed, listOf(k))
}

// PropTestConfig(seed = ...) is marked experimental in Kotest 6.0.5; it is the supported way to inject a seed.
@OptIn(ExperimentalKotest::class)
private fun config(execution: LawExecution?, iterations: Int?): PropTestConfig =
    PropTestConfig(seed = execution?.let(::nextSeed), iterations = iterations)

private suspend inline fun recording(
    execution: LawExecution?,
    crossinline render: () -> String,
    body: () -> Unit
) {
    try {
        body()
    } catch (c: CancellationException) {
        execution?.capture?.let { if (it.cancellation == null) it.cancellation = c }
        throw c
    } catch (t: Throwable) {
        // Kotest's assumption mechanism skips an iteration with an exception; that is not a failure.
        if (t::class.simpleName == "IterationSkippedException") throw t
        val capture = execution?.capture ?: throw t
        // A matcher failure in a law that asserts with matchers is the law being false.
        val signal =
            if (capture.assertionsAreViolations && t !is LawViolation && isOwnAssertion(t))
                LawViolation(t.message ?: "Assertion failed.", t)
            else t
        capture.counterexample = runCatching { render() }.getOrElse { e -> "<could not render input: $e>" }
        capture.lastCause = signal
        throw signal
    }
}
