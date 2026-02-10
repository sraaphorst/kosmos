package org.vorpal.kosmos.algebra.structures.instances.gaussian

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.instances.Quaternion
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras
import org.vorpal.kosmos.algebra.structures.instances.base.GaussianAlgebras
import org.vorpal.kosmos.algebra.structures.instances.base.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.base.eqRational
import org.vorpal.kosmos.algebra.structures.instances.embeddings.AxisSignEmbeddings
import org.vorpal.kosmos.algebra.structures.instances.embeddings.QuaternionEmbeddingKit
import org.vorpal.kosmos.algebra.structures.instances.quaternion
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.gaussian.GaussianRat
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

typealias RationalQuaternion = CD<GaussianRat>
val RationalQuaternion.w: Rational get() = a.re
val RationalQuaternion.x: Rational get() = a.im
val RationalQuaternion.y: Rational get() = b.re
val RationalQuaternion.z: Rational get() = b.im

fun rationalQuaternion(w: Rational,
                       x: Rational,
                       y: Rational,
                       z: Rational): RationalQuaternion =
    RationalQuaternion(
        GaussianRat(w, x),
        GaussianRat(y, z),
    )

object RationalQuaternionAlgebras {

    /**
     * This is the ring of quaternions with rational coefficients `ℍ_ℚ`:
     * - associative
     * - noncommutative
     * - involutive
     * - a division ring.
     */
    object RationalQuaternionDivisionRing:
        DivisionRing<RationalQuaternion>,
        InvolutiveRing<RationalQuaternion>,
        NormedDivisionAlgebra<Rational, RationalQuaternion> {

        internal val base: NonAssociativeInvolutiveRing<RationalQuaternion> =
            CayleyDickson.usual(GaussianAlgebras.GaussianRatField)


        override val add = base.add

        override val mul: Monoid<RationalQuaternion> = Monoid.of(
            identity = base.mul.identity,
            op = base.mul.op
        )

        override val reciprocal: Endo<RationalQuaternion> = Endo(Symbols.SLASH) { q ->
            val n2 = normSq(q)
            require(eqRational.neqv(n2, Rational.ZERO)) {
                "Zero has no multiplicative inverse in ${Symbols.BB_Q}."
            }

            val qc = conj(q)
            val scale = n2.reciprocal()

            rationalQuaternion(
                scale * q.w, scale * q.x, scale * q.y, scale * q.z
            )
        }

        override fun fromBigInt(n: BigInteger) =
            base.fromBigInt(n)

        override val conj: Endo<RationalQuaternion> =
            base.conj

        override val normSq: UnaryOp<RationalQuaternion, Rational> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL){ q -> mul(q, conj(q)).w }

        // Disambiguate zero and one.
        override val zero = add.identity
        override val one = mul.identity
    }

    // Scalars: rationals, act componentwise on (a, b)
    val RationalQuaternionVectorSpace: FiniteVectorSpace<Rational, RationalQuaternion> = FiniteVectorSpace.of(
        scalars = RationalAlgebras.RationalField,
        add = RationalQuaternionDivisionRing.add,
        dimension = 4,
        leftAction = LeftAction { q, rq ->
            rationalQuaternion(q * rq.w, q * rq.x, q * rq.y, q * rq.z)
        }
    )

    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical

    /**
     * Return the ring monomorphism embedding Gaussian-ℚ into Quaternion-ℚ as determined by [embedding].
     */
    fun gaussianRatEmbeddingToQuaternion(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<GaussianRat, RationalQuaternion> = RingMonomorphism.of(
        domain = GaussianAlgebras.GaussianRatField,
        codomain = RationalQuaternionDivisionRing,
        map = UnaryOp { z ->
            val s =
                if (embedding.sign == AxisSignEmbeddings.Sign.PLUS) Rational.ONE
                else -Rational.ONE

            // This can still use the helper if you pass imScaled = s * z.im.
            val re = z.re
            val im = s * z.im

            QuaternionEmbeddingKit.embedComplexLike(
                axisSign = AxisSignEmbeddings.AxisSignEmbedding(
                    axis = embedding.axis,
                    sign = AxisSignEmbeddings.Sign.PLUS
                ),
                re = re,
                im = im,
                zero = Rational.ZERO,
                negate = { -it },
                mkQuaternion = ::rationalQuaternion
            )
        }
    )

    val RationalQuaternionToQuaternionMonomorphism: RingMonomorphism<RationalQuaternion, Quaternion> = RingMonomorphism.of(
        domain = RationalQuaternionDivisionRing,
        codomain = QuaternionAlgebras.QuaternionDivisionRing,
        map = UnaryOp { lq ->
            val w = lq.a.re.toReal()
            val x = lq.a.im.toReal()
            val y = lq.b.re.toReal()
            val z = lq.b.im.toReal()
            quaternion(w, x, y, z)
        }
    )

    val eqRationalQuaternion: Eq<RationalQuaternion> = Eq { q1, q2 -> q1 == q2}
}
