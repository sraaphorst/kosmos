package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.TernOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Biunitary law:
 * ```text
 * [h, h, k] = k = [k, h, h]
 * ```
 */
class BiunitaryLaw<H : Any>(
    private val op: TernOp<H>,
    arb: Arb<H>,
    private val eq: Eq<H> = Eq.default(),
    private val pr: Printable<H> = Printable.default()
): TestingLaw {
    val pairArb = TestingLaw.arbPair(arb)

    private fun expr(left: String, middle: String, right: String) =
        "[$left, $middle, $right]"

    private suspend fun biunitaryCheck() {
        checkAll(pairArb) { (h, k) ->
            val lhs = op(h, h, k)
            val rhs = op(k, h, h)

            withClue(failureMessage(h, k, lhs, rhs)) {
                check(eq(lhs, k) && eq(rhs, k))
            }
        }
    }

    private fun failureMessage(
        h: H, k: H, lhs: H, rhs: H
    ): () -> String = {
        buildString {
            val sh = pr(h)
            val sk = pr(k)
            val slhs = pr(lhs)
            val srhs = pr(rhs)
            appendLine("Biunitary law failed:")
            append(expr(sh, sh, sk))
            appendLine(" = $slhs")
            append(expr(sk, sh, sh))
            appendLine(" = $srhs")
            append("Expected: $sk")
        }
    }

    override suspend fun test() =
        biunitaryCheck()
}