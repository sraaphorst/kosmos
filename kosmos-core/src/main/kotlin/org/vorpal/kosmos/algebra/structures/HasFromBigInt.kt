package org.vorpal.kosmos.algebra.structures

import java.math.BigInteger

interface HasFromBigInt<A : Any> {
    // Assume add is an additive group.
    val add: AbelianGroup<A>

    // Thus, one is not the identity of add: it must be defined externally to be used as the
    // identity of a (supposedly) multiplicative group.
    val one: A

    /**
     * The canonical additive-group homomorphism `ℤ → (A, +)` sending `1 ↦ 1_A` (the multiplicative identity),
     * i.e. `n ↦ n·1_A` via repeated addition.
     *
     * Note: this map is injective iff the algebra has characteristic 0 (or, more generally,
     * if `n·1_A = 0` implies `n = 0`).
     */
    fun fromBigInt(n: BigInteger): A {
        tailrec fun aux(rem: BigInteger, acc: A): A =
            when (rem) {
                BigInteger.ZERO -> acc
                else -> aux(
                    rem - BigInteger.ONE,
                    add(acc, one)
                )
            }

        val pos = aux(n.abs(), add.identity)
        return if (n.signum() == -1) add.inverse(pos) else pos
    }

    /**
     * Convenience function to get the negation of the multiplicative identity.
     */
    val negOne: A
        get() = add.inverse(one)
}
