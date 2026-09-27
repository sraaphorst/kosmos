package org.vorpal.kosmos.hypercomplex.complex

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.quadratic.quadraticRank2MatrixEmbedding
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasReciprocal
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
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger

/**
 * `ℂ = ℝ[i]/(i² + 1)`: the Cayley-Dickson doubling of ℝ (trivial involution) at `σ = 1`.
 *
 * This is the same construction, one level down, as the bicomplex numbers `𝔹 = ℂ[j]/(j² + 1)`.
 *
 * File-level rather than a member of [ComplexAlgebras] so that initializing
 * [ComplexAlgebras.ComplexField] never recursively initializes [ComplexAlgebras].
 */
private val cayleyDicksonComplex: CommutativeInvolutiveRing<Complex> =
    CayleyDickson.commutative(RealAlgebras.RealField, 1.0)

/**
 * The componentwise real scalar action `r ⊳ (a + bi) = ra + rb·i`.
 */
private val realScaling: LeftAction<Real, Complex> =
    LeftAction(Symbols.TRIANGLE_RIGHT) { r, (a, b) -> complex(r * a, r * b) }

/**
 * The squared modulus `|a + bi|² = a² + b²`.
 */
private val complexNormSq: UnaryOp<Complex, Real> =
    UnaryOp(Symbols.NORM_SQ_SYMBOL) { c -> c.re * c.re + c.im * c.im }

/**
 * The floating-point invertibility predicate of [HasReciprocal]: the squared modulus is finite and
 * not approximately zero.
 */
private fun isInvertibleComplex(c: Complex): Boolean {
    val n2 = complexNormSq(c)
    return n2.isFinite() && RealAlgebras.eqRealApprox.neqv(n2, 0.0)
}

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
 * - [ComplexToRank2RMatrixMonomorphism]: the regular representation `ℂ ↪ M₂(ℝ)`.
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

    /**
     * The complex field.
     *
     * Reciprocals follow the floating-point policy of [HasReciprocal]: `c` is invertible iff
     * `|c|²` is finite and not approximately zero, and [reciprocal] throws [ArithmeticException]
     * otherwise.
     */
    object ComplexField : Field<Complex> {

        override val add = cayleyDicksonComplex.add

        override val mul: CommutativeMonoid<Complex> = cayleyDicksonComplex.mul

        override fun hasReciprocal(a: Complex): Boolean =
            isInvertibleComplex(a)

        override val reciprocal: Endo<Complex> = Endo(Symbols.INVERSE) { c ->
            if (!hasReciprocal(c)) throw HasReciprocal.notInvertible(c, Symbols.BB_C)
            realScaling(1.0 / complexNormSq(c), cayleyDicksonComplex.conj(c))
        }

        override fun fromBigInt(n: BigInteger): Complex =
            complex(n.toDouble(), 0.0)
    }

    object ComplexStarAlgebra :
        Field<Complex> by ComplexField,
        InvolutiveRing<Complex>,
        RealNormedDivisionAlgebra<Complex>,
        StarAlgebra<Real, Complex> {

        override val zero: Complex = ComplexField.zero
        override val one: Complex = ComplexField.one

        override val scalars = RealAlgebras.RealField

        override val conj: Endo<Complex> =
            cayleyDicksonComplex.conj

        override val normSq: UnaryOp<Complex, Real> =
            complexNormSq

        override val leftAction: LeftAction<Real, Complex> =
            realScaling

        override fun hasReciprocal(a: Complex): Boolean =
            ComplexField.hasReciprocal(a)
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
        override val leftAction = realScaling
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
    ): RingMonomorphism<Complex, Quaternion> =
        QuaternionAlgebras.complexEmbedding(emb)

    /**
     * Canonical monomorphism from the complex numbers into `M₂(ℝ)`, given by:
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
        coeffs = UnaryOp { (re, im) -> re to im }
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
            re = UnaryOp { it.re },
            im = UnaryOp { it.im },
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
