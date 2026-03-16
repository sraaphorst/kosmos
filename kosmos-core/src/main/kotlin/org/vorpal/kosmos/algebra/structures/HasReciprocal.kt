package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.functional.datastructures.Option

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
     * Precondition: `a != add.identity` (zero) in your actual algebra, i.e. [hasReciprocal] returns true.
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
}
