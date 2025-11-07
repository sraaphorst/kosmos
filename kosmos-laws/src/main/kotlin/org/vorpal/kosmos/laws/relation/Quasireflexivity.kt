package org.vorpal.kosmos.laws.relation

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.relations.Relation

private sealed interface QuasiReflexivityCore<A: Any> {
    val rel: Relation<A>
    val pairArb: Arb<Pair<A, A>>
    val pr: Printable<A>
    val symbol: String
    val notSymbol: String

    suspend fun leftQuasiReflexivityCheck() {
        checkAll(pairArb) { (a, b) ->
            if (rel(a, b)) {
                withClue(leftFailureMessage(a, b)) {
                    check(rel(a, a))
                }
            }
        }
    }

    private fun leftFailureMessage(a: A, b: A): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        "Left quasi-reflexivity check failed: $sa $symbol $sb but $sa $notSymbol $sa"
    }

    suspend fun rightQuasiReflexivityCheck() {
        checkAll(pairArb) { (a, b) ->
            if (rel(a, b)) {
                withClue(rightFailureMessage(a, b)) {
                    check(rel(b, b))
                }
            }
        }
    }

    private fun rightFailureMessage(a: A, b: A): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        "Right quasi-reflexive check failed: $sa $symbol $sb but $sb $notSymbol $sb"
    }
}

/** Left quasi-reflexivity: a R b ⇒ a R a */
class LeftQuasiReflexivityLaw<A: Any>(
    override val rel: Relation<A>,
    override val pairArb: Arb<Pair<A, A>>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "R",
    override val notSymbol: String = "¬$symbol"
) : TestingLaw, QuasiReflexivityCore<A> {

    constructor(
        rel: Relation<A>,
        arb: Arb<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "R",
        notSymbol: String = "¬$symbol"
    ) : this(rel, Arb.pair(arb, arb), pr, symbol, notSymbol)

    override val name = "left quasi-reflexivity ($symbol)"
    override suspend fun test() = leftQuasiReflexivityCheck()
}

/** Right quasi-reflexivity: a R b ⇒ b R b */
class RightQuasiReflexivityLaw<A: Any>(
    override val rel: Relation<A>,
    override val pairArb: Arb<Pair<A, A>>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "R",
    override val notSymbol: String = "¬$symbol"
) : TestingLaw, QuasiReflexivityCore<A> {

    constructor(
        rel: Relation<A>,
        arb: Arb<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "R",
        notSymbol: String = "¬$symbol"
    ) : this(rel, Arb.pair(arb, arb), pr, symbol, notSymbol)

    override val name = "right quasi-reflexivity ($symbol)"
    override suspend fun test() = rightQuasiReflexivityCheck()
}

/** Quasi-reflexivity:
 * * a R b ⇒ a R a
 * * a R b ⇒ b R b
 */
class QuasiReflexivityLaw<A: Any>(
    override val rel: Relation<A>,
    override val pairArb: Arb<Pair<A, A>>,
    override val pr: Printable<A> = Printable.default(),
    override val symbol: String = "R",
    override val notSymbol: String = "¬$symbol"
) : TestingLaw, QuasiReflexivityCore<A> {

    constructor(
        rel: Relation<A>,
        arb: Arb<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "R",
        notSymbol: String = "¬$symbol"
    ) : this(rel, Arb.pair(arb, arb), pr, symbol, notSymbol)

    override val name = "quasi-reflexivity ($symbol)"
    override suspend fun test() {
        leftQuasiReflexivityCheck()
        rightQuasiReflexivityCheck()
    }
}
