package org.vorpal.kosmos.hypercomplex.splitquaternion

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

object ArbSplitQuaternion {

    /**
     * Generic split-quaternions built coordinatewise from an arbitrary base-ring generator.
     */
    fun <R : Any> splitQuaternionOf(arbR: Arb<R>): Arb<SplitQuaternion<R>> =
        Arb.bind(arbR, arbR, arbR, arbR) { w, x, y, z -> SplitQuaternion(w, x, y, z) }

    /**
     * Rationals `p/q` with `|p| ≤ 12`, `1 ≤ q ≤ 12`: small, so exact triple products stay fast.
     */
    val smallRational: Arb<Rational> =
        Arb.pair(Arb.int(-12..12), Arb.int(1..12)).map { (p, d) -> Rational.of(p, d) }

    /**
     * Exact split-quaternions over ℚ, for laws that hold with strict equality.
     */
    val rationalSplitQuaternion: Arb<SplitQuaternion<Rational>> =
        splitQuaternionOf(smallRational)

    /**
     * Exact split-quaternions over ℚ off the null cone `w² + x² = y² + z²`: exactly the units.
     */
    val unitRationalSplitQuaternion: Arb<SplitQuaternion<Rational>> =
        rationalSplitQuaternion.filter { (w, x, y, z) -> w * w + x * x != y * y + z * z }

    /**
     * Split-quaternions over ℤ.
     */
    val integerSplitQuaternion: Arb<SplitQuaternion<BigInteger>> =
        splitQuaternionOf(Arb.int(-20..20).map(Int::toBigInteger))

    /**
     * Smallish real split-quaternions for associativity / distributivity / bilinearity tests.
     */
    val smallSplitQuaternion: Arb<SplitQuaternion<Real>> =
        splitQuaternionOf(ArbReal.smallReal)

    /**
     * Real split-quaternions safely off the null cone: `|N(q)|` in `[0.01, 10000]`, so the
     * reciprocal is numerically well-conditioned.
     */
    val unitSplitQuaternion: Arb<SplitQuaternion<Real>> =
        smallSplitQuaternion.filter { (w, x, y, z) ->
            kotlin.math.abs(w * w + x * x - y * y - z * z) in 0.01..10_000.0
        }
}
