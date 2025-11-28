package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

class SymmetryLaw<A : Any, B : Any>(
    private val op: (A, A) -> B,
    private val pairArb: Arb<Pair<A, A>>,
    private val eq: Eq<B>,
    private val prA: Printable<A> = Printable.default(),
    private val prB: Printable<B> = Printable.default(),
    private val symbol: String = "⋆"
) : TestingLaw {

    override val name: String = "symmetry ($symbol)"

    override suspend fun test() {
        checkAll(pairArb) { (x, y) ->
            val left = op(x, y)
            val right = op(y, x)

            withClue(failureMessage(x, y, left, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    private fun failureMessage(
        x: A,
        y: A,
        left: B,
        right: B
    ): () -> String = {
        val sx = prA.render(x)
        val sy = prA.render(y)
        val sl = prB.render(left)
        val sr = prB.render(right)

        buildString {
            appendLine("Symmetry law failed for $symbol:")
            appendLine("$symbol($sx, $sy) = $sl")
            appendLine("$symbol($sy, $sx) = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }
}