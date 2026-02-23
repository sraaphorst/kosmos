package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Endo

/**
 * A Meadow is a commutative ring with unity and with a total inverse operation.
 *
 * Typical model: a field with inv(0) = 0.
 */
interface Meadow<A : Any> : CommutativeRing<A> {
    val inv: Endo<A>

    companion object {
        fun <A : Any> fromField(
            field: Field<A>,
            eq: Eq<A>
        ): Meadow<A> = object : Meadow<A> {
            override val zero = field.add.identity

            override val inv = Endo<A>(Symbols.INVERSE) { a ->
                if (eq(a, zero)) zero
                else field.reciprocal(a)
            }

            override val add = field.add
            override val mul = field.mul
        }

        /**
         * Warning: Unless the field is known to have a default Eq instance that will measure zero precisely,
         * using this function may lead to unexpected behavior.
         */
        fun <A : Any> fromFieldDefaultEq(
            field: Field<A>
        ): Meadow<A> = fromField(field, Eq.default())
    }
}
