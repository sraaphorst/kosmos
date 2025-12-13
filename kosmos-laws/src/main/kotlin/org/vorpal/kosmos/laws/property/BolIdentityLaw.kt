package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface BolIdentityCore<A : Any> {
    val op: BinOp<A>
    val arb: Arb<A>
    val eq: Eq<A>
    val pr: Printable<A>

    private fun expr(left: String, right: String) = "$left ${op.symbol} $right"
    private fun pexpr(left: String, right: String) = "(" + expr(left, right) + ")"

    // a(b(ac)) = (a(ba))c
    suspend fun leftBolIdentityCheck() {
        checkAll(TestingLaw.arbTriple(arb)) { (a, b, c) ->
            val ac = op(a, c)
            val bAc = op(b, ac)
            val left = op(a, bAc)

            val ba = op(b, a)
            val aBA = op(a, ba)
            val right = op(aBA, c)

            withClue(leftBolIdentityFailure(a, b, c, ac, bAc, left, ba, aBA, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun leftBolIdentityFailure(
        a: A, b: A, c: A,
        ac: A, bAc: A, left: A,
        ba: A, aBa: A, right: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)
            val sac = pr(ac)
            val sbAc = pr(bAc)
            val sLeft = pr(left)
            val sba = pr(ba)
            val saBa = pr(aBa)
            val sRight = pr(right)

            appendLine("Left Bol identity failed:")

            append(expr(sa,pexpr(sb, pexpr(sa, sc))))
            append(" = ")
            append(expr(sa,pexpr(sb, sac)))
            append(" = ")
            append(expr(sa, sbAc))
            append(" = ")
            appendLine(sLeft)

            append(expr(pexpr(sa,pexpr(sb, sa)), sc))
            append(" = ")
            append(expr(pexpr(sa, sba), sc))
            append(" = ")
            append(expr(saBa, sc))
            append(" = ")
            appendLine(sRight)

            appendLine("Expected: $sLeft = $sRight")
        }
    }

    // ((ca)b)a = c((ab)a)
    suspend fun rightBolIdentityCheck() {
        checkAll(TestingLaw.arbTriple(arb)) { (a, b, c) ->
            val ca = op(c, a)
            val caB = op(ca, b)
            val left = op(caB, a)

            val ab = op(a, b)
            val abA = op(ab, a)
            val right = op(c, abA)

            withClue(rightBolIdentityFailure(a, b, c, ca, caB, left, ab, abA, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun rightBolIdentityFailure(
        a: A, b: A, c: A,
        ca: A, caB: A, left: A,
        ab: A, abA: A, right: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)
            val sca = pr(ca)
            val scaB = pr(caB)
            val sLeft = pr(left)
            val sab = pr(ab)
            val sabA = pr(abA)
            val sRight = pr(right)

            appendLine("Right Bol identity failed:")

            append(expr(pexpr(pexpr(sc, sa), sb), sa))
            append(" = ")
            append(expr(pexpr(sca, sb), sa))
            append(" = ")
            append(expr(scaB, sa))
            append(" = ")
            appendLine(sLeft)

            append(expr(sc,pexpr(pexpr(sa, sb), sa)))
            append(" = ")
            append(expr(sc,pexpr(sab, sa)))
            append(" = ")
            append(expr(sc, sabA))
            append(" = ")
            appendLine(sRight)

            appendLine("Expected: $sLeft = $sRight")
        }
    }
}


/**
 * A loop `L` is said to be a Bol loop if it satisfies the identities:
 * - `a(b(ac)) = (a(ba))c`
 * - `((ca)b)a = c((ab)a)`
 *
 * for all `a, b, c ∈ L`.
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
class BolIdentityLaw<A : Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, BolIdentityCore<A> {
    override val name = "Bol identity (${op.symbol})"

    override suspend fun test() {
        leftBolIdentityCheck()
        rightBolIdentityCheck()
    }
}

/**
 * A loop `L` is said to be a left Bol loop if it satisfies the identity:
 *
 *     a(b(ac)) = (a(ba))c
 *
 * for all `a, b, c ∈ L`.
 *
 * This identity can be seen as a:
 * * weakened form of associativity
 * * strengthened form of left alternativity
 *
 * A left Bol loop is Moufang iff it satisfies the flexibility law.
 */
class LeftBolIdentityLaw<A : Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, BolIdentityCore<A> {
    override val name = "left Bol identity (${op.symbol})"

    override suspend fun test() =
        leftBolIdentityCheck()
}

/**
 * A loop `L` is said to be a right Bol loop if it satisfies the identity:
 *
 *    ((ca)b)a = c((ab)a)
 *
 * for all `a, b, c ∈ L`.
 *
 * This identity can be seen as a:
 * * weakened form of associativity
 * * strengthened form of right alternativity
 *
 * A right Bol loop is Moufang iff it satisfies the flexibility law.
 */
class RightBolIdentityLaw<A : Any>(
    override val op: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, BolIdentityCore<A> {
    override val name = "right Bol identity (${op.symbol})"

    override suspend fun test() =
        rightBolIdentityCheck()
}