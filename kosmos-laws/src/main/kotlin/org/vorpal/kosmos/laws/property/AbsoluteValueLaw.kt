package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

/*
 * Laws of an absolute value `|·|` on an ordered ring, with values in the ring itself (e.g. ℤ, ℚ, ℝ).
 */

/** Nonnegativity: `0 ≤ |a|`. */
class AbsNonnegativeLaw<A : Any>(
    private val abs: Endo<A>,
    private val zero: A,
    private val le: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    override val binding = LawBinding.of(this, abs, zero, le, arb)

    override val name = "nonnegativity (${abs.symbol})"

    override suspend fun test() {
        lawCheckAll(arb, render = renderWith(pr)) { a ->
            val v = abs(a)
            lawCheck(le(zero, v)) { "${abs.symbol}(${pr(a)}) = ${pr(v)} is negative" }
        }
    }
}

/** Definiteness: `|a| = 0 ⇔ a = 0`. Also checks `|0| = 0` directly, in case the generator rarely draws 0. */
class AbsDefiniteLaw<A : Any>(
    private val abs: Endo<A>,
    private val zero: A,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    override val binding = LawBinding.of(this, abs, zero, arb, eq)

    override val name = "definiteness (${abs.symbol})"

    override suspend fun test() {
        val absZero = abs(zero)
        lawCheck(eq(absZero, zero)) { "${abs.symbol}(${pr(zero)}) = ${pr(absZero)}, expected ${pr(zero)}" }
        lawCheckAll(arb, render = renderWith(pr)) { a ->
            val v = abs(a)
            lawCheck(eq(v, zero) == eq(a, zero)) {
                "${abs.symbol}(${pr(a)}) = ${pr(v)}: |a| = 0 should hold exactly when a = 0"
            }
        }
    }
}

/** Multiplicativity: `|ab| = |a||b|`. */
class AbsMultiplicativeLaw<A : Any>(
    private val abs: Endo<A>,
    private val mul: BinOp<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    override val binding = LawBinding.of(this, abs, mul, arb, eq)

    override val name = "multiplicativity (${abs.symbol}, ${mul.symbol})"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbPair(arb), render = renderWith(pr)) { (a, b) ->
            val left = abs(mul(a, b))
            val right = mul(abs(a), abs(b))
            lawCheck(eq(left, right)) {
                "${abs.symbol}(${pr(a)} ${mul.symbol} ${pr(b)}) = ${pr(left)}, but " +
                    "${abs.symbol}(${pr(a)}) ${mul.symbol} ${abs.symbol}(${pr(b)}) = ${pr(right)}"
            }
        }
    }
}

/** Triangle inequality: `|a + b| ≤ |a| + |b|`. */
class TriangleInequalityLaw<A : Any>(
    private val abs: Endo<A>,
    private val add: BinOp<A>,
    private val le: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    override val binding = LawBinding.of(this, abs, add, le, arb)

    override val name = "triangle inequality (${abs.symbol}, ${add.symbol})"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbPair(arb), render = renderWith(pr)) { (a, b) ->
            val left = abs(add(a, b))
            val right = add(abs(a), abs(b))
            lawCheck(le(left, right)) {
                "${abs.symbol}(${pr(a)} ${add.symbol} ${pr(b)}) = ${pr(left)} is not ${le.symbol} " +
                    "${abs.symbol}(${pr(a)}) ${add.symbol} ${abs.symbol}(${pr(b)}) = ${pr(right)}"
            }
        }
    }
}

/** The absolute value of an ordered ring: `|a| = a` if `0 ≤ a`, and `|a| = -a` otherwise. */
class AbsFromOrderLaw<A : Any>(
    private val abs: Endo<A>,
    private val zero: A,
    private val neg: Endo<A>,
    private val le: Relation<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    override val binding = LawBinding.of(this, abs, zero, neg, le, arb, eq)

    override val name = "agrees with the order (${abs.symbol}, ${le.symbol})"

    override suspend fun test() {
        lawCheckAll(arb, render = renderWith(pr)) { a ->
            val expected = if (le(zero, a)) a else neg(a)
            val v = abs(a)
            lawCheck(eq(v, expected)) { "${abs.symbol}(${pr(a)}) = ${pr(v)}, expected ${pr(expected)}" }
        }
    }
}
