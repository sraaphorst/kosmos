package org.vorpal.kosmos.core.relations.instances

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.relations.StrictOrder
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.core.relations.TotalStrictOrder
import org.vorpal.kosmos.core.relations.instances.RationalRelations.RationalLT
import org.vorpal.kosmos.core.relations.leRelation
import org.vorpal.kosmos.core.relations.ltRelation

object RealRelations {
    /**
     * Comparator for the Reals. We do not allow NaN or Inf values.
     */
    val RealComparator = Comparator<Real> { a, b ->
        require(a.isFinite() && !a.isNaN()) { "Real must be finite/non-NaN, got: $a" }
        require(b.isFinite() && !b.isNaN()) { "Real must be finite/non-NaN, got: $b" }
        a.compareTo(b)
    }

    val RealLT: Relation<Real> =
        RealComparator.ltRelation()

    val RealStrictOrder: StrictOrder<Real> =
        StrictOrder.of(RealLT)

    val RealTotalOrder: TotalOrder<Real> =
        TotalOrder.of(RealComparator.leRelation())

    val RealTotalStrictOrder: TotalStrictOrder<Real> =
        TotalStrictOrder.of(RealLT)

    val RealEqByComparator: Relation<Real> =
        Relation(Symbols.APPROX) { a, b -> RealComparator.compare(a, b) == 0 }

    val RealLE: Relation<Real> =
        RealStrictOrder.leFrom(RealEqByComparator)
}
