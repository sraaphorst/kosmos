package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.basisPrintable
import org.vorpal.kosmos.core.render.Printable

internal object OctonionPrintable {
    /**
     * Create a [Printable] for an octonionic type.
     */
    fun <A : Any, O : Any> octonionPrintable(
        signed: SignedOps<A>,
        zero: A,
        one: A,
        prA: Printable<A>,
        eqA: Eq<A>,
        decompose: (O) -> List<A>
    ): Printable<O> =
        basisPrintable(
            labels = listOf("", "e1", "e2", "e3", "e4", "e5", "e6", "e7"),
            decompose = decompose,
            signed = signed,
            zero = zero,
            one = one,
            prA = prA,
            eqA = eqA
        )
}
