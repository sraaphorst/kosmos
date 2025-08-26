package org.vorpal.kosmos.relations.demo

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.registerEquivalence
import org.vorpal.kosmos.registerStrictTotalOrder
import org.vorpal.kosmos.registerTotalOrder
import org.vorpal.kosmos.relations.examples.EqRelation
import org.vorpal.kosmos.relations.laws.EquivalenceLaws
import org.vorpal.kosmos.relations.laws.StrictTotalOrderLaws
import org.vorpal.kosmos.relations.laws.TotalOrderLaws
import org.vorpal.org.vorpal.kosmos.relations.Relation
import org.vorpal.org.vorpal.kosmos.relations.StrictTotalOrder
import org.vorpal.org.vorpal.kosmos.relations.TotalOrder
import org.vorpal.org.vorpal.kosmos.relations.strictFromPoset

object IntLE : TotalOrder<Int> {
    override val R = Relation<Int> { a, b -> a <= b }
}

object StrictInt : StrictTotalOrder<Int> {
    override val LT = strictFromPoset(IntLE)
}

class RelationSpec : StringSpec({
    registerEquivalence(
        "Built-in equality on Int",
        EquivalenceLaws(EqRelation(), Arb.int())
    )

    registerTotalOrder(
        "Int ≤",
        TotalOrderLaws(IntLE, Arb.int(), Eqs.int)
    )

    registerStrictTotalOrder(
        name = "Int < (derived from ≤)",
        laws = StrictTotalOrderLaws(StrictInt, Arb.int(), Eqs.int)
    )
})
