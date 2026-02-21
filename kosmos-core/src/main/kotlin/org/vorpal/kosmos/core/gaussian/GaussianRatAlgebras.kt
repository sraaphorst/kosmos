package org.vorpal.kosmos.core.gaussian

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.instances.Complex
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras
import org.vorpal.kosmos.algebra.structures.instances.complex
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

object GaussianRatAlgebras {
    val GaussianRatField: Field<GaussianRat> = Field.of(
        add = AbelianGroup.of(
            identity = GaussianRat.ZERO,
            op = BinOp(Symbols.PLUS, GaussianRat::plus),
            inverse = Endo(Symbols.MINUS, GaussianRat::unaryMinus)
        ),
        mul = CommutativeMonoid.of(
            identity = GaussianRat.ONE,
            op = BinOp(Symbols.ASTERISK, GaussianRat::times)
        ),
        reciprocal = Endo(Symbols.INVERSE, GaussianRat::reciprocal)
    )

    val GaussianRatNormSq: HasNormSq<GaussianRat, Rational> =
        object : HasNormSq<GaussianRat, Rational> {
            override val normSq: UnaryOp<GaussianRat, Rational> =
                UnaryOp(Symbols.NORM_SQ_SYMBOL) {
                    it.re * it.re + it.im * it.im
                }
        }

    object GaussianRatFieldWithNormSq :
        Field<GaussianRat> by GaussianRatField,
        HasNormSq<GaussianRat, Rational> by GaussianRatNormSq

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
}
