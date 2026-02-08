package org.vorpal.kosmos.algebra.poly

/**
 * Represent a dumb polynomial over a type [A]. The coefficient for x_i is the value in position i of [coeffs].
 *
 * The canonical form has no trailing zeros, and the zero polynomial has an empty [List] as its representation.
 *
 * Normalized creation of polynomials is done via the PolyKit or PolyScope, and not directly via [Poly].
 */
@ConsistentCopyVisibility
data class Poly<A : Any> internal constructor(
    val coeffs: List<A>
) {
    companion object {
        fun <A : Any> zero(): Poly<A> = Poly(emptyList())
        internal fun <A : Any> ofUnsafe(coeffs: List<A>): Poly<A> = Poly(coeffs)
    }
}
