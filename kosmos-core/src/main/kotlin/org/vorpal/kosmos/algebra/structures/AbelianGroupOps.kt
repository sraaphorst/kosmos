package org.vorpal.kosmos.algebra.structures

import java.math.BigInteger

/**
 * * Compute `n · x` in the abelian group by doubling: `O(log |n|)` additions.
 *
 * @param n a [BigInteger] scalar multiplier
 * @param x a value of [A] to be scaled
 */
fun <A : Any> AbelianGroup<A>.zTimes(
    n: BigInteger,
    x: A
): A {
    val s = n.signum()
    if (s == 0) return identity
    if (n == BigInteger.ONE) return x
    if (n == BigInteger.ONE.negate()) return inverse(x)

    fun natTimes(k: BigInteger): A {
        var rem = k
        var acc = identity
        var cur = x

        while (rem.signum() > 0) {
            if (rem.testBit(0)) {
                acc = op(acc, cur)
            }
            cur = op(cur, cur)
            rem = rem.shiftRight(1)
        }
        return acc
    }

    val pos = natTimes(n.abs())
    if (s < 0) return inverse(pos)
    return pos
}
