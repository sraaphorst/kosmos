package org.vorpal.kosmos.core.gaussian

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

object GaussianIntAlgebras {
    /**
     * Note that the Gaussian integers actually form an integral domain and a
     * Euclidean domain with norm function n(a + bi) = a^2 + b^2, thus giving division
     * with remainder (up to rounding in C), hence gcd and unique factorization.
     */
    val GaussianIntCommutativeRing: CommutativeRing<GaussianInt> = CommutativeRing.of(
        add = AbelianGroup.of(
            identity = GaussianInt.ZERO,
            op = BinOp(Symbols.PLUS, GaussianInt::plus),
            inverse = Endo(Symbols.MINUS, GaussianInt::unaryMinus)
        ),
        mul = CommutativeMonoid.of(
            identity = GaussianInt.ONE,
            op = BinOp(Symbols.ASTERISK, GaussianInt::times)
        )
    )

    val GaussianIntNormSq: HasNormSq<GaussianInt, BigInteger> =
        object : HasNormSq<GaussianInt, BigInteger> {
            override val normSq: UnaryOp<GaussianInt, BigInteger> =
                UnaryOp(Symbols.NORM_SQ_SYMBOL) {
                    it.re * it.re + it.im * it.im
                }
        }

    object GaussianIntCommutativeRingWithNorm :
        CommutativeRing<GaussianInt> by GaussianIntCommutativeRing,
        HasNormSq<GaussianInt, BigInteger> by GaussianIntNormSq

    val ZToGaussianIntMonomorphism: RingMonomorphism<BigInteger, GaussianInt> = RingMonomorphism.of(
        IntegerAlgebras.ZCommutativeRing,
        GaussianIntCommutativeRing,
        UnaryOp { z -> GaussianInt(z, BigInteger.ZERO) }
    )
}
