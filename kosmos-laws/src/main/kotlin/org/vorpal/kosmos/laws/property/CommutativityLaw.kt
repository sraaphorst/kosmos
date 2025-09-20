package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** Commutativity Law
 * Note that we allow a Double producing Arb so that we can impose constraints if necessary on the values produced,
 * e.g. that they all be distinct, or to avoid NaN / overflow for floating point types. */
class CommutativityLaw<A>(
    private val op: BinOp<A>,
    private val pairArb: Arb<Pair<A, A>>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) : TestingLaw {

    /** Convenience secondary constructor that converts an Arb to an Arb producing a Pair. */
    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.pair(arb, arb), eq, pr, symbol)

    override val name = "commutativity ($symbol)"

    override suspend fun test() {
        checkAll(pairArb) { (a, b) ->
            val left  = op.combine(a, b)
            val right = op.combine(b, a)

            withClue(failureMessage(a, b, left, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    private fun failureMessage(
        a: A, b: A,
        left: A, right: A
    ): () -> String = {
        fun infix(l: String, r: String) = "$l $symbol $r"

        val sa = pr.render(a)
        val sb = pr.render(b)
        val sLeft = pr.render(left)
        val sRight = pr.render(right)

        buildString {
            appendLine("Commutativity failed:")

            append(infix(sa, sb))
            append(" = ")
            append(sLeft)
            appendLine()

            append(infix(sb, sa))
            append(" = ")
            append(sRight)
            appendLine()

            append("Expected: $sLeft = $sRight")
            appendLine()
        }
    }
}
