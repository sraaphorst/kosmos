package org.vorpal.kosmos.algebra.structures

import java.math.BigInteger

interface HasFromBigInt<A : Any> {
    val add: AbelianGroup<A>

    // Thus, one is not the identity of add: it must be defined externally to be used as the
    // identity of a (supposedly) multiplicative group.
    val one: A

    /**
     * The canonical additive-group homomorphism `ℤ → (A, +)` sending `1 ↦ one`,
     * i.e. `n ↦ n · one` via repeated addition (doubling, O(log |n|)).
     *
     * This map is injective iff `one` has infinite additive order, i.e.
     * `n · one = 0` implies `n = 0`.
     */
    fun fromBigInt(n: BigInteger): A  =
        add.zTimes(n, one)

    fun fromInt(n: Int): A =
        fromBigInt(n.toBigInteger())

    fun fromLong(n: Long): A =
        fromBigInt(n.toBigInteger())

    /**
     * Convenience function to get the negation of the multiplicative identity.
     */
    val negOne: A
        get() = add.inverse(one)
}
