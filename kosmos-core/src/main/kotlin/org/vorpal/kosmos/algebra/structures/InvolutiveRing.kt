package org.vorpal.kosmos.algebra.structures

/**
 * A ring with an involution (conjugation) satisfying:
 * 1. `(a*)* == a`
 * 2. `(a + b)* == a* + b*`
 * 3. `(ab)* == b*a*`
 * 4. `1* = 1`
 */
interface InvolutiveRing<A : Any> : InvolutiveAlgebra<A>, Ring<A>
