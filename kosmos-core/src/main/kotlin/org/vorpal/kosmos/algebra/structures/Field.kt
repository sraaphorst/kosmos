package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

/**
 * A Field is a commutative Ring where the multiplicative operator has inverses for
 * all elements except for the additive identity.
 */
interface Field<A : Any> : DivisionRing<A>, CommutativeRing<A> {
    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: CommutativeMonoid<A>,
            reciprocal: Endo<A>
        ): Field<A> = object : Field<A> {
            override val add: AbelianGroup<A> = add
            override val mul: CommutativeMonoid<A> = mul
            override val reciprocal: Endo<A> = reciprocal
        }
    }
}
