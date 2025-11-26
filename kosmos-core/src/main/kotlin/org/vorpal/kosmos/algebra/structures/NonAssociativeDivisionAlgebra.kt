package org.vorpal.kosmos.algebra.structures

interface NonAssociativeDivisionAlgebra<A : Any> : InvolutiveAlgebra<A>, HasReciprocal<A> {
    override val zero: A
        get() = add.identity
}
