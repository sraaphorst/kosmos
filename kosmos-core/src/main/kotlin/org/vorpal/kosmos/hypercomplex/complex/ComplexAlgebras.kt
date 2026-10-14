package org.vorpal.kosmos.hypercomplex.complex

import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.quadratic.quadraticRank2MatrixEmbedding
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CarrierTransport
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.pow
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.contramap
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings
import org.vorpal.kosmos.hypercomplex.quaternion.RealQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger

/**
 * `R[i]/(i² + 1)`: the Cayley-Dickson doubling of [base] (trivial involution) at `σ = 1`,
 * transported to the [Complex] carrier. Its involution is complex conjugation `a + bi ↦ a - bi`.
 *
 * File-level so that the named rings (ℂ, `ℚ(i)`, `ℤ[i]`) can be built from it without initializing
 * [ComplexAlgebras].
 */
internal fun <R : Any> complexRingOver(
    base: CommutativeRing<R>
): CommutativeInvolutiveRing<Complex<R>> =
    CarrierTransport<CD<R>, Complex<R>>(
        wrap = UnaryOp { cd -> Complex(cd) },
        unwrap = UnaryOp { z -> z.cd }
    ).commutativeInvolutiveRing(
        CayleyDickson.commutative(base, base.one)
    )

/**
 * `ℂ = ℝ[i]/(i² + 1)`.
 *
 * This is the same construction, one level down, as the bicomplex numbers `𝔹 = ℂ[j]/(j² + 1)`.
 *
 * File-level rather than a member of [ComplexAlgebras] so that initializing
 * [ComplexAlgebras.ComplexField] never recursively initializes [ComplexAlgebras].
 */
private val cayleyDicksonComplex: CommutativeInvolutiveRing<RealComplex> =
    complexRingOver(RealAlgebras.RealField)

/**
 * The componentwise real scalar action `r ⊳ (a + bi) = ra + rb·i`.
 */
private val realScaling: LeftAction<Real, RealComplex> =
    LeftAction(Symbols.TRIANGLE_RIGHT) { r, (a, b) -> complex(r * a, r * b) }

/**
 * The squared modulus `|a + bi|² = a² + b²`.
 */
private val complexNormSq: UnaryOp<RealComplex, Real> =
    UnaryOp(Symbols.NORM_SQ_SYMBOL) { c -> c.re * c.re + c.im * c.im }

/**
 * The floating-point invertibility predicate of [HasReciprocal]: the squared modulus is finite and
 * not approximately zero.
 */
private fun isInvertibleComplex(c: RealComplex): Boolean {
    val n2 = complexNormSq(c)
    return n2.isFinite() && RealAlgebras.eqRealApprox.neqv(n2, 0.0)
}

