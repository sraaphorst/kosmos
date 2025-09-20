package org.vorpal.kosmos.laws

import arrow.core.Option
import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Some
import arrow.core.nonEmptyListOf

typealias LawOutcome = Option<NonEmptyList<Throwable>>
typealias LawFailure = Some<NonEmptyList<Throwable>>
typealias LawSuccess = None

/** Abstraction of a Law that must be adhered to for a structure, operation, etc.
 * If the operation succeeds the check, then None is returned, representing that no
 * failures occurred when checking the law. If one or more errors occurred,
 * a Some with a non-empty list of the errors that occurred is returned.*/
interface Law {
    val name: String
    suspend fun check(): LawOutcome
}

/** Convenience function to create laws. */
fun law(name: String,
        run: suspend () -> LawOutcome): Law =
    object : Law {
        override val name = name
        override suspend fun check() = run()
    }

/** Convenience function to create a list of laws. */
fun laws(vararg ls: Law): List<Law> =
    ls.toList()

/** Converts any runnable function to a Law. */
fun <A> throwingLaw(name: String,
                    run: suspend () -> A): Law =
    law(name, throwingCheck(run))

/** Converts any runnable function into a function that reports an outcome. */
fun <A> throwingCheck(run: suspend () -> A): suspend () -> LawOutcome = {
    try {
        run()
        LawSuccess
    } catch (t: Throwable) {
        LawFailure(nonEmptyListOf(t))
    }
}
