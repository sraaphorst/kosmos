package org.vorpal.kosmos.core.gaussian

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.Complex
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.complex
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

object GaussianIntAlgebras {
    /**
     * Note that the Gaussian integers actually form an integral domain and
     * Euclidean domain with norm function `n(a + bi) = a^2 + b^2`, thus giving division
     * with remainder (up to rounding in C), hence gcd and unique factorization.
     */
    object GaussianIntCommutativeRing :
        CommutativeRing<GaussianInt>,
        InvolutiveRing<GaussianInt>,
        HasNormSq<GaussianInt, BigInteger> {

        override val zero: GaussianInt = GaussianInt.ZERO
        override val one: GaussianInt = GaussianInt.ONE

        override val add: AbelianGroup<GaussianInt> = AbelianGroup.of(
            identity = zero,
            op = BinOp(Symbols.PLUS, GaussianInt::plus),
            inverse = Endo(Symbols.MINUS, GaussianInt::unaryMinus)
        )

        override val mul: CommutativeMonoid<GaussianInt> = CommutativeMonoid.of(
            identity = one,
            op = BinOp(Symbols.ASTERISK, GaussianInt::times)
        )

        override val conj: Endo<GaussianInt> = Endo(Symbols.CONJ) { a ->
            GaussianInt(a.re, -a.im)
        }

        override val normSq: UnaryOp<GaussianInt, BigInteger> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) {
                it.re * it.re + it.im * it.im
            }

        override fun fromBigInt(n: BigInteger): GaussianInt =
            GaussianInt(n, BigInteger.ZERO)
    }

    object ZModuleGaussianInt: ZModule<GaussianInt> {
        override val scalars = IntegerAlgebras.ZCommutativeRing
        override val add = GaussianIntCommutativeRing.add
        override val leftAction: LeftAction<BigInteger, GaussianInt> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { n, gi -> GaussianInt(n * gi.re, n * gi.im) }
    }

    val ZToGaussianIntMonomorphism: RingMonomorphism<BigInteger, GaussianInt> = RingMonomorphism.of(
        IntegerAlgebras.ZCommutativeRing,
        GaussianIntCommutativeRing,
        UnaryOp { z -> GaussianInt(z, BigInteger.ZERO) }
    )

    val GaussianIntToComplexMonomorphism: RingMonomorphism<GaussianInt, Complex> = RingMonomorphism.of(
        GaussianIntCommutativeRing,
        ComplexAlgebras.ComplexField,
        UnaryOp { gi -> complex(gi.re.toReal(), gi.im.toReal()) }
    )

    val eqGaussianInt: Eq<GaussianInt> = Eq { gz1, gz2 -> gz1.re == gz2.re && gz1.im == gz2.im }
}
