package org.vorpal.kosmos.laws

import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.triple

/**
 * A single property / law that can be checked.
 *
 * Implementations should throw assertion failures (Kotest, etc.)
 * if the law does not hold.
 */
fun interface TestingLaw {
    suspend fun test()

    /**
     * Human-readable label for this law.
     *
     * Defaults to the simple class name, but you can override it
     * when you want a more descriptive name.
     */
    val name: String
        get() = this::class.simpleName ?: "UnnamedLaw"

    companion object {
        fun <A> arbPair(arb: Arb<A>): Arb<Pair<A, A>> = Arb.pair(arb, arb)
        fun <A> arbTriple(arb: Arb<A>): Arb<Triple<A, A, A>> = Arb.triple(arb, arb, arb)
    }
}

/**
 * Utility to run several laws in sequence.
 *
 * If any law fails (throws), the surrounding test framework
 * will report the failure.
 */
suspend fun runLaws(vararg laws: TestingLaw) {
    for (law in laws) {
        law.test()
    }
}
