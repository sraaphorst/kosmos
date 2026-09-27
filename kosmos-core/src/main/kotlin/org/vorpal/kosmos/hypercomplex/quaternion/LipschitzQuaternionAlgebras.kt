package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.BilinearForm
import org.vorpal.kosmos.core.ops.BinaryOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.geometry.lattices.EuclideanLattice
import org.vorpal.kosmos.hypercomplex.octonion.GravesianOctonion
import org.vorpal.kosmos.hypercomplex.octonion.GravesianOctonionAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.RationalOctonion
import org.vorpal.kosmos.linear.values.Vec4
import org.vorpal.kosmos.numberfields.quadratic.GaussianInt
import org.vorpal.kosmos.numberfields.quadratic.GaussianIntAlgebras
import java.math.BigInteger

/**
 * `ℤ⟨i, j, k⟩ = CD⟨ℤ[i]⟩`: the Lipschitz quaternion tier.
 *
 * File-level so that initializing [LipschitzQuaternionAlgebras.LipschitzQuaternionRing] never
 * recursively initializes [LipschitzQuaternionAlgebras].
 */
internal val lipschitzQuaternionTier: QuaternionTier<BigInteger, GaussianInt> =
    QuaternionTier(
        complexRing = GaussianIntAlgebras.GaussianIntEuclideanDomain,
        scalars = IntegerAlgebras.IntegerCommutativeRing,
        re = UnaryOp { z -> z.re },
        im = UnaryOp { z -> z.im },
        mkComplex = BinaryOp { a, b -> GaussianInt(a, b) }
    )

/**
 * Main structures:
 * - [LipschitzQuaternionRing]: the Lipschitz quaternions.
 *
 * Vector spaces and modules:
 * - [ZModuleLipschitzQuaternion]: the rank-4 ℤ-module of Lipschitz quaternions.
 *
 * Lattices:
 * - [LipschitzQuaternionLattice]: the rank-4 Euclidean lattice of Lipschitz quaternions in `ℝ⁴`.
 *
 * Homomorphisms:
 * - [LipschitzToHurwitzQuaternionMonomorphism]: the inclusion of orders `ℤ⟨i, j, k⟩ ⊂ 𝓗`.
 * - [LipschitzToRationalQuaternionMonomorphism]: the inclusion `ℤ⟨i, j, k⟩ ⊂ ℍ_ℚ`.
 * - [LipschitzToGravesianOctonionMonomorphism]: the Cayley-Dickson embedding into the Gravesian octonions.
 * - [LipschitzToRationalOctonionMonomorphism]: the composite into the rational octonions.
 *
 * Conversions:
 * - [lipschitzToQuaternionApprox]: the floating-point conversion into ℍ (not a monomorphism: large
 *   integers collide in `Double`).
 *
 * Eqs:
 * - [eqLipschitzQuaternion]
 *
 * Printables:
 * - [printableLipschitzQuaternion]
 * - [printableLipschitzQuaternionPretty]
 */
object LipschitzQuaternionAlgebras {

    /**
     * The Lipschitz quaternions are associative, noncommutative, and involutive.
     *
     * Only the 8 units `±1, ±i, ±j, ±k` are invertible in the ring: they are exactly the elements of
     * norm 1, and their inverse is their conjugate. Every other nonzero element is invertible only in
     * `ℍ_ℚ`, after [LipschitzToRationalQuaternionMonomorphism].
     */
    object LipschitzQuaternionRing:
        InvolutiveRing<LipschitzQuaternion> by lipschitzQuaternionTier.ring,
        HasNormSq<LipschitzQuaternion, BigInteger>,
        HasReciprocal<LipschitzQuaternion> {

        override val zero: LipschitzQuaternion = lipschitzQuaternionTier.ring.zero

        /**
         * The norm-square lands in ℤ:
         * ```text
         * normSq(q) = w² + x² + y² + z²
         * ```
         */
        override val normSq: UnaryOp<LipschitzQuaternion, BigInteger> =
            lipschitzQuaternionTier.normSq

        /**
         * `q` is a unit of the Lipschitz order iff `N(q) = 1`.
         */
        override fun hasReciprocal(a: LipschitzQuaternion): Boolean =
            normSq(a) == BigInteger.ONE

        /**
         * The reciprocal *within* the Lipschitz order, defined only on the units, where `q⁻¹ = conj(q)`.
         */
        override val reciprocal: Endo<LipschitzQuaternion> = Endo(Symbols.INVERSE) { q ->
            if (!hasReciprocal(q)) throw HasReciprocal.notInvertible(q, "the Lipschitz quaternions")
            conj(q)
        }

        /**
         * The standard basis `1, i, j, k`, obtained by doubling the basis `1, i` of `ℤ[i]`.
         */
        val basis: List<LipschitzQuaternion> =
            lipschitzQuaternionTier.basis

        /**
         * The 8 units `±1, ±i, ±j, ±k`: the quaternion group `Q₈`.
         */
        val units: List<LipschitzQuaternion> =
            basis.flatMap { e -> listOf(e, add.inverse(e)) }
    }

