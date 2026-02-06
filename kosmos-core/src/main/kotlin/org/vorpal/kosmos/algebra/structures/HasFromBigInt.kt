package org.vorpal.kosmos.algebra.structures

import java.math.BigInteger

interface HasFromBigInt<A : Any> {
    val add: AbelianGroup<A>

    // Thus, one is not the identity of add: it must be defined externally to be used as the
    // identity of a (supposedly) multiplicative group.
    val one: A

    /**
     * The canonical additive-group homomorphism `ℤ → (A, +)` sending `1 ↦ 1_A` (the multiplicative identity),
     * i.e. `n ↦ n·1_A` via repeated addition (implemented with doubling for efficiency).
     *
     * Note: this map is injective iff the algebra has characteristic 0 (or, more generally,
     * if `n·1_A = 0` implies `n = 0`).
     */
    fun fromBigInt(n: BigInteger): A {
        fun natTimes(k: BigInteger): A {
            var rem = k
            var acc = add.identity
            var cur = one

            while (rem.signum() > 0) {
                if (rem.testBit(0)) {
                    acc = add(acc, cur)
                }
                cur = add(cur, cur)
                rem = rem.shiftRight(1)
            }

            return acc
        }

        val sign = n.signum()
        if (sign == 0) return add.identity
        val pos = natTimes(n.abs())
        return if (sign < 0) add.inverse(pos) else pos
    }

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
