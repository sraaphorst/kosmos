package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.triple
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface BolIdentityCore<A> {
    val op: BinOp<A>
    val tripleArb: Arb<Triple<A, A, A>>
    val eq: Eq<A>
    val pr: Printable<A>
    val symbol: String

    private fun infix(l: String, r: String) = "$l $symbol $r"
    private fun pinfix(l: String, r: String) = "(" + infix(l, r) + ")"

    // a(b(ac)) = (a(ba))c
    suspend fun leftBolIdentityCheck() {
        checkAll(tripleArb) { (a, b, c) ->
            val ac = op.combine(a, c)
            val b_ac = op.combine(b, ac)
            val left = op.combine(a, b_ac)

            val ba = op.combine(b, a)
            val a_ba = op.combine(a, ba)
            val right = op.combine(a_ba, c)

            withClue(leftFailureMessage(a, b, c, ac, b_ac, left, ba, a_ba, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    private fun leftFailureMessage(
        a: A, b: A, c: A,
        ac: A, b_ac: A, left: A,
        ba: A, a_ba: A, right: A
    ): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        val sc = pr.render(c)
        val sac = pr.render(ac)
        val sb_ac = pr.render(b_ac)
        val sLeft = pr.render(left)
        val sba = pr.render(ba)
        val sa_ba = pr.render(a_ba)
        val sRight = pr.render(right)

        buildString {
            appendLine("Left Bol identity failed:")

            append(infix(sa,pinfix(sb, pinfix(sa, sc))))
            append(" = ")
            append(infix(sa,pinfix(sb, sac)))
            append(" = ")
            append(infix(sa, sb_ac))
            append(" = ")
            append(sLeft)
            appendLine()

            append(infix(pinfix(sa,pinfix(sb, sa)), sc))
            append(" = ")
            append(infix(pinfix(sa, sba), sc))
            append(" = ")
            append(infix(sa_ba, sc))
            append(" = ")
            append(sRight)
            appendLine()

            append("Expected: $sLeft = $sRight")
            appendLine()
        }
    }

    // ((ca)b)a = c((ab)a)
    suspend fun rightBolIdentityCheck() {
        checkAll(tripleArb) { (a, b, c) ->
            val ca = op.combine(c, a)
            val ca_b = op.combine(ca, b)
            val left = op.combine(ca_b, a)

            val ab = op.combine(a, b)
            val ab_a = op.combine(ab, a)
            val right = op.combine(c, ab_a)

            withClue(rightFailureMessage(a, b, c, ca, ca_b, left, ab, ab_a, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    private fun rightFailureMessage(
        a: A, b: A, c: A,
        ca: A, ca_b: A, left: A,
        ab: A, ab_a: A, right:A
    ): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        val sc = pr.render(c)
        val sca = pr.render(ca)
        val sca_b = pr.render(ca_b)
        val sLeft = pr.render(left)
        val sab = pr.render(ab)
        val sab_a = pr.render(ab_a)
        val sRight = pr.render(right)

        buildString {
            appendLine("Right Bol identity failed:")

            // (ca)b
            append(infix(pinfix(pinfix(sc, sa), sb), sa))
            append(" = ")
            append(infix(pinfix(sca, sb), sa))
            append(" = ")
            append(infix(sca_b, sa))
            append(" = ")
            append(sLeft)
            appendLine()

            append(infix(sc,pinfix(pinfix(sa, sb), sa)))
            append(" = ")
            append(infix(sc,pinfix(sab, sa)))
            append(" = ")
            append(infix(sc, sab_a))
            append(" = ")
            append(sRight)
            appendLine()

            append("Expected: $sLeft = $sRight")
            appendLine()
        }
    }
}

class LeftBolIdentityLaw<A>(
    override val op: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "⋆"
) : TestingLaw, BolIdentityCore<A> {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.triple(arb, arb, arb), eq, pr, symbol)

    override val name = "Bol identity (left: $symbol)"
    override suspend fun test() = leftBolIdentityCheck()
}

class RightBolIdentityLaw<A>(
    override val op: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "⋆"
) : TestingLaw, BolIdentityCore<A> {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.triple(arb, arb, arb), eq, pr, symbol)

    override val name = "Bol identity (right: $symbol)"
    override suspend fun test() = rightBolIdentityCheck()
}

/** The Bol identity laws.
 * A loop L is said to be a Bol loop if it satisfies the identities:
 * * a(b(ac)) = (a(ba))c
 * * ((ca)b)a = c((ab)a)
 *
 * for all a, b, c in L.
 *
 * These identities can be seen as:
 * * weakened forms of associativity
 * * strengthened forms of alternativity
 *
 * A loop is both left Bol and right Bol iff it is a Moufang loop.
 *
 * Alternatively, a right or left Bol loop is Moufang iff it satisfies
 * the flexibility law.
 */
class BolIdentityLaw<A>(
    override val op: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "⋆"
) : TestingLaw, BolIdentityCore<A> {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.triple(arb, arb, arb), eq, pr, symbol)

    override val name = "Bol identity (both: $symbol)"
    override suspend fun test() {
        leftBolIdentityCheck()
        rightBolIdentityCheck()
    }
}
