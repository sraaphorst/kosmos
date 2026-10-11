package org.vorpal.kosmos.hypercomplex.dual

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
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
 * Algebraic structures and utilities for dual numbers `a + bε`, `ε² = 0`.
 *
 * Main constructions:
 * - [dualRing]: the dual ring `R[ε]/(ε²)` over a commutative base ring.
 * - [dualRingWithReciprocal]: the same over a field, with partial reciprocals.
 * - [infinitesimalUnit]: the element `ε` over a given base.
 * - [normSq]: the (degenerate) norm `N(a + bε) = a²`.
 * - [dualLeftAction]: scalar action `r ⊳ (a + bε) = (ra) + (rb)ε`.
 * - [dualModule]: `Dual<R>` as a free `R`-module of rank 2.
 * - [dualAlgebra]: `Dual<R>` as an `R`-algebra.
 * - [dualStarAlgebra]: `Dual<R>` as an `R`-star-algebra (with conjugation).
 *
 * The arithmetic is not implemented here: it is the Cayley-Dickson doubling of `R` (with the
 * trivial involution) at `σ = 0`, i.e. [CayleyDickson.commutative], transported to the [Dual]
 * carrier.
 *
 * Real conveniences:
 * - [RealDualRing]: dual ring over `Real`, with numerically guarded reciprocals.
 * - [RealDualStarAlgebra]: dual star algebra over `Real`.
 * - [DualRealVectorSpace]: two-dimensional real vector space.
 * - [epsilon]: the element `ε` over the reals.
 *
 * Morphisms:
 * - [scalarEmbedding]: embeds `R` into `Dual<R>`, `r ↦ r + 0ε`.
 * - [primalProjection]: the augmentation `Dual<R> → R`, `a + bε ↦ a` (set `ε = 0`).
 * - [dualToRank2MatrixMonomorphism]: embeds `a + bε` as `[[a, 0], [b, a]]`.
 *
 * Differentiation:
 * - [variable], [constant]: the seeds `a + 1ε` and `a + 0ε` of forward-mode differentiation.
 * - [diffAt], [diffAtReal]: the value and derivative of a scalar map at a point.
 *
 * Elementary functions on dual numbers live in [DualRealFns] and [DualRationalFns].
 */
object DualAlgebras {

    private fun <R : Any> transport(): CarrierTransport<CD<R>, Dual<R>> =
        CarrierTransport(
            wrap = UnaryOp { cd -> Dual(cd) },
            unwrap = UnaryOp { d -> d.cd }
        )

    /**
     * The element `ε` of the dual ring, which depends on the base ring.
     *
     * Note that the 0 and 1 elements are accessible via the ring instance.
     */
    fun <R : Any> infinitesimalUnit(
        base: CommutativeRing<R>
    ): Dual<R> =
        dual(
            f = base.zero,
            df = base.one
        )

    /**
     * The element `ε` of the dual ring over the Reals.
     */
    val epsilon: Dual<Real> =
        infinitesimalUnit(RealAlgebras.RealField)

    /**
     * The dual ring over a commutative base ring.
     *
     * This is the Cayley-Dickson doubling of [base] equipped with the *trivial* involution at
     * `σ = 0` (see [CayleyDickson.commutative]), i.e. `R[ε] / (ε²)`.
     *
     * Multiplication is:
     * ```text
     * (a + bε)(c + dε) = ac + (ad + bc)ε
     * ```
     * so `ε² = 0`.
     *
     * The involution is the Cayley-Dickson conjugation:
     * ```text
     * conj(a + bε) = a - bε
     * ```
     */
    fun <R : Any> dualRing(
        base: CommutativeRing<R>
    ): CommutativeInvolutiveRing<Dual<R>> =
        transport<R>().commutativeInvolutiveRing(
            CayleyDickson.commutative(
                base = base,
                sigma = base.zero
            )
        )

    /**
     * Norm-like quadratic form for a generic commutative ring:
     * ```text
     * N(a + bε) = (a + bε)·conj(a + bε) = a²
     * ```
     * It is multiplicative but *degenerate*: it ignores the tangent part entirely, and `N(z) = 0`
     * exactly on the nilpotent ideal `εR` when the base has no zero divisors.
     */
    fun <R : Any> normSq(
        base: CommutativeRing<R>
    ): UnaryOp<Dual<R>, R> =
        UnaryOp(Symbols.NORM_SQ_SYMBOL) { z -> base.mul(z.a, z.a) }

