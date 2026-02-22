package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

/**
 * A [Semigroup], i.e. an associative structure with an identity element.
 *
 * It adds associativity to a [NonAssociativeMonoid], so it extends that as well.
 *
 * Laws:
 * 1. Associativity (from [Semigroup])
 * 2. Identity is both left and right unit: `op(identity, x) == x == op(x, identity)`.
 */
interface Monoid<A : Any> : Semigroup<A>, NonAssociativeMonoid<A> {
    companion object {
        fun <A: Any> of(
            identity: A,
            op: BinOp<A>
        ): Monoid<A> = object : Monoid<A> {
            override val identity = identity
            override val op = op
        }
    }
}

/**
 * Calculate `a^n` in the [monoid] recursively using exponentiation by squaring.
 */
fun <A : Any> Monoid<A>.pow(a: A, n: Int): A {
    require(n >= 0) { "pow n must be non-negative, got: $n" }
    if (n == 0) return identity

    /**
     * Use doubling to minimize the number of multiplications.
     */
    tailrec fun aux(curr: A, remPow: Int, acc: A = this.identity): A =
        when {
            remPow <= 0 -> acc
            remPow % 2 == 1 -> aux(this(curr, curr), remPow / 2, this(acc, curr))
            else -> aux(this(curr, curr), remPow / 2, acc)
        }
    return aux(a, n)
}
