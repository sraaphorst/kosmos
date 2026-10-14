package org.vorpal.kosmos.hypercomplex.splitcomplex

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.quadratic.quadraticRank2MatrixEmbedding
import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CarrierTransport
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.contramap
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.complex.ComplexPrintable
import org.vorpal.kosmos.linear.values.DenseMat

/**
 * Algebraic structures and utilities for split-complex numbers.
 *
 * Main constructions:
 * - [splitComplexRing]: the split-complex ring `R[j]/(j² - 1)` over a commutative base ring.
 * - [splitComplexRingWithReciprocal]: the same over a field, with partial reciprocals.
 * - [splitComplexLeftAction]: scalar action `r ⊳ (a + bj) = (ra) + (rb)j`.
 * - [splitComplexModule]: `SplitComplex<R>` as a free `R`-module of rank 2.
 * - [splitComplexAlgebra]: `SplitComplex<R>` as an `R`-algebra.
 * - [splitComplexStarAlgebra]: `SplitComplex<R>` as an `R`-star-algebra (with conjugation).
 *
 * The arithmetic is not implemented here: it is the Cayley-Dickson doubling of `R` (with the
 * trivial involution) at `σ = -1`, i.e. [CayleyDickson.commutative], transported to the
 * [SplitComplex] carrier.
 *
 * Real conveniences:
 * - [RealSplitComplexRing]: split-complex ring over `Real`, with numerically guarded reciprocals.
 * - [RealSplitComplexStarAlgebra]: split-complex star algebra over `Real`.
 * - [SplitComplexRealVectorSpace]: two-dimensional real vector space.
 *
 * Morphisms:
 * - [scalarEmbedding]: embeds `R` into `SplitComplex<R>`.
 * - [splitComplexToRank2MatrixMonomorphism]: embeds `a + bj` as `[[a, b], [b, a]]`.
 */
object SplitComplexAlgebras {

    private fun <R : Any> transport(): CarrierTransport<CD<R>, SplitComplex<R>> =
        CarrierTransport(
            wrap = UnaryOp { cd -> SplitComplex(cd) },
            unwrap = UnaryOp { z -> z.cd }
        )

    /**
     * The j-element of the split complex algebra, which depends on the base ring.
     *
     * Note that the 0 and 1 elements are accessible via the ring instance.
     */
    fun <R : Any> hyperbolicUnit(
        base: CommutativeRing<R>
    ): SplitComplex<R> =
        splitComplex(
            re = base.zero,
            hy = base.one
        )

    /**
     * The j-element of the split complex algebra over the Reals.
     */
    val j: SplitComplex<Real> =
        hyperbolicUnit(RealAlgebras.RealField)

    /**
     * The ordinary split-complex ring over a commutative base ring.
     *
     * This is the Cayley-Dickson doubling of [base] equipped with the *trivial* involution at
     * `σ = -1` (see [CayleyDickson.commutative]), i.e. `R[j] / (j² - 1)`. Because the base
     * involution is trivial, no involution is required of the base: rationals, finite fields,
     * etc. can all be used directly.
     *
     * Multiplication is:
     * ```text
     * (a + bj)(c + dj) = (ac + bd) + (ad + bc)j
     * ```
     * so `j² = 1`.
     *
     * The involution is the Cayley-Dickson conjugation:
     * ```text
     * conj(a + bj) = a - bj
     * ```
     */
    fun <R : Any> splitComplexRing(
        base: CommutativeRing<R>
    ): CommutativeInvolutiveRing<SplitComplex<R>> =
        transport<R>().commutativeInvolutiveRing(
            CayleyDickson.commutative(
                base = base,
                sigma = base.negOne
            )
        )