    /**
     * Dual numbers over a field, with reciprocals on the units.
     *
     * `a + bε` is invertible iff `a` is invertible in [field], and then
     * ```text
     * (a + bε)⁻¹ = conj(a + bε) / N(a + bε) = a⁻¹ - (b / a²) ε.
     * ```
     * [isZero] decides when a primal part counts as zero; over an exact field the default equality
     * is right, while over `Real` a numerical test should be supplied (see [RealDualRing]).
     */
    fun <F : Any> dualRingWithReciprocal(
        field: Field<F>,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): DualRingWithReciprocal<F> =
        DualRingWithReciprocal(field, isZero)

    class DualRingWithReciprocal<F : Any>(
        private val field: Field<F>,
        private val isZero: UnaryOp<F, Boolean>
    ) : CommutativeInvolutiveRing<Dual<F>> by dualRing(field),
        HasNormSq<Dual<F>, F>,
        HasReciprocal<Dual<F>> {

        override val zero: Dual<F>
            get() = add.identity

        override val normSq: UnaryOp<Dual<F>, F> =
            normSq(field)

        override fun hasReciprocal(a: Dual<F>): Boolean =
            !isZero(a.a)

        override val reciprocal: Endo<Dual<F>> = Endo(Symbols.INVERSE) { z ->
            if (!hasReciprocal(z)) throw HasReciprocal.notInvertible(z, "the dual numbers")
            val scale = field.reciprocal(normSq(z))
            dualLeftAction(field)(scale, conj(z))
        }
    }

    /**
     * The componentwise scalar action of a commutative base ring on `Dual<R>`:
     * ```text
     * r ⊳ (a + bε) = (r·a) + (r·b)ε
     * ```
     */
    fun <R : Any> dualLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<R, Dual<R>> =
        LeftAction(Symbols.TRIANGLE_RIGHT) { r, (a, b) ->
            dual(
                f = base.mul(r, a),
                df = base.mul(r, b)
            )
        }

    /**
     * `Dual<R>` as a free `R`-module of rank 2.
     *
     * Carrier addition is taken from [dualRing]; the action is [dualLeftAction].
     */
    fun <R : Any> dualModule(
        base: CommutativeRing<R>
    ): RModule<R, Dual<R>> =
        RModule.of(
            scalars = base,
            add = dualRing(base).add,
            leftAction = dualLeftAction(base)
        )

    /**
     * `Dual<R>` as an `R`-algebra.
     *
     * Combines the commutative ring structure from [dualRing] with the scalar action
     * [dualLeftAction]. Multiplication is `R`-bilinear by construction.
     */
    fun <R : Any> dualAlgebra(
        base: CommutativeRing<R>
    ): Algebra<R, Dual<R>> =
        Algebra.of(
            scalars = base,
            algebraRing = dualRing(base),
            leftAction = dualLeftAction(base)
        )

    /**
     * `Dual<R>` as an `R`-star-algebra.
     *
     * The involution is the standard dual conjugation `a + bε ↦ a - bε`, supplied by [dualRing].
     */
    fun <R : Any> dualStarAlgebra(
        base: CommutativeRing<R>
    ): StarAlgebra<R, Dual<R>> =
        StarAlgebra.of(
            scalars = base,
            involutiveRing = dualRing(base),
            leftAction = dualLeftAction(base)
        )

    /**
     * The floating-point zero test of [HasReciprocal]: not finite, or approximately zero.
     */
    private val realIsZero: UnaryOp<Real, Boolean> =
        UnaryOp { n -> !n.isFinite() || RealAlgebras.eqRealApprox(n, 0.0) }

