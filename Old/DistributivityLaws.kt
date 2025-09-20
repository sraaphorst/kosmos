package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.OpTag

/**
 * Laws for distributivity of a “left” op (⋆) over a “right” op (◦):
 *
 * Left-distributivity:  a ⋆ (b ◦ c) = (a ⋆ b) ◦ (a ⋆ c)
 * Right-distributivity: (a ◦ b) ⋆ c = (a ⋆ c) ◦ (b ⋆ c)
 *
 * Typically: (⋆,◦) = (mul,add).
 */
class DistributivityLaws<A, LTag, RTag>(
    private val left: BinOp<A>,     // ⋆
    private val right: BinOp<A>,    // ◦
    private val arb: Arb<A>,
    private val eq: Eq<A>
) where LTag : OpTag, RTag : OpTag {

    suspend fun leftOverRight() = checkAll(arb, arb, arb) { a, b, c ->
        val l = left.combine(a, right.combine(b, c))
        val r = right.combine(left.combine(a, b), left.combine(a, c))
        eq.assertEquals(l, r)
    }

    suspend fun rightOverRight() = checkAll(arb, arb, arb) { a, b, c ->
        val l = left.combine(right.combine(a, b), c)
        val r = right.combine(left.combine(a, c), left.combine(b, c))
        eq.assertEquals(l, r)
    }

    suspend fun bothSides() {
        leftOverRight()
        rightOverRight()
    }

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