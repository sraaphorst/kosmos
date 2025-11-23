package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

object IntegerAlgebras {
    val Z2AdditiveAbelianGroup : AbelianGroup<Int> = AbelianGroup.of(
        identity = 0,
        op = BinOp(Symbols.PLUS) { a, b -> (a + b) % 2 },
        inverse = Endo(Symbols.MINUS, Identity())
    )

    val Z2MultiplicativeCommutativeMonoid: CommutativeMonoid<Int> = CommutativeMonoid.of(
        identity = 1,
        op = BinOp(Symbols.ASTERISK) { a, b -> (a * b) % 2 }
    )

    val F2: PrimeField = PrimeField(BigInteger.TWO)

    val ZAdditiveAbelianGroup: AbelianGroup<BigInteger> = AbelianGroup.of(
        identity = BigInteger.ZERO,
        op = BinOp(Symbols.PLUS, BigInteger::plus),
        inverse = Endo(Symbols.MINUS) { -it }
    )

    val ZMultiplicativeCommutativeMonoid: CommutativeMonoid<BigInteger> = CommutativeMonoid.of(
        identity = BigInteger.ONE,
        op = BinOp(Symbols.ASTERISK, BigInteger::multiply)
    )

    object ZCommutativeRing: CommutativeRing<BigInteger> {
        override val add = ZAdditiveAbelianGroup
        override val mul = ZMultiplicativeCommutativeMonoid
        override fun fromBigInt(n: BigInteger): BigInteger = n
    }
}