    /**
     * The dual numbers over the reals.
     *
     * Reciprocals follow the floating-point policy of [HasReciprocal]. Algebraically, `a + bε` is
     * invertible iff `a ≠ 0`. Numerically, it counts as invertible iff `a` is finite and not
     * approximately zero *and* the inverse `a⁻¹ - (b/a²)ε` is finite: unlike the norm-based
     * algebras, the tangent part `b/a²` can overflow even when `a` is safely nonzero, so the
     * predicate checks it directly.
     */
    object RealDualRing :
        CommutativeInvolutiveRing<Dual<Real>> by dualRing(RealAlgebras.RealField),
        HasNormSq<Dual<Real>, Real>,
        HasReciprocal<Dual<Real>> {

        private val guarded = dualRingWithReciprocal(
            field = RealAlgebras.RealField,
            isZero = realIsZero
        )

        override val zero: Dual<Real>
            get() = add.identity

        override val normSq: UnaryOp<Dual<Real>, Real> = guarded.normSq

        override fun hasReciprocal(a: Dual<Real>): Boolean =
            guarded.hasReciprocal(a) && a.b.isFinite() && (a.b / (a.a * a.a)).isFinite()

        override val reciprocal: Endo<Dual<Real>> = Endo(Symbols.INVERSE) { z ->
            if (!hasReciprocal(z)) throw HasReciprocal.notInvertible(z, "the dual numbers")
            val invA = 1.0 / z.a
            dual(f = invA, df = -z.b * invA * invA)
        }
    }

    /**
     * The dual star algebra over the Reals.
     *
     * Mirrors `ComplexAlgebras.ComplexStarAlgebra` and `SplitComplexAlgebras.RealSplitComplexStarAlgebra`,
     * but `Dual<Real>` is *not* a field (`ε` is nilpotent) and its norm form is degenerate, so it is
     * deliberately not a `RealNormedDivisionAlgebra`. Use [RealDualRing.normSq] when the quadratic
     * form `a²` is wanted.
     */
    object RealDualStarAlgebra :
        CommutativeInvolutiveRing<Dual<Real>> by RealDualRing,
        StarAlgebra<Real, Dual<Real>> {

        override val zero: Dual<Real> = RealDualRing.zero
        override val one: Dual<Real> = RealDualRing.one

        override val scalars = RealAlgebras.RealField

        override val conj: Endo<Dual<Real>> = RealDualRing.conj

        override val leftAction: LeftAction<Real, Dual<Real>> =
            dualLeftAction(RealAlgebras.RealField)
    }

    object DualRealVectorSpace : FiniteVectorSpace<Real, Dual<Real>> {
        override val scalars = RealAlgebras.RealField
        override val add = RealDualRing.add
        override val dimension = 2
        override val leftAction: LeftAction<Real, Dual<Real>> =
            RealDualStarAlgebra.leftAction
    }

    /**
     * A base ring embedding from the ring R to Dual<R>, `r ↦ r + 0ε`.
     */
    fun <R : Any> scalarEmbedding(
        base: CommutativeRing<R>
    ): RingMonomorphism<R, Dual<R>> =
        RingMonomorphism.of(
            domain = base,
            codomain = dualRing(base),
            map = UnaryOp { r -> dual(f = r, df = base.zero) }
        )

    /**
     * The augmentation `Dual<R> → R`, `a + bε ↦ a`: evaluate at `ε = 0`.
     *
     * It is a surjective ring homomorphism with kernel the nilpotent ideal `εR`, and a left inverse
     * of [scalarEmbedding]. In automatic-differentiation terms it forgets the derivative and keeps
     * the value. (The tangent part `a + bε ↦ b` is *not* a homomorphism: it is a derivation,
     * `D(xy) = x₀·D(y) + D(x)·y₀`, which is the product rule.)
     */
    fun <R : Any> primalProjection(
        base: CommutativeRing<R>
    ): RingHomomorphism<Dual<R>, R> =
        RingHomomorphism.of(
            domain = dualRing(base),
            codomain = base,
            map = UnaryOp { z -> z.a }
        )

