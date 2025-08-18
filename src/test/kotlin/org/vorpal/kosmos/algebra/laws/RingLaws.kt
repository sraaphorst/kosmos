package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.core.Eq

class RingLaws<A>(
    R: Ring<A, *>,
    arb: Arb<A>,
    EQ: Eq<A>
) {
    private val additiveGroup = AbelianGroupLaws(R.add, arb, EQ)
    private val multiplicativeMonoid = MonoidLaws(R.mul, arb, EQ)
    private val distributivity = DistributivityLaws.forRing(R, arb, EQ)

    suspend fun all() {
        additiveGroup.all()
        multiplicativeMonoid.all()
        distributivity.bothSides()
    }
}