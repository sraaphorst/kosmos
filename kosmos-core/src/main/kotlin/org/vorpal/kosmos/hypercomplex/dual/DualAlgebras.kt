package org.vorpal.kosmos.hypercomplex.dual

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.quadratic.quadraticRank2MatrixEmbedding
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CarrierTransport
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.contramap
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.complex.ComplexPrintable
import org.vorpal.kosmos.linear.values.DenseMat

/**
 * Algebraic structures and utilities for [Dual] numbers `a + bε`, `ε² = 0`.
 *
 * Main structures:
 * - [DualCommutativeRing]: the commutative involutive ring `F[ε]/(ε²)` over a commutative ring `F`.
 * - [DualCommutativeRingWithReciprocal]: the same over a field, with partial reciprocals.
 *
 * The arithmetic is not implemented here: it is the Cayley-Dickson doubling of `F` (with the trivial
 * involution) at `σ = 0`, i.e. [CayleyDickson.commutative], transported to the [Dual] carrier.
 *
 * Differentiation:
 * - [diffAt], [diffAtReal]: forward-mode automatic differentiation of scalar maps.
 *
 * Morphisms:
 * - [dualToRank2MatrixMonomorphism]: `a + bε ↦ [[a, 0], [b, a]]` (the regular representation).
 *
 * Eqs:
 * - [dualEq], [eqDualReal], [eqDualRealStrict].
 *
 * Printables:
 * - [printableDual], [printableDualReal], [printableDualRealStrict], [printableDualRealPretty].
 */
object DualAlgebras {

    private fun <F : Any> transport(): CarrierTransport<CD<F>, Dual<F>> =
        CarrierTransport(
            wrap = UnaryOp { cd -> Dual(cd) },
            unwrap = UnaryOp { d -> d.cd }
        )

    /**
     * `F[ε]/(ε²)` as the Cayley-Dickson doubling of [base] at `σ = 0`, carried by [Dual].
     */
    private fun <F : Any> cayleyDicksonDual(
        base: CommutativeRing<F>
    ): CommutativeInvolutiveRing<Dual<F>> =
        transport<F>().commutativeInvolutiveRing(
            CayleyDickson.commutative(base, base.zero)
        )

    /**
     * Dual numbers over a commutative base ring [base].
     *
     * The duals form a commutative involutive ring with:
     * ```text
     * (a + bε) + (c + dε) = (a + c) + (b + d)ε
     * (a + bε)(c + dε)    = ac + (ad + bc)ε
     * conj(a + bε)        = a - bε
     * ```
     * The involution is the Cayley-Dickson conjugation, and `(a + bε)·conj(a + bε) = a²`.
     */
    open class DualCommutativeRing<F : Any>(
        protected val base: CommutativeRing<F>
    ) : CommutativeInvolutiveRing<Dual<F>> by cayleyDicksonDual(base) {

        /**
         * Embed an element a ∈ F as a + 0ε.
         */
        fun lift(a: F): Dual<F> =
            dual(a, base.zero)

        /**
         * Build a pure infinitesimal 0 + bε.
         */
        fun eps(b: F): Dual<F> =
            dual(base.zero, b)

        /**
         * The canonical infinitesimal ε = 0 + 1ε.
         */
        val epsOne: Dual<F>
            get() = eps(base.one)

        /**
         * The seed `a + 1ε` of an independent variable at the point `a`.
         */
        fun variable(a: F): Dual<F> =
            dual(a, base.one)
    }

    /**
     * Dual numbers over a base field [F].
     *
     * A dual number is invertible iff its real part `a` is nonzero in the base field.
     *
     * In that case:
     * ```text
     * (a + bε)^{-1} = a^{-1} - (b / a^2) ε
     * ```
     *
     * Note that if `x = a + bε`, then
     * ```text
     * f(x) = f(a) + b f'(a) ε
     * ```
     * by the Taylor expansion:
     * ```text
     *  f(a + h) = f(a) + f'(a) h + f''(a) h^2 / 2! + f'''(a) h^3 / 3! + ...
     *           = f(a) + f'(a)(bε) + 0 + ...
     * ```
     * since `ε² = 0`, and this is how the autodifferentiation is achieved.
     */
    open class DualCommutativeRingWithReciprocal<F : Any>(
        private val field: Field<F>,
        private val isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ) : DualCommutativeRing<F>(field),
        HasReciprocal<Dual<F>> {

        override val zero: Dual<F>
            get() = add.identity

        override fun hasReciprocal(a: Dual<F>): Boolean =
            !isZero(a.a)

        override val reciprocal: Endo<Dual<F>> = Endo(Symbols.INVERSE) { x ->
            if (!hasReciprocal(x)) throw HasReciprocal.notInvertible(x, "the dual numbers")

            val invA = field.reciprocal(x.a)
            val invA2 = field.mul(invA, invA)
            val epsPart = field.mul(field.add.inverse(x.b), invA2)

            dual(invA, epsPart)
        }
    }

    /**
     * Convenience function to build the dual ring over a base commutative ring.
     */
    fun <F : Any> CommutativeRing<F>.dualRing(): DualCommutativeRing<F> =
        DualCommutativeRing(this)

