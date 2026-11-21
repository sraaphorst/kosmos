package org.vorpal.kosmos.laws.relation

import io.kotest.property.Arb
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

private sealed interface QuasiReflexivityCore<A: Any> {
    val rel: Relation<A>
    val arb: Arb<A>
    val pr: Printable<A>

    suspend fun leftQuasiReflexivityCheck() {
        lawCheckAll(TestingLaw.arbPair(arb), render = renderWith(pr)) { (a, b) ->
            if (rel(a, b)) {
                lawCheck(rel(a, a), leftFailureMessage(a, b))
            }
        }
    }

    private fun leftFailureMessage(a: A, b: A): () -> String = {
        val sa = pr(a)
        val sb = pr(b)
        "Left quasi-reflexivity check failed: $sa ${rel.symbol} $sb but not $sa ${rel.symbol} $sa"
    }

    suspend fun rightQuasiReflexivityCheck() {
        lawCheckAll(TestingLaw.arbPair(arb), render = renderWith(pr)) { (a, b) ->
            if (rel(a, b)) {
                lawCheck(rel(b, b), rightFailureMessage(a, b))
            }
        }
    }

    private fun rightFailureMessage(a: A, b: A): () -> String = {
        val sa = pr(a)
        val sb = pr(b)
        "Right quasi-reflexive check failed: $sa ${rel.symbol} $sb but not $sb ${rel.symbol} $sb"
    }
}

/**
 * Left quasi-reflexivity:
 *
 *    a R b ⇒ a R a
 **/
class LeftQuasiReflexivityLaw<A: Any>(
    override val rel: Relation<A>,
    override val arb: Arb<A>,
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, QuasiReflexivityCore<A> {
    override val binding = LawBinding.of(this, rel, arb)

    override val name = "left quasi-reflexivity (${rel.symbol})"
    override suspend fun test() = leftQuasiReflexivityCheck()
}

/**
 * Right quasi-reflexivity:
 *
 *    a R b ⇒ b R b
 */
class RightQuasiReflexivityLaw<A: Any>(
    override val rel: Relation<A>,
    override val arb: Arb<A>,
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, QuasiReflexivityCore<A> {
    override val binding = LawBinding.of(this, rel, arb)

    override val name = "right quasi-reflexivity (${rel.symbol})"
    override suspend fun test() = rightQuasiReflexivityCheck()
}

/** Quasi-reflexivity:
 * - `a R b ⇒ a R a` (left quasi-reflexivity)
 * - `a R b ⇒ b R b` (right quasi-reflexivity)
 */
class QuasiReflexivityLaw<A: Any>(
    private val rel: Relation<A>,
    private val arb: Arb<A>,
    private val pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = "quasi-reflexivity (${rel.symbol})"

    override val binding = LawBinding.of(this, rel, arb)

    override fun children(): List<LawNode> = listOf(
        LeftQuasiReflexivityLaw(rel, arb, pr),
        RightQuasiReflexivityLaw(rel, arb, pr)
    ).asNodes()
}
