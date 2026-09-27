package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.quaternion.HurwitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.HurwitzQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.rationalQuaternionTier
import org.vorpal.kosmos.numberfields.quadratic.GaussianRat

/**
 * `𝕆_ℚ = CD⟨ℍ_ℚ⟩`: the rational octonion tier.
 *
 * File-level so that initializing [RationalOctonionAlgebras.RationalOctonionDivisionAlgebra] never
 * recursively initializes [RationalOctonionAlgebras].
 */
internal val rationalOctonionTier: OctonionTier<Rational, GaussianRat> =
    OctonionTier(rationalQuaternionTier)

/**
 * [RationalOctonionAlgebras] contains the algebraic structures over the [RationalOctonion] type, as well as the
 * homomorphisms, conversions and [Eq] instances.
 *
 * These include:
 * - [RationalOctonionDivisionAlgebra]: the rational octonions, an exact non-associative normed division algebra.
 * - [RationalOctonionVectorSpace]: the eight-dimensional vector space of rational octonions over the rationals.
 *
 * Maps:
 * - [RationalQuaternionToRationalOctonionMonomorphism]: the canonical Cayley-Dickson embedding `ℍ_ℚ ↪ 𝕆_ℚ`.
 * - [HurwitzToRationalOctonionMonomorphism]: `𝓗 ↪ ℍ_ℚ ↪ 𝕆_ℚ`.
 * - [allQuaternionEmbeddings]: the 168 signed-basis-unit embeddings `ℍ_ℚ ↪ 𝕆_ℚ`.
 * - [rationalOctonionToOctonionApprox]: the floating-point conversion `𝕆_ℚ → 𝕆` (not a monomorphism).
 *
 * We also have the following [Eq]s:
 * - [eqRationalOctonion]: equality on rational octonions.
 */
object RationalOctonionAlgebras {

    /**
     * The rational octonions: a non-associative (alternative) normed division algebra over ℚ.
     *
     * Every nonzero `x` is invertible, with
     * ```text
     * x⁻¹ = conj(x) / N(x),
     * ```
     * which is a two-sided inverse because the norm is a composition norm. Arithmetic is exact, so
     * invertibility is exactly `x ≠ 0`.
     */
    object RationalOctonionDivisionAlgebra:
        NonAssociativeInvolutiveRing<RationalOctonion> by rationalOctonionTier.ring,
        NormedDivisionAlgebra<Rational, RationalOctonion> {

        override val zero: RationalOctonion = rationalOctonionTier.ring.zero

        override val normSq: UnaryOp<RationalOctonion, Rational> =
            rationalOctonionTier.normSq

        override val reciprocal: Endo<RationalOctonion> = Endo(Symbols.INVERSE) { x ->
            if (!hasReciprocal(x)) throw HasReciprocal.notInvertible(x, "𝕆_${Symbols.BB_Q}")
            rationalOctonionTier.scalarAction(normSq(x).reciprocal(), conj(x))
        }

        /**
         * The standard basis `1, e₁, …, e₇`, obtained by doubling the basis `1, i, j, k` of `ℍ_ℚ`.
         */
        val basis: List<RationalOctonion> = rationalOctonionTier.basis

        val basisMap: Map<Int, RationalOctonion> = rationalOctonionTier.basisMap
    }

    /**
     * ℚ-Vector space over RationalOctonions of dimension 8.
     */
    val RationalOctonionVectorSpace: FiniteVectorSpace<Rational, RationalOctonion> = FiniteVectorSpace.of(
        scalars = RationalAlgebras.RationalField,
        add = RationalOctonionDivisionAlgebra.add,
        dimension = 8,
        leftAction = rationalOctonionTier.scalarAction
    )

    val RationalQuaternionToRationalOctonionMonomorphism: NonAssociativeRingMonomorphism<RationalQuaternion, RationalOctonion> =
        CayleyDickson.canonicalEmbedding(
            base = RationalQuaternionAlgebras.RationalQuaternionDivisionRing,
            doubled = RationalOctonionDivisionAlgebra
        )

    val HurwitzToRationalOctonionMonomorphism: NonAssociativeRingMonomorphism<HurwitzQuaternion, RationalOctonion> =
        HurwitzQuaternionAlgebras.HurwitzToRationalQuaternionMonomorphism andThen RationalQuaternionToRationalOctonionMonomorphism

    /**
     * The floating-point conversion `𝕆_ℚ → 𝕆`, coordinatewise `Rational.toReal()`.
     *
     * Not a monomorphism: distinct rationals can round to the same `Double`, and products are preserved
     * only up to rounding.
     */
    val rationalOctonionToOctonionApprox: UnaryOp<RationalOctonion, Octonion> =
        UnaryOp(Symbols.APPROX) { o ->
            realOctonionTier.fromCoordinates(rationalOctonionTier.coordinates(o).map(Rational::toReal))
        }

    val eqRationalOctonion: Eq<RationalOctonion> = Eq.default()

    val embeddingKit: OctonionEmbeddingKit.OctonionEmbeddingKit<Rational, RationalQuaternion, RationalOctonion> =
        rationalOctonionTier.embeddingKit(eqRationalOctonion)

    /**
     * The 168 signed-basis-unit embeddings `ℍ_ℚ ↪ 𝕆_ℚ`. See [OctonionEmbeddingKit].
     */
    fun allQuaternionEmbeddings(): Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<RationalQuaternion, RationalOctonion>> =
        embeddingKit.allEmbeddings()

    /**
     * The 84 embeddings normalized so that the image of quaternion `j` is a *positive* basis unit.
     *
     * Every one of the 168 embeddings in [allQuaternionEmbeddings] is either normalized or a
     * normalized embedding precomposed with the quaternion automorphism `i ↦ i, j ↦ -j, k ↦ -k`,
     * so this is a set of representatives for the embeddings modulo that automorphism.
     */
    fun normalizedQuaternionEmbeddings(): Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<RationalQuaternion, RationalOctonion>> =
        embeddingKit.normalizedEmbeddings()

    val printableRationalOctonion: Printable<RationalOctonion> =
        rationalOctonionTier.printable(
            RationalAlgebras.SignedRational,
            RationalAlgebras.printableRational,
            RationalAlgebras.eqRational
        )

    val printableRationalOctonionPretty = printableRationalOctonion
}
