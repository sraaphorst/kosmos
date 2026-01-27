package org.vorpal.kosmos.algebra.extensions

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

/**
 * Dual numbers over a base field [F].
 *
 * Carrier: `Dual(a, b)` represents `a + bε` with `ε² = 0`.
 *
 * The duals form a commutative ring with:
 * - addition:
 *
 *
 *    (a,b) + (c,d) = (a+c, b+d)
 *
 * - multiplication:
 *
 *
 *    (a,b)(c,d) = (ac, ad+bc)
 *
 * A dual number is invertible iff its real part `a` is nonzero in the base field.
 *
 * In that case:
 *
 *
 *    (a + bε)^{-1} = a^{-1} - (b / a^2) ε
 *
 *
 * Note that if `x = a + bε` then `f(x) = f(a) + bf(a)ε` from the Taylor expansion:
 *
 *
 *    f(a + h) = f(a) + f(a)h + f(a)h^2/2! + f(a)h^3/3! + ... = f(a) + f(a)(bε) + 0 + ...
 *
 * and this is how the autodifferentiation is achieved.
 */
class DualRing<F : Any>(
    private val base: Field<F>
) : CommutativeRing<Dual<F>> {

    override val add = AbelianGroup.of(
        identity = Dual(base.add.identity, base.add.identity),
        op = BinOp(Symbols.PLUS) { x, y ->
            Dual(base.add(x.a, y.a), base.add(x.b, y.b))
        },
        inverse = Endo(Symbols.MINUS) { x ->
            Dual(base.add.inverse(x.a),base.add.inverse(x.b))
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

    /**
     * Reciprocal in the dual ring.
     *
     * Defined iff the real part is nonzero in the base field.
     */
    fun reciprocalOrNull(x: Dual<F>): Dual<F>? {
        if (x.a == base.zero) return null

        val invA = base.reciprocal(x.a)
        val invA2 = base.mul(invA, invA)

        val minusB = base.add.inverse(x.b)
        val epsPart = base.mul(minusB, invA2)

        return Dual(invA, epsPart)
    }

    fun reciprocal(x: Dual<F>): Dual<F> =
        reciprocalOrNull(x)
            ?: throw ArithmeticException("Cannot invert dual number with zero real part: $x")

    override fun fromBigInt(n: BigInteger): Dual<F> =
        lift(base.fromBigInt(n))
}

/**
 * Dual carrier: a + bε.
 *
 * Values are intentionally dumb. Use [DualRing] for operations.
 */
data class Dual<F : Any>(
    val a: F,
    val b: F
)

/**
 * Convenience: build the dual ring over a base field.
 */
fun <F : Any> Field<F>.dual(): DualRing<F> =
    DualRing(this)

/**
 * Differentiate a scalar function f: Real -> Real at x.
 *
 * Returns f(x), f'(x).
 */
fun diffAt(
    f: (Dual<Real>) -> Dual<Real>,
    x: Real,
): Pair<Real, Real> {
    val d = RealAlgebras.RealField.dual()
    val xDual = d.add(d.lift(x), d.epsOne)   // x + 1ε
    val y = f(xDual)
    return y.a to y.b
}
