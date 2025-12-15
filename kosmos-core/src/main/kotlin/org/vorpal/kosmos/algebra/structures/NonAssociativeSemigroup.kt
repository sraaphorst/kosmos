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
        ): NonAssociativeSemigroup<A> = object : NonAssociativeSemigroup<A> {
            override val op = op
        }
    }
}