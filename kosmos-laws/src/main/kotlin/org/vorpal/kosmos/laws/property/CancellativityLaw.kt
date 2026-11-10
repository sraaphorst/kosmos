package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith


private sealed interface CancellativityCore<A : Any> {
    val op: BinOp<A>
    val arb: Arb<A>
    val eq: Eq<A>
    val pr: Printable<A>

    private fun expr(left: String, right: String) = "$left ${op.symbol} $right"

    suspend fun rightCancelCheck() {
        lawCheckAll(TestingLaw.arbTriple(arb), render = renderWith(pr)) { (a, b, c) ->
            val ac = op(a, c)
            val bc = op(b, c)
            if (eq(ac, bc)) {
                lawCheck(eq(a, b), rightCancelFailure(a, b, c, ac, bc))
            }
        }
    }

    private fun rightCancelFailure(
        a: A, b: A, c: A,
        ac: A, bc: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)
            val sac = pr(ac)
            val sbc = pr(bc)
            
            appendLine("Right cancellation failed for op ${op.symbol}:")

            append(expr(sa, sc))
            append(" = ")
            appendLine(sac)

            append(expr(sb, sc))
            append(" = ")
            appendLine(sbc)

            appendLine("Premise holds: ${expr(sa, sc)} = ${expr(sb, sc)}, but expected $sa = $sb.")
        }
    }

    suspend fun leftCancelCheck() {
        lawCheckAll(TestingLaw.arbTriple(arb), render = renderWith(pr)) { (a, b, c) ->
            val ca = op(c, a)
            val cb = op(c, b)
            if (eq(ca, cb)) {
                lawCheck(eq(a, b), leftCancelFailure(a, b, c, ca, cb))
            }
        }
    }

    private fun leftCancelFailure(
        a: A, b: A, c: A,
        ca: A, cb: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)
            val sca = pr(ca)
            val scb = pr(cb)

            appendLine("Left cancellation failed for op ${op.symbol}:")

            append(expr(sc, sa))
            append(" = ")
            appendLine(sca)

            append(expr(sc, sb))
            append(" = ")
            appendLine(scb)

            appendLine("Premise holds: ${expr(sc, sa)} = ${expr(sc, sb)}, but expected $sa = $sb.")
        }
    }
}

/**
 * Only right-cancellative: if `a⋆c = b⋆c`, then `a = b`.
 */
class RightCancellativityLaw<A : Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, CancellativityCore<A> {
    override val name = "right-cancellative (${op.symbol})"
    override suspend fun test() = rightCancelCheck()
}

/**
 * Only left-cancellative: if `c⋆a = c⋆b`, then `a = b`.
 */
class LeftCancellativityLaw<A : Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, CancellativityCore<A> {
    override val name = "left-cancellative (${op.symbol})"
    override suspend fun test() = leftCancelCheck()
}
/**
 * Cancellativity Laws:
 *  - Right: if `a⋆c = b⋆c`, then `a = b`
 *  - Left : if `c⋆a = c⋆b`, then `a = b`
 */
class CancellativityLaw<A : Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, CancellativityCore<A> {
    override val name = "cancellative (${op.symbol})"

    override suspend fun test() {
        leftCancelCheck()
        rightCancelCheck()
    }
}
