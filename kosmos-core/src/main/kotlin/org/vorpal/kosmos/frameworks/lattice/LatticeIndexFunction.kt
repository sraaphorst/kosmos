package org.vorpal.kosmos.frameworks.lattice

import org.vorpal.kosmos.algebra.IndexFunction
import java.math.BigInteger

/**
 * Functional view of a lattice: n ↦ L.index(n)
 * Supports composition, monadic flatMap, and repetition.
 *
 * Composition and flatMap now operate in the *value space* of the lattice
 * (BigInteger outputs), not the index-space (Int inputs), so that identity
 * and associativity hold.
 */
data class LatticeIndexFunction<L : IndexableLattice<BigInteger>>(
    val lattice: L,
    val eval: (Int) -> BigInteger
) : IndexFunction<LatticeIndexFunction<L>> {

    /** Int-level view (for IndexFunction laws and Kotest contexts). */
    override fun run(n: Int): Int = eval.invoke(n).toInt()

    /** Composition in value space: (f ∘ g)(n) = g(f(n)) */
    override infix fun andThen(other: LatticeIndexFunction<L>): LatticeIndexFunction<L> =
        LatticeIndexFunction(lattice) { n ->
            val y: BigInteger = this.eval(n)
            other.eval(y.toInt())
        }

    /** Monad flatMap: binds in value space for proper identity and associativity. */
    override fun flatMap(f: (Int) -> LatticeIndexFunction<L>): LatticeIndexFunction<L> =
        LatticeIndexFunction(lattice) { n ->
            val y: BigInteger = this.eval(n)
            val g: LatticeIndexFunction<L> = f(y.toInt())
            g.eval(y.toInt())
        }

    /** Repeat k times via functional composition. */
    override fun repeat(k: Int): LatticeIndexFunction<L> {
        require(k >= 0)
        var acc = id(lattice)
        repeat(k) { acc = acc andThen this }
        return acc
    }

    /** Sequence of Int outputs. */
    fun indices(): Sequence<Int> =
        generateSequence(1) { it + 1 }.map { run(it) }

    /** Sequence of BigInteger outputs. */
    fun values(): Sequence<BigInteger> =
        generateSequence(1) { it + 1 }.map { eval(it) }

    companion object {
        /** Identity: n ↦ n */
        fun <L : IndexableLattice<BigInteger>> id(lattice: L): LatticeIndexFunction<L> =
            LatticeIndexFunction(lattice) { n -> BigInteger.valueOf(n.toLong()) }

        /** Base morphism: n ↦ L.index(n) */
        fun <L : IndexableLattice<BigInteger>> base(lattice: L): LatticeIndexFunction<L> =
            LatticeIndexFunction(lattice, lattice::index)

        /** Pure lift: (n ↦ f(n)) wrapped in BigInteger. */
        fun <L : IndexableLattice<BigInteger>> pure(
            lattice: L,
            f: (Int) -> Int
        ): LatticeIndexFunction<L> =
            LatticeIndexFunction(lattice) { n -> BigInteger.valueOf(f(n).toLong()) }
    }
}
