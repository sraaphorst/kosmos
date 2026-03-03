package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras.eqRational
import org.vorpal.kosmos.hypercomplex.embeddings.AxisSignEmbeddings
import org.vorpal.kosmos.hypercomplex.embeddings.QuaternionEmbeddingKit
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.hypercomplex.complex.GaussianRat
import org.vorpal.kosmos.hypercomplex.complex.GaussianRatAlgebras
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

/**
 * [RationalQuaternionAlgebras] contains the algebraic structures over the [RationalQuaternion] type, as well as the
 * homomorphisms and [Eq] instances.
 *
 * These include:
 * - [RationalQuaternionDivisionRing]: the rational quaternions.
 * - [RationalQuaternionVectorSpace]: the two-dimensional vector space of rational quaternions over the rationals.
 *
 * We have the following homomorphisms:
 * - [gaussianRatToQuaternionMonomorphism]: a ring monomorphism from [GaussianRat] to [RationalQuaternion].
 * - [RationalQuaternionToQuaternionMonomorphism]: a ring homomorphism from [RationalQuaternion] to [Quaternion].
 * - [HurwitzToRationalQuaternionMonomorphism]: a ring homomorphism from [HurwitzQuaternion] to [RationalQuaternion].
 * - [LipschitzToRationalQuaternionMonomorphism]: a ring homomorphism from [LipschitzQuaternion] to [RationalQuaternion]
 * (passing through the Hurwitz quaternions).
 *
 * We also have the following [Eq]s:
 * - [eqRationalQuaternion]: equality on rational quaternions.
 */
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
            CayleyDickson.usual(GaussianRatAlgebras.GaussianRatField)

        override val zero = base.add.identity
        override val one = base.mul.identity

        override val add = base.add

        override val mul: Monoid<RationalQuaternion> = Monoid.of(
            identity = one,
            op = base.mul.op
        )

        override val reciprocal: Endo<RationalQuaternion> = Endo(Symbols.SLASH) { q ->
            val n2 = normSq(q)
            require(eqRational.neqv(n2, Rational.ZERO)) {
                "Zero has no multiplicative inverse in ${Symbols.BB_Q}."
            }

            val scale = n2.reciprocal()
            val qc = conj(q)
            rationalQuaternion(
                scale * qc.w, scale * qc.x, scale * qc.y, scale * qc.z
            )
        }

        override fun fromBigInt(n: BigInteger) =
            base.fromBigInt(n)

        override val conj: Endo<RationalQuaternion> =
            base.conj

        override val normSq: UnaryOp<RationalQuaternion, Rational> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL){ q -> mul(q, conj(q)).w }
    }

    /**
     * Scalars: rationals, act componentwise on `(a, b)`.
     */
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
    fun gaussianRatToQuaternionMonomorphism(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<GaussianRat, RationalQuaternion> = RingMonomorphism.of(
        domain = GaussianRatAlgebras.GaussianRatField,
        codomain = RationalQuaternionDivisionRing,
        map = UnaryOp { z ->
            QuaternionEmbeddingKit.embedComplexLike(
                axisSign = embedding,
                re = z.re,
                im = z.im,
                zero = Rational.ZERO,
                negate = Rational::unaryMinus,
                mkQuaternion = ::rationalQuaternion
            )
        }
    )

    val RationalQuaternionToQuaternionMonomorphism: RingMonomorphism<RationalQuaternion, Quaternion> = RingMonomorphism.of(
        domain = RationalQuaternionDivisionRing,
        codomain = QuaternionAlgebras.QuaternionDivisionRing,
        map = UnaryOp { lq ->
            quaternion(lq.a.re.toReal(), lq.a.im.toReal(), lq.b.re.toReal(), lq.b.im.toReal())
        }
    )

    object HurwitzToRationalQuaternionMonomorphism: RingMonomorphism<HurwitzQuaternion, RationalQuaternion> {
        override val domain = HurwitzQuaternionAlgebras.HurwitzQuaternionRing
        override val codomain = RationalQuaternionDivisionRing
        override val map = UnaryOp<HurwitzQuaternion, RationalQuaternion> { (a, b, c, d) ->
            rationalQuaternion(a, b, c, d)
        }
    }

    val LipschitzToRationalQuaternionMonomorphism: RingMonomorphism<LipschitzQuaternion, RationalQuaternion> =
        HurwitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism andThen HurwitzToRationalQuaternionMonomorphism

    val eqRationalQuaternion: Eq<RationalQuaternion> = Eq { q1, q2 -> q1 == q2}
}
