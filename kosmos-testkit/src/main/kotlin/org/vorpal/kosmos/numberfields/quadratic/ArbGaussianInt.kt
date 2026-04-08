package org.vorpal.kosmos.numberfields.quadratic

import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger

object ArbGaussianInt {
    val small: Arb<GaussianInt> =
        Arb.pair(ArbInteger.small, ArbInteger.small)
            .map { (a, b) -> GaussianInt(a, b) }

    val nonzero: Arb<GaussianInt> =
        Arb.choice(
            Arb.pair(ArbInteger.nonZeroSmall, ArbInteger.small)
                .map { (a, b) -> GaussianInt(a, b) },
            Arb.pair(ArbInteger.small, ArbInteger.nonZeroSmall)
                .map { (a, b) -> GaussianInt(a, b) },
        )
}