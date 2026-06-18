package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import org.vorpal.kosmos.algebra.structures.instances.ArbReal

object ArbBicomplex {

    /**
     * Generic bicomplex numbers with small real coefficients, drawn in the standard basis
     * `a + bi + cj + dk`.
     */
    val bicomplex: Arb<Bicomplex> =
        Arb.bind(
            ArbReal.smallReal,
            ArbReal.smallReal,
            ArbReal.smallReal,
            ArbReal.smallReal
        ) { a, b, c, d ->
            Bicomplex.ofStandard(a, b, c, d)
        }
}
