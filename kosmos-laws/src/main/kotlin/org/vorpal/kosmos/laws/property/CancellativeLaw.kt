package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.triple
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** Cancellativity Laws:
 *  Right: if a⋆c = b⋆c, then a = b
 *  Left : if c⋆a = c⋆b, then a = b
 */
private sealed interface CancellativeCore<A: Any> {
    val op: BinOp<A>
    val tripleArb: Arb<Triple<A, A, A>>
    val eq: Eq<A>
    val pr: Printable<A>
    val symbol: String

    private fun infix(l: String, r: String) = "$l $symbol $r"

    suspend fun rightCancelCheck() {
        checkAll(tripleArb) { (a, b, c) ->
            val ac = op(a, c)
            val bc = op(b, c)
            if (eq.eqv(ac, bc)) {
                withClue(rightFailureMessage(a, b, c, ac, bc)) {
                    check(eq.eqv(a, b))
                }
            }
        }
    }

    private fun rightFailureMessage(
        a: A, b: A, c: A,
        ac: A, bc: A
    ): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        val sc = pr.render(c)
        val sac = pr.render(ac)
        val sbc = pr.render(bc)
        buildString {
            appendLine("Right cancellation failed:")

            append(infix(sa, sc))
            append(" = ")
            append(sac)
            appendLine()

            append(infix(sb, sc))
            append(" = ")
            append(sbc)
            appendLine()

            append("Premise holds (equal results), but expected $sa = $sb.")
            appendLine()
        }
    }

    suspend fun leftCancelCheck() {
        checkAll(tripleArb) { (a, b, c) ->
            val ca = op(c, a)
            val cb = op(c, b)
            if (eq.eqv(ca, cb)) {
                withClue(leftFailureMessage(a, b, c, ca, cb)) {
                    check(eq.eqv(a, b))
                }
            }
        }
    }

    private fun leftFailureMessage(
        a: A, b: A, c: A,
        ca: A, cb: A
    ): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        val sc = pr.render(c)
        val sca = pr.render(ca)
        val scb = pr.render(cb)

        buildString {
            appendLine("Left cancellation failed:")

            append(infix(sc, sa))
            append(" = ")
            append(sca)
            appendLine()

            append(infix(sc, sb))
            append(" = ")
            append(scb)
            appendLine()

            append("Premise holds (equal results), but expected $sa = $sb.")
            appendLine()
        }
    }
}

/** Only right-cancellative: if a⋆c = b⋆c then a = b. */
class RightCancellativeLaw<A: Any>(
    override val op: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "⋆"
) : TestingLaw, CancellativeCore<A> {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.triple(arb, arb, arb), eq, pr, symbol)

    override val name = "right-cancellative ($symbol)"
    override suspend fun test() = rightCancelCheck()
}

/** Only left-cancellative: if c⋆a = c⋆b then a = b */
class LeftCancellativeLaw<A: Any>(
    override val op: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "⋆"
) : TestingLaw, CancellativeCore<A> {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.triple(arb, arb, arb), eq, pr, symbol)

    override val name = "left-cancellative ($symbol)"
    override suspend fun test() = leftCancelCheck()
}

/** Both directions (cancellative) */
class CancellativeLaw<A: Any>(
    override val op: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "⋆"
) : TestingLaw, CancellativeCore<A> {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.triple(arb, arb, arb), eq, pr, symbol)

    override val name = "cancellative (left & right, $symbol)"
    override suspend fun test() {
        leftCancelCheck()
        rightCancelCheck()
    }
}
