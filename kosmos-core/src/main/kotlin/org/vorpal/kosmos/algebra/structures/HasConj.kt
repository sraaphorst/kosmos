package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo

/**
 * A distinguished self-map on the carrier, and the involution operator of the algebra,
 * `x ↦ x*`, written `conj(x) = x*`.
 *
 * In richer structures such as [NonAssociativeInvolutiveRing], this map is not merely
 * an arbitrary endomorphism: it is intended by law to be an involutive additive
 * anti-homomorphism, i.e. it satisfies:
 * ```text
 * conj(conj(x)) = x
 * conj(x + y) = conj(x) + conj(y)
 * conj(x * y) = conj(y) * conj(x)
 * conj(0) = 0
 * conj(1) = 1
 * ```
 * Thus, in those settings, `conj` is an anti-automorphism whose square is the identity.
 *
 * It need not itself have order 2 as a map: in commutative fixed-point cases
 * such as the standard conjugation on Real or Rational, we have `conj(x) = x` for
 * all `x`, so the map is simply the identity and therefore has order 1.
 *
 * Accordingly, the correct general statement is that `conj` is involutive, or equivalently
 * that its order divides 2.
 */
interface HasConj<A : Any> {
    val conj: Endo<A>
}
