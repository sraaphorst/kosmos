package org.vorpal.kosmos.algebra.structures

/**
 * We get the scalar ring, `CommutativeRing<R>` from the `Algebra<R, A>`.
 * We get the ring on `A` and `conj: Endo<A>` from `InvolutiveRing<A>`.
 */
interface StarAlgebra<R : Any, A : Any> : Algebra<R, A>, InvolutiveRing<A>
