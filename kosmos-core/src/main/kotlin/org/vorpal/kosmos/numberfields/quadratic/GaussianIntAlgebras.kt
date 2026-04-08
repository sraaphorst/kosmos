package org.vorpal.kosmos.numberfields.quadratic

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.quadratic.quadraticRank2MatrixEmbedding
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
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ComplexPrintable
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionEmbeddingKit
import org.vorpal.kosmos.hypercomplex.quaternion.HurwitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.Quaternion
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.lipschitzQuaternion
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger

/**
 * Main structures:
 * - [GaussianIntEuclideanDomain]: the Euclidean domain of Gaussian integers.
 *
 * Vector spaces and modules:
 * - [ZModuleGaussianInt]: The Gaussian integers as a ℤ-Module.
 *
 * Homomorphisms:
 * - [ZToGaussianIntMonomorphism]: embedding homomorphism from the integers to the Gaussian integers.
 * - [GaussianIntToRatMonomorphism]: embedding homomorphism from the Gaussian integers to the Gaussian rationals.
 * - [toGaussianRat]: convenience method for this monomorphism.
 * - [GaussianIntToComplexMonomorphism]: embedding homomorphism from the Gaussian integers to the [Complex] numbers.
 * - [gaussianIntToLipschitzQuaternionEmbedding]: unital embedding factory from the Gaussian integers to the Lipschitz quaternions.
 * - [asLipschitzQuaternion]: convenience method for this monomorphism.
 * - [gaussianIntToHurwitzQuaternionEmbedding]: unital embedding factory from the Gaussian integers to the Hurwitz quaternions.
 * - [gaussianIntToRationalQuaternionEmbedding]: unital embedding factory from the Gaussian integers to the rational quaternions.
 * - [gaussianIntToQuaternionEmbedding]: unital embedding factory from the Gaussian integers to the quaternions.
 * - [GaussianIntRank2ZMatrixEmbedding]: embedding homomorphism from the Gaussian integers to the rank 2 Z matrix.
 *
 * Eqs:
 * - [eqGaussianInt]: equality on Gaussian integers.
 *
 * Printables:
 * - [printableGaussianInt]: a printable Gaussian integer.
 * - [printableGaussianIntPretty]: a pretty printable Gaussian integer.
 */
object GaussianIntAlgebras {
    /**
     * The Gaussian integers `ℤ[i]`.
     *
     * This is a Euclidean domain with Euclidean measure
     * ```text
     * δ(a + bi) = a² + b².
     * ```
     * Division with remainder is performed by dividing in `ℚ(i)` and rounding each component
     * of the quotient to the nearest integer in `ℤ`. This yields a quotient `q` and remainder `r`
     * satisfying:
     * ```text
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
         * ```text
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

    val GaussianIntRank2ZMatrixEmbedding: RingMonomorphism<GaussianInt, DenseMat<BigInteger>> =
        quadraticRank2MatrixEmbedding(
            domain = GaussianIntEuclideanDomain,
            coefficientRing = IntegerAlgebras.IntegerCommutativeRing,
            s = -BigInteger.ONE,
            t = BigInteger.ZERO,
            coeffs = { it.re to it.im }
        )

    /**
     * The canonical embedding `ℤ[i] → ℚ(i)`, given by
     * ```text
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
     * Convert a Gaussian integer to the corresponding Gaussian rational.
     */
    fun GaussianInt.toGaussianRat(): GaussianRat =
        GaussianIntToRatMonomorphism(this)

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

    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical

    /**
     * Unital embedding factory from the Gaussian integers into the Lipschitz quaternions,
     * parameterized by the chosen quaternion axis/sign convention.
     */
    fun gaussianIntToLipschitzQuaternionEmbedding(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<GaussianInt, LipschitzQuaternion> = RingMonomorphism.of(
        domain = GaussianIntEuclideanDomain,
        codomain = LipschitzQuaternionAlgebras.LipschitzQuaternionRing,
        map = UnaryOp { z ->
            QuaternionEmbeddingKit.embedComplexLike(
                axisSign = embedding,
                re = z.re,
                im = z.im,
                zero = BigInteger.ZERO,
                negate = BigInteger::negate,
                mkQuaternion = ::lipschitzQuaternion
            )
        }
    )

    /**
     * Convert a Gaussian integer to the corresponding Lipschitz quaternion.
     */
    fun GaussianInt.asLipschitzQuaternion(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): LipschitzQuaternion = gaussianIntToLipschitzQuaternionEmbedding(embedding)(this)

    /**
     * Create the [GaussianInt] ↪ [LipschitzQuaternion] monomorphism according to the [embedding] and then
     * apply the `LipschitzToHurwitzQuaternionMonomorphism` to get a [RingMonomorphism]:
     * ```text
     * GaussianInt ↪ LipschitzQuaternion ↪ HurwitzQuaternion
     * ```
     */
    fun gaussianIntToHurwitzQuaternionEmbedding(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<GaussianInt, HurwitzQuaternion> =
        gaussianIntToLipschitzQuaternionEmbedding(embedding) andThen
            LipschitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism

    /**
     * Create the [GaussianInt] ↪ [LipschitzQuaternion] monomorphism according to the [embedding] and then
     * apply the `LipschitzToRationalQuaternionMonomorphism` to get a [RingMonomorphism]:
     * ```text
     * GaussianInt ↪ LipschitzQuaternion ↪ RationalQuaternion
     * ```
     */
    fun gaussianIntToRationalQuaternionEmbedding(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<GaussianInt, RationalQuaternion> =
        gaussianIntToLipschitzQuaternionEmbedding(embedding) andThen
            LipschitzQuaternionAlgebras.LipschitzToRationalQuaternionMonomorphism

    /**
     * Apply the [GaussianInt] ↪ [LipschitzQuaternion] monomorphism according to the [embedding] and then
     * apply the [LipschitzQuaternionAlgebras.LipschitzToQuaternionMonomorphism] to get a [RingMonomorphism]:
     * ```text
     * GaussianInt ↪ LipschitzQuaternion ↪ Quaternion
     * ```
     */
    fun gaussianIntToQuaternionEmbedding(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<GaussianInt, Quaternion> =
        gaussianIntToLipschitzQuaternionEmbedding(embedding) andThen LipschitzQuaternionAlgebras.LipschitzToQuaternionMonomorphism

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
            eqA = IntegerAlgebras.eqInteger
        )

    val printableGaussianIntPretty = printableGaussianInt
}

