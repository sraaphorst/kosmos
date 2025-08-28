package org.vorpal.kosmos.core

/** A lawful instance of a type, i.e. one that has passed the requisite tests. */
sealed interface Lawful<out A> {
    val subject: A

    /** A registered Lawful instance of a structure, operation, etc. */
    data class Pass<A>(override val subject: A): Lawful<A>

    /** Maintains the subject for informational purposes, even though it failed the requisite tests. */
    data class Fail<A>(override val subject: A, val errors: List<Throwable>): Lawful<A>
}