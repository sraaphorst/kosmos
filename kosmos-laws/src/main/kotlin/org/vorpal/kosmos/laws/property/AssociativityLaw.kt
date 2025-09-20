package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.triple
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** Associativity Law
 * Note that we allow a Triple producing Arb so that we can impose constraints if necessary on the values produced,
 * e.g. that they all be distinct, or to avoid NaN / overflow for floating point types. */
class AssociativityLaw<A>(
    private val op: BinOp<A>,
    private val tripleArb: Arb<Triple<A, A, A>>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) : TestingLaw {

    /** Convenience secondary constructor that converts an Arb to an Arb producing a Triple */
    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.triple(arb, arb, arb), eq, pr, symbol)

    override val name = "associativity ($symbol)"

    override suspend fun test() {
        checkAll(tripleArb) { (a, b, c) ->
            val bc = op.combine(b, c)
            val left  = op.combine(a, bc)

            val ab = op.combine(a, b)
            val right = op.combine(ab, c)

            withClue(failureMessage(a, b, c, ab, bc, left, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    /** Lazy evaluated failure message upon failed test case. */
    private fun failureMessage(
        a: A, b: A, c: A,
        ab: A, bc: A,
        left: A, right: A
    ): () -> String = {
        fun infix(l: String, r: String) = "$l $symbol $r"

        val sa = pr.render(a)
        val sb = pr.render(b)
        val sc = pr.render(c)
        val sab = pr.render(ab)
        val sbc = pr.render(bc)
        val sLeft = pr.render(left)
        val sRight = pr.render(right)

        buildString {
            appendLine("Associativity failed:")

            append(infix(sa, "(" + infix(sb, sc) + ")"))
            append(" = ")
            append(infix(sa, sbc))
            append(" = ")
            append(sLeft)
            appendLine()

            append(infix("(" + infix(sa, sb) + ")", sc))
            append(" = ")
            append(infix(sab, sc))
            append(" = ")
            append(sRight)
            appendLine()

            append("Expected: $sLeft = $sRight")
            appendLine()
        }
    }
}
