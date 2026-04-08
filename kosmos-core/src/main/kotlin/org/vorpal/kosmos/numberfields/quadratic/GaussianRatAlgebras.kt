package org.vorpal.kosmos.numberfields.quadratic

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.quadratic.quadraticRank2MatrixEmbedding
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ComplexPrintable
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings
import org.vorpal.kosmos.hypercomplex.quaternion.Quaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionEmbeddingKit
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.rationalQuaternion
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger

/**
 * Main structures:
 * - [GaussianRatField]: the field Gaussian rationals.
 * - [GaussianRatStarAlgebra]: the Gaussian star algebra.
 *
 * Vector spaces and modules:
 * - [ZModuleGaussianRat]: the module of Gaussian rationals over the integers.
 * - [GaussianRatVectorSpace]: the two-dimensional vector space of Gaussian rationals over the rationals.
 *
 * Homomorphisms:
 * - [ZToGaussianRatMonomorphism]: monomorphism from the integers to the Gaussian rationals.
 * - [QToGaussianRatMonomorphism]: monomorphism from the rationals to the Gaussian rationals.
 * - [GaussianRatRank2QMatrixEmbedding]: a ring monomorphism from the Gaussian rational field to the rank 2 Q matrix ring.
 * - [GaussianRatToComplexHomomorphism]: a ring homomorphism from the Gaussian rational field to the complex field.
 *   Note that this could defensibly be a ring monomorphism, but due to floating point errors, we cannot guarantee
 *   injectivity.
 * - [gaussianRatToRationalQuaternionEmbedding]: unital embedding factory from the Gaussian rationals to the rational quaternions.
 * - [asRationalQuaternion]: convenience method for this monomorphism.
 * - [gaussianRatToQuaternionEmbedding]: unital embedding factory from the Gaussian rationals to the quaternions.
 *
 * Eqs:
 * - [eqGaussianRat]: equality on Gaussian rationals.
 *
 * Printables:
 * - [printableGaussianRat]: a printable Gaussian rational.
 * - [printableGaussianRatPretty]: a pretty printable Gaussian rational.
 */
object GaussianRatAlgebras {

    object GaussianRatField : Field<GaussianRat> {

        override val zero = GaussianRat.ZERO
        override val one = GaussianRat.ONE

        override val add: AbelianGroup<GaussianRat> = AbelianGroup.of(
            identity = zero,
            op = BinOp(Symbols.PLUS) { gq1, gq2 -> GaussianRat(gq1.re + gq2.re, gq1.im + gq2.im) },
            inverse = Endo(Symbols.MINUS) { gq -> GaussianRat(-gq.re, -gq.im) }
        )

        override val mul: CommutativeMonoid<GaussianRat> = CommutativeMonoid.of(
            identity = one,
            op = BinOp(Symbols.ASTERISK) { gq1, gq2 ->
                GaussianRat(gq1.re * gq2.re - gq1.im * gq2.im, gq1.re * gq2.im + gq1.im * gq2.re)
            }
        )

        override fun fromBigInt(n: BigInteger): GaussianRat =
            GaussianRat(n.toRational(), Rational.ZERO)

        override fun hasReciprocal(a: GaussianRat): Boolean =
            a != zero

        override val reciprocal: Endo<GaussianRat> = Endo(Symbols.INVERSE) { gq ->
            require(gq != zero) { "The reciprocal of $zero is undefined" }
            val n2 = gq.re * gq.re + gq.im * gq.im
            GaussianRat(gq.re / n2, -gq.im / n2)
        }

        internal val conj: Endo<GaussianRat> = Endo(Symbols.CONJ) { a ->
            GaussianRat(a.re, -a.im)
        }
    }

    object GaussianRatStarAlgebra:
        StarAlgebra<Rational, GaussianRat>,
        InvolutiveRing<GaussianRat>,
        HasNormSq<GaussianRat, Rational>,
        HasReciprocal<GaussianRat> {

        override val zero = GaussianRat.ZERO
        override val one = GaussianRat.ONE

        override val scalars = RationalAlgebras.RationalField
        override val add = GaussianRatField.add

        override val mul = GaussianRatField.mul

        override val conj = GaussianRatField.conj

        override val normSq: UnaryOp<GaussianRat, Rational> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { gq ->
                gq.re * gq.re + gq.im * gq.im
            }

        override val reciprocal = GaussianRatField.reciprocal

        override val leftAction: LeftAction<Rational, GaussianRat> = LeftAction(Symbols.TRIANGLE_RIGHT) { q, gq ->
            GaussianRat(q * gq.re, q * gq.im)
        }
    }

