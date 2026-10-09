package org.vorpal.kosmos.hypercomplex.complex

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.basisPrintable
import org.vorpal.kosmos.core.render.Printable

internal object ComplexPrintable {
    /**
     * Prints a complex-like object as a linear combination of basis elements.
     *
     * Examples: RealComplex, RationalComplex, GaussianInt, GaussianRat, EisensteinInt.
     */
    fun <A : Any, Z : Any> complexLikePrintable(
        signed: SignedOps<A>,
        zero: A,
        one: A,
        re: UnaryOp<Z, A>,
        im: UnaryOp<Z, A>,
        basis: String,
        prA: Printable<A> = Printable.default(),
        eqA: Eq<A> = Eq.default()
    ): Printable<Z> =
        basisPrintable(
            labels = listOf("", basis),
            decompose = { z -> listOf(re(z), im(z)) },
            signed = signed,
            zero = zero,
            one = one,
            prA = prA,
            eqA = eqA
        )
}
