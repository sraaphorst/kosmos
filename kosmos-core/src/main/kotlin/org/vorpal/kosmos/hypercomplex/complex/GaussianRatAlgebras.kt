package org.vorpal.kosmos.hypercomplex.complex

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras.RationalField
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
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
 * - [GaussianIntToRatMonomorphism]: a ring homomorphism from the Gaussian integers to the Gaussian rationals.
 * - [ZToGaussianRatMonomorphism]: a ring homomorphism from the integers to the Gaussian rationals.
 * - [GaussianRatToComplexMonomorphism]: a ring homomorphism from the Gaussian rationals to the complex numbers.
 * - [GaussianInt.toGaussianRat]: convenience method for this monomorphism.
 *
 * We also have the following [Eq]s:
 * - [eqGaussianRat]: equality on Gaussian rationals.
 */
object GaussianRatAlgebras {
    /**
     * Note that the Gaussian rationals actually form an integral domain and
     * Euclidean domain with norm function `n(a + bi) = a^2 + b^2`, thus giving division
     * with remainder (up to rounding in C), hence gcd and unique factorization.
     */
    object GaussianRatField :
        Field<GaussianRat>,
        InvolutiveRing<GaussianRat>,
        HasNormSq<GaussianRat, Rational> {

        override val zero: GaussianRat = GaussianRat.ZERO
        override val one: GaussianRat = GaussianRat.ONE

        override val add: AbelianGroup<GaussianRat> = AbelianGroup.of(
            identity = zero,
            op = BinOp(Symbols.PLUS, GaussianRat::plus),
            inverse = Endo(Symbols.MINUS, GaussianRat::unaryMinus)
        )

        override val mul: CommutativeMonoid<GaussianRat> = CommutativeMonoid.of(
            identity = GaussianRat.ONE,
            op = BinOp(Symbols.ASTERISK, GaussianRat::times)
        )

        override val reciprocal: Endo<GaussianRat> =
            Endo(Symbols.INVERSE, GaussianRat::reciprocal)

        override val conj: Endo<GaussianRat> = Endo(Symbols.CONJ) { a ->
            GaussianRat(a.re, -a.im)
        }
        override val normSq: UnaryOp<GaussianRat, Rational> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) {
                it.re * it.re + it.im * it.im
            }
    }

    val GaussianRatStarAlgebra: StarAlgebra<Rational, GaussianRat> = StarAlgebra.of(
        scalars = RationalField,
        involutiveRing = GaussianRatField,
        leftAction = GaussianRatVectorSpace.leftAction
    )

    object GaussianRatVectorSpace: FiniteVectorSpace<Rational, GaussianRat> {
        override val scalars = RationalField
        override val add = GaussianRatField.add
        override val dimension = 2
        override val leftAction: LeftAction<Rational, GaussianRat> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { q, (a, b) -> GaussianRat(q * a, q * b) }
    }

    object QtoGaussianRatMonomorphism: RingMonomorphism<Rational, GaussianRat> {
        override val domain = RationalField
        override val codomain = GaussianRatField
        override val map = UnaryOp<Rational, GaussianRat> { q -> GaussianRat(q, Rational.ZERO) }
    }

    object GaussianIntToRatMonomorphism: RingMonomorphism<GaussianInt, GaussianRat> {
        override val domain = GaussianIntAlgebras.GaussianIntCommutativeRing
        override val codomain = GaussianRatField
        override val map = UnaryOp<GaussianInt, GaussianRat> { (a, b) ->
            GaussianRat(a.toRational(), b.toRational())
        }
    }

    val ZToGaussianRatMonomorphism: RingMonomorphism<BigInteger, GaussianRat> =
        GaussianIntAlgebras.ZToGaussianIntMonomorphism andThen GaussianIntToRatMonomorphism

    /**
     * This may not be a perfect monomorphism due to floating point imprecision of converting
     * Rational to Real when building Complex.
     */
    object GaussianRatToComplexMonomorphism: RingMonomorphism<GaussianRat, Complex> {
        override val domain = GaussianRatField
        override val codomain = ComplexAlgebras.ComplexField
        override val map = UnaryOp<GaussianRat, Complex> { (a, b) -> complex(a.toReal(), b.toReal()) }
    }

    fun GaussianInt.toGaussianRat(): GaussianRat =
        GaussianIntToRatMonomorphism(this)

    val eqGaussianRat: Eq<GaussianRat> = Eq { gq1, gq2 -> gq1.re == gq2.re && gq1.im == gq2.im }
}
