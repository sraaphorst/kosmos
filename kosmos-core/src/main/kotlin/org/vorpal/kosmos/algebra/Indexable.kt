package org.vorpal.kosmos.algebra

/**
 * Typeclass for indexable integer sequences, supporting self-indexing recursion:
 *
 * Given a base sequence S = { s_n }, we define the indexed recursion:
 *   S⁽ᵈ⁾(n) = s_{s_{..._{s_n}}}
 * where the index operator is applied d times.
 *
 * This generalizes structures like:
 *   - Prime-indexed primes (PrimeLattice)
 *   - Fibonacci-indexed Fibonacci numbers
 *   - Perfect-indexed perfect numbers
 */
interface Indexable<S, N> {

    /** Get the n-th element of the sequence (1-indexed). */
    fun index(n: Int): N

    /** Determine whether a number belongs to the base domain of the sequence. */
    fun inDomain(n: N): Boolean

    /** Produce the index of the next element under k levels of recursion. */
    fun iterate(n: Int, depth: Int): N

    /** Return the base seed (for example, 1 for primes, 1 or 0 for Fibonacci). */
    val seed: N

    /** Optional: textual name for this indexable sequence. */
    val name: String
}
