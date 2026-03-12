package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.hypercomplex.embeddings.OctonionEmbeddingKit
import org.vorpal.kosmos.hypercomplex.quaternion.HurwitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.w
import org.vorpal.kosmos.hypercomplex.quaternion.x
import org.vorpal.kosmos.hypercomplex.quaternion.y
import org.vorpal.kosmos.hypercomplex.quaternion.z
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.core.render.Printable
import java.math.BigInteger

/**
 * [RationalOctonionAlgebras] contains the algebraic structures over the [RationalOctonion] type, as well as the
 * homomorphisms and [Eq] instances.
 *
 * These include:
 * - [RationalOctonionNonAssociativeInvolutiveRing]: the rational octonions.
 * - [RationalOctonionVectorSpace]: the eight-dimensional vector space of rational octonions over the rationals.
 *
 * We have the following homomorphisms:
 * - [CayleyToRationalOctonionMonomorphism]: a ring homomorphism from the Cayley octonions to the rational octonions.
 * - [LipschitzToRationalOctonionMonomorphism]: a ring homomorphism from the Lipschitz quaternions to the rational octonions.
 * - [RationalQuaternionToRationalOctonionMonomorphism]: a ring homomorphism from the rational quaternions to the rational octonions.
 * - [HurwitzToRationalOctonionMonomorphism]: a ring homomorphism from the Hurwitz quaternions to the rational octonions.
 * - [RationalToOctonionMonomorphism]: a ring homomorphism from the rational octonions to the octonions.
 *
 * We also have the following [Eq]s:
 * - [eqRationalOctonion]: equality on rational octonions.
 */
object RationalOctonionAlgebras {

    object RationalOctonionNonAssociativeInvolutiveRing:
        NonAssociativeInvolutiveRing<RationalOctonion>,
        HasNormSq<RationalOctonion, Rational> {

        internal val base =
            CayleyDickson.usual(RationalQuaternionAlgebras.RationalQuaternionDivisionRing)

        override val add = base.add
        override val mul = base.mul
        override val conj = base.conj

        override fun fromBigInt(n: BigInteger): RationalOctonion =
            base.fromBigInt(n)

        override val normSq: UnaryOp<RationalOctonion, Rational> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { co -> mul(co, conj(co)).a.w }

        val basisMap: Map<Int, RationalOctonion> = run {
            val z = Rational.ZERO
            val o = Rational.ONE
            mapOf(
                0 to rationalOctonion(o,z,z,z, z,z,z,z),
                1 to rationalOctonion(z,o,z,z, z,z,z,z),
                2 to rationalOctonion(z,z,o,z, z,z,z,z),
                3 to rationalOctonion(z,z,z,o, z,z,z,z),
                4 to rationalOctonion(z,z,z,z, o,z,z,z),
                5 to rationalOctonion(z,z,z,z, z,o,z,z),
                6 to rationalOctonion(z,z,z,z, z,z,o,z),
                7 to rationalOctonion(z,z,z,z, z,z,z,o)
            )
        }
    }

    /**
     * ℚ-Vector space over RationalOctonions of dimension 8.
     */
    val RationalOctonionVectorSpace: FiniteVectorSpace<Rational, RationalOctonion> = FiniteVectorSpace.of(
            RationalAlgebras.RationalField,
        add = RationalOctonionNonAssociativeInvolutiveRing.add,
        dimension = 8,
        leftAction = LeftAction { q, ro ->
            rationalOctonion(
                q * ro.w, q * ro.x, q * ro.y, q * ro.z,
                q * ro.u, q * ro.v, q * ro.s, q * ro.t
            )
        }
    )

    /**
     * CayleyOctonion to RationalOctonion ring homomorphism.
     */
    object CayleyToRationalOctonionMonomorphism: NonAssociativeRingMonomorphism<CayleyOctonion, RationalOctonion> {
        override val domain = CayleyOctonionAlgebras.CayleyOctonionNonAssociativeInvolutiveRing
        override val codomain = RationalOctonionNonAssociativeInvolutiveRing
        override val map = UnaryOp<CayleyOctonion, RationalOctonion> { co ->
            rationalOctonion(
                co.w.toRational(), co.x.toRational(), co.y.toRational(), co.z.toRational(),
                co.u.toRational(), co.v.toRational(), co.s.toRational(), co.t.toRational()
            )
        }
    }

    val LipschitzToRationalOctonionMonomorphism: NonAssociativeRingMonomorphism<LipschitzQuaternion, RationalOctonion> =
        CayleyOctonionAlgebras.LipschitzToCayleyMonomorphism andThen CayleyToRationalOctonionMonomorphism

    val RationalQuaternionToRationalOctonionMonomorphism: NonAssociativeRingMonomorphism<RationalQuaternion, RationalOctonion> =
        CayleyDickson.canonicalEmbedding(
            base = RationalQuaternionAlgebras.RationalQuaternionDivisionRing,
            doubled = RationalOctonionNonAssociativeInvolutiveRing
        )

    val HurwitzToRationalOctonionMonomorphism: NonAssociativeRingMonomorphism<HurwitzQuaternion, RationalOctonion> =
        RationalQuaternionAlgebras.HurwitzToRationalQuaternionMonomorphism andThen RationalQuaternionToRationalOctonionMonomorphism

    /**
     * RationalOctonion to Octonion ring monomorphism.
     */
    object RationalToOctonionMonomorphism: NonAssociativeRingMonomorphism<RationalOctonion, Octonion> {
        override val domain = RationalOctonionNonAssociativeInvolutiveRing
        override val codomain = OctonionAlgebras.OctonionDivisionAlgebraReal
        override val map = UnaryOp<RationalOctonion, Octonion> { ro ->
            octonion(
                ro.w.toReal(), ro.x.toReal(), ro.y.toReal(), ro.z.toReal(),
                ro.u.toReal(), ro.v.toReal(), ro.s.toReal(), ro.t.toReal()
            )
        }
    }

    val eqRationalOctonion: Eq<RationalOctonion> = Eq { o1, o2 -> o1 == o2 }

    val embeddingKit = OctonionEmbeddingKit.OctonionEmbeddingKit(
        quaternionRing = RationalQuaternionAlgebras.RationalQuaternionDivisionRing,
        octonionRing = RationalOctonionNonAssociativeInvolutiveRing,
        basisMap = RationalOctonionNonAssociativeInvolutiveRing.basisMap,
        leftAction = RationalOctonionVectorSpace.leftAction,
        eq = eqRationalOctonion,
        decompose = { rq -> listOf(rq.w, rq.x, rq.y, rq.z) }
    )

    fun allEmbeddings() = embeddingKit.allEmbeddings()

    val printableRationalOctonion: Printable<RationalOctonion> =
        OctonionPrintable.octonionPrintable(
            signed = RationalAlgebras.SignedRational,
            zero = Rational.ZERO,
            one = Rational.ONE,
            prA = RationalAlgebras.printableRational,
            eqA = RationalAlgebras.eqRational,
            decompose = { ro -> listOf(ro.w, ro.x, ro.y, ro.z, ro.u, ro.v, ro.s, ro.t) }
        )

    val printableRationalOctonionPretty = printableRationalOctonion
}
