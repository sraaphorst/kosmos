package org.vorpal.kosmos.algebra.structures

/**
 * A [CommutativeRing] that is also an [InvolutiveRing].
 */
interface CommutativeInvolutiveRing<A : Any> :
    CommutativeRing<A>,
    InvolutiveRing<A>
