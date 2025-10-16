package org.vorpal.kosmos.frameworks.lattice

import org.vorpal.kosmos.algebra.Indexable
import java.math.BigInteger

/**
 * Adapter to expose any [IndexableLattice] as a standard [Indexable]
 * for use in property-based and law-based test suites.
 *
 * This simply delegates all [Indexable] members to the wrapped lattice.
 */
/** Adapter so testkit (which depends on algebra.Indexable) can test a lattice. */
class LatticeAsIndexable<L : IndexableLattice<BigInteger>>(
    private val lattice: L,
    override val name: String = "Lattice"
) : Indexable<LatticeAsIndexable<L>, BigInteger> {

    override val seed: BigInteger = BigInteger.ONE

    override fun index(n: Int): BigInteger = lattice.index(n)

    override fun iterate(n: Int, depth: Int): BigInteger {
        require(n >= 1) { "iterate expects 1-based n" }
        require(depth >= 0)
        var idx = n
        repeat(depth) { idx = lattice.index(idx).toInt().coerceAtLeast(1) }
        return lattice.index(idx)
    }

    override fun inDomain(n: BigInteger): Boolean = n >= BigInteger.ONE
}
