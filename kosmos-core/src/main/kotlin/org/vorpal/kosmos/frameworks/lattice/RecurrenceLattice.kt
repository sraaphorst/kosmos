package org.vorpal.kosmos.frameworks.lattice

import org.vorpal.kosmos.frameworks.sequence.Recurrence
import java.math.BigInteger

/**
 * Wraps a recurrence (like Fibonacci or PrimeSequence) into a 1-based lattice.
 * Provides `index(n)`, `iterate()`, `row(k)`, `column(k)`, etc.
 *
 * For ordinary recurrences (Fibonacci, Catalan...), use [of].
 * For sequences that are already 0-based in mathematical indexing (e.g. primes),
 * use [ofZeroBased].
 */
open class RecurrenceLattice<T>(
    val name: String,
    private val recurrence: Recurrence<T>,
    private val converter: (T) -> BigInteger
) : IndexableLattice<BigInteger> {

    /** Default 1-based indexing: n ↦ recurrence(n-1). */
    override fun index(n: Int): BigInteger {
        require(n >= 1) { "Lattice index must be ≥ 1 (got $n)" }
        // If RecurrenceLattice was written around a Sequence<T> named `recurrence`
        // and you *don’t* keep an offset flag inside the class, then assume the input
        // sequence is already aligned so that element 0 == term for n=1.
        return converter(recurrence(n-1))
    }

    override fun iterate(): Sequence<BigInteger> =
        generateSequence(1) { it + 1 }.map(::index)

    override fun row(k: Int): Sequence<BigInteger> =
        generateSequence(k) { index(it).toInt() }.map(::index)

    override fun column(k: Int): Sequence<BigInteger> =
        generateSequence(index(k).toInt()) { index(it).toInt() }.map(::index)

    companion object {
        /** Standard 1-based lattice (for Fibonacci, etc.). */
        fun <T : Any> of(
            name: String,
            recurrence: Recurrence<T>,
            converter: (T) -> BigInteger
        ): RecurrenceLattice<T> =
            RecurrenceLattice(name, recurrence, converter)

        /** Zero-based lattice (for primes, which are already 0-based). */
        fun <T : Any> ofZeroBased(
            name: String,
            recurrence: Recurrence<T>,
            converter: (T) -> BigInteger
        ): RecurrenceLattice<T> =
            object : RecurrenceLattice<T>(name, recurrence, converter) {
                override fun index(n: Int): BigInteger {
                    require(n >= 1) { "Lattice index must be ≥ 1 (got $n)" }
                    // no -1 shift: primes are indexed from p₁ = recurrence(0)
                    return converter(recurrence(n - 1))
                }
            }
    }
}
