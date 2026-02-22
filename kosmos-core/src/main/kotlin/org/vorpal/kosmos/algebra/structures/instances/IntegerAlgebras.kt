package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

object IntegerAlgebras {
    val F2: PrimeField = PrimeField(BigInteger.TWO)

    object ZCommutativeRing:
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
}