    /**
     * Norm-like quadratic form for a generic commutative ring:
     * ```text
     * N(a + bj) = (a + bj)·conj(a + bj) = a² - b²
     * ```
     * It is indefinite: `N(z) = 0` exactly on the null cone `a = ±b`, which consists of the zero
     * divisors (and 0).
     */
    fun <R : Any> normSq(
        base: CommutativeRing<R>
    ): UnaryOp<SplitComplex<R>, R> =
        UnaryOp(Symbols.NORM_SQ_SYMBOL) { z ->
            base.add(
                base.mul(z.re, z.re),
                base.add.inverse(base.mul(z.hy, z.hy))
            )
        }

    /**
     * Split-complex numbers over a field, with reciprocals on the units.
     *
     * `a + bj` is invertible iff `N(a + bj) = a² - b²` is invertible in [field], i.e. iff it is off
     * the null cone, and then
     * ```text
     * (a + bj)⁻¹ = conj(a + bj) / N(a + bj) = (a - bj) / (a² - b²).
     * ```
     * [isZero] decides when a norm counts as zero; over an exact field the default equality is
     * right, while over `Real` a numerical test should be supplied (see [RealSplitComplexRing]).
     */
    fun <F : Any> splitComplexRingWithReciprocal(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): SplitComplexRingWithReciprocal<F> =
        SplitComplexRingWithReciprocal(field, isZero)

    class SplitComplexRingWithReciprocal<F : Any>(
        private val field: Field<F>,
        private val isZero: UnaryOp<F, Boolean>
    ) : CommutativeInvolutiveRing<SplitComplex<F>> by splitComplexRing(field),
        HasNormSq<SplitComplex<F>, F>,
        HasReciprocal<SplitComplex<F>> {

        override val zero: SplitComplex<F>
            get() = add.identity

        override val normSq: UnaryOp<SplitComplex<F>, F> =
            normSq(field)

        override fun hasReciprocal(a: SplitComplex<F>): Boolean =
            !isZero(normSq(a))

        override val reciprocal: Endo<SplitComplex<F>> = Endo(Symbols.INVERSE) { z ->
            if (!hasReciprocal(z)) throw HasReciprocal.notInvertible(z, "the split-complex numbers")
            val scale = field.reciprocal(normSq(z))
            splitComplexLeftAction(field)(scale, conj(z))
        }
    }

    /**
     * Norm-like quadratic form for the Reals.
     */
    val splitComplexNormSq: UnaryOp<SplitComplex<Real>, Real> =
        normSq(RealAlgebras.RealField)

    /**
     * The componentwise scalar action of a commutative base ring on `SplitComplex<R>`:
     * ```text
     * r ⊳ (a + bj) = (r·a) + (r·b)j
     * ```
     */
    fun <R : Any> splitComplexLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<R, SplitComplex<R>> =
        LeftAction(Symbols.TRIANGLE_RIGHT) { r, (re, hy) ->
            splitComplex(
                re = base.mul(r, re),
                hy = base.mul(r, hy)
            )
        }

    /**
     * `SplitComplex<R>` as a free `R`-module of rank 2.
     *
     * Carrier addition is taken from [splitComplexRing]; the action is [splitComplexLeftAction].
     */
    fun <R : Any> splitComplexModule(
        base: CommutativeRing<R>
    ): RModule<R, SplitComplex<R>> =
        RModule.of(
            scalars = base,
            add = splitComplexRing(base).add,
            leftAction = splitComplexLeftAction(base)
        )

    /**
     * `SplitComplex<R>` as an `R`-algebra.
     *
     * Combines the commutative ring structure from [splitComplexRing] with the scalar
     * action [splitComplexLeftAction]. Multiplication is `R`-bilinear by construction.
     */
    fun <R : Any> splitComplexAlgebra(
        base: CommutativeRing<R>
    ): Algebra<R, SplitComplex<R>> =
        Algebra.of(
            scalars = base,
            algebraRing = splitComplexRing(base),
            leftAction = splitComplexLeftAction(base)
        )

