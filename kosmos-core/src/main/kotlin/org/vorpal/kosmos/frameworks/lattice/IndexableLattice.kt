package org.vorpal.kosmos.frameworks.lattice

/**
 * Base interface for any "lattice-like" indexable structure.
 *
 * A lattice provides a mapping n ↦ value (e.g., Fibonacci(n), Prime(n)),
 * and exposes both horizontal and vertical traversals (rows/columns)
 * that conceptually correspond to compositional or self-indexing operations.
 *
 * Examples:
 *  - PrimeLattice:     pₙ = nth prime
 *  - FibonacciLattice: Fₙ = nth Fibonacci number
 *  - RecurrenceLattice: general wrapper for Recurrence<T>
 */
interface IndexableLattice<A> {

    /** The nth element in the lattice (1-based by convention). */
    fun index(n: Int): A

    /** Infinite lazy iteration of lattice values: 1, 2, 3, ... */
    fun iterate(): Sequence<A>

    /**
     * "Row" of the lattice beginning at element k.
     * For the prime lattice, row(k) = sequence starting at the k-th composite or prime index.
     */
    fun row(k: Int): Sequence<A>
}
