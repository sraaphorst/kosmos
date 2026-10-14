package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CarrierTransport
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.BilinearForm
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.contramap
import org.vorpal.kosmos.geometry.lattices.EuclideanLattice
import org.vorpal.kosmos.linear.values.Vec4
import java.math.BigInteger

/**
 * The inclusion `𝓗 ↪ ℍ_ℚ` and its trusted corestriction, as a [CarrierTransport].
 *
 * File-level so that initializing [HurwitzQuaternionAlgebras.HurwitzQuaternionRing] never
 * recursively initializes [HurwitzQuaternionAlgebras].
 */
private val hurwitzInclusion: CarrierTransport<RationalQuaternion, HurwitzQuaternion> =
    CarrierTransport(
        wrap = UnaryOp { q -> HurwitzQuaternion.wrap(q) },
        unwrap = UnaryOp { h -> h.quaternion }
    )

/**
 * The ring structure of `𝓗`: the restriction of `ℍ_ℚ` to the order.
 */
private val hurwitzRestriction: InvolutiveRing<HurwitzQuaternion> =
    hurwitzInclusion.involutiveRing(RationalQuaternionAlgebras.RationalQuaternionDivisionRing)

/**
 * Main structures:
 * - [HurwitzQuaternionRing]: the Hurwitz order `𝓗 ⊂ ℍ_ℚ`.
 *
 * Vector spaces and modules:
 * - [ZModuleHurwitzQuaternion]: the rank-4 ℤ-module of Hurwitz quaternions.
 *
 * Lattices:
 * - [HurwitzQuaternionLattice]: the rank-4 Euclidean lattice of Hurwitz quaternions in `ℝ⁴`,
 *   related to the `D₄`/24-cell configuration.
 *
 * Homomorphisms:
 * - [HurwitzToRationalQuaternionMonomorphism]: the inclusion `𝓗 ↪ ℍ_ℚ`.
 * - [hurwitzToQuaternionApprox]: the floating-point conversion `𝓗 → ℍ` (not a monomorphism).
 *
 * Eqs:
 * - [eqHurwitzQuaternion]
 *
 * Printables:
 * - [printableHurwitzQuaternion]
 * - [printableHurwitzQuaternionPretty]
 */
object HurwitzQuaternionAlgebras {
    /**
     * Hurwitz quaternions are associative, noncommutative, involutive, and form a finite unit group of size 24.
     *
     * They are the maximal order in the rational quaternion division algebra ℍ(ℚ):
     *
     * An order in ℍ(ℚ) is a subring 𝒪 such that:
     * - 1 ∈ 𝒪
     * - 𝒪 is closed under addition and multiplication
     * - 𝒪 is a lattice (finitely generated ℤ-module of rank 4)
     * - 𝒪 spans ℍ(ℚ) over ℚ (i.e., `ℚ ⊗_ℤ 𝒪 = ℍ(ℚ)`).
     *
     * The containment can be described as:
     * ```text
     * Lipschitz (ring) ⊂ Hurwitz (ring) ⊂ ℍ(ℚ) (division ring) ⊂ ℍ (division ring)
     * ```
     *
     * No quaternion arithmetic is implemented here: `+`, `-`, `·`, conjugation and the norm are
     * those of [RationalQuaternionAlgebras.RationalQuaternionDivisionRing], restricted to `𝓗`.
     *
     * Only the *units* of `𝓗` have reciprocals in `𝓗`. A unit has `N(h) = 1`, so its inverse is its
     * conjugate. Any other nonzero `h` is invertible only in the ambient `ℍ_ℚ`.
     */
    object HurwitzQuaternionRing :
        InvolutiveRing<HurwitzQuaternion> by hurwitzRestriction,
        HasNormSq<HurwitzQuaternion, BigInteger>,
        HasReciprocal<HurwitzQuaternion> {

        override val zero: HurwitzQuaternion = HurwitzQuaternion.ZERO

        /**
         * `normSq` always lands in ℤ:
         * 1. If the values are already in ℤ, the calculation remains there.
         * 2. If the values are all half-integers, then they all can be written `n/2` with `n` odd:
         * ```text
         * a^2 + b^2 + c^2 + d^2 = (n_a^2 + n_b^2 + n_c^2 + n_d^2) / 4
         * ```
         * with each odd square being `1 (mod 8)`, so the numerator is `4 (mod 8)`, and hence divisible by 4.
         */
        override val normSq: UnaryOp<HurwitzQuaternion, BigInteger> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { h ->
                val n = RationalQuaternionAlgebras.RationalQuaternionDivisionRing.normSq(h.quaternion)
                check(n.isInteger) { "Hurwitz invariant violated: N($h) = $n is not an integer." }
                n.numerator
            }

        /**
         * `h` is a unit of `𝓗` iff `N(h) = 1`. There are exactly 24 of them: `±1, ±i, ±j, ±k`
         * and `(±1 ± i ± j ± k)/2`.
         */
        override fun hasReciprocal(a: HurwitzQuaternion): Boolean =
            normSq(a) == BigInteger.ONE

        /**
         * The 24 units of `𝓗`: the 8 Lipschitz units `±1, ±i, ±j, ±k` and the 16 half-integer units
         * `(±1 ± i ± j ± k)/2`. They form the binary tetrahedral group `2T`, and as points of `ℝ⁴`
         * they are the vertices of the 24-cell.
         */
        val units: List<HurwitzQuaternion> by lazy {
            val half = Rational.of(1, 2)
            val signs = listOf(Rational.ONE, -Rational.ONE)
            val lipschitzUnits = (0 until 4).flatMap { axis ->
                signs.map { sign -> List(4) { idx -> if (idx == axis) sign else Rational.ZERO } }
            }
            val halfUnits = (0 until 16).map { mask ->
                List(4) { bit -> if ((mask shr bit) and 1 == 1) half else -half }
            }
            (lipschitzUnits + halfUnits).map { (a, b, c, d) -> HurwitzQuaternion(a, b, c, d) }
        }

        /**
         * The reciprocal *within* `𝓗`, defined only on units, where `h⁻¹ = conj(h)`.
         *
         * @throws ArithmeticException if `h` is not a unit of `𝓗`. Such an `h` may still be invertible
         * in `ℍ_ℚ`: use [HurwitzToRationalQuaternionMonomorphism] and the ambient division ring.
         */
        override val reciprocal: Endo<HurwitzQuaternion> = Endo(Symbols.INVERSE) { h ->
            if (!hasReciprocal(h)) {
                throw HasReciprocal.notInvertible(h, "the Hurwitz order (invert it in ℍ_ℚ instead)")
            }
            conj(h)
        }
    }