    /**
     * Convenience function to build the dual ring over a base field.
     */
    fun <F : Any> Field<F>.dualRingWithReciprocal(
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == zero }
    ): DualCommutativeRingWithReciprocal<F> =
        DualCommutativeRingWithReciprocal(this, isZero)

    /**
     * Differentiate a function represented on dual numbers, corresponding to a scalar map `F → F`
     * at a point `x`.
     *
     * Returns `f(x), f'(x)`.
     */
    fun <F : Any> diffAt(
        field: Field<F>,
        f: Endo<Dual<F>>,
        x: F,
        isZero: UnaryOp<F, Boolean> = UnaryOp { it == field.zero }
    ): Pair<F, F> {
        val d = field.dualRingWithReciprocal(isZero)
        val y = f(d.variable(x))
        return y.f to y.df
    }

    /**
     * Differentiate a function `f` represented on dual numbers, corresponding to a scalar map:
     * ```text
     * Real → Real
     * ```
     * at a point `x`.
     *
     * Returns `f(x), f'(x)`.
     */
    fun diffAtReal(
        f: Endo<Dual<Real>>,
        x: Real
    ): Pair<Real, Real> =
        diffAt(
            field = RealAlgebras.RealField,
            f = f,
            x = x,
            isZero = realIsZero
        )

    /**
     * The floating-point zero test of [HasReciprocal]: not finite, or approximately zero.
     */
    private val realIsZero: UnaryOp<Real, Boolean> =
        UnaryOp { !it.isFinite() || RealAlgebras.eqRealApprox(it, 0.0) }

    /**
     * Dual numbers over the reals with reciprocals following the floating-point policy of
     * [HasReciprocal].
     *
     * Algebraically, `a + bε` is invertible iff `a ≠ 0`. Numerically, it counts as invertible iff
     * `a` is finite and not approximately zero *and* the inverse `a⁻¹ - (b/a²)ε` is finite: unlike
     * the norm-based algebras, the tangent part `b/a²` can overflow even when `a` is safely nonzero,
     * so the predicate checks it directly.
     */
    object DualRealRing : DualCommutativeRingWithReciprocal<Real>(RealAlgebras.RealField, realIsZero) {
        override fun hasReciprocal(a: Dual<Real>): Boolean =
            super.hasReciprocal(a) && a.b.isFinite() && (a.b / (a.a * a.a)).isFinite()
    }

    /**
     * The regular-representation matrix embedding into `M₂(R)` for the basis `{1, ε}` with `ε² = 0`:
     * ```text
     * a + bε ↦ [[a, 0], [b, a]]
     * ```
     * The columns are the coordinates of `(a + bε)·1` and `(a + bε)·ε`. This is the same
     * [quadraticRank2MatrixEmbedding] used for the complex (`ε² = -1`) and split-complex
     * (`ε² = 1`) numbers.
     */
    fun <F : Any> dualToRank2MatrixMonomorphism(
        base: CommutativeRing<F>
    ): RingMonomorphism<Dual<F>, DenseMat<F>> =
        quadraticRank2MatrixEmbedding(
            domain = base.dualRing(),
            coefficientRing = base,
            s = base.zero,
            t = base.zero,
            coeffs = UnaryOp { (a, b) -> a to b }
        )

    /**
     * Eqs
     */
    fun <A : Any> dualEq(
        eqA: Eq<A>
    ): Eq<Dual<A>> =
        CD.eq(eqA).contramap(Dual<A>::cd)

    val eqDualReal: Eq<Dual<Real>> =
        dualEq(RealAlgebras.eqRealApprox)

    val eqDualRealStrict: Eq<Dual<Real>> =
        dualEq(RealAlgebras.eqRealStrict)

    /**
     * Printables: `a + bε` rendered as a linear combination over the basis `{1, ε}`.
     */
    fun <F : Any> printableDual(
        signed: SignedOps<F>,
        zero: F,
        one: F,
        prF: Printable<F>,
        eqF: Eq<F>
    ): Printable<Dual<F>> =
        ComplexPrintable.complexLikePrintable(
            signed = signed,
            zero = zero,
            one = one,
            re = UnaryOp { it.a },
            im = UnaryOp { it.b },
            basis = Symbols.EPSILON,
            prA = prF,
            eqA = eqF
        )

    private fun printableDualRealGenerator(
        prReal: Printable<Real>,
        eqReal: Eq<Real>
    ): Printable<Dual<Real>> =
        printableDual(
            signed = RealAlgebras.SignedReal,
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            prF = prReal,
            eqF = eqReal
        )

    val printableDualReal: Printable<Dual<Real>> =
        printableDualRealGenerator(
            prReal = RealAlgebras.printableReal,
            eqReal = RealAlgebras.eqRealApprox
        )

    val printableDualRealStrict: Printable<Dual<Real>> =
        printableDualRealGenerator(
            prReal = RealAlgebras.printableRealStrict,
            eqReal = RealAlgebras.eqRealStrict
        )

    val printableDualRealPretty: Printable<Dual<Real>> =
        printableDualRealGenerator(
            prReal = RealAlgebras.printableRealPretty,
            eqReal = RealAlgebras.eqRealApprox
        )
}
