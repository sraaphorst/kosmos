package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.hypercomplex.embeddings.AxisSignEmbeddings
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.hypercomplex.complex.GaussianInt
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.times
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.geometry.lattices.EuclideanLattice
import org.vorpal.kosmos.linear.values.Vec4
import java.math.BigInteger

/**
 * Main structures:
 * - [HurwitzQuaternionRing]: the Hurwitz quaternions.
 *
 * Vector spaces and modules:
 * - [ZModuleHurwitzQuaternion]: the rank-4 ℤ-module of Hurwitz quaternions.
 *
 * Lattices:
 * - [HurwitzQuaternionLattice]: the rank-4 Euclidean lattice of Hurwitz quaternions in `ℝ⁴`,
 *   related to the `D₄`/24-cell configuration.
 *
 *
 * Homomorphisms:
 * - [gaussianIntEmbeddingToHurwitz]: the unital embeddings from the Gaussian integers to the Hurwitz quaternions.
 * - [HurwitzToRationalQuaternionMonomorphism]: a ring monomorphism from the Hurwitz quaternions to the rational quaternions.
 * - [HurwitzToQuaternionMonomorphism]: a ring monomorphism from the Hurwitz quaternions to the quaternions.
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
     */
    object HurwitzQuaternionRing:
        InvolutiveRing<HurwitzQuaternion>,
        HasNormSq<HurwitzQuaternion, Rational> {

        override val zero: HurwitzQuaternion = HurwitzQuaternion.ZERO
        override val one: HurwitzQuaternion = HurwitzQuaternion.ONE

        override val add: AbelianGroup<HurwitzQuaternion> = AbelianGroup.of(
            identity = zero,
            op = BinOp(Symbols.PLUS) { hq1, hq2 ->
                HurwitzQuaternion(
                    hq1.a + hq2.a, hq1.b + hq2.b,
                    hq1.c + hq2.c, hq1.d + hq2.d
                )
            },
            inverse = Endo(Symbols.MINUS) { hq ->
                HurwitzQuaternion(-hq.a, -hq.b, -hq.c, -hq.d)
            }
        )

        override val mul: Monoid<HurwitzQuaternion> = Monoid.of(
            identity = one,
            op = BinOp(Symbols.ASTERISK) { hq1, hq2 ->
                HurwitzQuaternion(
                    hq1.a * hq2.a - hq1.b * hq2.b - hq1.c * hq2.c - hq1.d * hq2.d,
                    hq1.a * hq2.b + hq1.b * hq2.a + hq1.c * hq2.d - hq1.d * hq2.c,
                    hq1.a * hq2.c - hq1.b * hq2.d + hq1.c * hq2.a + hq1.d * hq2.b,
                    hq1.a * hq2.d + hq1.b * hq2.c - hq1.c * hq2.b + hq1.d * hq2.a
                )
            }
        )

        override fun fromBigInt(n: BigInteger): HurwitzQuaternion =
            HurwitzQuaternion(n.toRational(), Rational.ZERO, Rational.ZERO, Rational.ZERO)

        // This always is a Hurwitz quaternion as:
        // - doubling maintains integrality; and
        // - parity mod 2 doesn't change under negation.
        override val conj: Endo<HurwitzQuaternion> = Endo(Symbols.CONJ) { hq ->
            HurwitzQuaternion(hq.a, -hq.b, -hq.c, -hq.d)
        }

        /**
         * normSq always lands in ℤ:
         * 1. If the values are already in ℤ, the calculation remains there.
         * 2. If the values are all half-integers, then they all can be written `n/2` with `n` odd:
         * ```text
         * a^2 + b^2 + c^2 + d^2 = (n_a^2 + n_b^2 + n_c^2 + n_d^2) / 4
         * ```
         * with each odd square being `1 (mod 8)`, so the numerator is `4 (mod 8)`, and hence divisible by 4.
         */
        override val normSq: UnaryOp<HurwitzQuaternion, Rational> = UnaryOp(Symbols.NORM_SQ_SYMBOL) {
            hq -> hq.a * hq.a + hq.b * hq.b + hq.c * hq.c + hq.d * hq.d
        }
    }

    object ZModuleHurwitzQuaternion : ZModule<HurwitzQuaternion> {
        override val scalars = IntegerAlgebras.IntegerCommutativeRing
        override val add = HurwitzQuaternionRing.add
        override val leftAction: LeftAction<BigInteger, HurwitzQuaternion> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { s, hq ->
                val sr = s.toRational()
                HurwitzQuaternion( sr * hq.a, sr * hq.b, sr * hq.c, sr * hq.d)
            }
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
     * i.e. the cubic lattice together with its all-half-integer translate.
     *
     * The 24 norm-1 Hurwitz units form the vertices of the regular 24-cell, equivalently a scaled
     * `D₄` root configuration.
     */
    object HurwitzQuaternionLattice: EuclideanLattice<HurwitzQuaternion, Rational> {
        override val rank = 4
        private val ring = HurwitzQuaternionRing

        // Alternatively, we could use the following:
        // (ring.normSq(ring.add(hq1, hq2)) - ring.normSq(hq1) - ring.normSq(hq2)) / Rational.TWO
        override val dot: (HurwitzQuaternion, HurwitzQuaternion) -> Rational = { hq1, hq2 ->
            hq1.a * hq2.a + hq1.b * hq2.b + hq1.c * hq2.c + hq1.d * hq2.d
        }

        /**
         * A ℤ-basis of the Hurwitz quaternion lattice is `[1, I, J, (1 + I + J + K) / 2]`.
         */
        private val HALF = Rational.of(BigInteger.ONE, BigInteger.TWO)
        override val basis: List<HurwitzQuaternion> = listOf(
            HurwitzQuaternion.ONE,
            HurwitzQuaternion.I,
            HurwitzQuaternion.J,
            HurwitzQuaternion(HALF, HALF, HALF, HALF)
        )

        override val addV: AbelianGroup<HurwitzQuaternion> = ring.add
        override val scale: LeftAction<BigInteger, HurwitzQuaternion> = LeftAction(Symbols.TRIANGLE_RIGHT) { s, hq ->
            HurwitzQuaternion(s * hq.a, s * hq.b, s * hq.c, s * hq.d)
        }

        val embedR4: UnaryOp<HurwitzQuaternion, Vec4<Real>> = UnaryOp { hq ->
            Vec4(hq.a.toReal(), hq.b.toReal(), hq.c.toReal(), hq.d.toReal())
        }

        /**
         * Call validate() last to make sure all necessary conditions are met AFTER initialization happens.
         */
        @Suppress("unused")
        private val _validated = validate()
    }

    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical

    /**
     * Create the [GaussianInt] ↪ [LipschitzQuaternion] monomorphism according to the [embedding] and then
     * apply the [LipschitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism] to get a [RingMonomorphism]:
     * ```text
     * GaussianInt ↪ LipschitzQuaternion ↪ HurwitzQuaternion
     * ```
     */
    fun gaussianIntEmbeddingToHurwitz(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<GaussianInt, HurwitzQuaternion> =
        LipschitzQuaternionAlgebras.gaussianIntEmbeddingToQuaternion(embedding) andThen
            LipschitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism

    /**
     * Monomorphism: HurwitzQuaternion to RationalQuaternion.
     */
    object HurwitzToRationalQuaternionMonomorphism: RingMonomorphism<HurwitzQuaternion, RationalQuaternion> {
        override val domain = HurwitzQuaternionRing
        override val codomain = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        override val map = UnaryOp<HurwitzQuaternion, RationalQuaternion> { (a, b, c, d) ->
            rationalQuaternion(a, b, c, d)
        }
    }

    /**
     * Monomorphism: HurwitzQuaternion to Quaternion.
     */
    object HurwitzToQuaternionMonomorphism: RingMonomorphism<HurwitzQuaternion, Quaternion> {
        override val domain = HurwitzQuaternionRing
        override val codomain = QuaternionAlgebras.QuaternionDivisionRing
        override val map = UnaryOp<HurwitzQuaternion, Quaternion> { (a, b, c, d) ->
            quaternion(a.toReal(),  b.toReal(), c.toReal(), d.toReal())
        }
    }

    val eqHurwitzQuaternion: Eq<HurwitzQuaternion> = Eq.default()

    val printableHurwitzQuaternion: Printable<HurwitzQuaternion> =
        QuaternionPrintable.quaternionPrintable(
            signed = RationalAlgebras.SignedRational,
            zero = Rational.ZERO,
            one = Rational.ONE,
            prA = RationalAlgebras.printableRational,
            eqA = RationalAlgebras.eqRational,
            decompose = { q -> listOf(q.w, q.x, q.y, q.z) }
        )

    val printableHurwitzQuaternionPretty = printableHurwitzQuaternion
}
