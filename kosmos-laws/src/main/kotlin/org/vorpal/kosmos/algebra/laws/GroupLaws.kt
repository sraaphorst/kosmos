package org.vorpal.kosmos.algebra.laws

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.core.Eq
import io.kotest.property.Arb
import org.vorpal.kosmos.laws.property.InvertibilityLaw

open class GroupLaws<A, S>(
    S: S,
    arb: Arb<A>,
    EQ: Eq<A>,
    nonInvertible: Set<A> = emptySet()
) : MonoidLaws<A, S>(S, arb, EQ)
        where S : Group<A, *> {

    private val inverses = InvertibilityLaw(S, arb, EQ, nonInvertible)

    override suspend fun all() {
        super.all()
        inverses.existsAndCancels()
    }
}