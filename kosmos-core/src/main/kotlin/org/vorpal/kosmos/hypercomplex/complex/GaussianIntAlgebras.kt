package org.vorpal.kosmos.hypercomplex.complex

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.EuclidMeasure
import org.vorpal.kosmos.algebra.structures.EuclideanDomain
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.BinaryOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.complex.GaussianIntAlgebras.GaussianIntToRatMonomorphism
import org.vorpal.kosmos.hypercomplex.complex.GaussianRatAlgebras.GaussianRatField
import java.math.BigInteger

/**
 * Algebraic structures, morphisms, equality, and rendering support for [GaussianInt].
 */
object GaussianIntAlgebras {
    /**
     * The Gaussian integers `Z[i]`.
     *
     * This is a Euclidean domain with Euclidean measure
     *
     *     δ(a + bi) = a² + b².
     *
     * Division with remainder is performed by dividing in `Q(i)` and rounding each component
     * of the quotient to the nearest integer in `Z`. This yields a quotient `q` and remainder `r`
     * satisfying
     *
     *     a = qb + r
     *
     * and either `r = 0` or `δ(r) < δ(b)`.
     *
     * Since nearest-lattice rounding is not unique on half-ties, `divRem` is deterministic but
     * not canonical.
     */
    object GaussianIntEuclideanDomain :
        EuclideanDomain<GaussianInt, BigInteger>,
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
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { a ->
                a.re * a.re + a.im * a.im
            }

        override val measure: EuclidMeasure<GaussianInt, BigInteger> =
            EuclidMeasure.ofZ(normSq)

        /**
         * Euclidean division in `Z[i]`.
         *
         * For `b != 0`, write
         *
         *     a / b = (a * conjugate(b)) / N(b)
         *
         * in `Q(i)`, round each component to the nearest integer, and take that Gaussian integer
         * as the quotient.
         *
         * The rounding policy breaks half-ties away from zero.
         */
        override val divRem: BinaryOp<GaussianInt, GaussianInt, Pair<GaussianInt, GaussianInt>> =
            BinaryOp(Symbols.DIV_REM) { a, b ->
                require(b != zero) { "division by zero in Gaussian integers" }

                val denominator = normSq(b)
                val numerator = mul(a, conj(b))

                val qRe = roundDivNearest(numerator.re, denominator)
                val qIm = roundDivNearest(numerator.im, denominator)

                val q = GaussianInt(qRe, qIm)
                val r = a - q * b

                q to r
            }

        override fun fromBigInt(n: BigInteger): GaussianInt =
            GaussianInt(n, BigInteger.ZERO)

        /**
         * Round `p / q` to the nearest integer, assuming `q > 0`.
         *
         * Half-ties are rounded away from zero.
         */
        private fun roundDivNearest(p: BigInteger, q: BigInteger): BigInteger {
            require(q.signum() > 0) { "roundDivNearest requires q > 0" }

            val (base, rem) = p.divideAndRemainder(q)
            val doubledAbsRem = rem.abs().shiftLeft(1)

            return when {
                doubledAbsRem < q -> base
                p.signum() >= 0 -> base + BigInteger.ONE
                else -> base - BigInteger.ONE
            }
        }
    }

    /**
     * The natural `Z`-module structure on `Z[i]`.
     */
    object ZModuleGaussianInt : ZModule<GaussianInt> {
        override val scalars = IntegerAlgebras.IntegerCommutativeRing
        override val add = GaussianIntEuclideanDomain.add

        override val leftAction: LeftAction<BigInteger, GaussianInt> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { n, gi ->
                GaussianInt(n * gi.re, n * gi.im)
            }
    }

    /**
     * The canonical embedding `Z -> Z[i]`, given by `n ↦ n + 0i`.
     */
    object ZToGaussianIntMonomorphism : RingMonomorphism<BigInteger, GaussianInt> {
        override val domain = IntegerAlgebras.IntegerCommutativeRing
        override val codomain = GaussianIntEuclideanDomain

        override val map = UnaryOp<BigInteger, GaussianInt> { z ->
            GaussianInt(z, BigInteger.ZERO)
        }
    }

    /**
     * The canonical embedding `Z[i] -> Q(i)`, given by
     *
     *     a + bi ↦ a/1 + (b/1)i.
     */
    object GaussianIntToRatMonomorphism : RingMonomorphism<GaussianInt, GaussianRat> {
        override val domain = GaussianIntEuclideanDomain
        override val codomain = GaussianRatField

        override val map = UnaryOp<GaussianInt, GaussianRat> { (a, b) ->
            GaussianRat(a.toRational(), b.toRational())
        }
    }

    /**
     * The canonical embedding `Z[i] -> C`, given by
     *
     *     a + bi ↦ Complex(a, b).
     */
    object GaussianIntToComplexMonomorphism : RingMonomorphism<GaussianInt, Complex> {
        override val domain = GaussianIntEuclideanDomain
        override val codomain = ComplexAlgebras.ComplexField

        override val map = UnaryOp<GaussianInt, Complex> { (a, b) ->
            complex(a.toReal(), b.toReal())
        }
    }

    val eqGaussianInt: Eq<GaussianInt> = Eq.default()

    val printableGaussianInt: Printable<GaussianInt> =
        ComplexPrintable.complexLikePrintable(
            signed = IntegerAlgebras.SignedInteger,
            zero = IntegerAlgebras.IntegerCommutativeRing.zero,
            one = IntegerAlgebras.IntegerCommutativeRing.one,
            re = { it.re },
            im = { it.im },
            basis = Symbols.IMAGINARY_I,
            prA = IntegerAlgebras.printableInteger,
            eqA = IntegerAlgebras.eqInt
        )

    val printableGaussianIntPretty = printableGaussianInt
}

/**
 * Convert a Gaussian integer to the corresponding Gaussian rational.
 */
fun GaussianInt.toGaussianRat(): GaussianRat =
    GaussianIntToRatMonomorphism(this)
