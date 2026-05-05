package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.triple
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.core.math.Real

object ArbSplitComplex {

    /**
     * Generic split-complex values built componentwise from an arbitrary base-ring generator.
     */
    fun <R : Any> splitComplexOf(arbR: Arb<R>): Arb<SplitComplex<R>> =
        Arb.pair(arbR, arbR)
            .map { (re, hy) -> CD(re, hy) }

    /**
     * Generic split-complex values, componentwise from `ArbReal.real`.
     */
    val splitComplex: Arb<SplitComplex<Real>> =
        splitComplexOf(ArbReal.real)

    /**
     * Bounded split-complex values for ring / module / algebra laws,
     * keeping overflow and catastrophic cancellation under control.
     */
    val boundedSplitComplex: Arb<SplitComplex<Real>> =
        splitComplexOf(ArbReal.fieldReal)

    /**
     * Smallish split-complex values for associativity / distributivity / bilinearity tests.
     */
    val smallSplitComplex: Arb<SplitComplex<Real>> =
        splitComplexOf(ArbReal.smallReal)

    /**
     * Non-zero split-complex values: at least one component non-zero.
     *
     * Note: unlike the complex case, "non-zero" is *not* the same as "invertible"
     * — split-complex numbers have zero divisors on the null cone `a² = b²`.
     */
    val nonZeroSplitComplex: Arb<SplitComplex<Real>> =
        splitComplex.filter { z -> z.a != 0.0 || z.b != 0.0 }

    /**
     * Split-complex values strictly off the null cone `a² = b²`,
     * i.e. those with `N(z) = a² − b² ≠ 0`. These are exactly the units of
     * `SplitComplex<Real>` and are well-suited for any test that wants invertibility.
     */
    val unitSplitComplex: Arb<SplitComplex<Real>> =
        boundedSplitComplex.filter { z ->
            val n = z.a * z.a - z.b * z.b
            kotlin.math.abs(n) > 0.01 && n.isFinite()
        }

    val splitComplexPair: Arb<Pair<SplitComplex<Real>, SplitComplex<Real>>> =
        Arb.pair(splitComplex, splitComplex)

    val nonZeroSplitComplexPair: Arb<Pair<SplitComplex<Real>, SplitComplex<Real>>> =
        Arb.pair(nonZeroSplitComplex, nonZeroSplitComplex)

    val splitComplexTriple: Arb<Triple<SplitComplex<Real>, SplitComplex<Real>, SplitComplex<Real>>> =
        Arb.triple(boundedSplitComplex, boundedSplitComplex, boundedSplitComplex)
}
