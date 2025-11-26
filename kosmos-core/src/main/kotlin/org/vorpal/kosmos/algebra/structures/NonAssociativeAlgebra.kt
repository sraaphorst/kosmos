package org.vorpal.kosmos.algebra.structures

import java.math.BigInteger

interface NonAssociativeAlgebra<A : Any> {
    val add: AbelianGroup<A>
    val mul: NonAssociativeMonoid<A>

    fun fromBigInt(n: BigInteger): A {
        tailrec fun aux(rem: BigInteger, acc: A): A =
            when (rem) {
                BigInteger.ZERO -> acc
                else -> aux(
                    rem - BigInteger.ONE,
                    add.op(acc, mul.identity)
                )
            }

        val pos = aux(n.abs(), add.identity)
        return if (n.signum() == -1) add.inverse(pos) else pos
    }
}