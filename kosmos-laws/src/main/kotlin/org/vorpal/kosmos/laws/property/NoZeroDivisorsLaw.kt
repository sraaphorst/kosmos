package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.testing.nonZeroBoth
import org.vorpal.kosmos.testing.nonZeroLeft
import org.vorpal.kosmos.testing.nonZeroRight

/** No zero divisors (two–sided):  (a ≠ 0 ∧ b ≠ 0) ⇒ a ⋆ b ≠ 0. */
class NoZeroDivisorsLaw<A>(
    private val op: BinOp<A>,
    private val zero: A,
    private val pairArb: Arb<Pair<A, A>>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆",
) : TestingLaw {

    override val name = "no zero divisors ($symbol)"

    override suspend fun test() {
        checkAll(pairArb.nonZeroBoth(eq, zero)) { (a, b) ->
            val prod = op.combine(a, b)
            withClue({
                val sa = pr.render(a); val sb = pr.render(b); val s0 = pr.render(zero)
                "Zero divisor found: $sa $symbol $sb = $s0 with $sa ≠ $s0 and $sb ≠ $s0"
            }) {
                check(!eq.eqv(prod, zero))
            }
        }
    }
}

/** No left zero divisors: (a ≠ 0 ∧ a ⋆ b = 0) ⇒ b = 0. */
class LeftNoZeroDivisorsLaw<A>(
    private val op: BinOp<A>,
    private val zero: A,
    private val pairArb: Arb<Pair<A, A>>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆",
) : TestingLaw {

    override val name = "no left zero divisors ($symbol)"

    override suspend fun test() {
        checkAll(pairArb.nonZeroLeft(eq, zero)) { (a, b) ->
            val prod = op.combine(a, b)
            if (eq.eqv(prod, zero)) {
                withClue({
                    val sa = pr.render(a); val sb = pr.render(b); val s0 = pr.render(zero)
                    "Left zero divisor: $sa $symbol $sb = $s0 with $sa ≠ $s0 ⇒ must have $sb = $s0"
                }) {
                    check(eq.eqv(b, zero))
                }
            }
        }
    }
}

/** No right zero divisors: (b ≠ 0 ∧ a ⋆ b = 0) ⇒ a = 0. */
class RightNoZeroDivisorsLaw<A>(
    private val op: BinOp<A>,
    private val zero: A,
    private val pairArb: Arb<Pair<A, A>>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆",
) : TestingLaw {

    override val name = "no right zero divisors ($symbol)"

    override suspend fun test() {
        checkAll(pairArb.nonZeroRight(eq, zero)) { (a, b) ->
            val prod = op.combine(a, b)
            if (eq.eqv(prod, zero)) {
                withClue({
                    val sa = pr.render(a); val sb = pr.render(b); val s0 = pr.render(zero)
                    "Right zero divisor: $sa $symbol $sb = $s0 with $sb ≠ $s0 ⇒ must have $sa = $s0"
                }) {
                    check(eq.eqv(a, zero))
                }
            }
        }
    }
}