package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo

/**
 * A ring-like structure equipped with a distinguished conjugation operation.
 *
 * The prefix “non-associative” indicates that multiplication is not **assumed** to be associative.
 * This interface therefore covers both genuinely non-associative examples, such as the octonions,
 * and associative examples viewed in a more general setting, such as ordinary involutive rings.
 *
 * The conjugation `conj` is required by law to be an involutive additive anti-homomorphism:
 * ```text
 * conj(conj(x)) = x
 * conj(x + y) = conj(x) + conj(y)
 * conj(x * y) = conj(y) * conj(x)
 * conj(0) = 0
 * conj(1) = 1
 * ```
 * Thus `conj` reverses the order of multiplication and has square equal to the identity.
 *
 * Note that it need not itself have order 2 as a map: in fixed-point cases such as the standard
 * conjugation on Integer, Real, or Rational, `conj` is simply the identity and therefore has
 * order 1. The accurate general statement is that `conj` is involutive, or equivalently, that
 * its order divides 2.
 *
 * When multiplication happens to be commutative, the distinction between homomorphism and
 * anti-homomorphism disappears, since:
 * ```text
 * conj(x * y) = conj(y) * conj(x) = conj(x) * conj(y).
 * ```
 * When multiplication is associative, this specializes to the usual notion of an involutive
 * ring. In particular, [InvolutiveRing] is the associative subcase of this interface.
 */
interface NonAssociativeInvolutiveRing<A : Any> :
    NonAssociativeRing<A>,
    HasConj<A> {

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: NonAssociativeMonoid<A>,
            conj: Endo<A>
        ): NonAssociativeInvolutiveRing<A> = object : NonAssociativeInvolutiveRing<A> {
            override val add = add
            override val mul = mul
            override val conj = conj
        }
    }
}
