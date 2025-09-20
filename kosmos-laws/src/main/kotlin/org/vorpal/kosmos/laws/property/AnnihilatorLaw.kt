package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Test an annihilator (zero) for left and / or right annihilation of elements
 * of a given type under a binary operation. */
class AnnihilatorLaw<A>(
    private val op: BinOp<A>,
    private val zero: A,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) : TestingLaw {
    override val name = "annihilator ($symbol)"

    private suspend fun leftAnnihilationCheck() {
        checkAll(arb) { a ->
            val value = op.combine(zero, a)
            withClue(leftFailureMessage(a, value)) {
                check(eq.eqv(zero, value))
            }
        }
    }

    private fun leftFailureMessage(
        a: A, value: A
    ): () -> String = {
        val sa = pr.render(a)
        val sValue = pr.render(value)
        val sZero = pr.render(zero)
        buildString {
            appendLine("Left annihilation failed:")
            append("$sZero $symbol $sa")
            append(" = ")
            append(sValue)
            append(" (expected: $sZero)")
            appendLine()
        }
    }

    private suspend fun rightAnnihilationCheck() {
        checkAll(arb) { a ->
            val value = op.combine(a, zero)
            withClue(rightFailureMessage(a, value)) {
                check(eq.eqv(zero, value))
            }
        }
    }

    private fun rightFailureMessage(
        a: A, value: A
    ): () -> String = {
        val sa = pr.render(a)
        val sValue = pr.render(value)
        val sZero = pr.render(zero)
        buildString {
            appendLine("Right annihilation failed:")
            append("$sa $symbol $sZero")
            append(" = ")
            append(sValue)
            append(" (expected: $sZero)")
            appendLine()
        }
    }

    override suspend fun test() {
        leftAnnihilationCheck()
        rightAnnihilationCheck()
    }
}
