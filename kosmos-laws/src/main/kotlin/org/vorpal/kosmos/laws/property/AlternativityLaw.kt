package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface AlternativityCore<A : Any> {
    val op: BinOp<A>
    val arb: Arb<A>
    val eq: Eq<A>
    val pr: Printable<A>

    private fun expr(l: String, r: String): String = "$l ${op.symbol} $r"

    /**
     * Left alternativity: `x(xy) = (xx)y`
     */
    suspend fun leftAlternativityCheck() {
        checkAll(TestingLaw.arbPair(arb)) { (x, y) ->
            val xy = op(x, y)
            val left = op(x, xy)

            val xx = op(x, x)
            val right = op(xx, y)

            withClue(leftAlternativityFailure(x, y, xy, left, xx, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun leftAlternativityFailure(
        x: A, y: A,
        xy: A, left: A,
        xx: A, right: A
    ): () -> String = {
        buildString {
            val sx = pr(x)
            val sy = pr(y)
            val sxy = pr(xy)
            val sxx = pr(xx)
            val sLeft = pr(left)
            val sRight = pr(right)

            appendLine("Left alternativity failed:")

            append(expr(sx, "(" + expr(sx, sy) + ")"))
            append(" = ")
            append(expr(sx, sxy))
            append(" = ")
            appendLine(sLeft)

            append(expr("(" + expr(sx, sx) + ")", sy))
            append(" = ")
            append(expr(sxx, sy))
            append(" = ")
            appendLine(sRight)

            appendLine("Expected: $sLeft = $sRight")
        }
    }

    /**
     * Right alternativity: `(yx)x = y(xx)`
     */
    suspend fun rightAlternativityCheck() {
        checkAll(TestingLaw.arbPair(arb)) { (x, y) ->
            // (yx)x = y(xx)
            val yx = op(y, x)
            val left = op(yx, x)

            val xx = op(x, x)
            val right = op(y, xx)

            withClue(rightAlternativityFailure(x, y, yx, left, xx, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun rightAlternativityFailure(
        x: A, y: A,
        yx: A, left: A,
        xx: A, right: A
    ): () -> String = {
        buildString {
            val sx = pr(x)
            val sy = pr(y)
            val syx = pr(yx)
            val sxx = pr(xx)
            val sLeft = pr(left)
            val sRight = pr(right)

            appendLine("Right alternativity failed:")

            append(expr("(" + expr(sy, sx) + ")", sx))
            append(" = ")
            append(expr(syx, sx))
            append(" = ")
            appendLine(sLeft)

            append(expr(sy, "(" + expr(sx, sx) + ")"))
            append(" = ")
            append(expr(sy, sxx))
            append(" = ")
            appendLine(sRight)

            appendLine("Expected: $sLeft = $sRight")
        }
    }
}

/**
 * Left alternativity: `x(xy) = (xx)y`
 */
class LeftAlternativityLaw<A : Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
): TestingLaw, AlternativityCore<A> {
    override val name = "left alternativity (${op.symbol})"
    override suspend fun test() = leftAlternativityCheck()
}

/**
 * Right alternativity: `(yx)x = y(xx)`
 */
class RightAlternativityLaw<A : Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
): TestingLaw, AlternativityCore<A> {
    override val name = "right alternativity (${op.symbol})"
    override suspend fun test() = rightAlternativityCheck()
}

/**
 * Alternativity is a condition that is implied by but strictly weaker than associativity.
 *
 * The classic example is the octonions: they are nonassociative, but alternative.
 *
 * The following conditions must be met:
 * - Left alternativity: `x(xy) = (xx)y`
 * - Right alternativity: `(yx)x = y(xx)`
 *
 * for all `x`, `y` in the algebra.
 *
 * They are named as such because the associator is alternating:
 *
 *     [x, y, z] = (xy)z - x(yz)
 *
 * Alternativity is quite strong: it forces any two elements to generate an associative subalgebra
 * (see Artin's theorem).
 */
class AlternativityLaw<A : Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, AlternativityCore<A> {
    override val name = "alternativity (${op.symbol})"

    override suspend fun test() {
        leftAlternativityCheck()
        rightAlternativityCheck()
    }
}
