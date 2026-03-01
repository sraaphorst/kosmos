package org.vorpal.kosmos.core.relations.instances

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.rational.WheelZ
import org.vorpal.kosmos.core.relations.Poset
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.relations.StrictOrder

object WheelZRelations {
    val WheelZLe: Relation<WheelZ> = Relation(Symbols.LESS_THAN_EQ) { a, b ->
        when {
            a.isBottom || b.isBottom -> a.isBottom && b.isBottom
            a.isInfinite && b.isInfinite -> a.n <= b.n // -1/0 <= +1/0
            a.isInfinite -> a.n.signum() < 0 // -∞ <= finite, +∞ <= finite is false
            b.isInfinite -> b.n.signum() > 0 // finite <= +∞ true, finite <= -∞ false
            else -> a.n * b.d <= b.n * a.d
        }
    }

    val WheelZPartialOrder: Poset<WheelZ> = Poset.of(WheelZLe)
    val WheelZLt: Relation<WheelZ> = WheelZPartialOrder.lt
    val WheelZGt: Relation<WheelZ> = WheelZPartialOrder.gt
    val WheelZGe: Relation<WheelZ> = WheelZPartialOrder.ge
    val WheelZEqByComparator: Relation<WheelZ> = WheelZPartialOrder.eq
    val WheelZStrictOrder: StrictOrder<WheelZ> = StrictOrder.of(WheelZLt)
}
