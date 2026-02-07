package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

/**
 * An [AbelianGroup] is both a [Group] and a [CommutativeMonoid].
 */
interface AbelianGroup<A : Any> : Group<A>, CommutativeMonoid<A> {
    companion object {
        fun <A : Any> of(
            identity: A,
            op: BinOp<A>,
            inverse: Endo<A>
        ): AbelianGroup<A> = object : AbelianGroup<A> {
            override val identity = identity
            override val op = op
            override val inverse = inverse
        }
    }
}
