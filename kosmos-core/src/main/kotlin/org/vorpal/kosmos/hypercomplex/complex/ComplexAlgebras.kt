package org.vorpal.kosmos.hypercomplex.complex

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.quadratic.quadraticRank2MatrixEmbedding
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings
import org.vorpal.kosmos.hypercomplex.quaternion.Quaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger

/**
 * Main structures:
 * - [i]: the imaginary unit.
 * - [ComplexField]: the complex field.
 * - [ComplexStarAlgebra]: the complex star algebra with conjugation and norm.
 *
 * Convenience functions:
 * - [Complex.powInt]: compute `c^n` for a complex number `c` and a non-negative integer `n`.
 *
 * Vector spaces and modules:
 * - [ComplexRealVectorSpace]: the two-dimensional vector space of complex numbers over the real numbers.
 *
 * Homomorphisms:
 * - [complexToQuaternionEmbedding]: the unital embeddings from the complex numbers to the quaternions.
 * - [Complex.asQuaternion]: convenience extension for the canonical embedding.
 *
 * Eqs:
 * - [eqComplexStrict]: strict equality on complex numbers.
 * - [eqComplex]: approximate equality on complex numbers.
 *
 * Printables:
 * - [printableComplex]: a printable complex number.
 * - [printableComplexStrict]: a strict printable complex number.
 * - [printableComplexPretty]: a pretty printable complex number.
 */
object ComplexAlgebras {

    val i: Complex = complex(0.0, 1.0)

    private val base: NonAssociativeInvolutiveRing<Complex> =
        CayleyDickson.usual(RealAlgebras.RealStarField)

    object ComplexField : Field<Complex> {

        override val add = base.add

        override val mul: CommutativeMonoid<Complex> = CommutativeMonoid.of(
            identity = base.mul.identity,
            op = base.mul.op
        )

        override val reciprocal: Endo<Complex> = Endo(Symbols.INVERSE) { c ->
            val n2 = c.re * c.re + c.im * c.im
            require(RealAlgebras.eqRealApprox.neqv(n2, 0.0) && n2.isFinite()) {
                "Zero has no multiplicative inverse in ${Symbols.BB_C}."
            }
            complex(c.re / n2, -c.im / n2)
        }

        override fun fromBigInt(n: BigInteger): Complex =
            complex(n.toDouble(), 0.0)
    }

    private val action: LeftAction<Real, Complex> =
        LeftAction(Symbols.TRIANGLE_RIGHT) { r, (a, b) -> complex(r * a, r * b) }

    object ComplexStarAlgebra :
        Field<Complex> by ComplexField,
        InvolutiveRing<Complex>,
        RealNormedDivisionAlgebra<Complex>,
        StarAlgebra<Real, Complex> {

        override val zero: Complex = ComplexField.zero
        override val one: Complex = ComplexField.one

        override val scalars = RealAlgebras.RealField

        override val conj: Endo<Complex> =
            base.conj

        override val normSq: UnaryOp<Complex, Real> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { c -> c.re * c.re + c.im * c.im }

        override val leftAction: LeftAction<Real, Complex> =
            action
    }

    /**
     * Compute `c^n` for a complex number `c` and a non-negative integer `n`
     * using exponentiation by squaring.
     */
    fun Complex.powInt(n: Int): Complex {
        require(n >= 0) { "n must be >= 0" }
        val field = ComplexField

        tailrec fun go(base: Complex, exp: Int, acc: Complex): Complex = when {
            exp == 0         -> acc
            (exp and 1) == 1 -> go(field.mul(base, base), exp ushr 1, field.mul(acc, base))
            else             -> go(field.mul(base, base), exp ushr 1, acc)
        }

        return go(this, n, field.one)
    }

    object ComplexRealVectorSpace : FiniteVectorSpace<Real, Complex> {
        override val scalars = RealAlgebras.RealField
        override val add = ComplexField.add
        override val dimension = 2
        override val leftAction = action
    }

    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical

    /**
     * Return the ring monomorphism embedding ℂ into ℍ determined by [emb].
     *
     * Embed a complex number into ℍ using the subfield `ℝ ⊕ ℝ·i`.
     * Sends:
     * ```text
     * a + b i_C ↦ a·1 + b·u
     * ```
     * where `u ∈ {±i, ±j, ±k}` based on `emb.axis` and `emb.sign`.
     */
    fun complexToQuaternionEmbedding(
        emb: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<Complex, Quaternion> = RingMonomorphism.of(
        domain = ComplexField,
        codomain = QuaternionAlgebras.QuaternionDivisionRing,
        map = UnaryOp { (re, im) ->
            val s = emb.sign.factor.toDouble()
            when (emb.axis) {
                AxisSignEmbeddings.ImagAxis.I -> quaternion(re, s * im, 0.0, 0.0)
                AxisSignEmbeddings.ImagAxis.J -> quaternion(re, 0.0, s * im, 0.0)
                AxisSignEmbeddings.ImagAxis.K -> quaternion(re, 0.0, 0.0, s * im)
            }
        }
    )

    /**
     * Canonical monomorphism from the complex numbers into `M_2(ℝ)`, given by:
     * - `1 ↦ [[1, 0], [0, 1]]`
     * - `i ↦ [[0, -1], [1, 0]]`
     *
     * Equivalently,
     * ```text
     * a + bi ↦ [[a, -b], [b, a]]
     * ```
     */
    val ComplexToRank2RMatrixMonomorphism: RingMonomorphism<Complex, DenseMat<Real>> = quadraticRank2MatrixEmbedding(
        domain = ComplexField,
        coefficientRing = RealAlgebras.RealField,
        s = -1.0,
        t = 0.0,
        coeffs = { (re, im) -> re to im }
    )

    /**
     * Convenience extension.
     *
     * Canonical default is `i_C ↦ i`.
     */
    fun Complex.asQuaternion(
        emb: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): Quaternion = complexToQuaternionEmbedding(emb)(this)

    /** EQs **/
    val eqComplexStrict: Eq<Complex> = CD.eq(RealAlgebras.eqRealStrict)
    val eqComplex: Eq<Complex> = CD.eq(RealAlgebras.eqRealApprox)

    private fun printableComplexGenerator(
        prReal: Printable<Real>,
        eqReal: Eq<Real>,
    ): Printable<Complex> =
        ComplexPrintable.complexLikePrintable(
            signed = RealAlgebras.SignedReal,
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            re = { it.re },
            im = { it.im },
            basis = Symbols.IMAGINARY_I,
            prA = prReal,
            eqA = eqReal
        )

    val printableComplex: Printable<Complex> =
        printableComplexGenerator(
            prReal = RealAlgebras.printableReal,
            eqReal = RealAlgebras.eqRealApprox
        )

    val printableComplexStrict: Printable<Complex> =
        printableComplexGenerator(
            prReal = RealAlgebras.printableRealStrict,
            eqReal = RealAlgebras.eqRealStrict
        )

    val printableComplexPretty: Printable<Complex> =
        printableComplexGenerator(
            prReal = RealAlgebras.printableRealPretty,
            eqReal = RealAlgebras.eqRealApprox
        )
}
