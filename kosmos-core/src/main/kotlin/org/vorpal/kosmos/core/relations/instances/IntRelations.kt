package org.vorpal.kosmos.core.relations.instances

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.core.relations.TotalStrictOrder
import org.vorpal.kosmos.core.relations.eqRelation
import org.vorpal.kosmos.core.relations.ltRelation

object IntRelations {
    val comparatorInt: Comparator<Int> =
        Comparator.naturalOrder()

    val ltInt: Relation<Int> =
        comparatorInt.ltRelation()

    val totalStrictInt: TotalStrictOrder<Int> =
        TotalStrictOrder.of(ltInt)

    val eqInt: Relation<Int> =
        comparatorInt.eqRelation(Symbols.EQUALS)

    val leInt: Relation<Int> =
        totalStrictInt.leFrom(eqInt)

    val totalInt: TotalOrder<Int> =
        TotalOrder.of(leInt)
}
