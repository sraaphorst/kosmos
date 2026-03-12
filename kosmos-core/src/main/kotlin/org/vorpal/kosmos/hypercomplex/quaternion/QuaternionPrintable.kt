package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.basisPrintable
import org.vorpal.kosmos.core.render.Printable

internal object QuaternionPrintable {
    /**
     * Create a [Printable] for a quaternionic type.
     */
    fun <A : Any, Q : Any> quaternionPrintable(
        signed: SignedOps<A>,
        zero: A,
        one: A,
        prA: Printable<A>,
        eqA: Eq<A>,
        decompose: (Q) -> List<A>
    ): Printable<Q> =
        basisPrintable(
            labels = listOf("", Symbols.IMAGINARY_I, Symbols.IMAGINARY_J, Symbols.IMAGINARY_K),
            decompose = decompose,
            signed = signed,
            zero = zero,
            one = one,
            prA = prA,
            eqA = eqA
        )
}
