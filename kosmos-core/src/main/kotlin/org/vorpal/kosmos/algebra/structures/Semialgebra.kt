package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.LeftAction

// TODO: WRITE LAWS FOR THIS.
/**
 * An R-semialgebra is a semiring A that is also an R-semimodule,
 * with scalar action compatible with multiplication (bilinearity).
 *
 * Scalars are typically taken from a commutative semiring so that left/right scaling agrees.
 *
 * Compatibility laws (in addition to semiring + semimodule laws):
 *
 *    r ⊳ (a · b) = (r ⊳ a) · b
 *    r ⊳ (a · b) = a · (r ⊳ b)         // requires scalars act "centrally"; holds for commutative scalars in the usual setting
 */
interface Semialgebra<R : Any, A : Any> :
    Semimodule<R, A>,
    Semiring<A> {

    override val scalars: CommutativeSemiring<R>

    companion object {
        fun <R : Any, A : Any> of(
            scalars: CommutativeSemiring<R>,
            add: CommutativeMonoid<A>,
            mul: Monoid<A>,
            leftAction: LeftAction<R, A>,
        ): Semialgebra<R, A> =
            object : Semialgebra<R, A> {
                override val scalars = scalars
                override val add = add
                override val mul = mul
                override val leftAction = leftAction
            }
    }
}
