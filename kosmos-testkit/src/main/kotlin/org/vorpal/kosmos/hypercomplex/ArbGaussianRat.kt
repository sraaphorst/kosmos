package org.vorpal.kosmos.hypercomplex

import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.hypercomplex.complex.GaussianRat
import org.vorpal.kosmos.std.ArbRational

object ArbGaussianRat {
    val gaussianRat: Arb<GaussianRat> =
        Arb.pair(ArbRational.small, ArbRational.small)
            .map { (a, b) -> GaussianRat(a, b) }

    /**
     * Build a GaussianRat that is not zero, i.e. at least one of the components is non-zero.
     */
    val nonzero: Arb<GaussianRat> =
        Arb.choice(
            Arb.pair(ArbRational.nonZero, ArbRational.small)
                .map { (a, b) -> GaussianRat(a, b) },
            Arb.pair(ArbRational.small, ArbRational.nonZero)
                .map { (a, b) -> GaussianRat(a, b) }
        )
}
