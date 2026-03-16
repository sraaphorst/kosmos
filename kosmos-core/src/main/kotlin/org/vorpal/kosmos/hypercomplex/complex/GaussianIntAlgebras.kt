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
import java.math.BigInteger

/**
 * Algebraic structures, morphisms, equality, and rendering support for [GaussianInt].
 */
object GaussianIntAlgebras {
    /**
     * The Gaussian integers `ℤ[i]`.
     *
     * This is a Euclidean domain with Euclidean measure
     * ```kotlin
     * δ(a + bi) = a² + b².
     * ```
     * Division with remainder is performed by dividing in `ℚ(i)` and rounding each component
     * of the quotient to the nearest integer in `ℤ`. This yields a quotient `q` and remainder `r`
     * satisfying:
     * ```kotlin
     * a = qb + r, with r = 0 or δ(r) < δ(b).
     * ```
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
            op = BinOp(Symbols.PLUS) { gz1, gz2 -> GaussianInt(gz1.re + gz2.re, gz1.im + gz2.im) },
            inverse = Endo(Symbols.MINUS) { gz -> GaussianInt(-gz.re, -gz.im) }
        )

        override val mul: CommutativeMonoid<GaussianInt> = CommutativeMonoid.of(
            identity = one,
            op = BinOp(Symbols.ASTERISK) { gz1, gz2 ->
                GaussianInt(gz1.re * gz2.re - gz1.im * gz2.im, gz1.re * gz2.im + gz1.im * gz2.re)
            }
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
         * Euclidean division in `ℤ[i]`.
         *
         * For `b != 0`, write:
         * ```kotlin
         * a / b = (a * conjugate(b)) / N(b)
         * ```
         * in `ℚ(i)`, round each component to the nearest integer, and take that Gaussian integer
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
                val qb = mul(q, b)
                val r = add(a, add.inverse(qb))
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
     * The natural `ℤ`-module structure on `ℤ[i]`.
     */
    object ZModuleGaussianInt : ZModule<GaussianInt> {
        override val scalars = IntegerAlgebras.IntegerCommutativeRing
        override val add = GaussianIntEuclideanDomain.add

        override val leftAction: LeftAction<BigInteger, GaussianInt> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { n, gz ->
                GaussianInt(n * gz.re, n * gz.im)
            }
    }

    /**
     * The canonical embedding `ℤ → ℤ[i]`, given by `n ↦ n + 0i`.
     */
    object ZToGaussianIntMonomorphism : RingMonomorphism<BigInteger, GaussianInt> {
        override val domain = IntegerAlgebras.IntegerCommutativeRing
        override val codomain = GaussianIntEuclideanDomain

        override val map = UnaryOp<BigInteger, GaussianInt> { z ->
            GaussianInt(z, BigInteger.ZERO)
        }
    }

    /**
     * The canonical embedding `ℤ[i] → ℚ(i)`, given by
     * ```kotlin
     * a + bi ↦ a/1 + (b/1)i.
     * ```
     */
    object GaussianIntToRatMonomorphism : RingMonomorphism<GaussianInt, GaussianRat> {
        override val domain = GaussianIntEuclideanDomain
        override val codomain = GaussianRatAlgebras.GaussianRatField

        override val map = UnaryOp<GaussianInt, GaussianRat> { gz ->
            GaussianRat(gz.re.toRational(), gz.im.toRational())
        }
    }

    /**
     * The canonical monomorphism `ℤ[i] → ℂ`.
     */
    object GaussianIntToComplexMonomorphism : RingMonomorphism<GaussianInt, Complex> {
        override val domain = GaussianIntEuclideanDomain
        override val codomain = ComplexAlgebras.ComplexField

        override val map = UnaryOp<GaussianInt, Complex> { gz ->
            complex(gz.re.toReal(), gz.im.toReal())
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
    GaussianIntAlgebras.GaussianIntToRatMonomorphism(this)
