package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.w
import org.vorpal.kosmos.hypercomplex.quaternion.x
import org.vorpal.kosmos.hypercomplex.quaternion.y
import org.vorpal.kosmos.hypercomplex.quaternion.z
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.core.render.Printable
import java.math.BigInteger

/**
 * Main structure:
 * [GravesianOctonionAlgebras] contains the algebraic structures over the [GravesianOctonion] type, as well as the
 * homomorphisms and [Eq] instances.
 *
 * These include:
 * - [GravesianOctonionNonAssociativeInvolutiveRing]: the Gravesian octonions.
 * - [ZModuleGravesianOctonion]: the Gravesian octonions viewed as a free ℤ-module of rank 8.
 *
 * We have the following homomorphisms:
 * - [GravesianToRationalOctonionMonomorphism]: a ring homomorphism from the Gravesian octonions to the rational octonions.
 * - [GravesianToOctonionMonomorphism]: a ring monomorphism from the Gravesian octonions to the octonions.
 *
 * We also have the following [Eq]s:
 * - [eqGravesianOctonion]: equality on Gravesian octonions.
 */
object GravesianOctonionAlgebras {

    // Use CayleyDickson to double the LipschitzQuaternions to get the GravesianOctonions.
    internal val base =
        CayleyDickson.usual(LipschitzQuaternionAlgebras.LipschitzQuaternionRing)

    object GravesianOctonionNonAssociativeInvolutiveRing:
        NonAssociativeInvolutiveRing<GravesianOctonion>,
        HasNormSq<GravesianOctonion, BigInteger> {

        override val add = base.add
        override val mul = base.mul
        override val conj = base.conj

        override fun fromBigInt(n: BigInteger): GravesianOctonion =
            base.fromBigInt(n)

        override val normSq: UnaryOp<GravesianOctonion, BigInteger> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { co ->
                val prod = mul(co, conj(co))
                prod.a.w
            }

        val basisMap: Map<Int, GravesianOctonion> = run {
            val z = BigInteger.ZERO
            val o = BigInteger.ONE
            mapOf(
                0 to gravesianOctonion(o,z,z, z, z,z,z,z),
                1 to gravesianOctonion(z,o,z, z, z,z,z,z),
                2 to gravesianOctonion(z,z,o, z, z,z,z,z),
                3 to gravesianOctonion(z,z,z,o, z,z,z,z),
                4 to gravesianOctonion(z,z,z, z, o,z,z,z),
                5 to gravesianOctonion(z,z,z, z, z,o,z,z),
                6 to gravesianOctonion(z,z,z, z, z,z,o,z),
                7 to gravesianOctonion(z,z,z, z, z,z,z,o)
            )
        }
    }

    /**
     * We can't build a vector space over the GravesianOctonions since they are not acted on by a field,
     * but we can build a Z-module and we need the LeftAction to scale. Instead of building using the
     * AbelianGroupZModuleBridge, we build it directly for efficiency on the scaling operation.
     */
    object ZModuleGravesianOctonion: ZModule<GravesianOctonion> {
        override val scalars = IntegerAlgebras.IntegerCommutativeRing
        override val add = base.add
        override val leftAction: LeftAction<BigInteger, GravesianOctonion> = LeftAction { n, co ->
            gravesianOctonion(
                n * co.w, n * co.x, n * co.y, n * co.z,
                n * co.u, n * co.v, n * co.s, n * co.t
            )
        }
    }

    object GravesianToRationalOctonionMonomorphism: NonAssociativeRingMonomorphism<GravesianOctonion, RationalOctonion> {
        override val domain = GravesianOctonionNonAssociativeInvolutiveRing
        override val codomain = RationalOctonionAlgebras.RationalOctonionNonAssociativeInvolutiveRing
        override val map = UnaryOp<GravesianOctonion, RationalOctonion> { co ->
            rationalOctonion(
                co.w.toRational(), co.x.toRational(), co.y.toRational(), co.z.toRational(),
                co.u.toRational(), co.v.toRational(), co.s.toRational(), co.t.toRational()
            )
        }
    }

    /**
     * Monomorphism from GravesianOctonions to Octonions.
     */
    object GravesianToOctonionMonomorphism: NonAssociativeRingMonomorphism<GravesianOctonion, Octonion> {
        override val domain = GravesianOctonionNonAssociativeInvolutiveRing
        override val codomain = OctonionAlgebras.OctonionDivisionAlgebraReal
        override val map = UnaryOp<GravesianOctonion, Octonion> { co ->
            octonion(
                co.w.toReal(), co.x.toReal(), co.y.toReal(), co.z.toReal(),
                co.u.toReal(), co.v.toReal(), co.s.toReal(), co.t.toReal()
            )
        }
    }

    val eqGravesianOctonion: Eq<GravesianOctonion> = Eq.default()

    val embeddingKit = OctonionEmbeddingKit.OctonionEmbeddingKit(
        quaternionRing = LipschitzQuaternionAlgebras.LipschitzQuaternionRing,
        octonionRing = GravesianOctonionNonAssociativeInvolutiveRing,
        basisMap = GravesianOctonionNonAssociativeInvolutiveRing.basisMap,
        leftAction = ZModuleGravesianOctonion.leftAction,
        eq = eqGravesianOctonion,
        decompose = { lq -> listOf(lq.w, lq.x, lq.y, lq.z) }
    )

    fun allQuaternionEmbeddings() = embeddingKit.allEmbeddings()

    val printableGravesianOctonion: Printable<GravesianOctonion> =
        OctonionPrintable.octonionPrintable(
            signed = IntegerAlgebras.SignedInteger,
            zero = BigInteger.ZERO,
            one = BigInteger.ONE,
            prA = IntegerAlgebras.printableInteger,
            eqA = IntegerAlgebras.eqInteger,
            decompose = { co -> listOf(co.w, co.x, co.y, co.z, co.u, co.v, co.s, co.t) }
        )

    val printableGravesianOctonionPretty = printableGravesianOctonion
}
