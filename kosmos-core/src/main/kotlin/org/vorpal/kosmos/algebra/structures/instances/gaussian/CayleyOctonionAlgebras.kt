package org.vorpal.kosmos.algebra.structures.instances.gaussian

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.Octonion
import org.vorpal.kosmos.algebra.structures.instances.OctonionAlgebras
import org.vorpal.kosmos.algebra.structures.instances.embeddings.OctonionEmbeddingKit
import org.vorpal.kosmos.algebra.structures.instances.octonion
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

typealias CayleyOctonion = CD<LipschitzQuaternion>

val CayleyOctonion.w: BigInteger get() = a.w
val CayleyOctonion.x: BigInteger get() = a.x
val CayleyOctonion.y: BigInteger get() = a.y
val CayleyOctonion.z: BigInteger get() = a.z

val CayleyOctonion.u: BigInteger get() = b.w
val CayleyOctonion.v: BigInteger get() = b.x
val CayleyOctonion.s: BigInteger get() = b.y
val CayleyOctonion.t: BigInteger get() = b.z

fun cayleyOctonion(
    w: BigInteger, x: BigInteger, y: BigInteger, z: BigInteger,
    u: BigInteger, v: BigInteger, s: BigInteger, t: BigInteger
): CayleyOctonion {
    val a = lipschitzQuaternion(w, x, y, z)
    val b = lipschitzQuaternion(u, v, s, t)
    return CayleyOctonion(a, b)
}

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
    object CayleyOctonionZModule: ZModule<CayleyOctonion> {
        override val scalars = IntegerAlgebras.ZCommutativeRing
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
    val CayleyToOctonionMonomorphism: NonAssociativeRingMonomorphism<CayleyOctonion, Octonion> =
        NonAssociativeRingMonomorphism.of(
            domain = CayleyOctonionNonAssociativeInvolutiveRing,
            codomain = OctonionAlgebras.OctonionDivisionAlgebraReal,
            map = UnaryOp { co ->
                octonion(
                    co.w.toReal(),co.x.toReal(),co.y.toReal(),co.z.toReal(),
                    co.u.toReal(),co.v.toReal(),co.s.toReal(),co.t.toReal()
                )
            }
        )

    val eqCayleyOctonion: Eq<CayleyOctonion> = Eq { o1, o2 -> o1 == o2 }

    val embeddingKit = OctonionEmbeddingKit.OctonionEmbeddingKit(
        quaternionRing = LipschitzQuaternionAlgebras.LipschitzQuaternionRing,
        octonionRing = CayleyOctonionNonAssociativeInvolutiveRing,
        basisMap = CayleyOctonionNonAssociativeInvolutiveRing.basisMap,
        leftAction = CayleyOctonionZModule.leftAction,
        eq = eqCayleyOctonion,
        decompose = { lq -> listOf(lq.w, lq.x, lq.y, lq.z) }
    )

    fun allQuaternionEmbeddings() = embeddingKit.allEmbeddings()
}
