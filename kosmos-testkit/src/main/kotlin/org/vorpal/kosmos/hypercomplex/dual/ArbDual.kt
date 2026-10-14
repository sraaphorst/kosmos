package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger
import kotlin.math.abs

object ArbDual {

    /**
     * Generic dual numbers `a + bε` with primal part drawn from [primal] and tangent part drawn from
     * [tangent].
     */
    fun <R : Any> dualOf(
        primal: Arb<R>,
        tangent: Arb<R> = primal
    ): Arb<Dual<R>> =
        Arb.bind(primal, tangent) { a, b -> dual(f = a, df = b) }

    /**
     * Generic elements `primal + ε·tangent` of a scalar extension.
     */
    fun <V : Any> dualVectorOf(arbV: Arb<V>): Arb<DualVector<V>> =
        Arb.bind(arbV, arbV) { p, t -> DualVector(primal = p, tangent = t) }

    // ── Exact: ℚ and ℤ ───────────────────────────────────────────────────────

    /**
     * Rationals `p/q` with `|p| ≤ 12`, `1 ≤ q ≤ 12`: genuinely fractional, and small enough that
     * exact triple products stay fast.
     */
    val rational: Arb<Rational> =
        Arb.bind(Arb.int(-12..12), Arb.int(1..12)) { p, d -> Rational.of(p, d) }

    val nonZeroRational: Arb<Rational> =
        rational.filter { it != Rational.ZERO }

    /**
     * Exact dual numbers over ℚ, for laws that hold with strict equality.
     */
    val rationalDual: Arb<Dual<Rational>> =
        dualOf(rational)

    /**
     * Exact dual numbers over ℚ with nonzero primal part: exactly the units.
     */
    val unitRationalDual: Arb<Dual<Rational>> =
        dualOf(nonZeroRational, rational)

    /**
     * Dual numbers over ℤ.
     */
    val integerDual: Arb<Dual<BigInteger>> =
        dualOf(Arb.int(-30..30).map(Int::toBigInteger))

    // ── Floating point: ℝ ────────────────────────────────────────────────────

    /**
     * Smallish real dual numbers for associativity / distributivity / bilinearity tests.
     */
    val smallDual: Arb<Dual<Real>> =
        dualOf(ArbReal.smallReal)

    /**
     * Real dual numbers with primal part bounded away from zero (`0.1 ≤ |a| ≤ 100`) and a bounded
     * tangent, so that the reciprocal `a⁻¹ - (b/a²)ε` is numerically well-conditioned.
     */
    val unitDual: Arb<Dual<Real>> =
        dualOf(ArbReal.fieldReal, Arb.double(-100.0..100.0))

    /**
     * Real dual numbers that are *not* invertible: zero primal part and a nonzero tangent, so the
     * additive zero is not always what is generated.
     */
    val nonUnitDual: Arb<Dual<Real>> =
        Arb.double(-100.0..100.0)
            .filter { abs(it) > 1e-3 }
            .map { b -> dual(f = 0.0, df = b) }
}
