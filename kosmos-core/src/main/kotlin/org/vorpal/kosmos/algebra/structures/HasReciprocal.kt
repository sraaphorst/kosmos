package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.functional.datastructures.Option

interface HasReciprocal<A : Any> {
    val zero: A

    /**
     * Multiplicative Inverse.
     *
     * Precondition: `a != add.identity` (zero) in your actual algebra.
     *
     * Law (context-dependent): `a * reciprocal(a) == 1 == reciprocal(a) * a`, where `1` is `mul.identity`
     * (or the non-associative analogue).
     */
    val reciprocal: Endo<A>

    val reciprocalOrNull: UnaryOp<A, A?>
        get() = UnaryOp(reciprocal.symbol) { a ->
            if (a == zero) null else reciprocal(a)
        }

    val reciprocalOption: UnaryOp<A, Option<A>>
        get() = UnaryOp(reciprocal.symbol) { a -> Option.of(reciprocalOrNull(a)) }
}