    object ZModuleLipschitzQuaternion : ZModule<LipschitzQuaternion> {
        override val scalars = IntegerAlgebras.IntegerCommutativeRing
        override val add = LipschitzQuaternionRing.add
        override val leftAction: LeftAction<BigInteger, LipschitzQuaternion> = lipschitzQuaternionTier.scalarAction
    }

    object LipschitzQuaternionLattice: EuclideanLattice<LipschitzQuaternion, BigInteger> {
        override val rank = 4

        /**
         * The Euclidean inner product `⟨q₁, q₂⟩ = Re(q₁ · conj(q₂))`, i.e. the polarization of `normSq`.
         */
        override val dot: BilinearForm<LipschitzQuaternion, BigInteger> = BilinearForm { lq1, lq2 ->
            val ring = LipschitzQuaternionRing
            ring.mul(lq1, ring.conj(lq2)).w
        }

        /**
         * A `ℤ`-basis of the Lipschitz quaternion lattice is `[1, I, J, K]`.
         */
        override val basis: List<LipschitzQuaternion> =
            LipschitzQuaternionRing.basis

        override val addV: AbelianGroup<LipschitzQuaternion> = LipschitzQuaternionRing.add
        override val scale: LeftAction<BigInteger, LipschitzQuaternion> = lipschitzQuaternionTier.scalarAction

        val embedR4: UnaryOp<LipschitzQuaternion, Vec4<Real>> = UnaryOp { lq ->
            Vec4(lq.w.toReal(), lq.x.toReal(), lq.y.toReal(), lq.z.toReal())
        }

        /**
         * Call validate() last to make sure all necessary conditions are met AFTER initialization happens.
         */
        @Suppress("unused")
        private val _validated = validate()
    }

    /**
     * The inclusion of the Lipschitz order into `ℍ_ℚ`.
     */
    object LipschitzToRationalQuaternionMonomorphism: RingMonomorphism<LipschitzQuaternion, RationalQuaternion> {
        override val domain = LipschitzQuaternionRing
        override val codomain = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        override val map = UnaryOp<LipschitzQuaternion, RationalQuaternion> { lq ->
            rationalQuaternionTier.fromCoordinates(lipschitzQuaternionTier.coordinates(lq).map(BigInteger::toRational))
        }
    }

    /**
     * The inclusion of orders `ℤ⟨i, j, k⟩ ⊂ 𝓗`: a Lipschitz quaternion has integer coordinates, so its
     * image in `ℍ_ℚ` is trivially Hurwitz and is wrapped without re-checking.
     */
    object LipschitzToHurwitzQuaternionMonomorphism: RingMonomorphism<LipschitzQuaternion, HurwitzQuaternion> {
        override val domain = LipschitzQuaternionRing
        override val codomain = HurwitzQuaternionAlgebras.HurwitzQuaternionRing
        override val map = UnaryOp<LipschitzQuaternion, HurwitzQuaternion> { lq ->
            HurwitzQuaternion.wrap(LipschitzToRationalQuaternionMonomorphism(lq))
        }
    }

    /**
     * The floating-point conversion `ℤ⟨i, j, k⟩ → ℍ`, coordinatewise `BigInteger.toReal()`.
     *
     * Not a monomorphism: integers beyond `2⁵³` collide in `Double`, and products are preserved only
     * up to rounding.
     */
    val lipschitzToQuaternionApprox: UnaryOp<LipschitzQuaternion, Quaternion> =
        UnaryOp(Symbols.APPROX) { lq ->
            realQuaternionTier.fromCoordinates(lipschitzQuaternionTier.coordinates(lq).map(BigInteger::toReal))
        }

    val LipschitzToGravesianOctonionMonomorphism: NonAssociativeRingMonomorphism<LipschitzQuaternion, GravesianOctonion> =
        CayleyDickson.canonicalEmbedding(
            base = LipschitzQuaternionRing,
            doubled = GravesianOctonionAlgebras.GravesianOctonionRing
        )

    val LipschitzToRationalOctonionMonomorphism: NonAssociativeRingMonomorphism<LipschitzQuaternion, RationalOctonion> =
        LipschitzToGravesianOctonionMonomorphism andThen GravesianOctonionAlgebras.GravesianToRationalOctonionMonomorphism

    val eqLipschitzQuaternion: Eq<LipschitzQuaternion> = Eq.default()

    val printableLipschitzQuaternion: Printable<LipschitzQuaternion> =
        lipschitzQuaternionTier.printable(
            IntegerAlgebras.SignedInteger,
            IntegerAlgebras.printableInteger,
            IntegerAlgebras.eqInteger
        )

    val printableLipschitzQuaternionPretty = printableLipschitzQuaternion
}
