package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.functional.datastructures.Option

interface DivisionRing<A : Any> : Ring<A> {
    /**
     * Multiplicative inverse (reciprocal).
     *
     * Law: for all `a ≠ add.identity`, `a * reciprocal(a) = mul.identity`
     * and `reciprocal(a) * a = mul.identity`.
     *
     * Precondition: `a != add.identity (zero)`.
     *
     * May throw `ArithmeticException` if called on zero.
     */
    val reciprocal: Endo<A>

    val reciprocalOrNull: UnaryOp<A, A?>
        get() = UnaryOp(Symbols.SLASH) { a ->
            if (a == add.identity) null else reciprocal(a)
        }

    val reciprocalOption: UnaryOp<A, Option<A>>
        get() = UnaryOp(Symbols.SLASH) { a ->
            reciprocalOrNull(a)?.let { Option.Some(it) } ?: Option.None
        }

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: Monoid<A>,
            reciprocal: Endo<A>
        ): Field<A> = object : Field<A> {
            override val add: AbelianGroup<A> = add
            override val mul: Monoid<A> = mul
            override val reciprocal: Endo<A> = reciprocal
        }
    }
}