/**
 * Generic constructions, over any commutative base ring `R`:
 * - [complexRing]: the ring `R[i]/(i² + 1)` with complex conjugation.
 * - [imaginaryUnit], [normSq], [complexLeftAction], [complexModule], [complexStarAlgebra],
 *   [scalarEmbedding], and the [eqComplex] and [printableComplex] overloads taking a base.
 *
 * The arithmetic is not implemented here: it is [CayleyDickson.commutative] at `σ = 1`, transported
 * to the [Complex] carrier. The Gaussian integers and rationals are this construction over ℤ and ℚ.
 *
 * The complex numbers ℂ:
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

    // ── Generic, over a commutative ring ─────────────────────────────────────

    /**
     * The ring `R[i]/(i² + 1)` over a commutative base ring: commutative and associative, with
     * ```text
     * (a + bi)(c + di) = (ac - bd) + (ad + bc)i
     * ```
     * and the involution `conj(a + bi) = a - bi`.
     *
     * Whether it is a field depends on `R`: it is over ℝ and ℚ, where `-1` is not a square, but over
     * a field containing a square root of `-1` (such as `𝔽₅`) it has zero divisors.
     */
    fun <R : Any> complexRing(
        base: CommutativeRing<R>
    ): CommutativeInvolutiveRing<Complex<R>> =
        complexRingOver(base)

    /**
     * The imaginary unit `0 + 1·i` over [base].
     */
    fun <R : Any> imaginaryUnit(
        base: CommutativeRing<R>
    ): Complex<R> =
        complex(base.zero, base.one)

    /**
     * The norm `N(a + bi) = (a + bi)·conj(a + bi) = a² + b²`. It is multiplicative.
     */
    fun <R : Any> normSq(
        base: CommutativeRing<R>
    ): UnaryOp<Complex<R>, R> =
        UnaryOp(Symbols.NORM_SQ_SYMBOL) { (a, b) -> base.add(base.mul(a, a), base.mul(b, b)) }

    /**
     * The componentwise scalar action `r ⊳ (a + bi) = ra + rb·i`.
     */
    fun <R : Any> complexLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<R, Complex<R>> =
        LeftAction(Symbols.TRIANGLE_RIGHT) { r, (a, b) -> complex(base.mul(r, a), base.mul(r, b)) }

    /**
     * `Complex<F>` over a field, with reciprocals on the units.
     *
     * `z = a + bi` is invertible iff `N(z) = a² + b²` is invertible in [field], and then
     * `z⁻¹ = conj(z) / N(z)`. Over a field in which `-1` is not a square that is every nonzero `z`,
     * and `Complex<F>` is a field; otherwise the norm is isotropic and there are zero divisors.
     * [isZero] decides when a norm counts as zero; over `Real` use [ComplexField], which applies the
     * floating-point policy.
     */
    fun <F : Any> complexRingWithReciprocal(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): ComplexRingWithReciprocal<F> =
        ComplexRingWithReciprocal(field, isZero)

    class ComplexRingWithReciprocal<F : Any>(
        private val field: Field<F>,
        private val isZero: UnaryOp<F, Boolean>
    ) : CommutativeInvolutiveRing<Complex<F>> by complexRingOver(field),
        HasNormSq<Complex<F>, F>,
        HasReciprocal<Complex<F>> {

        override val zero: Complex<F>
            get() = add.identity

        override val normSq: UnaryOp<Complex<F>, F> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { (a, b) -> field.add(field.mul(a, a), field.mul(b, b)) }

        override fun hasReciprocal(a: Complex<F>): Boolean =
            !isZero(normSq(a))

        override val reciprocal: Endo<Complex<F>> = Endo(Symbols.INVERSE) { z ->
            if (!hasReciprocal(z)) throw HasReciprocal.notInvertible(z, "the complex numbers over the given field")
            val scale = field.reciprocal(normSq(z))
            val (a, b) = conj(z)
            complex(field.mul(scale, a), field.mul(scale, b))
        }
    }

    /**
     * `Complex<R>` as a free `R`-module of rank 2.
     */
    fun <R : Any> complexModule(
        base: CommutativeRing<R>
    ): RModule<R, Complex<R>> =
        RModule.of(
            scalars = base,
            add = complexRing(base).add,
            leftAction = complexLeftAction(base)
        )

    /**
     * `Complex<R>` as an `R`-star-algebra under complex conjugation.
     */
    fun <R : Any> complexStarAlgebra(
        base: CommutativeRing<R>
    ): StarAlgebra<R, Complex<R>> =
        StarAlgebra.of(
            scalars = base,
            involutiveRing = complexRing(base),
            leftAction = complexLeftAction(base)
        )

    /**
     * The embedding of the base ring, `r ↦ r + 0i`.
     */
    fun <R : Any> scalarEmbedding(
        base: CommutativeRing<R>
    ): RingMonomorphism<R, Complex<R>> =
        RingMonomorphism.of(
            domain = base,
            codomain = complexRing(base),
            map = UnaryOp { r -> complex(r, base.zero) }
        )

    /**
     * Componentwise equality on `Complex<R>`.
     */
    fun <R : Any> eqComplex(eqR: Eq<R>): Eq<Complex<R>> =
        CD.eq(eqR).contramap { z: Complex<R> -> z.cd }

    /**
     * The printable `a + bi` over any base, dropping zero terms.
     */
    fun <R : Any> printableComplex(
        base: CommutativeRing<R>,
        signed: SignedOps<R>,
        prR: Printable<R>,
        eqR: Eq<R>
    ): Printable<Complex<R>> =
        ComplexPrintable.complexLikePrintable(
            signed = signed,
            zero = base.zero,
            one = base.one,
            re = UnaryOp { it.re },
            im = UnaryOp { it.im },
            basis = Symbols.IMAGINARY_I,
            prA = prR,
            eqA = eqR
        )

    // ── The complex numbers ℂ ────────────────────────────────────────────────

    val i: RealComplex = complex(0.0, 1.0)

    /**
     * The complex field.
     *
     * Reciprocals follow the floating-point policy of [HasReciprocal]: `c` is invertible iff
     * `|c|²` is finite and not approximately zero, and [reciprocal] throws [ArithmeticException]
     * otherwise.
     */
    object ComplexField : Field<RealComplex> {

        override val add = cayleyDicksonComplex.add

        override val mul: CommutativeMonoid<RealComplex> = cayleyDicksonComplex.mul

        override fun hasReciprocal(a: RealComplex): Boolean =
            isInvertibleComplex(a)

        override val reciprocal: Endo<RealComplex> = Endo(Symbols.INVERSE) { c ->
            if (!hasReciprocal(c)) throw HasReciprocal.notInvertible(c, Symbols.BB_C)
            realScaling(1.0 / complexNormSq(c), cayleyDicksonComplex.conj(c))
        }

        override fun fromBigInt(n: BigInteger): RealComplex =
            complex(n.toDouble(), 0.0)
    }

    object ComplexStarAlgebra :
        Field<RealComplex> by ComplexField,
        InvolutiveRing<RealComplex>,
        RealNormedDivisionAlgebra<RealComplex>,
        StarAlgebra<Real, RealComplex> {

        override val zero: RealComplex = ComplexField.zero
        override val one: RealComplex = ComplexField.one

        override val scalars = RealAlgebras.RealField

        override val conj: Endo<RealComplex> =
            cayleyDicksonComplex.conj

        override val normSq: UnaryOp<RealComplex, Real> =
            complexNormSq

        override val leftAction: LeftAction<Real, RealComplex> =
            realScaling

        override fun hasReciprocal(a: RealComplex): Boolean =
            ComplexField.hasReciprocal(a)
    }

    /**
     * Compute `c^n` for a complex number `c` and a non-negative integer `n`
     * using exponentiation by squaring.
     */
    fun RealComplex.powInt(n: Int): RealComplex {
        require(n >= 0) { "n must be >= 0" }
        return ComplexField.mul.pow(this, n)
    }

    object ComplexRealVectorSpace : FiniteVectorSpace<Real, RealComplex> {
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
    ): RingMonomorphism<RealComplex, RealQuaternion> =
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
    val ComplexToRank2RMatrixMonomorphism: RingMonomorphism<RealComplex, DenseMat<Real>> = quadraticRank2MatrixEmbedding(
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
    fun RealComplex.asQuaternion(
        emb: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RealQuaternion = complexToQuaternionEmbedding(emb)(this)

    /** EQs **/
    val eqComplexStrict: Eq<RealComplex> = eqComplex(RealAlgebras.eqRealStrict)
    val eqComplex: Eq<RealComplex> = eqComplex(RealAlgebras.eqRealApprox)

    private fun printableComplexGenerator(
        prReal: Printable<Real>,
        eqReal: Eq<Real>,
    ): Printable<RealComplex> =
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

    val printableComplex: Printable<RealComplex> =
        printableComplexGenerator(
            prReal = RealAlgebras.printableReal,
            eqReal = RealAlgebras.eqRealApprox
        )

    val printableComplexStrict: Printable<RealComplex> =
        printableComplexGenerator(
            prReal = RealAlgebras.printableRealStrict,
            eqReal = RealAlgebras.eqRealStrict
        )

    val printableComplexPretty: Printable<RealComplex> =
        printableComplexGenerator(
            prReal = RealAlgebras.printableRealPretty,
            eqReal = RealAlgebras.eqRealApprox
        )
}
