package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.TernOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Para-associativity law:
 * ```text
 * [[a, b, c], d, e] == [a, [d, c, b], e] == [a, b, [c, d, e]]
 * ```
 */
class ParaAssociativityLaw<H : Any>(
    private val op: TernOp<H>,
    arb: Arb<H>,
    private val eq: Eq<H> = Eq.default(),
    private val pr: Printable<H> = Printable.default()
): TestingLaw {
    private val quintupleArb = TestingLaw.arbQuintuple(arb)

    private fun expr(left: String, middle: String, right: String) =
        "[$left, $middle, $right]"

    private suspend fun paraAssociativityCheck() {
        checkAll(quintupleArb) { (a, b, c, d, e) ->
            val abc = op(a, b, c)
            val lhs = op(abc, d, e)

            val dcb = op(d, c, b)
            val mid = op(a, dcb, e)

            val cde = op(c, d, e)
            val rhs = op(a, b, cde)

            withClue(failureMessage1(a, b, c, d, e, abc, lhs, dcb, mid)) {
                check(eq(lhs, mid))
            }
            withClue(failureMessage2(a, b, c, d, e, dcb, mid, cde, rhs)) {
                check(eq(mid, rhs))
            }
        }
    }

    private fun failureMessage1(
        a: H, b: H, c: H, d: H, e: H,
        abc: H, lhs: H, dcb: H, mid: H
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)
            val sd = pr(d)
            val se = pr(e)
            val sABC = pr(abc)
            val sLHS = pr(lhs)
            val sDCB = pr(dcb)
            val sMID = pr(mid)

            appendLine("Para-associativity law failed:")
            val lhs1 = expr(expr(sa, sb, sc), sd, se)
            val lhs2 = expr(sABC, sd, se)
            appendLine("\t$lhs1 = $lhs2 = $sLHS")
            val mid1 = expr(sa, expr(sd, sc, sb), se)
            val mid2 = expr(sa, sDCB, se)
            appendLine("\t$mid1 = $mid2 = $sMID")
            appendLine("Expected: $sLHS = $sMID")
        }
    }

    private fun failureMessage2(
        a: H, b: H, c: H, d: H, e: H,
        dcb: H, mid: H, cde: H, rhs: H
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)
            val sd = pr(d)
            val se = pr(e)
            val sDCB = pr(dcb)
            val sMID = pr(mid)
            val sCDE = pr(cde)
            val sRHS = pr(rhs)

            appendLine("Para-associativity law failed:")
            val mid1 = expr(sa, expr(sd, sc, sb), se)
            val mid2 = expr(sa, sDCB, se)
            appendLine("\t$mid1 = $mid2 = $sMID")
            val rhs1 = expr(sa, sb, expr(sc, sd, se))
            val rhs2 = expr(sa, sb, sCDE)
            appendLine("\t$rhs1 = $rhs2 = $sRHS")
            appendLine("Expected: $sMID = $sRHS")
        }
    }

    override suspend fun test() =
        paraAssociativityCheck()
}