package org.vorpal.kosmos.core.relations.instances

import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.relations.StrictOrder
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.core.relations.TotalStrictOrder
import org.vorpal.kosmos.core.relations.leRelation
import org.vorpal.kosmos.core.relations.ltRelation

object RationalRelations {
    val RationalComparator: Comparator<Rational> =
        Comparator.naturalOrder()

    val RationalLT: Relation<Rational> =
        RationalComparator.ltRelation()

    val RationalStrictOrder: StrictOrder<Rational> =
        StrictOrder.of(RationalLT)

    val RationalTotalOrder: TotalOrder<Rational> =
        TotalOrder.of(RationalComparator.leRelation())

    val RationalTotalStrictOrder: TotalStrictOrder<Rational> =
        TotalStrictOrder.of(RationalComparator.ltRelation())

    val RationalEqByComparator: Relation<Rational> =
        Relation(Symbols.EQUALS) { a, b -> RationalComparator.compare(a, b) == 0 }

    val RationalLE: Relation<Rational> =
        RationalStrictOrder.leFrom(RationalEqByComparator)
}