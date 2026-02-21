package org.vorpal.kosmos.algebra.structures.instances.gaussian

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.Quaternion
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras
import org.vorpal.kosmos.algebra.structures.instances.embeddings.AxisSignEmbeddings
import org.vorpal.kosmos.algebra.structures.instances.embeddings.QuaternionEmbeddingKit
import org.vorpal.kosmos.algebra.structures.instances.quaternion
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols

import org.vorpal.kosmos.core.gaussian.GaussianInt
import org.vorpal.kosmos.core.gaussian.GaussianIntAlgebras
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

typealias LipschitzQuaternion = CD<GaussianInt>
val LipschitzQuaternion.w: BigInteger get() = a.re
val LipschitzQuaternion.x: BigInteger get() = a.im
val LipschitzQuaternion.y: BigInteger get() = b.re
val LipschitzQuaternion.z: BigInteger get() = b.im

fun lipschitzQuaternion(w: BigInteger,
                        x: BigInteger,
                        y: BigInteger,
                        z: BigInteger): LipschitzQuaternion =
    LipschitzQuaternion(
        GaussianInt(w, x),
        GaussianInt(y, z)
    )

object LipschitzQuaternionAlgebras {

    /**
     * This is the Lipschitz quaternion ring Lipschitz-ℍ:
     * - associative
     * - noncommutative
     * - involutive
     * - not a division ring.
     */
    object LipschitzQuaternionRing:
        InvolutiveRing<LipschitzQuaternion>,
        HasNormSq<LipschitzQuaternion, BigInteger> {

        internal val base: NonAssociativeInvolutiveRing<LipschitzQuaternion> =
            CayleyDickson.usual(GaussianIntAlgebras.GaussianIntCommutativeRing)


        override val add = base.add

        // base.mul is a NonAssociativeMonoid: for quaternions, it is actually a monoid and
        // Ring expects an associative monoid, so we wrap.
        override val mul: Monoid<LipschitzQuaternion> = Monoid.of(
            identity = base.mul.identity,
            op = base.mul.op
        )

        override fun fromBigInt(n: BigInteger): LipschitzQuaternion =
            base.fromBigInt(n)

        override val conj: Endo<LipschitzQuaternion> =
            base.conj

        /**
         * normSq(q) = scalar part of q * conj(q).
         * In ℍ_Z, this lands in ℤ (BigInteger).
         */
        override val normSq: UnaryOp<LipschitzQuaternion, BigInteger> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { q ->
                q.w * q.w + q.x * q.x + q.y * q.y + q.z * q.z
            }

        // Disambiguate zero and one.
        override val zero: LipschitzQuaternion = add.identity
        override val one: LipschitzQuaternion = mul.identity
    }

    object LipschitzQuaternionZModule : ZModule<LipschitzQuaternion> {
        override val scalars = IntegerAlgebras.ZCommutativeRing
        override val add = LipschitzQuaternionRing.add
        override val leftAction: LeftAction<BigInteger, LipschitzQuaternion> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { n, lq ->
                lipschitzQuaternion(n * lq.w, n * lq.x, n * lq.y, n * lq.z)
            }
    }

    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical

    /**
     * Return the ring monomorphism embedding Gaussian-ℤ into Lipschitz-ℍ as determined by [embedding].
     */
    fun gaussianIntEmbeddingToQuaternion(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<GaussianInt, LipschitzQuaternion> = RingMonomorphism.of(
        domain = GaussianIntAlgebras.GaussianIntCommutativeRing,
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

    val GaussianIntToLipschitzMonomorphism: NonAssociativeRingMonomorphism<GaussianInt, LipschitzQuaternion> =
        CayleyDickson.canonicalEmbedding(
            base = GaussianIntAlgebras.GaussianIntCommutativeRing,
            doubled = LipschitzQuaternionRing
        )

    val LipschitzQuaternionToQuaternionMonomorphism: RingMonomorphism<LipschitzQuaternion, Quaternion> = RingMonomorphism.of(
        domain = LipschitzQuaternionRing,
        codomain = QuaternionAlgebras.QuaternionDivisionRing,
        map = UnaryOp { lq ->
            val w = lq.a.re.toReal()
            val x = lq.a.im.toReal()
            val y = lq.b.re.toReal()
            val z = lq.b.im.toReal()
            quaternion(w, x, y, z)
        }
    )

    val eqLipschitzQuaternion: Eq<LipschitzQuaternion> = Eq { q1, q2 -> q1 == q2}
}
