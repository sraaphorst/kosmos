package org.vorpal.kosmos.relations.laws

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.org.vorpal.kosmos.relations.Asymmetric
import org.vorpal.org.vorpal.kosmos.relations.Irreflexive
import org.vorpal.org.vorpal.kosmos.relations.TotalOnInequality
import org.vorpal.org.vorpal.kosmos.relations.TransitiveS

class IrreflexivityLaws<A>(
    private val S: Irreflexive<A>,
    private val arb: Arb<A>
) {
    suspend fun holds() = checkAll(arb) { a -> require(!S.LT.lt(a, a))}
}

class AsymmetryLaws<A>(
    private val S: Asymmetric<A>,
    private val arb: Arb<A>
) {
    suspend fun holds() = checkAll(arb, arb) { a, b ->
        if (S.LT.lt(a, b)) require(!S.LT.lt(b, a))
    }
}

class TransitivityStrictLaws<A>(
    private val S: TransitiveS<A>,
    private val arb: Arb<A>
) {
    suspend fun holds() = checkAll(arb, arb, arb) { a, b, c ->
        if (S.LT.lt(a, b) && S.LT.lt(b, c)) require(S.LT.lt(a, c))
    }
}

class TotalOnInequalityLaws<A>(
    private val S: TotalOnInequality<A>,
    private val arb: Arb<A>,
    private val EQ: Eq<A>
) {
    suspend fun holds() = checkAll(arb, arb) { a, b ->
        if (!EQ.eqv(a, b)) {
            require(S.LT.lt(a, b) || S.LT.lt(b, a))
        }
    }
}
