package org.vorpal.kosmos.frameworks.lattice

import java.math.BigInteger

fun <L : IndexableLattice<BigInteger>> L.asIndexFunction(): LatticeIndexFunction<L> =
    LatticeIndexFunction.base(this)

fun <L : IndexableLattice<BigInteger>> L.asIndexIdentity(): LatticeIndexFunction<L> =
    LatticeIndexFunction.id(this)

fun <L : IndexableLattice<BigInteger>> L.asIndexPure(f: (Int) -> Int): LatticeIndexFunction<L> =
    LatticeIndexFunction.pure(this, f)
