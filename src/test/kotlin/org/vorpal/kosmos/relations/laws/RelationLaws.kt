package org.vorpal.kosmos.relations.laws

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals
import org.vorpal.kosmos.relations.*
import org.vorpal.org.vorpal.kosmos.relations.Antisymmetric
import org.vorpal.org.vorpal.kosmos.relations.Reflexive
import org.vorpal.org.vorpal.kosmos.relations.Symmetric
import org.vorpal.org.vorpal.kosmos.relations.Transitive

class ReflexivityLaws<A>(
    private val P: Reflexive<A>,
    private val arb: Arb<A>
) {
    suspend fun holds() = checkAll(arb) { a -> require(P.R.rel(a, a))}
}

class SymmetryLaws<A>(
    private val P: Symmetric<A>,
    private val arb: Arb<A>
) {
    suspend fun holds() = checkAll(arb, arb) { a, b ->
        if (P.R.rel(a, b))
            require(P.R.rel(b, a))
    }
}

class AntisymmetryLaws<A>(
    private val P: Antisymmetric<A>,
    private val arb: Arb<A>,
    private val EQ: Eq<A>
) {
    suspend fun holds() = checkAll(arb, arb) { a, b ->
        if (P.R.rel(a, b) && P.R.rel(b, a))
            EQ.assertEquals(a, b)
    }
}

class TransitivityLaws<A>(
    private val P: Transitive<A>,
    private val arb: Arb<A>
) {
    suspend fun holds() = checkAll(arb, arb, arb) { a, b, c ->
        if (P.R.rel(a, b) && P.R.rel(b, c))
            require(P.R.rel(a, c))
    }
}
