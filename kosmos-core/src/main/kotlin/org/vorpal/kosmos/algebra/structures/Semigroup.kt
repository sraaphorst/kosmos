package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp

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
