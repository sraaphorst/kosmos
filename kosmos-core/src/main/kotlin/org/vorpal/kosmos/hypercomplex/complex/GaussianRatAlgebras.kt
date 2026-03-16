package org.vorpal.kosmos.hypercomplex.complex

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
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
import java.math.BigInteger

/**
 * [GaussianRatAlgebras] contains the algebraic structures over the [GaussianRat] type, as well as the
 * homomorphisms and [Eq] instances.
 *
 * These include:
 * - [GaussianRatField]: the Gaussian rationals.
 * - [GaussianRatStarAlgebra]: the Gaussian star algebra.
 * - [GaussianRatVectorSpace]: the two-dimensional vector space of Gaussian rationals over the rationals.
 *
 * We have the following homomorphisms:
 * - [QtoGaussianRatMonomorphism]: a ring homomorphism from the rational numbers to the Gaussian rationals.
 * - [ZToGaussianRatMonomorphism]: a ring homomorphism from the integers to the Gaussian rationals.
 * - [GaussianRatToComplexMonomorphism]: a ring homomorphism from the Gaussian rationals to the complex numbers.
 * - [GaussianInt.toGaussianRat]: convenience method for this monomorphism.
 *
 * We also have the following [Eq]s:
 * - [eqGaussianRat]: equality on Gaussian rationals.
 */
object GaussianRatAlgebras {
    /**
     * Note that the Gaussian rationals actually form a field with norm function
     * ```kotlin
     * N(a + bi) = a^2 + b^2
     * ```
     * thus giving division with remainder (up to rounding in ℂ), hence gcd and unique factorization.
     */
    object GaussianRatField :
        Field<GaussianRat>,
        InvolutiveRing<GaussianRat>,
        HasNormSq<GaussianRat, Rational>,
        HasReciprocal<GaussianRat> {

        override val zero: GaussianRat = GaussianRat.ZERO
        override val one: GaussianRat = GaussianRat.ONE

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

        /**
         * The reciprocal of the Gaussian rational `a/b + i c/d` is:
         * ```kotlin
         * abd^2 / (a^2 d^2 + c^2 b^2) + i b^2 c d / (a^2 d^2 + c^2 b^2).
         * ```
         */
        override val reciprocal: Endo<GaussianRat> = Endo(Symbols.INVERSE) { gq ->
            if (gq == zero) throw ArithmeticException("The reciprocal of $zero is undefined")
            val (a, b) = gq.re
            val (c, d) = gq.im
            val newDenom = a * a * d * d + b * b * c * c
            GaussianRat(Rational.of(a * b * d * d, newDenom), Rational.of(- b * b * c * d, newDenom))
        }

        override val conj: Endo<GaussianRat> = Endo(Symbols.CONJ) { a ->
            GaussianRat(a.re, -a.im)
        }

        override val normSq: UnaryOp<GaussianRat, Rational> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { gq ->
                gq.re * gq.re + gq.im * gq.im
            }
    }

    val GaussianRatStarAlgebra: StarAlgebra<Rational, GaussianRat> = StarAlgebra.of(
        scalars = RationalAlgebras.RationalField,
        involutiveRing = GaussianRatField,
        leftAction = GaussianRatVectorSpace.leftAction
    )

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

    object QtoGaussianRatMonomorphism: RingMonomorphism<Rational, GaussianRat> {
        override val domain = RationalAlgebras.RationalField
        override val codomain = GaussianRatField
        override val map = UnaryOp<Rational, GaussianRat> { q -> GaussianRat(q, Rational.ZERO) }
    }

    val ZToGaussianRatMonomorphism: RingMonomorphism<BigInteger, GaussianRat> =
        GaussianIntAlgebras.ZToGaussianIntMonomorphism andThen GaussianIntAlgebras.GaussianIntToRatMonomorphism

    /**
     * This may not be a perfect monomorphism due to floating point imprecision of converting
     * Rational to Real when building Complex.
     */
    object GaussianRatToComplexMonomorphism: RingMonomorphism<GaussianRat, Complex> {
        override val domain = GaussianRatField
        override val codomain = ComplexAlgebras.ComplexField
        override val map = UnaryOp<GaussianRat, Complex> { gq -> complex(gq.re.toReal(), gq.im.toReal()) }
    }

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
