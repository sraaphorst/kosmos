package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinaryOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.numberfields.quadratic.GaussianRat
import org.vorpal.kosmos.numberfields.quadratic.GaussianRatAlgebras

/**
 * `ℍ_ℚ = CD⟨ℚ(i)⟩`: the rational quaternion tier.
 *
 * File-level so that initializing [RationalQuaternionAlgebras.RationalQuaternionDivisionRing] never
 * recursively initializes [RationalQuaternionAlgebras].
 */
internal val rationalQuaternionTier: QuaternionTier<Rational, GaussianRat> =
    QuaternionTier(
        complexRing = GaussianRatAlgebras.GaussianRatStarAlgebra,
        scalars = RationalAlgebras.RationalField,
        re = UnaryOp { z -> z.re },
        im = UnaryOp { z -> z.im },
        mkComplex = BinaryOp { a, b -> GaussianRat(a, b) }
    )

/**
 * [RationalQuaternionAlgebras] contains the algebraic structures over the [RationalQuaternion] type, as well as the
 * conversions and [Eq] instances.
 *
 * These include:
 * - [RationalQuaternionDivisionRing]: the rational quaternions.
 * - [RationalQuaternionVectorSpace]: the four-dimensional vector space of rational quaternions over the rationals.
 *
 * Maps:
 * - `ℚ(i) ↪ ℍ_ℚ` lives with the Gaussian rationals: `GaussianRatAlgebras.gaussianRatToRationalQuaternionEmbedding`.
 * - [rationalQuaternionToQuaternionApprox]: the floating-point conversion `ℍ_ℚ → ℍ`. It is *not* typed as a
 *   homomorphism: rounding breaks both exact multiplicativity and injectivity.
 *
 * The inclusions of the integral orders live with the orders themselves:
 * - [HurwitzQuaternionAlgebras.HurwitzToRationalQuaternionMonomorphism]: `𝓗 ↪ ℍ_ℚ`.
 * - [LipschitzQuaternionAlgebras.LipschitzToRationalQuaternionMonomorphism]: `ℤ⟨i, j, k⟩ ↪ ℍ_ℚ`.
 *
 * We also have the following [Eq]s:
 * - [eqRationalQuaternion]: equality on rational quaternions.
 */
object RationalQuaternionAlgebras {

    /**
     * This is the ring of quaternions with rational coefficients `ℍ_ℚ`:
     * - associative
     * - noncommutative
     * - involutive
     * - a division ring, with `q⁻¹ = conj(q) / N(q)` for every `q ≠ 0`.
     *
     * Arithmetic is exact, so invertibility is exactly `q ≠ 0`.
     */
    object RationalQuaternionDivisionRing:
        DivisionRing<RationalQuaternion>,
        InvolutiveRing<RationalQuaternion> by rationalQuaternionTier.ring,
        NormedDivisionAlgebra<Rational, RationalQuaternion> {

        override val zero: RationalQuaternion = rationalQuaternionTier.ring.zero
        override val one: RationalQuaternion = rationalQuaternionTier.ring.one

        override val normSq: UnaryOp<RationalQuaternion, Rational> =
            rationalQuaternionTier.normSq

        override val reciprocal: Endo<RationalQuaternion> = Endo(Symbols.INVERSE) { q ->
            if (!hasReciprocal(q)) throw HasReciprocal.notInvertible(q, "ℍ_${Symbols.BB_Q}")
            rationalQuaternionTier.scalarAction(normSq(q).reciprocal(), conj(q))
        }

        /**
         * The standard basis `1, i, j, k`, obtained by doubling the basis `1, i` of `ℚ(i)`.
         */
        val basis: List<RationalQuaternion> =
            rationalQuaternionTier.basis
    }

    /**
     * `ℍ_ℚ` as a four-dimensional vector space over ℚ; the scalars act coordinatewise.
     */
    val RationalQuaternionVectorSpace: FiniteVectorSpace<Rational, RationalQuaternion> = FiniteVectorSpace.of(
        scalars = RationalAlgebras.RationalField,
        add = RationalQuaternionDivisionRing.add,
        dimension = 4,
        leftAction = rationalQuaternionTier.scalarAction
    )

    /**
     * The floating-point conversion `ℍ_ℚ → ℍ`, coordinatewise `Rational.toReal()`.
     *
     * This is deliberately a plain conversion rather than a ring homomorphism: distinct rationals can
     * round to the same `Double`, and products are only preserved up to rounding. Tests check it as a
     * homomorphism *up to approximate equality*.
     */
    val rationalQuaternionToQuaternionApprox: UnaryOp<RationalQuaternion, Quaternion> =
        UnaryOp(Symbols.APPROX) { q ->
            realQuaternionTier.fromCoordinates(rationalQuaternionTier.coordinates(q).map(Rational::toReal))
        }

    val eqRationalQuaternion: Eq<RationalQuaternion> = Eq.default()

    val printableRationalQuaternion: Printable<RationalQuaternion> =
        rationalQuaternionTier.printable(
            RationalAlgebras.SignedRational,
            RationalAlgebras.printableRational,
            RationalAlgebras.eqRational
        )

    val printableRationalQuaternionPretty = printableRationalQuaternion
}
