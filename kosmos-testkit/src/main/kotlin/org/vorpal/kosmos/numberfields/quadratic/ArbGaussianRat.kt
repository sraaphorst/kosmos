package org.vorpal.kosmos.numberfields.quadratic

import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.core.rational.ArbRational

object ArbGaussianRat {
    val small: Arb<GaussianRat> =
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