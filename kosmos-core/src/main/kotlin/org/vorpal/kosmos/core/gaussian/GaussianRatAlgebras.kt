package org.vorpal.kosmos.core.gaussian

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.Complex
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras.RationalField
import org.vorpal.kosmos.algebra.structures.instances.complex
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

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

    val QtoGaussianRatMonomorphism: RingMonomorphism<Rational, GaussianRat> = RingMonomorphism.of(
        RationalField,
        GaussianRatField,
        UnaryOp { q -> GaussianRat(q, Rational.ZERO) }
    )

    val GaussianIntToRatMonomorphism: RingMonomorphism<GaussianInt, GaussianRat> = RingMonomorphism.of(
        GaussianIntAlgebras.GaussianIntCommutativeRing,
        GaussianRatField,
        UnaryOp { gi -> GaussianRat(
            Rational.of(gi.re, BigInteger.ONE),
            Rational.of(gi.im, BigInteger.ONE)
        ) }
    )

    val ZToGaussianRatMonomorphism: RingMonomorphism<BigInteger, GaussianRat> =
        GaussianIntAlgebras.ZToGaussianIntMonomorphism andThen GaussianIntToRatMonomorphism

    /**
     * This may not be a perfect monomorphism due to floating point imprecision of converting
     * Rational to Real when building Complex.
     */
    val GaussianRatToComplexMonomorphism: RingMonomorphism<GaussianRat, Complex> = RingMonomorphism.of(
        GaussianRatField,
        ComplexAlgebras.ComplexField,
        UnaryOp { gr -> complex(gr.re.toReal(), gr.im.toReal()) }
    )

    fun GaussianInt.toGaussianRat(): GaussianRat =
        GaussianIntToRatMonomorphism(this)

    val eqGaussianRat: Eq<GaussianRat> = Eq { gq1, gq2 -> gq1.re == gq2.re && gq1.im == gq2.im }
}
