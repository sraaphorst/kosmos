package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.core.render.LinearCombinationPrintable
import org.vorpal.kosmos.core.render.Printable
import java.math.BigInteger

object RationalAlgebras {

    object RationalField:
        Field<Rational> {

        override val add = AbelianGroup.of(
            identity = Rational.ZERO,
            op = BinOp(Symbols.PLUS) { a, b -> a + b },
            inverse = Endo(Symbols.MINUS) { -it }
        )
        override val mul = CommutativeMonoid.of(
            identity = Rational.ONE,
            op = BinOp(Symbols.ASTERISK) { a, b -> a * b }
        )
        override val reciprocal: Endo<Rational> = Endo(Symbols.INVERSE) { r ->
            require(r != Rational.ZERO) { "0 has no reciprocal." }
            r.reciprocal()
        }
        override fun fromBigInt(n: BigInteger): Rational =
            Rational.of(n, BigInteger.ONE)
    }

    object RationalStarField :
        Field<Rational> by RationalField,
        InvolutiveRing<Rational>,
        NormedDivisionAlgebra<Rational, Rational> {

        override val zero: Rational = Rational.ZERO
        override val one: Rational = Rational.ONE

        override val conj: Endo<Rational> =
            Endo(Symbols.CONJ, Identity())

        override val normSq: UnaryOp<Rational, Rational> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { a -> a * a }
    }

    val ZToQMonomorphism: RingMonomorphism<BigInteger, Rational> = RingMonomorphism.of(
        IntegerAlgebras.IntegerCommutativeRing,
        RationalField,
        UnaryOp { z -> z.toRational() }
    )

    val eqRational: Eq<Rational> = Eq.default()

    object SignedRational : LinearCombinationPrintable.SignedOps<Rational> {
        override fun isNeg(x: Rational): Boolean = x.signum < 0
        override fun abs(x: Rational): Rational = x.abs()
    }

    // Rational.toString() does exactly what we want.
    val printableRational: Printable<Rational> = Printable.default()

    val printableRationalPretty = printableRational
}
