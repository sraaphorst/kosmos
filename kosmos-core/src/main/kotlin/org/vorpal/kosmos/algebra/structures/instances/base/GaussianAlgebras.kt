package org.vorpal.kosmos.algebra.structures.instances.base

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.base.RationalAlgebras.RationalField
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.gaussian.GaussianInt
import org.vorpal.kosmos.core.gaussian.GaussianRat
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import java.math.BigInteger

object GaussianAlgebras {

    /**
     * The commutative, involutive ring with a norm over the [GaussianInt].
     */
    object GaussianIntRing:
        CommutativeRing<GaussianInt>,
        InvolutiveRing<GaussianInt>,
        HasNormSq<GaussianInt, BigInteger> {

        override val zero: GaussianInt = GaussianInt(BigInteger.ZERO, BigInteger.ZERO)
        override val one: GaussianInt = GaussianInt(BigInteger.ONE, BigInteger.ZERO)

        override val add: AbelianGroup<GaussianInt> = AbelianGroup.of(
            zero,
            BinOp(Symbols.PLUS) { a, b -> GaussianInt(a.re + b.re, a.im + b.im )},
            Endo(Symbols.MINUS) { a -> GaussianInt(-a.re, -a.im)}
        )

        // (a, b) * (c, d) = (ac - bd, ad + bc)
        override val mul: CommutativeMonoid<GaussianInt> = CommutativeMonoid.of(
            one,
            BinOp(Symbols.ASTERISK) { a, b ->
                GaussianInt(a.re * b.re - a.im * b.im, a.re * b.im + a.im * b.re)
            }
        )

        override val conj: Endo<GaussianInt> = Endo(Symbols.CONJ) { a ->
            GaussianInt(a.re, -a.im)
        }

        override val normSq: UnaryOp<GaussianInt, BigInteger> = UnaryOp(Symbols.NORM_SQ_SYMBOL) { a -> a.re * a.re + a.im * a.im }
    }

    /**
     * The involutive field over with a norm over the [GaussianRat].
     */
    object GaussianRatField:
        Field<GaussianRat>,
        InvolutiveRing<GaussianRat>,
        NormedDivisionAlgebra<Rational, GaussianRat> {

        override val zero = QtoGaussianRatMonomorphism(Rational.ZERO)
        override val one = QtoGaussianRatMonomorphism(Rational.ONE)

        override val add: AbelianGroup<GaussianRat> = AbelianGroup.of(
            zero,
            BinOp(Symbols.PLUS) { gq1, gq2 -> GaussianRat(gq1.re + gq2.re, gq1.im + gq2.im) },
            Endo(Symbols.MINUS) { gq -> GaussianRat(-gq.re, -gq.im) }
        )

        override val mul: CommutativeMonoid<GaussianRat> = CommutativeMonoid.of(
            one,
            BinOp(Symbols.ASTERISK) { gq1, gq2 ->
                GaussianRat(gq1.re * gq2.re - gq1.im * gq2.im, gq1.re * gq2.im + gq1.im * gq2.re)
            }
        )

        override val conj: Endo<GaussianRat> = Endo(Symbols.CONJ) { gq -> GaussianRat(gq.re, -gq.im) }

        override val normSq: UnaryOp<GaussianRat, Rational> = UnaryOp(Symbols.NORM_SQ_SYMBOL) { gq ->
            gq.re * gq.re + gq.im * gq.im
        }

        override val reciprocal: Endo<GaussianRat> = Endo(Symbols.INVERSE) { gq ->
            val n2 = normSq(gq)
            require(eqRational.neqv(n2, Rational.ZERO)) {
                "Zero has no multiplicative inverse in ${Symbols.BB_Q}(i)."
            }
            GaussianRat(gq.re / n2, -gq.im / n2)
        }
    }

    val GaussianRatVectorSpace: FiniteVectorSpace<Rational, GaussianRat> = FiniteVectorSpace.of(
        scalars = RationalField,
        add = GaussianRatField.add,
        dimension = 2,
        leftAction = LeftAction { q, (a, b) -> GaussianRat(q * a, q * b) }
    )

    val GaussianRatStarAlgebra: StarAlgebra<Rational, GaussianRat> = StarAlgebra.of(
        scalars = RationalField,
        involutiveRing = GaussianRatField,
        leftAction = GaussianRatVectorSpace.leftAction
    )

    /**
     * A Ring monomorphism from the Integers to the Gaussian Integers.
     */
    val ZtoGaussianIntMonomorphism: RingMonomorphism<BigInteger, GaussianInt> =
        RingMonomorphism.of(
            IntegerAlgebras.ZCommutativeRing,
            GaussianIntRing,
            UnaryOp { z -> GaussianInt(z, BigInteger.ZERO) }
        )

    /**
     * Ring monomorphism from the rationals to the Gaussian rationals.
     */
    val QtoGaussianRatMonomorphism: RingMonomorphism<Rational, GaussianRat> = RingMonomorphism.of(
        RationalAlgebras.RationalField,
        GaussianRatField,
        UnaryOp { q -> GaussianRat(q, Rational.ZERO) }
    )

    /**
     * Ring monomorphism from the Gaussian integers to the Gaussian rationals.
     */
    val GaussianIntToGaussianRatMonomorphism: RingMonomorphism<GaussianInt, GaussianRat> = RingMonomorphism.of(
        GaussianIntRing,
        GaussianRatField,
        UnaryOp { gz -> GaussianRat(gz.re.toRational(), gz.im.toRational()) }
    )

    /**
     * Ring monomorphism from the IntRing to the Gaussian rationals through the Gaussian integers.
     */
    val ZtoGaussianRatMonomorphism: RingMonomorphism<BigInteger, GaussianRat> =
        ZtoGaussianIntMonomorphism andThen GaussianIntToGaussianRatMonomorphism

    val GaussianRatToComplex: RingMonomorphism<GaussianRat, Complex> = RingMonomorphism.of(
        GaussianRatField,
        ComplexAlgebras.ComplexField,
        UnaryOp { g -> Complex(g.re.toReal(), g.im.toReal()) }
    )

    val GaussianIntToComplex: RingMonomorphism<GaussianInt, Complex> =
        GaussianIntToGaussianRatMonomorphism andThen GaussianRatToComplex

    val eqGaussianInt: Eq<GaussianInt> = Eq { gz1, gz2 -> gz1.re == gz2.re && gz1.im == gz2.im }
    val eqGaussianRat: Eq<GaussianRat> = Eq { gq1, gq2 -> gq1.re == gq2.re && gq1.im == gq2.im }
}
