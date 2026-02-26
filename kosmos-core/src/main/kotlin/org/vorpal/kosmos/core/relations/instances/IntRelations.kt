package org.vorpal.kosmos.core.relations.instances

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.core.relations.TotalStrictOrder
import org.vorpal.kosmos.core.relations.eqRelation
import org.vorpal.kosmos.core.relations.ltRelation

object IntRelations {
    val IntComparator: Comparator<Int> =
        Comparator.naturalOrder()

    val IntLT: Relation<Int> =
        IntComparator.ltRelation()

    val IntTotalStrict: TotalStrictOrder<Int> =
        TotalStrictOrder.of(IntLT)

    val IntEqByComparator: Relation<Int> =
        IntComparator.eqRelation(Symbols.EQUALS)

    val IntLE: Relation<Int> =
        IntTotalStrict.leFrom(IntEqByComparator)

    val IntTotalOrder: TotalOrder<Int> =
        TotalOrder.of(IntLE)
}
