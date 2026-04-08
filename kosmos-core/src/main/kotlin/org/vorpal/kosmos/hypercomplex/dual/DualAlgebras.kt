package org.vorpal.kosmos.hypercomplex.dual

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.render.Printable
import java.math.BigInteger

object DualAlgebras {
    /**
     * Dual numbers over a base field [F].
     *
     * Carrier: `Dual(a, b)` represents `a + bε` with `ε² = 0`.
     *
     * The duals form a commutative ring with:
     * - addition:
     * ```text
     * (a,b) + (c,d) = (a+c, b+d)
     * ```
     * - multiplication:
     * ```text
     * (a,b)(c,d) = (ac, ad+bc)
     * ```
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
    class DualRing<F : Any>(
        private val base: Field<F>,
        private val isZero: (F) -> Boolean = { it == base.zero }
    ) : CommutativeRing<Dual<F>>, HasReciprocal<Dual<F>> {

        override val add = AbelianGroup.of(
            identity = Dual(base.add.identity, base.add.identity),
            op = BinOp(Symbols.PLUS) { x, y ->
                Dual(base.add(x.a, y.a), base.add(x.b, y.b))
            },
            inverse = Endo(Symbols.MINUS) { x ->
                Dual(base.add.inverse(x.a), base.add.inverse(x.b))
            }
        )

        override val mul = CommutativeMonoid.of(
            identity = Dual(base.mul.identity, base.add.identity),
            op = BinOp(Symbols.ASTERISK) { x, y ->
                // (a + bε)(c + dε) = ac + (ad + bc)ε
                val ac = base.mul(x.a, y.a)
                val ad = base.mul(x.a, y.b)
                val bc = base.mul(x.b, y.a)
                val eps = base.add(ad, bc)

                Dual(ac, eps)
            }
        )

        /**
         * Embed an element a ∈ F as a + 0ε.
         */
        fun lift(a: F): Dual<F> =
            Dual(a, base.add.identity)

        /**
         * Build a pure infinitesimal 0 + bε.
         */
        fun eps(b: F): Dual<F> =
            Dual(base.add.identity, b)

        /**
         * The canonical infinitesimal ε = 0 + 1ε.
         */
        val epsOne: Dual<F>
            get() = eps(base.mul.identity)

        override fun hasReciprocal(a: Dual<F>): Boolean =
            !isZero(a.a)

        override val reciprocal: Endo<Dual<F>> = Endo(Symbols.INVERSE) { x ->
            if (!hasReciprocal(x)) throw ArithmeticException("Cannot invert dual number with zero real part: $x")

            val invA = base.reciprocal(x.a)
            val invA2 = base.mul(invA, invA)

            val minusB = base.add.inverse(x.b)
            val epsPart = base.mul(minusB, invA2)

            Dual(invA, epsPart)
        }

