package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Endo

/**
 * A Meadow is a commutative ring with unity equipped with a *total* inverse operation.
 *
 * Conceptually: a field-like structure where division is totalized by defining `inv(0) = 0`,
 * while *retaining all commutative ring equations* (in particular `0 * x = 0` still holds).
 *
 * Meadows satisfy:
 * - the reflection law `inv(inv(x)) = x`; and
 * - the restricted inverse law `x * (x * inv(x)) = x`.
 *
 * From these, one can derive (and `MeadowLaws` checks):
 * - `inv(0) = 0` and `inv(1) = 1`;
 * - `inv(x * y) = inv(x) * inv(y)`;
 * - `inv(-x) = -inv(x)`;
 * - `x * inv(x)` is idempotent.
 *
 * Note that unlike in a field, where `reciprocal` is used to find the inverse of elements, in a meadow, we distinguish
 * this inverse operation as `inv` since it is distinct. A field specifically dictates that the reciprocal
 * is undefined at `0` (see [HasReciprocal]), whereas `inv` is total.
 *
 * Typical models:
 * - A field with its reciprocal extended by `inv(0) = 0` (a *zero-totalized field*, see [fromField]).
 *   These are exactly the *cancellation meadows*: `x ≠ 0` implies `x * inv(x) = 1`.
 * - Finite products of meadows (see `Meadows.product`), which have zero divisors and so are not fields.
 * - `ℤ/nℤ` for squarefree `n` (see `MeadowAlgebras.ZModMeadow`): by the CRT this is a product of prime fields.
 *
 * More generally, the commutative rings that admit a meadow inverse are exactly the von Neumann regular
 * commutative rings, and the inverse is then unique.
 *
 * References:
 * - J. A. Bergstra, J. V. Tucker, "The rational numbers as an abstract data type," J. ACM 54(2), 2007.
 * - J. A. Bergstra, I. Bethke, A. Ponse, "Cancellation meadows: a generic basis theorem and some applications,"
 *   The Computer Journal 56(1):3–14, 2013.
 */
interface Meadow<A : Any> : CommutativeRing<A> {
    val inv: Endo<A>

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: CommutativeMonoid<A>,
            inv: Endo<A>
        ): Meadow<A> = object : Meadow<A> {
            override val add = add
            override val mul = mul
            override val inv = inv
        }

        /**
         * The zero-totalized field: `inv(a) = reciprocal(a)` when the field says `a` is invertible, and `0` otherwise.
         *
         * Invertibility is decided by [HasReciprocal.hasReciprocal], so non-canonical representatives of zero
         * (e.g. `7` in `𝔽₇`) map to zero rather than throwing.
         *
         * For floating-point fields, `hasReciprocal` is a numerical test, so tiny nonzero values are sent to `0`:
         * the result is then only approximately a meadow, and the restricted inverse law fails near zero.
         */
        fun <A : Any> fromField(field: Field<A>): Meadow<A> = of(
            add = field.add,
            mul = field.mul,
            inv = Endo(Symbols.INVERSE) { a ->
                if (field.hasReciprocal(a)) field.reciprocal(a) else field.zero
            }
        )
    }
}

/**
 * The zero-totalized meadow of this field. See [Meadow.fromField].
 */
fun <A : Any> Field<A>.toMeadow(): Meadow<A> =
    Meadow.fromField(this)
