package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinaryOp
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.Printable

/**
 * The quaternion construction `CD⟨C⟩`, parameterized by its coefficient ring.
 *
 * ℍ, ℍ_ℚ and the Lipschitz quaternions are one construction over three complex-like rings:
 * ```text
 * ℍ   = CD⟨ℂ⟩       coefficients S = Real,       C = Complex
 * ℍ_ℚ = CD⟨ℚ(i)⟩    coefficients S = Rational,   C = GaussianRat
 * Lip = CD⟨ℤ[i]⟩    coefficients S = BigInteger, C = GaussianInt
 * ```
 * and the same construction, over other bases or with the split sign, gives the rest of the
 * quaternion-level algebras (see [over]):
 * ```text
 * split-quaternions over R = CD_split⟨R[i]⟩    coefficients S = R,       C = CD<R>
 * biquaternions ℍ ⊗ ℂ      = CD⟨ℂ[i]⟩          coefficients S = Complex, C = CD<Complex>
 * ```
 * A [QuaternionTier] holds everything that depends only on that choice: the associative
 * Cayley-Dickson ring, the basis `1, i, j, k`, coordinates `[w, x, y, z]`, the coordinatewise scalar
 * action, the reduced norm, the six embeddings of the complex-like base, and the printable.
 *
 * The public `*Algebras` objects then add only what genuinely differs per tier: a reciprocal
 * (everywhere for ℍ and ℍ_ℚ, on the units only for Lipschitz), a vector space or `ℤ`-module, and
 * conversions between tiers.
 *
 * @param complexRing the complex-like base `C`: a commutative ring with its complex conjugation.
 * @param scalars the coefficient ring `S`.
 * @param re the real part `C → S`.
 * @param im the imaginary part `C → S`.
 * @param mkComplex builds `re + im·i` in `C`.
 * @param split `false` for the usual doubling (`j² = k² = -1`), `true` for the split doubling
 *   (`j² = k² = +1`).
 */
internal class QuaternionTier<S : Any, C : Any>(
    val complexRing: InvolutiveRing<C>,
    val scalars: CommutativeRing<S>,
    val re: UnaryOp<C, S>,
    val im: UnaryOp<C, S>,
    val mkComplex: BinaryOp<S, S, C>,
    val split: Boolean = false
) {
    /**
     * The associative Cayley-Dickson double `CD⟨C⟩`: usual (`σ = 1`) or split (`σ = -1`).
     */
    val ring: InvolutiveRing<CD<C>> =
        if (split) CayleyDickson.associativeSplit(complexRing)
        else CayleyDickson.associative(complexRing)

    /**
     * The coordinates `[w, x, y, z]` of `w + xi + yj + zk = (w + xi, y + zi)`.
     */
    val coordinates: UnaryOp<CD<C>, List<S>> =
        UnaryOp { q -> listOf(re(q.a), im(q.a), re(q.b), im(q.b)) }

    /**
     * The quaternion `w + xi + yj + zk` from its coordinates `[w, x, y, z]`.
     */
    val fromCoordinates: UnaryOp<List<S>, CD<C>> =
        UnaryOp { cs ->
            require(cs.size == 4) { "A quaternion has 4 coordinates: got ${cs.size}." }
            CD(mkComplex(cs[0], cs[1]), mkComplex(cs[2], cs[3]))
        }

    /**
     * The standard basis `1, i, j, k`, obtained by doubling the basis `1, i` of `C`.
     */
    val basis: List<CD<C>> =
        CayleyDickson.doubledBasis(
            zero = complexRing.zero,
            baseBasis = listOf(complexRing.one, mkComplex(scalars.zero, scalars.one))
        )

    /**
     * The coordinatewise scalar action `s ⊳ (w + xi + yj + zk) = sw + sx·i + sy·j + sz·k`.
     *
     * The scalars are central, so this is also left (and right) multiplication by the embedded scalar.
     */
    val scalarAction: LeftAction<S, CD<C>> =
        LeftAction(Symbols.TRIANGLE_RIGHT) { s, q ->
            fromCoordinates(coordinates(q).map { c -> scalars.mul(s, c) })
        }

    /**
     * The reduced norm `N(q) = q·conj(q)`, read off the real coordinate of the Cayley-Dickson
     * product: `w² + x² + y² + z²` for the usual doubling and `w² + x² - y² - z²` for the split one.
     */
    val normSq: UnaryOp<CD<C>, S> =
        UnaryOp(Symbols.NORM_SQ_SYMBOL) { q -> re(ring.mul(q, ring.conj(q)).a) }

    /**
     * The map `C → CD⟨C⟩` sending the imaginary unit of `C` to the chosen signed axis `±i`, `±j`, or `±k`.
     */
    fun complexEmbeddingMap(
        emb: AxisSignEmbeddings.AxisSignEmbedding
    ): UnaryOp<C, CD<C>> =
        UnaryOp { c ->
            QuaternionEmbeddingKit.embedComplexLike(
                axisSign = emb,
                re = re(c),
                im = im(c),
                zero = scalars.zero,
                negate = scalars.add.inverse,
                mkQuaternion = fromCoordinates
            )
        }

    /**
     * The printable `w + xi + yj + zk`, dropping zero terms.
     */
    fun printable(
        signed: SignedOps<S>,
        prS: Printable<S>,
        eqS: Eq<S>
    ): Printable<CD<C>> =
        QuaternionPrintable.quaternionPrintable(
            signed = signed,
            zero = scalars.zero,
            one = scalars.one,
            prA = prS,
            eqA = eqS,
            decompose = coordinates
        )

    companion object {
        /**
         * The quaternion tier over an arbitrary commutative ring `R`, with complex-like base
         * `R[i]/(i² + 1) = CD⟨R⟩` (trivial involution on `R`, see [CayleyDickson.commutative]):
         * - `split = false`: the Hamilton quaternion algebra `(-1, -1)_R`;
         * - `split = true`: the split-quaternion algebra `(-1, +1)_R ≅ M₂(R)` (when 2 is invertible).
         *
         * Over `R = ℝ` the carrier is `CD<CD<Real>> = CD<Complex>`, the same carrier as ℍ; over
         * `R = ℂ` this is the biquaternion algebra `ℍ ⊗ ℂ`.
         */
        fun <R : Any> over(
            base: CommutativeRing<R>,
            split: Boolean = false
        ): QuaternionTier<R, CD<R>> =
            QuaternionTier(
                complexRing = CayleyDickson.commutative(base, base.one),
                scalars = base,
                re = UnaryOp { c -> c.a },
                im = UnaryOp { c -> c.b },
                mkComplex = BinaryOp { a, b -> CD(a, b) },
                split = split
            )
    }
}
