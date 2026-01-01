package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface InvertibilityCore<A : Any> {
    val op: BinOp<A>
    val identity: A
    val inverseOrNull: UnaryOp<A, A?>
    val arb: Arb<A>
    val eq: Eq<A>
    val pr: Printable<A>

    private fun expr(left: String, right: String) = "$left ${op.symbol} $right"
    private fun arbInv(): Arb<Pair<A, A>> =
        arb.map { a -> Pair(a, inverseOrNull(a)) }
            .filter { (_, aInv) -> aInv != null }
            .map { (a, aInv) -> a to aInv!! }

    suspend fun leftInverseCheck() {
        checkAll(arbInv()) { (a, aInv) ->
            val right = op(aInv, a)
            withClue(leftInverseFailure(a, aInv, right)) {
                check(eq(right, identity))
            }
        }
    }

    private fun leftInverseFailure(
        a: A, aInv: A, right: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val saInv = pr(aInv)
            val sRight = pr(right)
            val sId = pr(identity)

            appendLine("Left invertibility failed:")

            append(expr("inv($sa)", sa))
            append(" = ")
            append(expr(saInv, sa))
            append(" = ")
            append(sRight)
            append(" (expected: $sId)")
            appendLine()
        }
    }

    suspend fun rightInverseCheck() {
        checkAll(arbInv()) { (a, aInv) ->
            val right = op(a, aInv)
            withClue(rightInverseFailure(a, aInv, right)) {
                check(eq(right, identity))
            }
        }
    }

    private fun rightInverseFailure(
        a: A, aInv: A, right: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val saInv = pr(aInv)
            val sRight = pr(right)
            val sId = pr(identity)

            appendLine("Right invertibility failed:")

            append(expr(sa, "inv($sa)"))
            append(" = ")
            append(expr(sa, saInv))
            append(" = ")
            append(sRight)
            append(" (expected: $sId)")
            appendLine()
        }
    }
}

/**
 * Left invertibility:
 *
 * For each `a` where `inverseOrNull(a)` returns a value `b`, check that, for the identity `e`:
 *
 *     ba = e
 */
class LeftInvertibilityLaw<A : Any>(
    override val op: BinOp<A>,
    override val identity: A,
    override val arb: Arb<A>,
    override val inverseOrNull: UnaryOp<A, A?>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, InvertibilityCore<A> {
    override val name: String = "left invertibility (${op.symbol})"
    override suspend fun test() = leftInverseCheck()
}

/**
 * Right invertibility:
 *
 * For each `a` where `inverseOrNull(a)` returns a value `b`, check that, for the identity `e`:
 *
 *     ab = e
 */
class RightInvertibilityLaw<A : Any>(
    override val op: BinOp<A>,
    override val identity: A,
    override val arb: Arb<A>,
    override val inverseOrNull: UnaryOp<A, A?>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, InvertibilityCore<A> {
    override val name: String = "right invertibility (${op.symbol})"
    override suspend fun test() = rightInverseCheck()
}

/**
 * Invertibility:
 *
 * For each `a` where `inverseOrNull(a)` returns a value `b`, check that, for the identity `e`:
 *
 *     ba = e
 *     ab = e
 */
class InvertibilityLaw<A : Any>(
    override val op: BinOp<A>,
    override val identity: A,
    override val arb: Arb<A>,
    override val inverseOrNull: UnaryOp<A, A?>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, InvertibilityCore<A> {
    override val name: String = "invertibility (${op.symbol})"
    override suspend fun test() {
        leftInverseCheck()
        rightInverseCheck()
    }
}

/**
 * Helper function to turn an Endo<A> into a UnaryOp<A, A?> to make complete [Endo] types fit.
 */
fun <A : Any> Endo<A>.asInverseOrNull(): UnaryOp<A, A?> =
    UnaryOp(this.symbol) { a -> this(a) }
