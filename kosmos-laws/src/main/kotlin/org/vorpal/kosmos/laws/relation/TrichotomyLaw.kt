package org.vorpal.kosmos.laws.relation

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith


/**
 * Trichotomy (strict total order):
 * Exactly one of: a R b, b R a, or a = b.
 *
 * This expects a strict order (typically irreflexive + transitive), e.g. `<`.
 */
class TrichotomyLaw<A : Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {

    private val symbol = rel.symbol

    override val name: String = "trichotomy ($symbol)"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbPair(arb), render = renderWith(pr)) { (a, b) ->
            val ab = rel(a, b)
            val ba = rel(b, a)
            val aeb = eq(a, b)

            val trueCount =
                (if (ab) 1 else 0) +
                    (if (ba) 1 else 0) +
                    (if (aeb) 1 else 0)

            lawCheck(trueCount == 1, failureMessage(a, b, ab, ba, aeb, trueCount))
        }
    }

    private fun failureMessage(
        a: A,
        b: A,
        ab: Boolean,
        ba: Boolean,
        aeb: Boolean,
        trueCount: Int,
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)

            appendLine("Trichotomy failed (expected exactly one to be true):")
            appendLine("  $sa $symbol $sb = $ab")
            appendLine("  $sb $symbol $sa = $ba")
            appendLine("  eq($sa, $sb) = $aeb")
            appendLine("  trueCount = $trueCount (expected 1)")
        }
    }
}
