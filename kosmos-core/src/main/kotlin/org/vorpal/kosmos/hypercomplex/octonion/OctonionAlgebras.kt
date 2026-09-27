package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NonAssociativeStarAlgebra
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.quaternion.Quaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.realQuaternionTier

/**
 * `𝕆 = CD⟨ℍ⟩`: the real octonion tier.
 *
 * File-level so that initializing [OctonionAlgebras.OctonionDivisionAlgebra] never recursively
 * initializes [OctonionAlgebras].
 */
internal val realOctonionTier: OctonionTier<Real, Complex> =
    OctonionTier(realQuaternionTier)

/**
 * [OctonionAlgebras] contains the algebraic structures over the [Octonion] type, as well as the
 * homomorphisms and [Eq] instances.
 *
 * These include:
 * - [OctonionDivisionAlgebra]: the real octonion non-associative normed division algebra.
 * - [OctonionVectorSpace]: the eight-dimensional vector space of octonions over the reals.
 * - [OctonionStarAlgebra]: the non-associative star algebra of octonions over the reals.
 *
 * We have the following homomorphisms:
 * - [QuaternionToOctonionMonomorphism]: canonical CD embedding of quaternions into octonions.
 * - [Quaternion.asOctonion]: convenience extension for the canonical embedding.
 * - [allQuaternionEmbeddings]: the 168 signed-basis-unit embeddings ℍ ↪ 𝕆.
 *
 * We also have the following [Eq]s:
 * - [eqOctonionStrict]: strict equality on octonions.
 * - [eqOctonion]: approximate equality on octonions.
 */
object OctonionAlgebras {

    /**
     * The real octonions: a non-associative (but alternative) real normed division algebra.
     *
     * Everything but the reciprocal comes from the [CayleyDickson] double of ℍ. Reciprocals follow
     * the floating-point policy of [HasReciprocal]: `o` is invertible iff `N(o)` is finite and not
     * approximately zero, and then `o⁻¹ = conj(o) / N(o)`; this works despite nonassociativity
     * because the norm is a composition norm.
     */
    object OctonionDivisionAlgebra :
        NonAssociativeInvolutiveRing<Octonion> by realOctonionTier.ring,
        RealNormedDivisionAlgebra<Octonion> {

        override val zero: Octonion = realOctonionTier.ring.zero
        override val one: Octonion = realOctonionTier.ring.one

        override val normSq: UnaryOp<Octonion, Real> =
            realOctonionTier.normSq

        override fun hasReciprocal(a: Octonion): Boolean {
            val n2 = normSq(a)
            return n2.isFinite() && RealAlgebras.eqRealApprox.neqv(n2, 0.0)
        }

        override val reciprocal: Endo<Octonion> = Endo(Symbols.INVERSE) { o ->
            if (!hasReciprocal(o)) throw HasReciprocal.notInvertible(o, Symbols.BB_O)
            realOctonionTier.scalarAction(1.0 / normSq(o), conj(o))
        }

        /**
         * The standard basis `1, e₁, …, e₇`, obtained by doubling the basis `1, i, j, k` of ℍ:
         * `e₁, e₂, e₃` are `i, j, k` in the first slot and `e₄, …, e₇` are `1, i, j, k` in the second.
         */
        val basis: List<Octonion> = realOctonionTier.basis
        val basisMap: Map<Int, Octonion> = realOctonionTier.basisMap

        val e1: Octonion = basis[1]
        val e2: Octonion = basis[2]
        val e3: Octonion = basis[3]
        val e4: Octonion = basis[4]
        val e5: Octonion = basis[5]
        val e6: Octonion = basis[6]
        val e7: Octonion = basis[7]
    }

    /**
     * The octonions as an eight-dimensional real vector space. The reals lie in the nucleus (indeed
     * the center) of 𝕆, so scalar multiplication is well-behaved despite nonassociativity.
     */
    val OctonionVectorSpace: FiniteVectorSpace<Real, Octonion> = FiniteVectorSpace.of(
        scalars = RealAlgebras.RealField,
        add = OctonionDivisionAlgebra.add,
        dimension = 8,
        leftAction = realOctonionTier.scalarAction
    )

    val OctonionStarAlgebra: NonAssociativeStarAlgebra<Real, Octonion> = NonAssociativeStarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = OctonionDivisionAlgebra,
        leftAction = OctonionVectorSpace.leftAction,
    )

    val QuaternionToOctonionMonomorphism: NonAssociativeRingMonomorphism<Quaternion, Octonion> =
        CayleyDickson.canonicalEmbedding(
            base = QuaternionAlgebras.QuaternionDivisionRing,
            doubled = OctonionDivisionAlgebra
        )

    /**
     * Embed a quaternion into the octonions via the canonical Cayley-Dickson embedding `q ↦ (q, 0)`.
     * This is the member of [allQuaternionEmbeddings] with `i ↦ e₁`, `j ↦ e₂` and both signs positive.
     */
    fun Quaternion.asOctonion(): Octonion =
        QuaternionToOctonionMonomorphism(this)

    val eqOctonionStrict: Eq<Octonion> = CD.eq(QuaternionAlgebras.eqQuaternionStrict)
    val eqOctonion: Eq<Octonion> = CD.eq(QuaternionAlgebras.eqQuaternion)

    val embeddingKit: OctonionEmbeddingKit.OctonionEmbeddingKit<Real, Quaternion, Octonion> =
        realOctonionTier.embeddingKit(eqOctonionStrict)

    /**
     * The 168 signed-basis-unit embeddings `ℍ ↪ 𝕆`: 42 ordered pairs of distinct imaginary units
     * for the images of `i` and `j`, times 4 independent sign choices. See [OctonionEmbeddingKit].
     */
    fun allQuaternionEmbeddings(): Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Quaternion, Octonion>> =
        embeddingKit.allEmbeddings()

    /**
     * The 84 embeddings normalized so that the image of quaternion `j` is a *positive* basis unit.
     *
     * Every one of the 168 embeddings in [allQuaternionEmbeddings] is either normalized or a
     * normalized embedding precomposed with the quaternion automorphism `i ↦ i, j ↦ -j, k ↦ -k`,
     * so this is a set of representatives for the embeddings modulo that automorphism.
     */
    fun normalizedQuaternionEmbeddings(): Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Quaternion, Octonion>> =
        embeddingKit.normalizedEmbeddings()

    val printableOctonion: Printable<Octonion> =
        realOctonionTier.printable(RealAlgebras.SignedReal, RealAlgebras.printableReal, RealAlgebras.eqRealApprox)

    val printableOctonionStrict: Printable<Octonion> =
        realOctonionTier.printable(RealAlgebras.SignedReal, RealAlgebras.printableRealStrict, RealAlgebras.eqRealStrict)

    val printableOctonionPretty: Printable<Octonion> =
        realOctonionTier.printable(RealAlgebras.SignedReal, RealAlgebras.printableRealPretty, RealAlgebras.eqRealApprox)
}
