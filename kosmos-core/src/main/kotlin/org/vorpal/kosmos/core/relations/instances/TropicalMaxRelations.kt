package org.vorpal.kosmos.core.relations.instances

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.core.relations.TotalStrictOrder
import org.vorpal.kosmos.core.relations.eqRelation
import org.vorpal.kosmos.core.relations.ltRelation
import org.vorpal.kosmos.core.tropical.TropicalMax

object TropicalMaxRelations {
    val comparatorTropicalMax: Comparator<TropicalMax> = Comparator { a, b ->
        when (a) {
            TropicalMax.NegInfinity ->
                if (b is TropicalMax.NegInfinity) 0 else -1
            is TropicalMax.Finite ->
                when (b) {
                    TropicalMax.NegInfinity -> 1
                    is TropicalMax.Finite -> a.value.compareTo(b.value)
                }
        }
    }

    val ltTropicalMax: Relation<TropicalMax> =
        comparatorTropicalMax.ltRelation(Symbols.LESS_THAN)

    val totalStrictTropicalMax: TotalStrictOrder<TropicalMax> =
        TotalStrictOrder.of(ltTropicalMax)

    val eqTropicalMax: Relation<TropicalMax> =
        comparatorTropicalMax.eqRelation(Symbols.EQUALS)

    val leTropicalMax: Relation<TropicalMax> =
        totalStrictTropicalMax.leFrom(eqTropicalMax)

    val totalTropicalMax: TotalOrder<TropicalMax> =
        TotalOrder.of(leTropicalMax)
}
