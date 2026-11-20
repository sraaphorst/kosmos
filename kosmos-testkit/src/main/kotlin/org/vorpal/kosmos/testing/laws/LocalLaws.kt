package org.vorpal.kosmos.testing.laws

import org.vorpal.kosmos.laws.LawGraphBuilder
import org.vorpal.kosmos.laws.TestingLaw

/**
 * A spec-local law: a check that belongs to one spec (a property of a concrete type, a specific example, an expected
 * error) rather than to a reusable law class. It has no binding, so it is never shared with another check.
 *
 * Inside it, use `lawCheck` for the predicate and `lawCheckAll` to draw inputs, as in any law.
 */
fun localLaw(name: String, test: suspend () -> Unit): TestingLaw =
    TestingLaw.named(name, test = test)

/** Add a spec-local law (see [localLaw]) to this graph. */
fun LawGraphBuilder.local(name: String, test: suspend () -> Unit) =
    law(localLaw(name, test))

/** Whether [block] throws a [T] (and not some other exception, or nothing). */
inline fun <reified T : Throwable> throwsA(block: () -> Unit): Boolean =
    try {
        block()
        false
    } catch (t: Throwable) {
        t is T
    }
