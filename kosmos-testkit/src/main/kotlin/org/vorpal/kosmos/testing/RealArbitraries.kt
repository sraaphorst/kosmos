package org.vorpal.kosmos.testing

import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.triple
import org.vorpal.kosmos.algebra.structures.instances.Real
import kotlin.math.abs

object RealArbitraries {

    /**
     * General real values, reasonably bounded and finite.
     */
    val real: Arb<Real> =
        Arb.double(-1_000.0..1_000.0)
            .filter { it.isFinite() }

    /**
     * Reals suitable for multiplication / field laws.
     * Away from 0 and not too large.
     */
    val fieldReal: Arb<Real> =
        Arb.double(-100.0..100.0)
            .filter { it.isFinite() && abs(it) > 0.1 }

    /**
     * Non-zero reals (for reciprocal / division).
     */
    val nonZeroReal: Arb<Real> =
        fieldReal
            .filter { it != 0.0 }

    /**
     * Smallish reals for associativity / distributivity,
     * to keep overflow and catastrophic cancellation down.
     */
    val smallReal: Arb<Real> =
        Arb.double(-10.0..10.0)
            .filter { it.isFinite() }

    val realPair: Arb<Pair<Real, Real>> =
        Arb.pair(real, real)

    val realTriple: Arb<Triple<Real, Real, Real>> =
        Arb.triple(smallReal, smallReal, smallReal)

    val fieldTriple: Arb<Triple<Real, Real, Real>> =
        Arb.triple(fieldReal, fieldReal, fieldReal)
}