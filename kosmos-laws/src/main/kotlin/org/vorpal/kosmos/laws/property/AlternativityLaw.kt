package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface AlternativityCore<A> {
    val op: BinOp<A>
    val pairArb: Arb<Pair<A, A>>
    val eq: Eq<A>
    val pr: Printable<A>
    val symbol: String

    private fun infix(l: String, r: String) = "$l $symbol $r"

    suspend fun leftAlternativityCheck() {
        checkAll(pairArb) { (x, y) ->
            val xy = op.combine(x, y)
            val left = op.combine(x, xy)

            val xx = op.combine(x, x)
            val right = op.combine(xx, y)

            withClue(leftFailureMessage(x, y, xy, left, xx, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    private fun leftFailureMessage(
        x: A, y: A,
        xy: A, left: A,
        xx: A, right: A
    ): () -> String = {
        val sx = pr.render(x)
        val sy = pr.render(y)
        val sxy = pr.render(xy)
        val sxx = pr.render(xx)
        val sLeft = pr.render(left)
        val sRight = pr.render(right)

        buildString {
            appendLine("Left alternativity failed:")

            append(infix(sx, "(" + infix(sx, sy) + ")"))
            append(" = ")
            append(infix(sx, sxy))
            append(" = ")
            append(sLeft)
            appendLine()

            append(infix("(" + infix(sx, sx) + ")", sy))
            append(" = ")
            append(infix(sxx, sy))
            append(" = ")
            append(sRight)
            appendLine()

            append("Expected: $sLeft = $sRight")
            appendLine()
        }
    }

    suspend fun rightAlternativityCheck() {
        checkAll(pairArb) { (x, y) ->
            // (yx)x = y(xx)
            val yx = op.combine(y, x)
            val left = op.combine(yx, x)

            val xx = op.combine(x, x)
            val right = op.combine(y, xx)

            withClue(rightFailureMessage(x, y, yx, left, xx, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    private fun rightFailureMessage(
        x: A, y: A,
        yx: A, left: A,
        xx: A, right: A
    ): () -> String = {
        val sx = pr.render(x)
        val sy = pr.render(y)
        val syx = pr.render(yx)
        val sxx = pr.render(xx)
        val sLeft = pr.render(left)
        val sRight = pr.render(right)

        buildString {
            appendLine("Right alternativity failed:")

            append(infix("(" + infix(sy, sx) + ")", sx))
            append(" = ")
            append(infix(syx, sx))
            append(" = ")
            append(sLeft)
            appendLine()

            append(infix(sy, "(" + infix(sx, sx) + ")"))
            append(" = ")
            append(infix(sy, sxx))
            append(" = ")
            append(sRight)
            appendLine()

            append("Expected: $sLeft = $sRight")
            appendLine()
        }
    }
}

class LeftAlternativityLaw<A>(
    override val op: BinOp<A>,
    override val pairArb: Arb<Pair<A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "⋆"
) : TestingLaw, AlternativityCore<A> {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.pair(arb, arb), eq, pr, symbol)

    override val name = "alternativity (left: $symbol)"
    override suspend fun test() = leftAlternativityCheck()
}

class RightAlternativityLaw<A>(
    override val op: BinOp<A>,
    override val pairArb: Arb<Pair<A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "⋆"
) : TestingLaw, AlternativityCore<A> {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.pair(arb, arb), eq, pr, symbol)

    override val name = "alternativity (right: $symbol)"
    override suspend fun test() = rightAlternativityCheck()
}

/**
 * Alternativity is a condition that is implied by but strictly weaker than associativity.
 * The classic example is the octonions: they are nonassociative, but alternative.
 * The following conditions must be met:
 * * Left alternativity: x(xy) = (xx)y
 * * Right alternativity: (yx)x = y(xx)
 *
 * for all x, y in the algebra.
 * They are named as such because the associator is alternating:
 * * [x, y, z] = (xy)z - x(yz).
 *
 * Alternativity is quite strong: it forces any two elements to generate an associative subalgebra
 * (see Artin's theorem).
 */
class AlternativityLaw<A>(
    override val op: BinOp<A>,
    override val pairArb: Arb<Pair<A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "⋆"
) : TestingLaw, AlternativityCore<A> {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.pair(arb, arb), eq, pr, symbol)

    override val name = "alternativity (both: $symbol)"
    override suspend fun test() {
        leftAlternativityCheck()
        rightAlternativityCheck()
    }
}
