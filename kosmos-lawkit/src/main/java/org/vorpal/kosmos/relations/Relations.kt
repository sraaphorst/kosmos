package org.vorpal.kosmos.relations

import org.vorpal.kosmos.core.Eq

object Relations {
    /** Given a Poset (≤), induce a strict relation: a < b iff a ≤ b && !(b ≤ a). */
    fun <A> strictFromPoset(poset: Poset<A>): StrictRelation<A> =
        StrictRelation { a, b -> poset.R.rel(a, b) && !poset.R.rel(b, a) }

    /** Given a strict relation and Eq, build a non-strict ≤ : a ≤ b iff a < b or a == b. */
    fun <A> nonStrictFromStrict(strict: StrictRelation<A>, EQ: Eq<A>): Relation<A> =
        Relation { a, b -> strict.lt(a, b) || EQ.eqv(a, b) }
}