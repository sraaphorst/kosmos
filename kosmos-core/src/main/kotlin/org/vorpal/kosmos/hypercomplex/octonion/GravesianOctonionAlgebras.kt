package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.lipschitzQuaternionTier
import org.vorpal.kosmos.numberfields.quadratic.GaussianInt
import java.math.BigInteger

/**
 * The Gravesian octonions `CD⟨ℤ⟨i, j, k⟩⟩`: the octonion tier over the Lipschitz quaternions.
 *
 * File-level so that initializing [GravesianOctonionAlgebras.GravesianOctonionRing] never recursively
 * initializes [GravesianOctonionAlgebras].
 */
internal val gravesianOctonionTier: OctonionTier<BigInteger, GaussianInt> =
    OctonionTier(lipschitzQuaternionTier)

/**
 * [GravesianOctonionAlgebras] contains the algebraic structures over the [GravesianOctonion] type, as well as the
 * homomorphisms, conversions and [Eq] instances.
 *
 * These include:
 * - [GravesianOctonionRing]: the Gravesian octonions.
 * - [ZModuleGravesianOctonion]: the Gravesian octonions viewed as a free ℤ-module of rank 8.
 *
 * Maps:
 * - [GravesianToRationalOctonionMonomorphism]: the inclusion into the rational octonions.
 * - [gravesianToOctonionApprox]: the floating-point conversion into 𝕆 (not a monomorphism).
 * - [allQuaternionEmbeddings]: the 168 signed-basis-unit embeddings of the Lipschitz quaternions.
 *
 * We also have the following [Eq]s:
 * - [eqGravesianOctonion]: equality on Gravesian octonions.
 */
object GravesianOctonionAlgebras {

    /**
     * The Gravesian octonions: the non-associative (alternative) involutive ring `ℤ⁸ ⊂ 𝕆_ℚ` with
     * integer coordinates in the Cayley-Dickson basis.
     *
     * Only the 16 units `±1, ±e₁, …, ±e₇` are invertible in the ring: they are exactly the elements
     * of norm 1, and their inverse is their conjugate. (The Gravesian octonions are *not* a maximal
     * order; the octavians, a maximal order containing them, have 240 units.)
     */
    object GravesianOctonionRing:
        NonAssociativeInvolutiveRing<GravesianOctonion> by gravesianOctonionTier.ring,
        HasNormSq<GravesianOctonion, BigInteger>,
        HasReciprocal<GravesianOctonion> {

        override val zero: GravesianOctonion = gravesianOctonionTier.ring.zero

        override val normSq: UnaryOp<GravesianOctonion, BigInteger> =
            gravesianOctonionTier.normSq

        override fun hasReciprocal(a: GravesianOctonion): Boolean =
            normSq(a) == BigInteger.ONE

        override val reciprocal: Endo<GravesianOctonion> = Endo(Symbols.INVERSE) { o ->
            if (!hasReciprocal(o)) throw HasReciprocal.notInvertible(o, "the Gravesian octonions")
            conj(o)
        }

        /**
         * The standard basis `1, e₁, …, e₇`, obtained by doubling the basis `1, i, j, k` of the
         * Lipschitz quaternions. It is also a `ℤ`-basis of the Gravesian lattice `ℤ⁸`.
         */
        val basis: List<GravesianOctonion> = gravesianOctonionTier.basis

        val basisMap: Map<Int, GravesianOctonion> = gravesianOctonionTier.basisMap

        /**
         * The 16 units `±1, ±e₁, …, ±e₇`.
         */
        val units: List<GravesianOctonion> =
            basis.flatMap { e -> listOf(e, add.inverse(e)) }
    }

    /**
     * We can't build a vector space over the GravesianOctonions since they are not acted on by a field,
     * but we can build a Z-module, with the coordinatewise action.
     */
    object ZModuleGravesianOctonion: ZModule<GravesianOctonion> {
        override val scalars = IntegerAlgebras.IntegerCommutativeRing
        override val add = GravesianOctonionRing.add
        override val leftAction: LeftAction<BigInteger, GravesianOctonion> = gravesianOctonionTier.scalarAction
    }

    /**
     * The inclusion of the Gravesian octonions into the rational octonions.
     */
    object GravesianToRationalOctonionMonomorphism: NonAssociativeRingMonomorphism<GravesianOctonion, RationalOctonion> {
        override val domain = GravesianOctonionRing
        override val codomain = RationalOctonionAlgebras.RationalOctonionDivisionAlgebra
        override val map = UnaryOp<GravesianOctonion, RationalOctonion> { o ->
            rationalOctonionTier.fromCoordinates(gravesianOctonionTier.coordinates(o).map(BigInteger::toRational))
        }
    }

    /**
     * The floating-point conversion into 𝕆, coordinatewise `BigInteger.toReal()`.
     *
     * Not a monomorphism: integers beyond `2⁵³` collide in `Double`, and products are preserved only
     * up to rounding.
     */
    val gravesianToOctonionApprox: UnaryOp<GravesianOctonion, Octonion> =
        UnaryOp(Symbols.APPROX) { o ->
            realOctonionTier.fromCoordinates(gravesianOctonionTier.coordinates(o).map(BigInteger::toReal))
        }

    val eqGravesianOctonion: Eq<GravesianOctonion> = Eq.default()

    val embeddingKit: OctonionEmbeddingKit.OctonionEmbeddingKit<BigInteger, LipschitzQuaternion, GravesianOctonion> =
        gravesianOctonionTier.embeddingKit(eqGravesianOctonion)

    /**
     * The 168 signed-basis-unit embeddings `ℤ⟨i, j, k⟩ ↪ Gravesian`. See [OctonionEmbeddingKit].
     */
    fun allQuaternionEmbeddings(): Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<LipschitzQuaternion, GravesianOctonion>> =
        embeddingKit.allEmbeddings()

    /**
     * The 84 embeddings normalized so that the image of quaternion `j` is a *positive* basis unit.
     *
     * Every one of the 168 embeddings in [allQuaternionEmbeddings] is either normalized or a
     * normalized embedding precomposed with the quaternion automorphism `i ↦ i, j ↦ -j, k ↦ -k`,
     * so this is a set of representatives for the embeddings modulo that automorphism.
     */
    fun normalizedQuaternionEmbeddings(): Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<LipschitzQuaternion, GravesianOctonion>> =
        embeddingKit.normalizedEmbeddings()

    val printableGravesianOctonion: Printable<GravesianOctonion> =
        gravesianOctonionTier.printable(
            IntegerAlgebras.SignedInteger,
            IntegerAlgebras.printableInteger,
            IntegerAlgebras.eqInteger
        )

    val printableGravesianOctonionPretty = printableGravesianOctonion
}
