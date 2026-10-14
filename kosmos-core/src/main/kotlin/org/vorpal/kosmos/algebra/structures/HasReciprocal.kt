package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.functional.datastructures.Option

/**
 * A structure in which some elements have a multiplicative inverse.
 *
 * The contract between the three members is:
 * - [hasReciprocal] is the single source of truth for invertibility. It is a *total* predicate.
 * - [reciprocal] is defined exactly on the elements for which [hasReciprocal] is `true`, and throws
 *   [ArithmeticException] on every other element.
 * - [reciprocalOption] is the total version of [reciprocal] and is derived from the two above.
 *
 * Implementations must keep [hasReciprocal] and [reciprocal] in agreement: an element for which
 * [hasReciprocal] returns `true` must invert without throwing.
 *
 * For exact carriers (integers, rationals, finite fields, and algebras over them) the default
 * `a != zero` is the right predicate for division rings and fields. Structures with zero divisors
 * (dual numbers, split-complex numbers, bicomplex numbers) and integral orders (Hurwitz, Lipschitz,
 * Gravesian, whose only invertible elements are the units) must override it.
 *
 * For floating-point carriers, invertibility is *numerical*: an element counts as invertible when
 * its squared norm is finite and not approximately zero under `RealAlgebras.eqRealApprox`. This
 * deliberately treats `NaN`, `±∞`, and subnormal-scale elements as non-invertible, so that
 * [reciprocal] never returns a non-finite value. Floating-point implementations must override
 * [hasReciprocal] with that predicate rather than inheriting the structural `a != zero`.
 */
interface HasReciprocal<A : Any> {
    val zero: A

    /**
     * Return true if the element has a multiplicative inverse, false otherwise.
     */
    fun hasReciprocal(a: A): Boolean =
        a != zero

    /**
     * Multiplicative Inverse.
     *
     * Precondition: [hasReciprocal] returns true for `a`.
     *
     * Throws [ArithmeticException] if the element does not have a reciprocal.
     *
     * Law (context-dependent): `a * reciprocal(a) == 1 == reciprocal(a) * a`, where `1` is `mul.identity`
     * (or the non-associative analogue).
     */
    val reciprocal: Endo<A>

    val reciprocalOption: UnaryOp<A, Option<A>>
        get() = UnaryOp(reciprocal.symbol) { a ->
            if (hasReciprocal(a)) Option.Some(reciprocal(a))
            else Option.None
        }

    companion object {
        /**
         * The exception thrown by every [reciprocal] on an element that [hasReciprocal] rejects.
         */
        fun notInvertible(a: Any, where: String): ArithmeticException =
            ArithmeticException("$a has no multiplicative inverse in $where.")
    }
}
