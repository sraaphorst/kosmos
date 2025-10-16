package org.vorpal.kosmos.frameworks.lattice

import java.math.BigInteger

/**
 * A lattice generated from a generic Kotlin [Sequence].
 * Provides `index(n)`, `iterate()`, `row(k)`, `column(k)`, etc.
 * The sequence should be infinite and 1-based (i.e., element at index 0 is ignored).
 */
class RecurrenceLattice<T> private constructor(
    val name: String,
    val recurrence: Sequence<T>,
    private val converter: (T) -> BigInteger
) : IndexableLattice<BigInteger> {

    // Assume a 1-based lattice.
    override fun index(n: Int): BigInteger {
        require(n >= 1) { "Lattice index must be ≥ 1 (got $n)" }
        return converter(recurrence.elementAt(n-1))
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
            recurrence: Sequence<T>,
            converter: (T) -> BigInteger
        ): RecurrenceLattice<T> =
            RecurrenceLattice(name, recurrence, converter)
    }
}