    /**
     * The matrix embedding into M₂(R): the regular representation on the basis `{1, ε}`.
     *
     * Note that:
     * ```text
     * a + bε ↪ [[a, 0], [b, a]]
     * ```
     * The columns are the coordinates of `(a + bε)·1` and `(a + bε)·ε`. This is the same
     * [quadraticRank2MatrixEmbedding] used for the complex (`x² = -1`) and split-complex
     * (`x² = 1`) numbers; the determinant is the norm `a²`.
     */
    fun <R : Any> dualToRank2MatrixMonomorphism(
        base: CommutativeRing<R>
    ): RingMonomorphism<Dual<R>, DenseMat<R>> =
        quadraticRank2MatrixEmbedding(
            domain = dualRing(base),
            coefficientRing = base,
            s = base.zero,
            t = base.zero,
            coeffs = UnaryOp { (a, b) -> a to b }
        )

    /**
     * The seed `a + 1ε` of an independent variable at the point `a`: its derivative with respect to
     * itself is 1.
     */
    fun <R : Any> variable(
        base: CommutativeRing<R>,
        a: R
    ): Dual<R> =
        dual(f = a, df = base.one)

    /**
     * The seed `a + 0ε` of a constant: its derivative is 0. This is [scalarEmbedding] as a plain
     * function.
     */
    fun <R : Any> constant(
        base: CommutativeRing<R>,
        a: R
    ): Dual<R> =
        dual(f = a, df = base.zero)

    /**
     * Forward-mode automatic differentiation of a scalar map.
     *
     * [f] is a function `R → R` written on dual numbers. Since `ε² = 0`, the Taylor expansion
     * ```text
     * f(a + ε) = f(a) + f'(a) ε
     * ```
     * terminates, so evaluating [f] at the seed `x + 1ε` produces the value and the derivative
     * together.
     *
     * Returns `(f(x), f'(x))`.
     */
    fun <R : Any> diffAt(
        base: CommutativeRing<R>,
        f: Endo<Dual<R>>,
        x: R
    ): Pair<R, R> {
        val y = f(variable(base, x))
        return y.f to y.df
    }

    /**
     * [diffAt] for a scalar map `Real → Real`.
     *
     * Returns `(f(x), f'(x))`.
     */
    fun diffAtReal(
        f: Endo<Dual<Real>>,
        x: Real
    ): Pair<Real, Real> =
        diffAt(
            base = RealAlgebras.RealField,
            f = f,
            x = x
        )

    /**
     * Eqs
     */
    fun <R : Any> eqDual(eqR: Eq<R>): Eq<Dual<R>> =
        CD.eq(eqR).contramap { z: Dual<R> -> z.cd }

    /**
     * Eq specifically based on a strict Real comparison.
     */
    val eqDualStrict: Eq<Dual<Real>> =
        eqDual(RealAlgebras.eqRealStrict)

    /**
     * Eq specifically based on a Real approximation.
     */
    val eqDual: Eq<Dual<Real>> =
        eqDual(RealAlgebras.eqRealApprox)

    /**
     * Printables: `a + bε` rendered as a linear combination over the basis `{1, ε}`.
     */
    fun <R : Any> printableDual(
        signed: SignedOps<R>,
        zero: R,
        one: R,
        prR: Printable<R>,
        eqR: Eq<R>
    ): Printable<Dual<R>> =
        ComplexPrintable.complexLikePrintable(
            signed = signed,
            zero = zero,
            one = one,
            re = UnaryOp { it.a },
            im = UnaryOp { it.b },
            basis = Symbols.EPSILON,
            prA = prR,
            eqA = eqR
        )

    private fun printableDualRealGenerator(
        prReal: Printable<Real>,
        eqReal: Eq<Real>
    ): Printable<Dual<Real>> =
        printableDual(
            signed = RealAlgebras.SignedReal,
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            prR = prReal,
            eqR = eqReal
        )

    /**
     * For Real.
     */
    val printableDualReal: Printable<Dual<Real>> =
        printableDualRealGenerator(
            prReal = RealAlgebras.printableReal,
            eqReal = RealAlgebras.eqRealApprox
        )

    val printableDualStrict: Printable<Dual<Real>> =
        printableDualRealGenerator(
            prReal = RealAlgebras.printableRealStrict,
            eqReal = RealAlgebras.eqRealStrict
        )

    val printableDualPretty: Printable<Dual<Real>> =
        printableDualRealGenerator(
            prReal = RealAlgebras.printableRealPretty,
            eqReal = RealAlgebras.eqRealApprox
        )
}
