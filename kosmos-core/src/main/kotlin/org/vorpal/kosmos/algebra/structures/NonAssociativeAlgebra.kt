package org.vorpal.kosmos.algebra.structures

/**
 * A non-associative algebra is a non-associative ring acted on by a commutative ring of scalars.
 */
interface NonAssociativeAlgebra<R : Any, A : Any> :
    RModule<R, A>,
    NonAssociativeRing<A> {
    override val scalars: CommutativeRing<R>
}
