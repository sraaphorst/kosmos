package org.vorpal.kosmos.algebra.poly

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.algebra.poly.kit.PolyKit
import org.vorpal.kosmos.core.math.Natural
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion
import java.math.BigInteger

/**
 * Arbitrary polynomials, built through a [PolyKit].
 *
 * The [Poly] constructor is internal to `kosmos-core`, so every polynomial here is built from the
 * kit's own operations by Horner's rule (see [polyOf]). That makes the generators depend on the
 * code they help test, so `PolyArithmeticSpec` checks [polyOf] against an independent list model
 * before anything else relies on it.
 *
 * Coefficient generators are deliberately tiny (mostly `|c| ≤ 3`), so that zero coefficients,
 * including trailing zeros in the raw coefficient list, show up often and normalization is
 * exercised on nearly every run.
 */
object ArbPoly {

    /**
     * The polynomial `c₀ + c₁x + ⋯ + cₙxⁿ` in the kit's last variable, built by Horner's rule:
     * ```text
     * c₀ + x·(c₁ + x·(c₂ + ⋯))
     * ```
     * The list may contain trailing zeros: the result is normalized by the kit's own arithmetic.
     */
    fun <A : Any> polyOf(kit: PolyKit<A>, coeffs: List<A>): Poly<A> =
        coeffs.foldRight(kit.zeroP) { c, acc ->
            kit.addP(kit.embed(c), kit.mulP(acc, kit.x()))
        }

    /**
     * Raw coefficient lists of length `0..maxDegree + 1`, trailing zeros allowed.
     */
    fun <A : Any> coeffLists(coeff: Arb<A>, maxDegree: Int = 4): Arb<List<A>> =
        Arb.list(coeff, 0..maxDegree + 1)

    /**
     * Polynomials of degree at most [maxDegree] (including the zero polynomial).
     */
    fun <A : Any> poly(kit: PolyKit<A>, coeff: Arb<A>, maxDegree: Int = 4): Arb<Poly<A>> =
        coeffLists(coeff, maxDegree).map { polyOf(kit, it) }

    /* --------------------------
     * Coefficient generators
     * -------------------------- */

    /** Integers in `-3..3`. */
    val smallInteger: Arb<BigInteger> =
        Arb.int(-3..3).map(Int::toBigInteger)

    /** Naturals in `0..4`. */
    val smallNatural: Arb<Natural> =
        Arb.int(0..4).map { Natural(it.toBigInteger()) }

    /** Rationals `p/q` with `|p| ≤ 3`, `1 ≤ q ≤ 3`. */
    val smallRational: Arb<Rational> =
        Arb.bind(Arb.int(-3..3), Arb.int(1..3)) { p, q -> Rational.of(p, q) }

    /** Canonical residues `0..p-1`. */
    fun residue(p: Int): Arb<BigInteger> =
        Arb.int(0 until p).map(Int::toBigInteger)

    /** Rational quaternions with integer coordinates in `-2..2`. */
    val smallQuaternion: Arb<RationalQuaternion> =
        Arb.list(Arb.int(-2..2), 4..4).map { (w, x, y, z) ->
            quaternion(Rational.of(w), Rational.of(x), Rational.of(y), Rational.of(z))
        }
}
