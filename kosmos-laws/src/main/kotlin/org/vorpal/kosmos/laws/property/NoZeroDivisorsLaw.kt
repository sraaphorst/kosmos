package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** No zero divisors (two–sided):  (a ≠ 0 ∧ b ≠ 0) ⇒ a ⋆ b ≠ 0. */
class NoZeroDivisorsLaw<A: Any>(
    private val op: BinOp<A>,
    private val zero: A,
    arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    private val pairArb = TestingLaw.arbPair(arb).nonZeroBoth(eq, zero)
    override val name = "no zero divisors (${op.symbol})"

    override suspend fun test() {
        checkAll(pairArb) { (a, b) ->
            val prod = op(a, b)
            withClue({
                val sa = pr(a)
                val sb = pr(b)
                val s0 = pr(zero)
                "Zero divisor found: $sa ${op.symbol} $sb = $s0 with $sa ≠ $s0 and $sb ≠ $s0"
            }) {
                check(!eq(prod, zero))
            }
        }
    }
}

/** No left zero divisors: (a ≠ 0 ∧ a ⋆ b = 0) ⇒ b = 0. */
class LeftNoZeroDivisorsLaw<A: Any>(
    private val op: BinOp<A>,
    private val zero: A,
    arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    private val pairArb = TestingLaw.arbPair(arb).nonZeroLeft(eq, zero)
    override val name = "no left zero divisors (${op.symbol})"

    override suspend fun test() {
        checkAll(pairArb) { (a, b) ->
            val prod = op(a, b)
            if (eq(prod, zero)) {
                withClue({
                    val sa = pr(a)
                    val sb = pr(b)
                    val s0 = pr(zero)
                    "Left zero divisor: $sa ${op.symbol} $sb = $s0 with $sa ≠ $s0 ⇒ must have $sb = $s0"
                }) {
                    check(eq(b, zero))
                }
            }
        }
    }
}

/** No right zero divisors: (b ≠ 0 ∧ a ⋆ b = 0) ⇒ a = 0. */
class RightNoZeroDivisorsLaw<A: Any>(
    private val op: BinOp<A>,
    private val zero: A,
    arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    private val pairArb = TestingLaw.arbPair(arb).nonZeroRight(eq, zero)
    override val name = "no right zero divisors (${op.symbol})"

    override suspend fun test() {
        checkAll(pairArb) { (a, b) ->
            val prod = op(a, b)
            if (eq(prod, zero)) {
                withClue({
                    val sa = pr(a)
                    val sb = pr(b)
                    val s0 = pr(zero)
                    "Right zero divisor: $sa ${op.symbol} $sb = $s0 with $sb ≠ $s0 ⇒ must have $sa = $s0"
                }) {
                    check(eq(a, zero))
                }
            }
        }
    }
}

// Local helpers so this law does not depend on kosmos-testkit.
/** Create a generator that does not produce the value [zero] on the left. */
private fun <A : Any> Arb<Pair<A, A>>.nonZeroLeft(eq: Eq<A>, zero: A): Arb<Pair<A, A>> =
    filter { (a, _) -> !eq(a, zero) }

/** Create a generator that does not produce the value [zero] on the right. */
private fun <A : Any> Arb<Pair<A, A>>.nonZeroRight(eq: Eq<A>, zero: A): Arb<Pair<A, A>> =
    filter { (_, b) -> !eq(b, zero) }

/** Create a generator that does not produce the value [zero] on either side. */
private fun <A : Any> Arb<Pair<A, A>>.nonZeroBoth(eq: Eq<A>, zero: A): Arb<Pair<A, A>> =
    nonZeroLeft(eq, zero).nonZeroRight(eq, zero)
