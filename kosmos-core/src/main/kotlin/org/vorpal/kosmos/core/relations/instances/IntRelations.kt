package org.vorpal.kosmos.core.relations.instances

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.core.relations.TotalStrictOrder
import org.vorpal.kosmos.core.relations.eqRelation
import org.vorpal.kosmos.core.relations.ltRelation
import java.math.BigInteger

object IntRelations {
    val IntComparator: Comparator<BigInteger> =
        Comparator.naturalOrder()

    val IntLT: Relation<BigInteger> =
        IntComparator.ltRelation()

    val IntTotalStrict: TotalStrictOrder<BigInteger> =
        TotalStrictOrder.of(IntLT)

    val IntEqByComparator: Relation<BigInteger> =
        IntComparator.eqRelation(Symbols.EQUALS)

    val IntLE: Relation<BigInteger> =
        IntTotalStrict.leFrom(IntEqByComparator)

    val IntTotalOrder: TotalOrder<BigInteger> =
        TotalOrder.of(IntLE)
}
