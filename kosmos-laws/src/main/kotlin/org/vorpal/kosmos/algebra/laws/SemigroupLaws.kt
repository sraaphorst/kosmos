package org.vorpal.kosmos.algebra.laws

import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.Eq
import io.kotest.property.Arb

open class SemigroupLaws<A, S>(
    protected val S: S,
    protected val arb: Arb<A>,
    protected val EQ: Eq<A>
) where S : Semigroup<A> {

    private val associativity = AssociativityLaws(S, arb, EQ)

    open suspend fun all() {
        associativity.holds()
    }
}
