package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

/**
 * A NonAssociativeSemigroup is really just another term for a Magma, but
 * we insert it into the hierarchy to make it complete.
 */

interface NonAssociativeSemigroup<A : Any> : Magma<A> {
    companion object {
        fun <A: Any> of(
            op: BinOp<A>
        ) = object : NonAssociativeSemigroup<A> {
            override val op = op
        }
    }
}

/**
 * A semigroup is an associative Magma, which is simply a BinOp.
 *
 * Thus, we enforce the law: `op(op(x, y), z) == op(x, op(y, z))`.
 *
 * No new members added. */
interface Semigroup<A : Any> : Magma<A>, NonAssociativeSemigroup<A> {
    companion object {
        fun <A: Any> of(
            op: BinOp<A>
        ) = object : Semigroup<A> {
            override val op = op
        }
    }
}
