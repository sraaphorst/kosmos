package org.vorpal.kosmos.hypercomplex.complex

import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.triple
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import kotlin.math.cos
import kotlin.math.sin

object ArbComplex {

    /**
     * Generic complex values, componentwise from RealArbitraries.real.
     */
    val complex: Arb<RealComplex> =
        Arb.pair(ArbReal.real, ArbReal.real)
            .map { (re, im) -> complex(re, im) }

    /**
     * Bounded complex values for “field-ish” laws.
     */
    val boundedComplex: Arb<RealComplex> =
        Arb.pair(ArbReal.fieldReal, ArbReal.fieldReal)
            .map { (re, im) -> complex(re, im) }

    /**
     * Non-zero complex values: at least one component non-zero.
     */
    val nonZeroComplex: Arb<RealComplex> =
        complex.filter { c -> c.re != 0.0 || c.im != 0.0 }

    /**
     * Complex numbers suitable for reciprocals:
     * norm bounded above and away from zero.
     */
    val reciprocalSafeComplex: Arb<RealComplex> =
        boundedComplex.filter { c ->
            val normSq = c.re * c.re + c.im * c.im
            normSq in 0.01..10_000.0
        }

    /**
     * Complex numbers on the unit circle: norm = 1.
     */
    val unitCircleComplex: Arb<RealComplex> =
        Arb.double(0.0..(2.0 * Math.PI))
            .map { theta ->
                complex(cos(theta), sin(theta))
            }

    val complexPair: Arb<Pair<RealComplex, RealComplex>> =
        Arb.pair(complex, complex)

    val nonZeroPair: Arb<Pair<RealComplex, RealComplex>> =
        Arb.pair(nonZeroComplex, nonZeroComplex)

    val complexTriple: Arb<Triple<RealComplex, RealComplex, RealComplex>> =
        Arb.triple(boundedComplex, boundedComplex, boundedComplex)

    val fieldTriple: Arb<Triple<RealComplex, RealComplex, RealComplex>> =
        Arb.triple(reciprocalSafeComplex, reciprocalSafeComplex, reciprocalSafeComplex)
}