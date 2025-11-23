package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import java.math.BigInteger

object NaturalAlgebras {
    val NAdditiveCommutativeMonoid: CommutativeMonoid<BigInteger> = CommutativeMonoid.of(
        identity = BigInteger.ZERO,
        op = BinOp(Symbols.PLUS, BigInteger::plus),
    )

    val NMultiplicativeCommutativeMonoid: CommutativeMonoid<BigInteger> = CommutativeMonoid.of(
        identity = BigInteger.ONE,
        op = BinOp(Symbols.ASTERISK, BigInteger::multiply)
    )

    val NCommutativeSemiring: CommutativeSemiring<BigInteger> = CommutativeSemiring.of(
        add = NAdditiveCommutativeMonoid,
        mul = NAdditiveCommutativeMonoid
    )
}
