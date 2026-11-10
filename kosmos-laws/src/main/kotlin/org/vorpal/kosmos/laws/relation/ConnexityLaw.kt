package org.vorpal.kosmos.laws.relation

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filterNot
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll

/** Connexity (Totality): a ≠ b ⇒ (a R b) ∨ (b R a) */
class ConnexityLaw<A : Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {

    private val symbol = rel.symbol

    override val name: String = "connexity ($symbol)"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbPair(arb).filterNot { (a, b) -> eq(a, b) }) { (a, b) ->
            val ab = rel(a, b)
            val ba = rel(b, a)

            lawCheck(ab || ba, failureMessage(a, b, ab, ba))
        }
    }

    private fun failureMessage(a: A, b: A, ab: Boolean, ba: Boolean): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)

            appendLine("Connexity failed (a ≠ b implies a R b OR b R a):")
            appendLine("  a = $sa")
            appendLine("  b = $sb")
            appendLine("  a $symbol b = $ab")
            appendLine("  b $symbol a = $ba")
            appendLine("  (a $symbol b) || (b $symbol a) = ${ab || ba} (expected true)")
        }
    }
}