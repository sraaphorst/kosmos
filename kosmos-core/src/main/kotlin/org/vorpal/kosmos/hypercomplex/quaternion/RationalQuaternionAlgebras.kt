package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.numberfields.quadratic.GaussianRat
import org.vorpal.kosmos.numberfields.quadratic.GaussianRatAlgebras
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.render.Printable
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
 * - `ℚ(i) ↪ ℍ_ℚ` lives with the Gaussian rationals: `GaussianRatAlgebras.gaussianRatToRationalQuaternionEmbedding`.
 * - [RationalQuaternionToQuaternionMonomorphism]: a ring homomorphism from [RationalQuaternion] to [Quaternion].
 *
 * The inclusions of the integral orders live with the orders themselves:
 * - [HurwitzQuaternionAlgebras.HurwitzToRationalQuaternionMonomorphism]: `𝓗 ↪ ℍ_ℚ`.
 * - [LipschitzQuaternionAlgebras.LipschitzToRationalQuaternionMonomorphism]: `ℤ⟨i, j, k⟩ ↪ ℍ_ℚ`.
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
            CayleyDickson.usual(GaussianRatAlgebras.GaussianRatStarAlgebra)

        override val zero = base.add.identity
        override val one = base.mul.identity

        override val add = base.add

        override val mul: Monoid<RationalQuaternion> = Monoid.of(
            identity = one,
            op = base.mul.op
        )

        override val reciprocal: Endo<RationalQuaternion> = Endo(Symbols.SLASH) { q ->
            val n2 = normSq(q)
            require(RationalAlgebras.eqRational.neqv(n2, Rational.ZERO)) {
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

        /**
         * The standard basis `1, i, j, k`, obtained by doubling the basis `1, i` of `ℚ(i)`.
         */
        val basis: List<RationalQuaternion> =
            CayleyDickson.doubledBasis(
                zero = GaussianRat.ZERO,
                baseBasis = listOf(GaussianRat.ONE, GaussianRat.I)
            )
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

    /**
     * This should theoretically be a ring monomorphism, but since we are mapping from rationals to floating point
     * quaternions and may lose precision, we use a ring homomorphism instead.
     */
    val RationalQuaternionToQuaternionMonomorphism: RingHomomorphism<RationalQuaternion, Quaternion> = RingHomomorphism.of(
        domain = RationalQuaternionDivisionRing,
        codomain = QuaternionAlgebras.QuaternionDivisionRing,
        map = UnaryOp { lq ->
            quaternion(lq.a.re.toReal(), lq.a.im.toReal(), lq.b.re.toReal(), lq.b.im.toReal())
        }
    )

    val eqRationalQuaternion: Eq<RationalQuaternion> = Eq.default()

    val printableRationalQuaternion: Printable<RationalQuaternion> =
        QuaternionPrintable.quaternionPrintable(
            signed = RationalAlgebras.SignedRational,
            zero = Rational.ZERO,
            one = Rational.ONE,
            prA = RationalAlgebras.printableRational,
            eqA = RationalAlgebras.eqRational,
            decompose = { q -> listOf(q.w, q.x, q.y, q.z) }
        )

    val printableRationalQuaternionPretty = printableRationalQuaternion
}
