package org.vorpal.kosmos.core.relations.instances

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.core.relations.TotalStrictOrder
import org.vorpal.kosmos.core.relations.eqRelation
import org.vorpal.kosmos.core.relations.ltRelation
import org.vorpal.kosmos.core.tropical.TropicalMin

object TropicalMinRelations {
    val comparatorTropicalMin: Comparator<TropicalMin> = Comparator { a, b ->
        when (a) {
            TropicalMin.PosInfinity ->
                if (b is TropicalMin.PosInfinity) 0 else 1
            is TropicalMin.Finite ->
                when (b) {
                    TropicalMin.PosInfinity -> -1
                    is TropicalMin.Finite -> a.value.compareTo(b.value)
                }
        }
    }

    val ltTropicalMin: Relation<TropicalMin> =
        comparatorTropicalMin.ltRelation(Symbols.LESS_THAN)

    val totalStrictTropicalMin: TotalStrictOrder<TropicalMin> =
        TotalStrictOrder.of(ltTropicalMin)

    val eqTropicalMin: Relation<TropicalMin> =
        comparatorTropicalMin.eqRelation(Symbols.EQUALS)

    val leTropicalMin: Relation<TropicalMin> =
        totalStrictTropicalMin.leFrom(eqTropicalMin)

    val totalTropicalMin: TotalOrder<TropicalMin> =
        TotalOrder.of(leTropicalMin)
}
