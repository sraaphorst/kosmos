package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

/**
 * A [CommutativeRing] that is also an [InvolutiveRing].
 */
interface CommutativeInvolutiveRing<A : Any> :
    CommutativeRing<A>,
    InvolutiveRing<A> {

    companion object {
        /**
         * Equip a [CommutativeRing] with the involution [conj].
         *
         * The caller is responsible for [conj] satisfying the involution laws.
         * Since the ring is commutative, an involutive anti-automorphism is the same thing
         * as an involutive automorphism.
         */
        fun <A : Any> of(
            ring: CommutativeRing<A>,
            conj: Endo<A>
        ): CommutativeInvolutiveRing<A> = object : CommutativeInvolutiveRing<A> {
            override val add = ring.add
            override val mul = ring.mul
            override val conj = conj
            override val one = ring.one

            override fun fromBigInt(n: BigInteger): A =
                ring.fromBigInt(n)
        }

        /**
         * Equip a [CommutativeRing] with the trivial involution `x* = x`.
         *
         * Every commutative ring is an involutive ring in this way. This is the base-level
         * involution that turns the Cayley-Dickson doubling into a commutative quadratic
         * extension; see [CayleyDickson.commutative].
         */
        fun <A : Any> trivial(
            ring: CommutativeRing<A>
        ): CommutativeInvolutiveRing<A> =
            of(
                ring = ring,
                conj = Endo(Symbols.CONJ) { it }
            )
    }
}
