package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.w
import org.vorpal.kosmos.hypercomplex.quaternion.x
import org.vorpal.kosmos.hypercomplex.quaternion.y
import org.vorpal.kosmos.hypercomplex.quaternion.z
import org.vorpal.kosmos.hypercomplex.embeddings.OctonionEmbeddingKit
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import java.math.BigInteger

/**
 * [CayleyOctonionAlgebras] contains the algebraic structures over the [CayleyOctonion] type, as well as the
 * homomorphisms and [Eq] instances.
 *
 * These include:
 * - [CayleyOctonionNonAssociativeInvolutiveRing]: the Cayley octonions.
 * - [ZModuleCayleyOctonion]: the eight-dimensional vector space of Cayley octonions over the rationals.
 *
 * We have the following homomorphisms:
 * - [LipschitzToCayleyMonomorphism]: a ring monomorphism from the Lipschitz quaternions to the Cayley octonions.
 * - [CayleyToOctonionMonomorphism]: a ring monomorphism from the Cayley octonions to the octonions.
 *
 * We also have the following [Eq]s:
 * - [eqCayleyOctonion]: equality on Cayley octonions.
 */
object CayleyOctonionAlgebras {

    // Use CayleyDickson to double the LipschitzQuaternions to get the CayleyOctonions.
    internal val base =
        CayleyDickson.usual(LipschitzQuaternionAlgebras.LipschitzQuaternionRing)

    object CayleyOctonionNonAssociativeInvolutiveRing:
        NonAssociativeInvolutiveRing<CayleyOctonion>,
        HasNormSq<CayleyOctonion, BigInteger> {

        override val add = base.add
        override val mul = base.mul
        override val conj = base.conj

        override fun fromBigInt(n: BigInteger): CayleyOctonion =
            base.fromBigInt(n)

        override val normSq: UnaryOp<CayleyOctonion, BigInteger> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { co ->
                val prod = mul(co, conj(co))
                prod.a.w
            }

        val basisMap: Map<Int, CayleyOctonion> = run {
            val z = BigInteger.ZERO
            val o = BigInteger.ONE
            mapOf(
                0 to cayleyOctonion(o,z,z, z, z,z,z,z),
                1 to cayleyOctonion(z,o,z, z, z,z,z,z),
                2 to cayleyOctonion(z,z,o, z, z,z,z,z),
                3 to cayleyOctonion(z,z,z,o, z,z,z,z),
                4 to cayleyOctonion(z,z,z, z, o,z,z,z),
                5 to cayleyOctonion(z,z,z, z, z,o,z,z),
                6 to cayleyOctonion(z,z,z, z, z,z,o,z),
                7 to cayleyOctonion(z,z,z, z, z,z,z,o)
            )
        }
    }

    /**
     * We can't build a vector space over the CayleyOctonions since they are not acted on by a field,
     * but we can build a Z-module and we need the LeftAction to scale. Instead of building using the
     * AbelianGroupZModuleBridge, we build it directly for efficiency on the scaling operation.
     */
    object ZModuleCayleyOctonion: ZModule<CayleyOctonion> {
        override val scalars = IntegerAlgebras.IntegerCommutativeRing
        override val add = base.add
        override val leftAction: LeftAction<BigInteger, CayleyOctonion> = LeftAction { n, co ->
            cayleyOctonion(
                n * co.w, n * co.x, n * co.y, n * co.z,
                n * co.u, n * co.v, n * co.s, n * co.t
            )
        }
    }

    val LipschitzToCayleyMonomorphism: NonAssociativeRingMonomorphism<LipschitzQuaternion, CayleyOctonion> =
        CayleyDickson.canonicalEmbedding(
            base = LipschitzQuaternionAlgebras.LipschitzQuaternionRing,
            doubled = CayleyOctonionNonAssociativeInvolutiveRing
        )

    /**
     * Monomorphism from CayleyOctonions to Octonions.
     */
    object CayleyToOctonionMonomorphism: NonAssociativeRingMonomorphism<CayleyOctonion, Octonion> {
        override val domain = CayleyOctonionNonAssociativeInvolutiveRing
        override val codomain = OctonionAlgebras.OctonionDivisionAlgebraReal
        override val map = UnaryOp<CayleyOctonion, Octonion> { co ->
            octonion(
                co.w.toReal(), co.x.toReal(), co.y.toReal(), co.z.toReal(),
                co.u.toReal(), co.v.toReal(), co.s.toReal(), co.t.toReal()
            )
        }
    }

    val eqCayleyOctonion: Eq<CayleyOctonion> = Eq { o1, o2 -> o1 == o2 }

    val embeddingKit = OctonionEmbeddingKit.OctonionEmbeddingKit(
        quaternionRing = LipschitzQuaternionAlgebras.LipschitzQuaternionRing,
        octonionRing = CayleyOctonionNonAssociativeInvolutiveRing,
        basisMap = CayleyOctonionNonAssociativeInvolutiveRing.basisMap,
        leftAction = ZModuleCayleyOctonion.leftAction,
        eq = eqCayleyOctonion,
        decompose = { lq -> listOf(lq.w, lq.x, lq.y, lq.z) }
    )

    fun allQuaternionEmbeddings() = embeddingKit.allEmbeddings()

    val printableCayleyOctonion: Printable<CayleyOctonion> =
        OctonionPrintable.octonionPrintable(
            signed = IntegerAlgebras.SignedInteger,
            zero = BigInteger.ZERO,
            one = BigInteger.ONE,
            prA = IntegerAlgebras.printableInteger,
            eqA = IntegerAlgebras.eqInteger,
            decompose = { co -> listOf(co.w, co.x, co.y, co.z, co.u, co.v, co.s, co.t) }
        )

    val printableCayleyOctonionPretty = printableCayleyOctonion
}