    /**
     * `SplitComplex<R>` as an `R`-star-algebra.
     *
     * The involution is the standard split-complex conjugation `a + bj ↦ a - bj`,
     * supplied by [splitComplexRing].
     */
    fun <R : Any> splitComplexStarAlgebra(
        base: CommutativeRing<R>
    ): StarAlgebra<R, SplitComplex<R>> =
        StarAlgebra.of(
            scalars = base,
            involutiveRing = splitComplexRing(base),
            leftAction = splitComplexLeftAction(base)
        )

    /**
     * The split-complex numbers over the reals.
     *
     * Reciprocals follow the floating-point policy of [HasReciprocal]: `z` is invertible iff
     * `N(z) = a² - b²` is finite and not approximately zero.
     */
    object RealSplitComplexRing :
        CommutativeInvolutiveRing<SplitComplex<Real>> by splitComplexRing(RealAlgebras.RealField),
        HasNormSq<SplitComplex<Real>, Real>,
        HasReciprocal<SplitComplex<Real>> {

        private val guarded = splitComplexRingWithReciprocal(
            field = RealAlgebras.RealField,
            isZero = UnaryOp { n -> !n.isFinite() || RealAlgebras.eqRealApprox(n, 0.0) }
        )

        override val zero: SplitComplex<Real>
            get() = add.identity

        override val normSq: UnaryOp<SplitComplex<Real>, Real> = guarded.normSq

        override fun hasReciprocal(a: SplitComplex<Real>): Boolean =
            guarded.hasReciprocal(a)

        override val reciprocal: Endo<SplitComplex<Real>> = guarded.reciprocal
    }

    /**
     * The split-complex star algebra over the Reals.
     *
     * Mirrors `ComplexAlgebras.ComplexStarAlgebra`, but note that `SplitComplex<Real>`
     * is *not* a field (it has zero divisors) and its norm form is indefinite, so we
     * deliberately omit `RealNormedDivisionAlgebra` here. Use [RealSplitComplexRing.normSq] or
     * [splitComplexNormSq] directly when the indefinite quadratic form is wanted.
     */
    object RealSplitComplexStarAlgebra :
        CommutativeInvolutiveRing<SplitComplex<Real>> by RealSplitComplexRing,
        StarAlgebra<Real, SplitComplex<Real>> {

        override val zero: SplitComplex<Real> = RealSplitComplexRing.zero
        override val one: SplitComplex<Real> = RealSplitComplexRing.one

        override val scalars = RealAlgebras.RealField

        override val conj: Endo<SplitComplex<Real>> = RealSplitComplexRing.conj

        override val leftAction: LeftAction<Real, SplitComplex<Real>> =
            splitComplexLeftAction(RealAlgebras.RealField)
    }

    /**
     * The `e₊` coordinate of this split-complex number in the diagonal / idempotent basis.
     *
     * If `z = x + yj`, then:
     * ```text
     * z = (x + y)e₊ + (x - y)e₋
     * ```
     * so this returns `x + y`.
     */
    fun SplitComplex<Real>.diagPlus(): Real =
        RealAlgebras.RealField.add(re, hy)

    /**
     * The `e₋` coordinate of this split-complex number in the diagonal / idempotent basis.
     *
     * If `z = x + yj`, then:
     * ```text
     * z = (x + y)e₊ + (x - y)e₋
     * ```
     * so this returns `x - y`.
     */
    fun SplitComplex<Real>.diagMinus(): Real =
        RealAlgebras.RealField.add(re, RealAlgebras.RealField.add.inverse(hy))

    /**
     * The diagonal / idempotent-basis coordinates `(x + y, x - y)` of `x + yj`.
     */
    fun SplitComplex<Real>.diagPlusMinus(): Pair<Real, Real> =
        diagPlus() to diagMinus()

