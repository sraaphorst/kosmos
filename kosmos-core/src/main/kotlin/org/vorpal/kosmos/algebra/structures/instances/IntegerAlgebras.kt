package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable
import org.vorpal.kosmos.core.render.Printable
import java.math.BigInteger

object IntegerAlgebras {
    val F2: PrimeField = PrimeField(BigInteger.TWO)

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

    val eqInt: Eq<BigInteger> = Eq.default()

    object SignedInteger : LinearCombinationPrintable.SignedOps<BigInteger> {
        override fun isNeg(x: BigInteger): Boolean = x.signum() < 0
        override fun abs(x: BigInteger): BigInteger = x.abs()
    }

    val printableInteger: Printable<BigInteger> = Printable.default()
}
