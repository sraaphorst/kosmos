package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.hypercomplex.embeddings.AxisSignEmbeddings
import org.vorpal.kosmos.hypercomplex.embeddings.QuaternionEmbeddingKit
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.hypercomplex.complex.GaussianInt
import org.vorpal.kosmos.hypercomplex.complex.GaussianIntAlgebras
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.core.render.Printable
import java.math.BigInteger

/**
 * Main structures:
 * - [LipschitzQuaternionRing]: the Lipschitz quaternions.
 *
 * Vector spaces and modules:
 * - [ZModuleLipschitzQuaternion]: the rank-4 ℤ-module of Lipschitz quaternions.
 *
 * Homomorphisms:
 * - [gaussianIntEmbeddingToQuaternion]: unital embedding factory from the Gaussian integers to the Lipschitz quaternions.
 * - [LipschitzToHurwitzQuaternionMonomorphism]: ring monomorphism from the Lipschitz quaternions to the Hurwitz quaternions.
 * - [LipschitzToRationalQuaternionMonomorphism]: ring monomorphism from the Lipschitz quaternions to the rational quaternions.
 * - [LipschitzToQuaternionMonomorphism]: ring monomorphism from the Lipschitz quaternions to the quaternions.
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
     */
    object LipschitzQuaternionRing:
        InvolutiveRing<LipschitzQuaternion>,
        HasNormSq<LipschitzQuaternion, BigInteger> {

        internal val base: NonAssociativeInvolutiveRing<LipschitzQuaternion> =
            CayleyDickson.usual(GaussianIntAlgebras.GaussianIntEuclideanDomain)

        override val zero: LipschitzQuaternion = base.add.identity
        override val one: LipschitzQuaternion = base.mul.identity

        override val add = base.add

        // base.mul is a NonAssociativeMonoid: for quaternions, it is actually a monoid and
        // Ring expects an associative monoid, so we wrap.
        override val mul: Monoid<LipschitzQuaternion> = Monoid.of(
            identity = one,
            op = base.mul.op
        )

        override fun fromBigInt(n: BigInteger): LipschitzQuaternion =
            base.fromBigInt(n)

        override val conj: Endo<LipschitzQuaternion> =
            base.conj

        /**
         * The norm-square lands in ℤ:
         * ```text
         * normSq(q) = w² + x² + y² + z²
         * ```
         */
        override val normSq: UnaryOp<LipschitzQuaternion, BigInteger> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { q ->
                q.w * q.w + q.x * q.x + q.y * q.y + q.z * q.z
            }
    }

    object ZModuleLipschitzQuaternion : ZModule<LipschitzQuaternion> {
        override val scalars = IntegerAlgebras.IntegerCommutativeRing
        override val add = LipschitzQuaternionRing.add
        override val leftAction: LeftAction<BigInteger, LipschitzQuaternion> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { s, lq ->
                lipschitzQuaternion(s * lq.w, s * lq.x, s * lq.y, s * lq.z)
            }
    }

    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical
    fun gaussianIntEmbeddingToQuaternion(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<GaussianInt, LipschitzQuaternion> = RingMonomorphism.of(
        domain = GaussianIntAlgebras.GaussianIntEuclideanDomain,
        codomain = LipschitzQuaternionRing,
        map = UnaryOp { z ->
            QuaternionEmbeddingKit.embedComplexLike(
                axisSign = embedding,
                re = z.re,
                im = z.im,
                zero = BigInteger.ZERO,
                negate = BigInteger::negate,
                mkQuaternion = ::lipschitzQuaternion
            )
        }
    )

    object LipschitzToHurwitzQuaternionMonomorphism: RingMonomorphism<LipschitzQuaternion, HurwitzQuaternion> {
        override val domain = LipschitzQuaternionRing
        override val codomain = HurwitzQuaternionAlgebras.HurwitzQuaternionRing
        override val map = UnaryOp<LipschitzQuaternion, HurwitzQuaternion> { lq ->
            HurwitzQuaternion(
                lq.w.toRational(), lq.x.toRational(), lq.y.toRational(), lq.z.toRational()
            )
        }
    }

    object LipschitzToRationalQuaternionMonomorphism: RingMonomorphism<LipschitzQuaternion, RationalQuaternion> {
        override val domain = LipschitzQuaternionRing
        override val codomain = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        override val map = UnaryOp<LipschitzQuaternion, RationalQuaternion> { lq ->
            rationalQuaternion(lq.w.toRational(), lq.x.toRational(), lq.y.toRational(), lq.z.toRational())
        }
    }

    object LipschitzToQuaternionMonomorphism: RingMonomorphism<LipschitzQuaternion, Quaternion> {
        override val domain = LipschitzQuaternionRing
        override val codomain = QuaternionAlgebras.QuaternionDivisionRing
        override val map = UnaryOp<LipschitzQuaternion, Quaternion> { lq ->
            val w = lq.w.toReal()
            val x = lq.x.toReal()
            val y = lq.y.toReal()
            val z = lq.z.toReal()
            quaternion(w, x, y, z)
        }
    }

    val eqLipschitzQuaternion: Eq<LipschitzQuaternion> = Eq.default()

    val printableLipschitzQuaternion: Printable<LipschitzQuaternion> =
        QuaternionPrintable.quaternionPrintable(
            signed = IntegerAlgebras.SignedInteger,
            zero = BigInteger.ZERO,
            one = BigInteger.ONE,
            prA = IntegerAlgebras.printableInteger,
            eqA = IntegerAlgebras.eqInt,
            decompose = { q -> listOf(q.w, q.x, q.y, q.z) }
        )

    val printableLipschitzQuaternionPretty = printableLipschitzQuaternion
}