    /**
     * Conversion from idempotent basis elements to a SplitComplex<Real> in standard form.
     */
    fun fromDiagonal(
        ePlus: Real,
        eMinus: Real
    ): SplitComplex<Real> {
        val real = RealAlgebras.RealField
        return splitComplex(
            re = real.mul(real.add(ePlus, eMinus), real.reciprocal(2.0)),
            hy = real.mul(real.add(ePlus, real.add.inverse(eMinus)), real.reciprocal(2.0))
        )
    }

    object SplitComplexRealVectorSpace : FiniteVectorSpace<Real, SplitComplex<Real>> {
        override val scalars = RealAlgebras.RealField
        override val add = RealSplitComplexRing.add
        override val dimension = 2
        override val leftAction: LeftAction<Real, SplitComplex<Real>> =
            RealSplitComplexStarAlgebra.leftAction
    }

    /**
     * A base ring embedding from the ring R to SplitComplex<R>.
     */
    fun <R : Any> scalarEmbedding(
        base: CommutativeRing<R>
    ): RingMonomorphism<R, SplitComplex<R>> {
        val split = splitComplexRing(base)
        return RingMonomorphism.of(
            domain = base,
            codomain = split,
            map = UnaryOp { r -> splitComplex(re = r, hy = base.zero) }
        )
    }

    /**
     * The matrix embedding into M₂(R).
     *
     * Note that:
     * ```text
     * x + yj ↪ [[x, y], [y, x]]
     * ```
     */
    fun <R : Any> splitComplexToRank2MatrixMonomorphism(
        base: CommutativeRing<R>
    ): RingMonomorphism<SplitComplex<R>, DenseMat<R>> =
        quadraticRank2MatrixEmbedding(
            domain = splitComplexRing(base),
            coefficientRing = base,
            s = base.one,
            t = base.zero,
            coeffs = UnaryOp { (re, hy) -> re to hy }
        )

    /**
     * Eqs
     */
    fun <R : Any> eqSplitComplex(eqR: Eq<R>): Eq<SplitComplex<R>> =
        CD.eq(eqR).contramap(SplitComplex<R>::cd)

    /**
     * Eq specifically based on a strict Real comparison.
     */
    val eqSplitComplexStrict: Eq<SplitComplex<Real>> =
        eqSplitComplex(RealAlgebras.eqRealStrict)

    /**
     * Eq specifically based on a Real approximation.
     */
    val eqSplitComplex: Eq<SplitComplex<Real>> =
        eqSplitComplex(RealAlgebras.eqRealApprox)

    /**
     * Printables
     */
    fun <R : Any> printableSplitComplex(
        signed: SignedOps<R>,
        zero: R,
        one: R,
        prR: Printable<R>,
        eqR: Eq<R>
    ): Printable<SplitComplex<R>> =
        ComplexPrintable.complexLikePrintable(
            signed = signed,
            zero = zero,
            one = one,
            re = UnaryOp { it.re },
            im = UnaryOp { it.hy },
            basis = Symbols.HYPERBOLIC_J,
            prA = prR,
            eqA = eqR
        )

    private fun printableSplitComplexRealGenerator(
        prReal: Printable<Real>,
        eqReal: Eq<Real>
    ): Printable<SplitComplex<Real>> =
        printableSplitComplex(
            signed = RealAlgebras.SignedReal,
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            prR = prReal,
            eqR = eqReal
        )

    /**
     * For Real.
     */
    val printableSplitComplexReal: Printable<SplitComplex<Real>> =
        printableSplitComplexRealGenerator(
            prReal = RealAlgebras.printableReal,
            eqReal = RealAlgebras.eqRealApprox
        )

    val printableSplitComplexStrict: Printable<SplitComplex<Real>> =
        printableSplitComplexRealGenerator(
            prReal = RealAlgebras.printableRealStrict,
            eqReal = RealAlgebras.eqRealStrict
        )

    val printableSplitComplexPretty: Printable<SplitComplex<Real>> =
        printableSplitComplexRealGenerator(
            prReal = RealAlgebras.printableRealPretty,
            eqReal = RealAlgebras.eqRealApprox
        )
}
