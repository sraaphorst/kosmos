package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

/**
 * A NonAssociativeMonoid is an extension of a [NonAssociativeSemigroup] but with
 * an identity element.
 */
interface NonAssociativeMonoid<A : Any> : NonAssociativeSemigroup<A> {
    val identity: A

    companion object {
        fun <A: Any> of(
            identity: A,
            op: BinOp<A>
        ) : NonAssociativeMonoid<A> = object : NonAssociativeMonoid<A> {
            override val identity = identity
            override val op = op
        }
    }
}

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
 * A commutative [Monoid].
 *
 * These add no properties to monoid, but come up often enough
 * that we include them as a means of tagging the monoid as commutative.
 *
 * [AbelianGroup] implements this.
 */
interface CommutativeMonoid<A: Any> : Monoid<A> {
    companion object {
        fun <A: Any> of(
            identity: A,
            op: BinOp<A>
        ) : CommutativeMonoid<A> = object : CommutativeMonoid<A> {
            override val identity = identity
            override val op = op
        }
    }
}
