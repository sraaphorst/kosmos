package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.triple
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.TestingLaw
import org.vorpal.kosmos.core.ops.BinOp

/** Distributivity Laws: the first operator, mul, distributes over the second, add.
 * This can be on the left, on the right, or both depending on the structure.
 * Note that we allow a Triple producing Arb so that we can impose constraints if necessary on the values produced,
 * e.g. that they all be distinct, or to avoid NaN / overflow for floating point types.
 * The checks are in an interface so that they can be reused across the three distributivity check variants. */
private interface DistributivityCore<A> {
    val mul: BinOp<A>
    val add: BinOp<A>
    val tripleArb: Arb<Triple<A, A, A>>
    val eq: Eq<A>

    suspend fun leftDistributivityCheck() {
        checkAll(tripleArb) { (a, b, c) ->
            val left = mul.combine(a, add.combine(b, c))
            val right = add.combine(mul.combine(a, b), mul.combine(a, c))
            withClue("Left distributivity failed: $a * ($b + $c) = $left, $a * $b + $a * $c = $right") {
                check(eq.eqv(left, right))
            }
        }
    }

    suspend fun rightDistributivityCheck() {
        checkAll(tripleArb) { (a, b, c) ->
            val left = mul.combine(add.combine(a, b), c)
            val right = add.combine(mul.combine(a, c), mul.combine(b, c))
            withClue("Right distributivity failed: ($a + $b) * $c = $left, $a * $c + $b * $c = $right") {
                check(eq.eqv(left, right))
            }
        }
    }
}

class LeftDistributivityLaw<A>(
    override val mul: BinOp<A>,
    override val add: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>
) : TestingLaw, DistributivityCore<A> {

    constructor(mul: BinOp<A>, add: BinOp<A>, arb: Arb<A>, eq: Eq<A>) :
            this(mul, add, Arb.triple(arb, arb, arb), eq)

    override val name = "distributivity (left)"
    override suspend fun test() = leftDistributivityCheck()
}

class RightDistributivityLaw<A>(
    override val mul: BinOp<A>,
    override val add: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>
) : TestingLaw, DistributivityCore<A> {

    constructor(mul: BinOp<A>, add: BinOp<A>, arb: Arb<A>, eq: Eq<A>) :
            this(mul, add, Arb.triple(arb, arb, arb), eq)

    override val name = "distributivity (right)"
    override suspend fun test() = rightDistributivityCheck()
}

class DistributivityLaw<A>(
    override val mul: BinOp<A>,
    override val add: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>
) : TestingLaw, DistributivityCore<A> {

    constructor(mul: BinOp<A>, add: BinOp<A>, arb: Arb<A>, eq: Eq<A>) :
            this(mul, add, Arb.triple(arb, arb, arb), eq)

    override val name = "distributivity (both)"
    override suspend fun test() {
        leftDistributivityCheck()
        rightDistributivityCheck()
    }
}

/*
    companion object {
        /** Convenience for a ring: mul distributes over add on both sides. */
        fun <A> forRing(
            R: Ring<A, Monoid<A, Mul>>,
            arb: Arb<A>,
            eq: Eq<A>
        ) = DistributivityLaws<A, Mul, Add>(
            left = R.mul,
            right = R.add,
            arb = arb,
            eq = eq
        )
    }
}
 */