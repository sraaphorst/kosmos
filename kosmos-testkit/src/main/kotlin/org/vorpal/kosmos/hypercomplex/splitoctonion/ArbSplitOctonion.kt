package org.vorpal.kosmos.hypercomplex.splitoctonion

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.splitquaternion.ArbSplitQuaternion

object ArbSplitOctonion {

    /**
     * Generic split-octonions built coordinatewise from an arbitrary base-ring generator.
     */
    fun <R : Any> splitOctonionOf(arbR: Arb<R>): Arb<SplitOctonion<R>> =
        Arb.bind(arbR, arbR, arbR, arbR, arbR, arbR, arbR, arbR) { w, x, y, z, u, v, s, t ->
            splitOctonion(w, x, y, z, u, v, s, t)
        }

    /**
     * Exact split-octonions over ℚ with small coordinates, for laws that hold with strict equality.
     */
    val rationalSplitOctonion: Arb<SplitOctonion<Rational>> =
        splitOctonionOf(ArbSplitQuaternion.smallRational)

    /**
     * Exact split-octonions over ℚ off the null cone: exactly the units.
     */
    val unitRationalSplitOctonion: Arb<SplitOctonion<Rational>> = run {
        val normSq = SplitOctonionAlgebras.normSq(RationalAlgebras.RationalField)
        rationalSplitOctonion.filter { o -> normSq(o) != Rational.ZERO }
    }

    /**
     * Smallish real split-octonions.
     */
    val smallSplitOctonion: Arb<SplitOctonion<Real>> =
        splitOctonionOf(ArbReal.smallReal)

    /**
     * Real split-octonions safely off the null cone: `|N(o)|` in `[0.01, 10000]`.
     */
    val unitSplitOctonion: Arb<SplitOctonion<Real>> =
        smallSplitOctonion.filter { o ->
            kotlin.math.abs(SplitOctonionAlgebras.RealSplitOctonionAlgebra.normSq(o)) in 0.01..10_000.0
        }
}
