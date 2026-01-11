package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo

/**
 * A ring with an involution (conjugation) satisfying:
 * 1. `(a*)* == a`
 * 2. `(a + b)* == a* + b*`
 * 3. `(ab)* == b* a*`
 * 4. `1* = 1`
 * 5. `0* = 0` (follows from additivity)
 */
interface InvolutiveRing<A : Any> : NonAssociativeInvolutiveRing<A>, Ring<A> {
    // We have to disambiguate one since it comes from both InvolutiveAlgebra and Ring.
    override val one: A

    companion object {
        fun <A : Any> of(
            ring: Ring<A>,
            conj: Endo<A>
        ): InvolutiveRing<A> = object : InvolutiveRing<A> {
            override val add = ring.add
            override val mul = ring.mul
            override val conj = conj
            override val one = ring.one
        }
    }
}