        override fun fromBigInt(n: BigInteger): Dual<F> =
            lift(base.fromBigInt(n))
    }

    /**
     * Convenience function to build the dual ring over a base field.
     */
    fun <F : Any> Field<F>.dual(
        isZero: (F) -> Boolean = { it == zero }
    ): DualRing<F> =
        DualRing(this, isZero)

    /**
     * Differentiate a function represented on dual numbers, corresponding to a scalar map `F → F`
     * at a point `x`.
     *
     * Returns `f(x), f'(x)`.
     */
    fun <F : Any> diffAt(
        field: Field<F>,
        f: (Dual<F>) -> Dual<F>,
        x: F
    ): Pair<F, F> {
        val d = field.dual()
        val xDual = d.add(d.lift(x), d.epsOne)
        val y = f(xDual)
        return y.a to y.b
    }

    /**
     * Differentiate a function `f` represented on dual numbers, corresponding to a scalar map `Real → Real`
     * at a point `x`.
     *
     * Returns `f(x), f'(x)`.
     */
    fun diffAtReal(
        f: (Dual<Real>) -> Dual<Real>,
        x: Real
    ): Pair<Real, Real> =
        diffAt(RealAlgebras.RealField, f, x)

    fun <A : Any> dualEq(
        eqA: Eq<A>
    ): Eq<Dual<A>> = Eq {
        x, y -> eqA(x.a, y.a) && eqA(x.b, y.b)
    }

    val eqDualReal: Eq<Dual<Real>> =
        dualEq(RealAlgebras.eqRealApprox)

    /** Always prints "a + bε". */
    fun <F : Any> liftPrintable(prF: Printable<F>): Printable<Dual<F>> =
        Printable { x ->
            "${prF(x.a)} + ${prF(x.b)}${Symbols.EPSILON}"
        }

    /**
     * Prints "a - |b|ε" iff rendered b starts with '-'; otherwise "a + bε".
     * Purely presentational: does not assume algebraic sign.
     */
    fun <F : Any> liftSigned(prF: Printable<F>): Printable<Dual<F>> =
        Printable { x ->
            val a = prF(x.a)
            val b = prF(x.b)

            if (b.startsWith("-")) "$a - ${b.drop(1).trimStart()}${Symbols.EPSILON}"
            else "$a + $b${Symbols.EPSILON}"
        }

    /**
     * Compact form: drops zero parts, prints ε for coefficient 1.
     * Does NOT introduce '-' based on negOne; stays algebraically neutral.
     */
    fun <F : Any> compact(
        zero: F,
        one: F,
        prF: Printable<F>,
        eq: Eq<F> = Eq.default(),
    ): Printable<Dual<F>> =
        Printable { x ->
            val a0 = eq(x.a, zero)
            val b0 = eq(x.b, zero)
            val b1 = eq(x.b, one)

            when {
                a0 && b0 -> prF(zero)                             // 0
                a0 && b1 -> Symbols.EPSILON                           // ε
                a0       -> "${prF(x.b)}${Symbols.EPSILON}"       // bε
                b0       -> prF(x.a)                              // a
                b1       -> "${prF(x.a)} + ${Symbols.EPSILON}"    // a + ε
                else     -> "${prF(x.a)} + ${prF(x.b)}${Symbols.EPSILON}"
            }
        }

    /**
     * Compact + string-minus prettiness.
     * Uses '-' only if the rendered coefficient starts with '-'.
     */
    fun <F : Any> compactSigned(
        zero: F,
        one: F,
        prF: Printable<F>,
        eq: Eq<F> = Eq.default(),
    ): Printable<Dual<F>> =
        Printable { x ->
            val a0 = eq(x.a, zero)
            val b0 = eq(x.b, zero)
            val b1 = eq(x.b, one)

            when {
                a0 && b0 -> prF(zero)
                a0 && b1 -> Symbols.EPSILON
                a0       -> "${prF(x.b)}${Symbols.EPSILON}"
                b0       -> prF(x.a)
                b1       -> "${prF(x.a)} + ${Symbols.EPSILON}"
                else     -> {
                    val a = prF(x.a)
                    val b = prF(x.b)

                    if (b.startsWith("-")) "$a - ${b.drop(1).trimStart()}${Symbols.EPSILON}"
                    else "$a + $b${Symbols.EPSILON}"
                }
            }
        }

    val printableDualReal: Printable<Dual<Real>> =
        liftPrintable(org.vorpal.kosmos.core.render.Printables.real)

    val printableDualRealSigned: Printable<Dual<Real>> =
        liftSigned(org.vorpal.kosmos.core.render.Printables.real)

    val printableDualRealCompact: Printable<Dual<Real>> =
        compact(
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            prF = org.vorpal.kosmos.core.render.Printables.real,
            eq = RealAlgebras.eqRealApprox
        )

    val printableDualRealCompactSigned: Printable<Dual<Real>> =
        compactSigned(
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            prF = org.vorpal.kosmos.core.render.Printables.real,
            eq = RealAlgebras.eqRealApprox
        )
}
