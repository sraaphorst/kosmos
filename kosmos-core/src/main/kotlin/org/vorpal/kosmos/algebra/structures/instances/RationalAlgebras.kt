package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.times
import org.vorpal.kosmos.core.render.LinearCombinationPrintable
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.numberfields.quadratic.GaussianRat
import org.vorpal.kosmos.numberfields.quadratic.GaussianRatAlgebras
import java.math.BigInteger

/**
 * Main structures:
 * - [RationalField]: the rational numbers.
 * - [RationalStarField]: the rational numbers equipped with the trivial involution.
 *
 * Vector spaces and modules:
 * - [ZModuleRational]: the rationals as a module over the integers.
 *
 * Homomorphisms:
 * - [QToRHomomorphism]: a ring homomorphism from rationals to reals: mathematically, this would be injective, but
 *   given the limitations of the [Real] type, it is not.
 * - [QToGaussianRatMonomorphism]: from rationals to Gaussian rationals.
 *
 * Eqs:
 * - [eqRational]
 *
 * Printables:
 * - [printableRational]
 * - [printableRationalPretty]
 */
object RationalAlgebras {

    object RationalField:
        Field<Rational> {

        override val add = AbelianGroup.of(
            identity = Rational.ZERO,
            op = BinOp(Symbols.PLUS, Rational::plus),
            inverse = Endo(Symbols.MINUS, Rational::unaryMinus)
        )
        override val mul = CommutativeMonoid.of(
            identity = Rational.ONE,
            op = BinOp(Symbols.ASTERISK, Rational::times)
        )
        // We already check for 0 in the reciprocal function.
        override val reciprocal: Endo<Rational> =
            Endo(Symbols.INVERSE, Rational::reciprocal)
        override fun fromBigInt(n: BigInteger): Rational =
            Rational.of(n)
    }

    /**
     * The field of rational numbers equipped with the trivial involution,
     * viewed in particular as a star algebra over itself.
     */
    object RationalStarField :
        Field<Rational> by RationalField,
        InvolutiveRing<Rational>,
        NormedDivisionAlgebra<Rational, Rational>,
        StarAlgebra<Rational, Rational> {

        override val zero: Rational = Rational.ZERO
        override val one: Rational = Rational.ONE

        override val scalars: CommutativeRing<Rational> =
            RationalField

        override val conj: Endo<Rational> =
            Endo(Symbols.CONJ, Identity())

        override val normSq: UnaryOp<Rational, Rational> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { a -> a * a }

        override val leftAction: LeftAction<Rational, Rational> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { r, a -> r * a }
    }

    object ZModuleRational: ZModule<Rational> {
        override val scalars = IntegerAlgebras.IntegerCommutativeRing
        override val add = RationalField.add
        override val leftAction: LeftAction<BigInteger, Rational> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { s, r -> s * r }
    }

    object QToRHomomorphism: RingHomomorphism<Rational, Real> {
        override val domain = RationalField
        override val codomain = RealAlgebras.RealField
        override val map = UnaryOp<Rational, Real> { it.toReal() }
    }

    object QToGaussianRatMonomorphism: RingMonomorphism<Rational, GaussianRat> {
        override val domain = RationalField
        override val codomain = GaussianRatAlgebras.GaussianRatField
        override val map = UnaryOp<Rational, GaussianRat> {
            GaussianRat(it, Rational.ZERO)
        }
    }

    val eqRational: Eq<Rational> = Eq.default()

    object SignedRational : LinearCombinationPrintable.SignedOps<Rational> {
        override fun isNeg(x: Rational): Boolean = x.signum < 0
        override fun abs(x: Rational): Rational = x.abs()
    }

    val printableRational: Printable<Rational> = Printable.default()
    val printableRationalPretty = printableRational
}
