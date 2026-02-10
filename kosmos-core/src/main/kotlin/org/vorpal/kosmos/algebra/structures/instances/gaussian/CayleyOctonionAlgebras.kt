package org.vorpal.kosmos.algebra.structures.instances.gaussian

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
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
    }

    val eqCayleyOctonion: Eq<CayleyOctonion> = Eq { o1, o2 -> o1 == o2 }
}
