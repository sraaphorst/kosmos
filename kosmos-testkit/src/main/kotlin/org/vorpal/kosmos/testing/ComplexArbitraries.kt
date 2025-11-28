package org.vorpal.kosmos.testing

import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.triple
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.instances.Complex
import kotlin.math.cos
import kotlin.math.sin

object ComplexArbitraries {

    /**
     * Generic complex values, componentwise from RealArbitraries.real.
     */
    val complex: Arb<Complex> =
        Arb.pair(RealArbitraries.real, RealArbitraries.real)
            .map { (re, im) -> CD(re, im) }

    /**
     * Bounded complex values for “field-ish” laws.
     */
    val boundedComplex: Arb<Complex> =
        Arb.pair(RealArbitraries.fieldReal, RealArbitraries.fieldReal)
            .map { (re, im) -> CD(re, im) }

    /**
     * Non-zero complex values: at least one component non-zero.
     */
    val nonZeroComplex: Arb<Complex> =
        complex.filter { c -> c.a != 0.0 || c.b != 0.0 }

    /**
     * Complex numbers suitable for reciprocals:
     * norm bounded above and away from zero.
     */
    val reciprocalSafeComplex: Arb<Complex> =
        boundedComplex.filter { c ->
            val normSq = c.a * c.a + c.b * c.b
            normSq in 0.01..10_000.0
        }

    /**
     * Complex numbers on the unit circle: norm = 1.
     */
    val unitCircleComplex: Arb<Complex> =
        Arb.double(0.0..(2.0 * Math.PI))
            .map { theta ->
                CD(cos(theta), sin(theta))
            }

    val complexPair: Arb<Pair<Complex, Complex>> =
        Arb.pair(complex, complex)

    val nonZeroPair: Arb<Pair<Complex, Complex>> =
        Arb.pair(nonZeroComplex, nonZeroComplex)

    val complexTriple: Arb<Triple<Complex, Complex, Complex>> =
        Arb.triple(boundedComplex, boundedComplex, boundedComplex)

    val fieldTriple: Arb<Triple<Complex, Complex, Complex>> =
        Arb.triple(reciprocalSafeComplex, reciprocalSafeComplex, reciprocalSafeComplex)
}