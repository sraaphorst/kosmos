package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** Transitivity: (a R b ∧ b R c) ⇒ a R c */
class TransitivityLaw<A : Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {

    private val symbol = rel.symbol

    override val name: String = "transitivity ($symbol)"

    override suspend fun test() {
        checkAll(TestingLaw.arbTriple(arb)) { (a, b, c) ->
            val ab = rel(a, b)
            val bc = rel(b, c)

            // Only constrained when the premise holds.
            if (ab && bc) {
                val ac = rel(a, c)
                withClue(failureMessage(a, b, c, ab, bc, ac)) {
                    check(ac)
                }
            }
        }
    }

    private fun failureMessage(
        a: A,
        b: A,
        c: A,
        ab: Boolean,
        bc: Boolean,
        ac: Boolean
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)

            appendLine("Transitivity failed:")
            appendLine("a = $sa")
            appendLine("b = $sb")
            appendLine("c = $sc")
            appendLine("a $symbol b = $ab")
            appendLine("b $symbol c = $bc")
            appendLine("a $symbol c = $ac (expected: true)")
        }
    }
}