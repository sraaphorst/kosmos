package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

class IdentityLaw<A>(
    private val op: BinOp<A>,
    private val identity: A,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) : TestingLaw {
    override val name = "identity ($symbol)"

    private suspend fun leftIdentityCheck() {
        checkAll(arb) { a ->
            val value = op.combine(identity, a)
            withClue(leftFailureMessage(a, value)) {
                check(eq.eqv(a, value))
            }
        }
    }

    private fun leftFailureMessage(
        a: A, value: A
    ) : () -> String = {
        val sa = pr.render(a)
        val sValue = pr.render(value)
        val sid = pr.render(identity)
        buildString {
            appendLine("Left identity failed:")
            append("$sid $symbol $sa")
            append(" = ")
            append(sValue)
            append(" (expected: $sa)")
            appendLine()
        }
    }

    private suspend fun rightIdentityCheck() {
        checkAll(arb) { a ->
            val value = op.combine(a, identity)
            withClue(rightFailureMessage(a, value)) {
                check(eq.eqv(a, value))
            }
        }
    }

    private fun rightFailureMessage(
        a: A, value: A
    ) : () -> String = {
        val sa = pr.render(a)
        val sValue = pr.render(value)
        val sid = pr.render(identity)
        buildString {
            appendLine("Right identity failed:")
            append("$sa $symbol $sid")
            append(" = ")
            append(sValue)
            append(" (expected: $sa)")
            appendLine()
        }
    }

    override suspend fun test() {
        leftIdentityCheck()
        rightIdentityCheck()
    }
}
