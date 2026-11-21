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
 * Laws tying a total order `≤` to ring operations. They are implications ("if a ≤ b then ..."); rather than
 * filtering random samples on the premise, each law arranges its samples so that the premise usually holds
 * (it swaps a pair into order, or negates a sample to make it nonnegative), and skips the rare sample where it
 * still does not. So the laws do not rely on the order being correct to be meaningful: the premise is always
 * checked with the relation under test.
 */

/** `(a, b)` arranged so that `a ≤ b` if the relation relates them either way. */
private fun <A : Any> Relation<A>.ordered(a: A, b: A): Pair<A, A> =
    if (this(a, b)) a to b else b to a

/** `a < b` as `a ≤ b ∧ a ≠ b`. */
private fun <A : Any> Relation<A>.strictly(eq: Eq<A>, a: A, b: A): Boolean =
    this(a, b) && !eq(a, b)

/**
 * Additive monotonicity (ordered group axiom): `a ≤ b ⇒ a + c ≤ b + c`.
 */
class AdditiveMonotonicityLaw<A : Any>(
    private val add: BinOp<A>,
    private val le: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    override val binding = LawBinding.of(this, add, le, arb)

    override val name = "monotonicity (${le.symbol}, ${add.symbol})"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbTriple(arb), render = renderWith(pr)) { (x, y, c) ->
            val (a, b) = le.ordered(x, y)
            if (!le(a, b)) return@lawCheckAll
            val ac = add(a, c)
            val bc = add(b, c)
            lawCheck(le(ac, bc)) {
                "${pr(a)} ${le.symbol} ${pr(b)}, but ${pr(a)} ${add.symbol} ${pr(c)} = ${pr(ac)} is not " +
                    "${le.symbol} ${pr(b)} ${add.symbol} ${pr(c)} = ${pr(bc)}"
            }
        }
    }
}

/**
 * Nonnegative products (ordered ring axiom): `0 ≤ a ∧ 0 ≤ b ⇒ 0 ≤ ab`.
 *
 * A sample that is not `≥ 0` is replaced by its additive inverse.
 */
class NonnegativeProductLaw<A : Any>(
    private val mul: BinOp<A>,
    private val zero: A,
    private val neg: Endo<A>,
    private val le: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    override val binding = LawBinding.of(this, mul, zero, neg, le, arb)

    override val name = "nonnegative products (${le.symbol}, ${mul.symbol})"

    private fun nonnegative(x: A): A = if (le(zero, x)) x else neg(x)

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbPair(arb), render = renderWith(pr)) { (x, y) ->
            val a = nonnegative(x)
            val b = nonnegative(y)
            if (!le(zero, a) || !le(zero, b)) return@lawCheckAll
            val ab = mul(a, b)
            lawCheck(le(zero, ab)) {
                "${pr(zero)} ${le.symbol} ${pr(a)} and ${pr(zero)} ${le.symbol} ${pr(b)}, " +
                    "but ${pr(zero)} ${le.symbol} ${pr(a)} ${mul.symbol} ${pr(b)} = ${pr(ab)} fails"
            }
        }
    }
}

/**
 * Scaling by a positive element preserves strict order (derived in an ordered ring):
 * `a < b ∧ 0 < c ⇒ ac < bc`.
 *
 * Strict order is `≤` and not equal. A sample `c` that is not `≥ 0` is replaced by its additive inverse.
 */
class PositiveScalingLaw<A : Any>(
    private val mul: BinOp<A>,
    private val zero: A,
    private val neg: Endo<A>,
    private val le: Relation<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    override val binding = LawBinding.of(this, mul, zero, neg, le, arb, eq)

    override val name = "positive scaling preserves strict order (${le.symbol}, ${mul.symbol})"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbTriple(arb), render = renderWith(pr)) { (x, y, z) ->
            val (a, b) = le.ordered(x, y)
            val c = if (le(zero, z)) z else neg(z)
            if (!le.strictly(eq, a, b) || !le.strictly(eq, zero, c)) return@lawCheckAll
            val ac = mul(a, c)
            val bc = mul(b, c)
            lawCheck(le.strictly(eq, ac, bc)) {
                "${pr(a)} < ${pr(b)} and ${pr(zero)} < ${pr(c)}, but ${pr(a)} ${mul.symbol} ${pr(c)} = ${pr(ac)} " +
                    "is not < ${pr(b)} ${mul.symbol} ${pr(c)} = ${pr(bc)}"
            }
        }
    }
}

/**
 * Scaling by a negative element reverses strict order (derived in an ordered ring):
 * `a < b ∧ c < 0 ⇒ bc < ac`.
 *
 * Strict order is `≤` and not equal. A sample `c` that is not `≤ 0` is replaced by its additive inverse.
 */
class NegativeScalingLaw<A : Any>(
    private val mul: BinOp<A>,
    private val zero: A,
    private val neg: Endo<A>,
    private val le: Relation<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    override val binding = LawBinding.of(this, mul, zero, neg, le, arb, eq)

    override val name = "negative scaling reverses strict order (${le.symbol}, ${mul.symbol})"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbTriple(arb), render = renderWith(pr)) { (x, y, z) ->
            val (a, b) = le.ordered(x, y)
            val c = if (le(z, zero)) z else neg(z)
            if (!le.strictly(eq, a, b) || !le.strictly(eq, c, zero)) return@lawCheckAll
            val ac = mul(a, c)
            val bc = mul(b, c)
            lawCheck(le.strictly(eq, bc, ac)) {
                "${pr(a)} < ${pr(b)} and ${pr(c)} < ${pr(zero)}, but ${pr(b)} ${mul.symbol} ${pr(c)} = ${pr(bc)} " +
                    "is not < ${pr(a)} ${mul.symbol} ${pr(c)} = ${pr(ac)}"
            }
        }
    }
}
