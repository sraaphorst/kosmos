package org.vorpal.kosmos.relations.laws

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.org.vorpal.kosmos.relations.Equivalence
import org.vorpal.org.vorpal.kosmos.relations.Poset
import org.vorpal.org.vorpal.kosmos.relations.Preorder
import org.vorpal.org.vorpal.kosmos.relations.StrictTotalOrder
import org.vorpal.org.vorpal.kosmos.relations.TotalOrder

class PreorderLaws<A>(
    S: Preorder<A>,
    arb: Arb<A>
) {
    private val ref = ReflexivityLaws(S, arb)
    private val tra = TransitivityLaws(S, arb)
    suspend fun all() {
        ref.holds()
        tra.holds()
    }
}

class EquivalenceLaws<A> (
    S: Equivalence<A>,
    arb: Arb<A>
) {
    private val ref = ReflexivityLaws(S, arb)
    private val sym = SymmetryLaws(S, arb)
    private val tra = TransitivityLaws(S, arb)
    suspend fun all() {
        ref.holds()
        sym.holds()
        tra.holds()
    }
}

class PosetLaws<A> (
    S: Poset<A>,
    arb: Arb<A>,
    EQ: Eq<A>
) {
    private val ref = ReflexivityLaws(S, arb)
    private val anti = AntisymmetryLaws(S, arb, EQ)
    private val tra = TransitivityLaws(S, arb)
    suspend fun all() {
        ref.holds()
        anti.holds()
        tra.holds()
    }
}

class TotalOrderLaws<A>(
    private val S: TotalOrder<A>,
    private val arb: Arb<A>,
    EQ: Eq<A>
) {
    private val poset = PosetLaws(S, arb, EQ)
    suspend fun totality() = checkAll(arb, arb) { a, b ->
        require(S.R.rel(a, b) || S.R.rel(b, a))
    }
    suspend fun all() {
        poset.all()
        totality()
    }
}

class StrictTotalOrderLaws<A>(
    S: StrictTotalOrder<A>,
    arb: Arb<A>,
    EQ: Eq<A>
) {
    private val irr = IrreflexivityLaws(S, arb)
    private val asym = AsymmetryLaws(S, arb)
    private val trans = TransitivityStrictLaws(S, arb)
    private val total = TotalOnInequalityLaws(S, arb, EQ)

    suspend fun all() {
        irr.holds()
        asym.holds()
        trans.holds()
        total.holds()
    }
}
