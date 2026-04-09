package org.vorpal.kosmos.algebra.structures

import java.math.BigInteger

/**
 * Compute `n · x` in the group by doubling: `O(log |n|)` group operations.
 *
 * Note that while this is typically associated with repeated addition, it can be used equally well for any repeated
 * group operation, such as repeated multiplication.
 *
 * @param n a [BigInteger] scalar multiplier
 * @param x a value of [A] to be scaled
 */
fun <A : Any> Group<A>.zTimes(
    n: BigInteger,
    x: A
): A {
    val s = n.signum()
    if (s == 0) return identity
    if (n == BigInteger.ONE) return x
    if (n == BigInteger.ONE.negate()) return inverse(x)

    tailrec fun natTimes(k: BigInteger, acc: A, cur: A): A =
        when {
            k.signum() == 0  -> acc
            k.testBit(0) -> natTimes(k.shiftRight(1), op(acc, cur), op(cur, cur))
            else             -> natTimes(k.shiftRight(1), acc,          op(cur, cur))
        }

    val pos = natTimes(n.abs(), identity, x)
    return if (s < 0) inverse(pos) else pos
}