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
import org.vorpal.kosmos.hypercomplex.complex.GaussianInt
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import java.math.BigInteger


val HurwitzQuaternion.w: Rational get() = a
val HurwitzQuaternion.x: Rational get() = b
val HurwitzQuaternion.y: Rational get() = c
val HurwitzQuaternion.z: Rational get() = d

/**
 * [HurwitzQuaternionAlgebras] contains the algebraic structures over the  [HurwitzQuaternion] type, as well as the
 * homomorphisms and [Eq] instances.
 *
 * These include:
 * - [HurwitzQuaternionRing]: the Hurwitz quaternions.
 *
 * We have the following homomorphisms:
 * - [gaussianIntEmbeddingToHurwitz]: the unital embeddings from the Gaussian integers to the Hurwitz quaternions.
 * - [LipschitzToHurwitzQuaternionMonomorphism]: a ring monomorphism from the Lipschitz quaternions to the Hurwitz
 * quaternions.
 * - [HurwitzToQuaternionMonomorphism]: a ring monomorphism from the Hurwitz quaternions to the quaternions.
 * - [LipschitzToQuaternionMonomorphism]: a ring monomorphism from the Lipschitz quaternions to the quaternions
 * (passing through the Hurwitz quaternions).
 *
 * We also have the following [Eq]s:
 * - [eqHurwitzQuaternion]: equality on Hurwitz quaternions.
 */
object HurwitzQuaternionAlgebras {
    /**
     * The Hurwitz quaternions are:
     * - associative
     * - noncommutative
     * - involutive
     * - a finite unit group of size 24
     * - but do not form a division ring.
     *
     * However, they are the maximal order in the rational quaternion division algebra ℍ(ℚ):
     * An order in ℍ(ℚ) is a subring 𝒪 such that:
     * - Contains 1 and is closed under addition and multiplication
     * - Is a lattice (finitely generated ℤ-module of rank 4)
     * - Spans ℍ(ℚ) over ℚ (i.e., ℚ ⊗_ℤ 𝒪 = ℍ(ℚ)).
     *
     * The containment can be described as:
     * ```
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
            op = BinOp(Symbols.PLUS, HurwitzQuaternion::plus),
            inverse = Endo(Symbols.MINUS, HurwitzQuaternion::unaryMinus)
        )

        override val mul: Monoid<HurwitzQuaternion> = Monoid.of(
            identity = one,
            op = BinOp(Symbols.ASTERISK, HurwitzQuaternion::times)
        )

        override fun fromBigInt(n: BigInteger): HurwitzQuaternion =
            HurwitzQuaternion(n.toRational(), Rational.ZERO, Rational.ZERO, Rational.ZERO)

        // This always is a Hurwitz quaternion as doubling maintains integrality and parity mod 2
        // doesn't change under negation.
        override val conj: Endo<HurwitzQuaternion> = Endo(Symbols.CONJ) { (a, b, c, d) ->
            HurwitzQuaternion(a, -b, -c, -d)
        }

        // normSq always lands in Z:
        // 1. If they're already in Z, obvious.
        // 2. If they're all half-integers, then they all can be written n/2 with n odd.
        //    a^2 + b^2 + c^2 + d^2 = (n_a^2 + n_b^2 + n_c^2 + n_d^2) / 4
        //    with each odd square being 1 (mod 8), so the numerator is 4 (mod 8), hence divisible by 4.
        override val normSq: UnaryOp<HurwitzQuaternion, Rational> = UnaryOp(Symbols.NORM_SQ_SYMBOL) {
            (a, b, c, d) -> a * a + b * b + c * c + d * d
        }
    }

    object HurwitzQuaternionZModule : ZModule<HurwitzQuaternion> {
        override val scalars = IntegerAlgebras.ZCommutativeRing
        override val add = HurwitzQuaternionRing.add
        override val leftAction: LeftAction<BigInteger, HurwitzQuaternion> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { s, hq ->
                val sr = s.toRational()
                HurwitzQuaternion( sr * hq.a, sr * hq.b, sr * hq.c, sr * hq.d)
            }
    }

    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical

    /**
     * Create the [GaussianInt] -> [LipschitzQuaternion] monomorphism according to the [embedding] and then
     * apply the [LipschitzToHurwitzQuaternionMonomorphism] to get a [RingMonomorphism]:
     * ```kotlin
     * GaussianInt ↪ LipschitzQuaternion ↪ HurwitzQuaternion
     * ```
     */
    fun gaussianIntEmbeddingToHurwitz(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<GaussianInt, HurwitzQuaternion> =
        LipschitzQuaternionAlgebras.gaussianIntEmbeddingToQuaternion(embedding) andThen
            LipschitzToHurwitzQuaternionMonomorphism

    /**
     * General mappings from the Lipschitz quaternion rings to the Hurwitz quaternion rings.
     */
    val LipschitzToHurwitzQuaternionMonomorphism: RingMonomorphism<LipschitzQuaternion, HurwitzQuaternion> =
        RingMonomorphism.of(
            domain = LipschitzQuaternionAlgebras.LipschitzQuaternionRing,
            codomain = HurwitzQuaternionRing,
            map = UnaryOp { lq -> HurwitzQuaternion(
                lq.w.toRational(), lq.x.toRational(), lq.y.toRational(), lq.z.toRational()
            ) }
        )

    /**
     * General mappings from the Hurwitz quaternion rings to the Quaternion ring.
     */
    val HurwitzToQuaternionMonomorphism: RingMonomorphism<HurwitzQuaternion, Quaternion> = RingMonomorphism.of(
        domain = HurwitzQuaternionRing,
        codomain = QuaternionAlgebras.QuaternionDivisionRing,
        map = UnaryOp { (a, b, c, d) ->
            quaternion(a.toReal(), b.toReal(), c.toReal(), d.toReal())
        }
    )

    val LipschitzToQuaternionMonomorphism: RingMonomorphism<LipschitzQuaternion, Quaternion> =
        LipschitzToHurwitzQuaternionMonomorphism andThen HurwitzToQuaternionMonomorphism

    val eqHurwitzQuaternion: Eq<HurwitzQuaternion> = Eq { q1, q2 -> q1 == q2 }
}