    /**
     * The `ℤ`-action on `𝓗`: the restriction of the `ℚ`-action on `ℍ_ℚ`.
     */
    private val zScaling: LeftAction<BigInteger, HurwitzQuaternion> =
        LeftAction(Symbols.TRIANGLE_RIGHT) { s, h ->
            HurwitzQuaternion.wrap(
                RationalQuaternionAlgebras.RationalQuaternionVectorSpace.leftAction(s.toRational(), h.quaternion)
            )
        }

    object ZModuleHurwitzQuaternion : ZModule<HurwitzQuaternion> {
        override val scalars = IntegerAlgebras.IntegerCommutativeRing
        override val add = HurwitzQuaternionRing.add
        override val leftAction: LeftAction<BigInteger, HurwitzQuaternion> = zScaling
    }

    /**
     * The Hurwitz quaternions form a rank-4 Euclidean lattice in `ℝ⁴` under the coordinate embedding:
     * ```text
     * a + bi + cj + dk ↦ (a, b, c, d).
     * ```
     *
     * As a set, this lattice is:
     * ```text
     * ℤ⁴ ∪ (ℤ + 1/2)⁴
     * ```
     * i.e. the cubic lattice together with its all-half-integer translate. A 2D slice would resemble:
     * ```text
     * •   •   •   •
     *   •   •   •
     * •   •   •   •
     *   •   •   •
     * •   •   •   •
     *```
     * i.e. one lattice plus a shifted coset.
     *
     * The 24 norm-1 Hurwitz units form the vertices of the regular 24-cell, equivalently a scaled
     * `D₄` root configuration.
     */
    object HurwitzQuaternionLattice: EuclideanLattice<HurwitzQuaternion, Rational> {
        override val rank = 4

        /**
         * The Euclidean inner product `⟨h₁, h₂⟩ = Re(h₁ · conj(h₂))`, i.e. the polarization of `normSq`.
         */
        override val dot: BilinearForm<HurwitzQuaternion, Rational> = BilinearForm { h1, h2 ->
            val ring = HurwitzQuaternionRing
            ring.mul(h1, ring.conj(h2)).w
        }

        /**
         * A ℤ-basis of the Hurwitz quaternion lattice is `[1, I, J, ω]` with `ω = (1 + I + J + K) / 2`.
         */
        override val basis: List<HurwitzQuaternion> = listOf(
            HurwitzQuaternion.ONE,
            HurwitzQuaternion.I,
            HurwitzQuaternion.J,
            HurwitzQuaternion.OMEGA
        )

        override val addV: AbelianGroup<HurwitzQuaternion> = HurwitzQuaternionRing.add
        override val scale: LeftAction<BigInteger, HurwitzQuaternion> = zScaling

        val embedR4: UnaryOp<HurwitzQuaternion, Vec4<Real>> = UnaryOp { h ->
            Vec4(h.w.toReal(), h.x.toReal(), h.y.toReal(), h.z.toReal())
        }

        /**
         * Call validate() last to make sure all necessary conditions are met AFTER initialization happens.
         */
        @Suppress("unused")
        private val _validated = validate()
    }

    /**
     * The inclusion of the order into its ambient division algebra, `𝓗 ↪ ℍ_ℚ`.
     */
    object HurwitzToRationalQuaternionMonomorphism: RingMonomorphism<HurwitzQuaternion, RationalQuaternion> {
        override val domain = HurwitzQuaternionRing
        override val codomain = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        override val map = UnaryOp<HurwitzQuaternion, RationalQuaternion> { h -> h.quaternion }
    }

    /**
     * The floating-point conversion `𝓗 → ℍ`, through `ℍ_ℚ`.
     *
     * Not a monomorphism: distinct large half-integers collide in `Double`, and products are preserved
     * only up to rounding.
     */
    val hurwitzToQuaternionApprox: UnaryOp<HurwitzQuaternion, RealQuaternion> =
        UnaryOp(Symbols.APPROX) { h ->
            RationalQuaternionAlgebras.rationalQuaternionToQuaternionApprox(h.quaternion)
        }

    val eqHurwitzQuaternion: Eq<HurwitzQuaternion> = Eq.default()

    val printableHurwitzQuaternion: Printable<HurwitzQuaternion> =
        RationalQuaternionAlgebras.printableRationalQuaternion.contramap { h: HurwitzQuaternion -> h.quaternion }

    val printableHurwitzQuaternionPretty = printableHurwitzQuaternion
}
