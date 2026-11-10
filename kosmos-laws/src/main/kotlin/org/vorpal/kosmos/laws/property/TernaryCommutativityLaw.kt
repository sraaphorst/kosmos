package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.TernOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

class TernaryCommutativityLaw<H : Any>(
    val op: TernOp<H>,
    arb: Arb<H>,
    val eq: Eq<H> = Eq.default(),
    val pr: Printable<H> = Printable.default()
): TestingLaw {
    private val arbTriple = TestingLaw.arbTriple(arb)

    private fun expr(left: String, middle: String, right: String) =
        "[$left, $middle, $right]"

    private suspend fun ternaryCommutativityCheck() {
        lawCheckAll(arbTriple, render = renderWith(pr)) { (x, y, z) ->
            val lhs = op(x, y, z)
            val rhs = op(z, y, x)

            lawCheck(eq(lhs, rhs), failureMessage(x, y, z, lhs, rhs))
        }
    }

    private fun failureMessage(
        x: H, y: H, z: H,
        lhs: H, rhs: H
    ): () -> String = {
        buildString {
            val sx = pr(x)
            val sy = pr(y)
            val sz = pr(z)
            val slhs = pr(lhs)
            val srhs = pr(rhs)

            appendLine("Ternary commutativity law failed:")
            append(expr(sx, sy, sz))
            append(" = ")
            appendLine(slhs)
            append(expr(sz, sy, sx))
            append(" = ")
            appendLine(srhs)
            appendLine("Expected: $slhs = $srhs")
        }
    }

    override suspend fun test() =
        ternaryCommutativityCheck()
}