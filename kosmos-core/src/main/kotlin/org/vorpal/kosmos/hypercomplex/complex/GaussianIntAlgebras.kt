package org.vorpal.kosmos.hypercomplex.complex

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

/**
 * [GaussianIntAlgebras] contains the algebraic structures over the [GaussianInt] type, as well as the
 * homomorphisms and [Eq] instances.
 *
 * These include:
 * - [GaussianIntCommutativeRing]: the Gaussian integers.
 * - [ZModuleGaussianInt]: the two-dimensional vector space of Gaussian integers over the integers.
 *
 * We have the following homomorphisms:
 * - [ZToGaussianIntMonomorphism]: from the integers to the Gaussian integers.
 * - [GaussianIntToComplexMonomorphism]: from the Gaussian integers to the complex numbers.
 *
 * We also have the following [Eq]s:
 * - [eqGaussianInt]: equality on Gaussian integers.
 */
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

    object ZToGaussianIntMonomorphism: RingMonomorphism<BigInteger, GaussianInt> {
        override val domain = IntegerAlgebras.ZCommutativeRing
        override val codomain = GaussianIntCommutativeRing
        override val map = UnaryOp<BigInteger, GaussianInt> { z -> GaussianInt(z, BigInteger.ZERO) }
    }

    object GaussianIntToComplexMonomorphism: RingMonomorphism<GaussianInt, Complex> {
        override val domain = GaussianIntCommutativeRing
        override val codomain = ComplexAlgebras.ComplexField
        override val map = UnaryOp<GaussianInt, Complex> { (a, b) -> complex(a.toReal(), b.toReal()) }
    }

    val eqGaussianInt: Eq<GaussianInt> = Eq { gz1, gz2 -> gz1.re == gz2.re && gz1.im == gz2.im }
}
