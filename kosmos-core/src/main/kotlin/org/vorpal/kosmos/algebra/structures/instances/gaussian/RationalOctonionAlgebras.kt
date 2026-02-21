package org.vorpal.kosmos.algebra.structures.instances.gaussian

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.Octonion
import org.vorpal.kosmos.algebra.structures.instances.OctonionAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.gaussian.CayleyOctonionAlgebras.CayleyOctonionNonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.octonion
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import java.math.BigInteger

typealias RationalOctonion = CD<RationalQuaternion>

val RationalOctonion.w: Rational get() = a.w
val RationalOctonion.x: Rational get() = a.x
val RationalOctonion.y: Rational get() = a.y
val RationalOctonion.z: Rational get() = a.z

val RationalOctonion.u: Rational get() = b.w
val RationalOctonion.v: Rational get() = b.x
val RationalOctonion.s: Rational get() = b.y
val RationalOctonion.t: Rational get() = b.z

fun rationalOctonion(w: Rational, x: Rational, y: Rational, z: Rational,
                     u: Rational, v: Rational, s: Rational, t: Rational
): RationalOctonion {
    val a = rationalQuaternion(w, x, y, z)
    val b = rationalQuaternion(u, v, s, t)
    return RationalOctonion(a, b)
}

object RationalOctonionAlgebras {

    internal val base =
        CayleyDickson.usual(RationalQuaternionAlgebras.RationalQuaternionDivisionRing)

    object RationalOctonionNonAssociativeInvolutiveRing:
        NonAssociativeInvolutiveRing<RationalOctonion>,
        HasNormSq<RationalOctonion, Rational> {

        override val add = base.add
        override val mul = base.mul
        override val conj = base.conj

        override fun fromBigInt(n: BigInteger): RationalOctonion =
            base.fromBigInt(n)

        override val normSq: UnaryOp<RationalOctonion, Rational> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { co -> mul(co, conj(co)).a.w }

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
    val CayleyToRationalOctonionRingHomomorphism: NonAssociativeRingMonomorphism<CayleyOctonion, RationalOctonion> =
    NonAssociativeRingMonomorphism.of(
        domain = CayleyOctonionNonAssociativeInvolutiveRing,
        codomain = RationalOctonionNonAssociativeInvolutiveRing,
        map = UnaryOp { co ->
            rationalOctonion(
                co.w.toRational(),co.x.toRational(),co.y.toRational(),co.z.toRational(),
                co.u.toRational(),co.v.toRational(),co.s.toRational(),co.t.toRational()
            )
        }
    )

    /**
     * RationalOctonion to Octonion ring monomorphism.
     */
    val RationalToOctonionRingMonomorphism: NonAssociativeRingMonomorphism<RationalOctonion, Octonion> =
        NonAssociativeRingMonomorphism.of(
            domain = RationalOctonionNonAssociativeInvolutiveRing,
            codomain = OctonionAlgebras.OctonionDivisionAlgebraReal,
            map = UnaryOp { ro ->
                octonion(
                    ro.w.toReal(),ro.x.toReal(),ro.y.toReal(),ro.z.toReal(),
                    ro.u.toReal(),ro.v.toReal(),ro.s.toReal(),ro.t.toReal()
                )
            }
        )

    val eqRationalOctonion: Eq<RationalOctonion> = Eq { o1, o2 -> o1 == o2 }
}