    object ZModuleGaussianRat: ZModule<GaussianRat> {
        override val scalars = IntegerAlgebras.IntegerCommutativeRing
        override val add = GaussianRatField.add
        override val leftAction: LeftAction<BigInteger, GaussianRat> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { n, gq ->
                val q = n.toRational()
                GaussianRat(q * gq.re, q * gq.im)
            }
    }

    object GaussianRatVectorSpace: FiniteVectorSpace<Rational, GaussianRat> {
        override val scalars = RationalAlgebras.RationalField
        override val add = GaussianRatField.add
        override val dimension = 2
        override val leftAction: LeftAction<Rational, GaussianRat> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { q, gq -> GaussianRat(q * gq.re, q * gq.im) }
    }

    val ZToGaussianRatMonomorphism: RingMonomorphism<BigInteger, GaussianRat> =
        GaussianIntAlgebras.ZToGaussianIntMonomorphism andThen GaussianIntAlgebras.GaussianIntToRatMonomorphism

    object QToGaussianRatMonomorphism: RingMonomorphism<Rational, GaussianRat> {
        override val domain = RationalAlgebras.RationalField
        override val codomain = GaussianRatField
        override val map = UnaryOp<Rational, GaussianRat> { q -> GaussianRat(q, Rational.ZERO) }
    }

    val GaussianRatRank2QMatrixEmbedding: RingMonomorphism<GaussianRat, DenseMat<Rational>> =
        quadraticRank2MatrixEmbedding(
            domain = GaussianRatField,
            coefficientRing = RationalAlgebras.RationalField,
            s = -Rational.ONE,
            t = Rational.ZERO,
            coeffs = { it.re to it.im }
        )

    /**
     * This may not be a perfect monomorphism due to floating point imprecision of converting
     * Rational to Real when building Complex.
     */
    object GaussianRatToComplexHomomorphism: RingHomomorphism<GaussianRat, Complex> {
        override val domain = GaussianRatField
        override val codomain = ComplexAlgebras.ComplexField
        override val map = UnaryOp<GaussianRat, Complex> { gq ->
            complex(gq.re.toReal(), gq.im.toReal())
        }
    }

    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical

    fun gaussianRatToRationalQuaternionEmbedding(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<GaussianRat, RationalQuaternion> = RingMonomorphism.of(
        domain = GaussianRatField,
        codomain = RationalQuaternionAlgebras.RationalQuaternionDivisionRing,
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

    /**
     * Convert a Gaussian rational to the corresponding rational quaternion.
     */
    fun GaussianRat.asRationalQuaternion(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RationalQuaternion = gaussianRatToRationalQuaternionEmbedding(embedding)(this)

    /**
     * This should theoretically be a ring monomorphism, but since we are mapping from rationals to floating point
     * quaternions and may lose precision, we use a ring homomorphism instead.
     */
    fun gaussianRatToQuaternionEmbedding(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingHomomorphism<GaussianRat, Quaternion> = gaussianRatToRationalQuaternionEmbedding(embedding) andThen
        RationalQuaternionAlgebras.RationalQuaternionToQuaternionMonomorphism

    val eqGaussianRat: Eq<GaussianRat> = Eq { gq1, gq2 -> gq1.re == gq2.re && gq1.im == gq2.im }

    val printableGaussianRat: Printable<GaussianRat> =
        ComplexPrintable.complexLikePrintable(
            signed = RationalAlgebras.SignedRational,
            zero = RationalAlgebras.RationalField.add.identity,
            one = RationalAlgebras.RationalField.mul.identity,
            re = { it.re },
            im = { it.im },
            basis = Symbols.IMAGINARY_I,
            prA = RationalAlgebras.printableRational,
            eqA = RationalAlgebras.eqRational
        )

    val printableGaussianRatPretty: Printable<GaussianRat> =
        printableGaussianRat
}
