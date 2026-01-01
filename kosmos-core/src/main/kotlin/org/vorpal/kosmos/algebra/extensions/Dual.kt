package org.vorpal.kosmos.algebra.extensions

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

class DualRing<F: Any>(private val base: Field<F>): CommutativeRing<DualRing<F>.Dual> {
    inner class Dual(val a: F, val b: F) {
        operator fun component1(): F = a
        operator fun component2(): F = b

        operator fun plus(other: Dual): Dual =
            Dual(base.add(a, other.a), base.add(b, other.b))

        operator fun plus(scalar: F): Dual =
            this + lift(scalar)

        operator fun minus(other: Dual): Dual =
            this + (-other)

        operator fun minus(scalar: F): Dual =
            this - lift(scalar)

        // (a + bε)(c + dε) = ac + (ad + bc)ε
        operator fun times(other: Dual): Dual {
            val (c, d) = other
            val ac = base.mul(a, c)
            val epsPart = base.add(base.mul(a, d), base.mul(b, c))
            return Dual(ac, epsPart)
        }

        operator fun times(scalar: F): Dual =
            this * lift(scalar)

        operator fun unaryMinus(): Dual =
            Dual(base.add.inverse(a), base.add.inverse(b))

        // We test the inverse of the additive identity here to cover the case of Real
        // where a could be 0.0 or -0.0.
        fun isInvertible(): Boolean =
            a != base.add.identity && a != base.add.inverse(base.add.identity)

        fun reciprocalOrNull(): Dual? {
            if (!isInvertible()) return null

            // invA = a^{-1}
            val invA = base.reciprocal(a)
            // invA2 = a^{-2}
            val invA2 = base.mul(invA, invA)

            // eps part = - b / a^2 = (-b) * a^{-2}
            val minusB = base.add.inverse(b)
            val epsPart = base.mul(minusB, invA2)

            return Dual(invA, epsPart)
        }

        fun reciprocal(): Dual =
            reciprocalOrNull() ?: throw ArithmeticException("Cannot take inverse of $this.")

        fun divOrNull(other: Dual): Dual? =
            other.reciprocalOrNull()?.let { this * it }

        operator fun div(other: Dual): Dual =
            this * other.reciprocal()

        fun divOrNull(scalar: F): Dual? =
            lift(scalar).reciprocalOrNull()?.let { this * it }

        operator fun div(scalar: F): Dual =
            divOrNull(scalar) ?: throw ArithmeticException("Cannot divide by $scalar.")

        override fun toString() = "$a + ${b}ε"
    }

    override val add: AbelianGroup<Dual> = AbelianGroup.of(
        identity = Dual(base.add.identity, base.add.identity),
        op = BinOp(Symbols.PLUS) { d1, d2 ->
            Dual(base.add(d1.a, d2.a), base.add(d1.b, d2.b)) },
        inverse = Endo(Symbols.MINUS) { d -> Dual(base.add.inverse(d.a), base.add.inverse(d.b)) }
    )

    override val mul: CommutativeMonoid<Dual> = CommutativeMonoid.of(
        identity = Dual(base.mul.identity, base.add.identity),
        op = BinOp(Symbols.ASTERISK) { d1, d2 ->
            val (a1, b1) = d1
            val (a2, b2) = d2
            val a = base.mul(a1, a2)
            val b = base.add(base.mul(a1, b2), base.mul(b1, a2))
            Dual(a, b)
        }
    )

    /**
     * Embed an element of the base field into the dual ring as `a + 0ε`.
     *
     * The canonical injection `F -> F[ε]/(ε^2)`.
     *
     * @param a The element of the base field to embed.
     * @return The dual number `a + 0ε`, representing a purely real element.
     */
    fun lift(a: F): Dual = Dual(a, base.add.identity)

    /**
     * Create a pure infinitesimal element `0 + bε` with the given coefficient b.
     *
     * The canonical injection `F -> F[ε]`.
     *
     * @param b The coefficient of the infinitesimal part.
     * @return The dual number `0 + bε`.
     */
    fun eps(b: F): Dual = Dual(base.add.identity, b)

    /**
     * The canonical infinitesimal ε = 0 + 1ε.
     *
     * This is the element that satisfies ε² = 0.
     */
    val e = eps(base.mul.identity)
}

/**
 * Given a Field F, create a dual commutative ring over it of the form `F[ε](ε^2)`.
 */
fun <F: Any> Field<F>.dual(): DualRing<F> =
    DualRing(this)

operator fun <F: Any> F.plus(dual: DualRing<F>.Dual): DualRing<F>.Dual =
    dual + this

operator fun <F: Any> F.minus(dual: DualRing<F>.Dual): DualRing<F>.Dual =
    (-dual) + this

operator fun <F: Any> F.times(dual: DualRing<F>.Dual): DualRing<F>.Dual =
    dual * this

fun <F: Any> F.divOrNull(dual: DualRing<F>.Dual): DualRing<F>.Dual? =
    dual.reciprocalOrNull()?.let { it * this }

operator fun <F: Any> F.div(dual: DualRing<F>.Dual): DualRing<F>.Dual =
    this.divOrNull(dual) ?: throw ArithmeticException("Cannot divide by $dual.")
