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
import org.vorpal.kosmos.core.render.Printable
import java.math.BigInteger

/**
 * [LipschitzQuaternionAlgebras] contains the algebraic structures over the [LipschitzQuaternion] type, as well as the
 * homomorphisms and [Eq] instances.
 *
 * These include:
 * - [LipschitzQuaternionRing]: the Lipschitz quaternions.
 * - [ZModuleLipschitzQuaternion]: the two-dimensional vector space of Lipschitz quaternions over the integers.
 *
 * We have the following homomorphisms:
 * - [gaussianIntEmbeddingToQuaternion]: the unital embeddings from the Gaussian integers to the Lipschitz quaternions.
 * - [LipschitzQuaternionToQuaternionMonomorphism]: a ring homomorphism from the Lipschitz quaternions to the quaternions.
 *
 * We also have the following [Eq]s:
 * - [eqLipschitzQuaternion]: equality on Lipschitz quaternions.
 */
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
         * normSq(q) = scalar part of q * conj(q).
         * In ℍ_Z, this lands in ℤ (BigInteger).
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

    object LipschitzQuaternionToQuaternionMonomorphism: RingMonomorphism<LipschitzQuaternion, Quaternion> {
        override val domain = LipschitzQuaternionRing
        override val codomain = QuaternionAlgebras.QuaternionDivisionRing
        override val map = UnaryOp<LipschitzQuaternion, Quaternion> { lq ->
            val w = lq.a.re.toReal()
            val x = lq.a.im.toReal()
            val y = lq.b.re.toReal()
            val z = lq.b.im.toReal()
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
