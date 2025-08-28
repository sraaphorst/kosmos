package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.laws.AssociativityLaws
import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.laws.property.AssociativityLaw

open class SemigroupLaws<A, S>(
    protected val candidate: S,
    protected val arb: Arb<A>,
    protected val eq: Eq<A>
) where S : Semigroup<A> {

    private val associativity = AssociativityLaw(candidate, arb, eq)

    open suspend fun all() {
        associativity.check()
    }
}