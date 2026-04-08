package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
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

/**
 * Main structures:
 * - [IntegerCommutativeRing]: the integers.
 *
 * Homomorphisms:
 * - [ZToQMonomorphism]: from integers to rationals.
 * - [ZToRHomomorphism]: from integers to reals. The abstract map `ℤ → ℝ` is a monomorphism,
 *   but this implementation uses Real (represented by [Double]), so it is not injective for
 *   sufficiently large integers, and is therefore only a ring homomorphism.
 *
 * Eqs:
 * - [eqInteger]
 *
 * Printables:
 * - [printableInteger]
 */
object IntegerAlgebras {

    object IntegerCommutativeRing:
        CommutativeRing<BigInteger>,
        InvolutiveRing<BigInteger>,
        HasNormSq<BigInteger, BigInteger> {

        override val zero: BigInteger = BigInteger.ZERO
        override val one: BigInteger = BigInteger.ONE

        override val add: AbelianGroup<BigInteger> = AbelianGroup.of(
            identity = zero,
            op = BinOp(Symbols.PLUS, BigInteger::plus),
            inverse = Endo(Symbols.MINUS, BigInteger::unaryMinus)
        )
        override val mul: CommutativeMonoid<BigInteger> = CommutativeMonoid.of(
            identity = one,
            op = BinOp(Symbols.ASTERISK, BigInteger::multiply)
        )

        override val conj: Endo<BigInteger> =
            Endo(Symbols.CONJ, Identity())

        override val normSq: UnaryOp<BigInteger, BigInteger> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { a -> a * a }

        override fun fromBigInt(n: BigInteger): BigInteger = n
    }

    object SignedInteger : LinearCombinationPrintable.SignedOps<BigInteger> {
        override fun isNeg(x: BigInteger): Boolean = x.signum() < 0
        override fun abs(x: BigInteger): BigInteger = x.abs()
    }

    val ZToQMonomorphism: RingMonomorphism<BigInteger, Rational> = RingMonomorphism.of(
        IntegerCommutativeRing,
        RationalAlgebras.RationalField,
        UnaryOp(transform = BigInteger::toRational)
    )

    val ZToRHomomorphism: RingHomomorphism<BigInteger, Double> = RingHomomorphism.of(
        IntegerCommutativeRing,
        RealAlgebras.RealField,
        UnaryOp(transform = BigInteger::toDouble)
    )

    val eqInteger: Eq<BigInteger> = Eq.default()

    val printableInteger: Printable<BigInteger> = Printable.default()
}
