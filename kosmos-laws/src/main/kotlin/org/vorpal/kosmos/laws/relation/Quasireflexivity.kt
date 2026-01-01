package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface QuasiReflexivityCore<A: Any> {
    val rel: Relation<A>
    val arb: Arb<A>
    val pr: Printable<A>

    suspend fun leftQuasiReflexivityCheck() {
        checkAll(TestingLaw.arbPair(arb)) { (a, b) ->
            if (rel(a, b)) {
                withClue(leftFailureMessage(a, b)) {
                    check(rel(a, a))
                }
            }
        }
    }

    private fun leftFailureMessage(a: A, b: A): () -> String = {
        val sa = pr(a)
        val sb = pr(b)
        "Left quasi-reflexivity check failed: $sa ${rel.symbol} $sb but not $sa ${rel.symbol} $sa"
    }

    suspend fun rightQuasiReflexivityCheck() {
        checkAll(TestingLaw.arbPair(arb)) { (a, b) ->
            if (rel(a, b)) {
                withClue(rightFailureMessage(a, b)) {
                    check(rel(b, b))
                }
            }
        }
    }

    private fun rightFailureMessage(a: A, b: A): () -> String = {
        val sa = pr(a)
        val sb = pr(b)
        "Right quasi-reflexive check failed: $sa ${rel.symbol} $sb but not $sb {symbol.rel} $sb"
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

    override val name = "right quasi-reflexivity (${rel.symbol})"
    override suspend fun test() = rightQuasiReflexivityCheck()
}

/** Quasi-reflexivity:
 * - `a R b ⇒ a R a` (left quasi-reflexivity)
 * - `a R b ⇒ b R b` (right quasi-reflexivity)
 */
class QuasiReflexivityLaw<A: Any>(
    override val rel: Relation<A>,
    override val arb: Arb<A>,
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, QuasiReflexivityCore<A> {

    override val name = "quasi-reflexivity (${rel.symbol})"
    override suspend fun test() {
        leftQuasiReflexivityCheck()
        rightQuasiReflexivityCheck()
    }
}
