package org.vorpal.kosmos.testing.laws

import kotlin.coroutines.cancellation.CancellationException
import org.vorpal.kosmos.laws.LawGraphBuilder
import org.vorpal.kosmos.laws.LawReport
import org.vorpal.kosmos.laws.LawStatus
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.leaves

/**
 * A spec-local law: a check that belongs to one spec (a property of a concrete type, a specific example, an expected
 * error) rather than to a reusable law class. It has no binding, so it is never shared with another check.
 *
 * Inside it, use `lawCheckAll` to draw inputs, and either `lawCheck` or Kotest matchers (`shouldBe`, `shouldThrow`,
 * ...) for the predicate: a failed matcher reports the law as failed, exactly like `lawCheck`
 * (see [TestingLaw.assertionsAreViolations]). Any other exception still reports it as errored.
 */
fun localLaw(name: String, test: suspend () -> Unit): TestingLaw =
    TestingLaw.named(name, assertionsAreViolations = true, test = test)

/** Add a spec-local law (see [localLaw]) to this graph. */
fun LawGraphBuilder.local(name: String, test: suspend () -> Unit) =
    law(localLaw(name, test))

/**
 * Whether [block] throws a [T] (and not some other exception, or nothing).
 *
 * A [CancellationException] is never an answer: it propagates, as everywhere in the law runner.
 */
inline fun <reified T : Throwable> throwsA(block: () -> Unit): Boolean =
    try {
        block()
        false
    } catch (c: CancellationException) {
        throw c
    } catch (t: Throwable) {
        t is T
    }

/**
 * The paths of the leaves this report shows as [LawStatus.FAILED], for negative tests ("this law rejects that
 * structure, for this reason").
 *
 * It first asserts that no leaf is [LawStatus.ERRORED]: a negative test must see the law *reject* the example, not
 * crash on it (a broken generator or an unexpected exception would otherwise pass for a counterexample).
 */
fun LawReport.failedLabels(): Set<String> {
    val leaves = root.leaves()
    val errored = leaves.filter { it.status == LawStatus.ERRORED }
    if (errored.isNotEmpty()) {
        throw AssertionError(
            "expected the laws to fail, not error; errored: " +
                errored.joinToString { "${it.path} (${it.outcome})" }
        )
    }
    return leaves.filter { it.status == LawStatus.FAILED }.map { it.path.toString() }.toSet()
}
