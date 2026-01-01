package org.vorpal.kosmos.core.setoid

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.relations.Relation

/**
 * A Setoid is a set equipped with a chosen equality (intended to be an equivalence relation).
 */
interface Setoid<A : Any> {
    val eq: Eq<A>

    /** View the equality as a Relation, when APIs want Relation. */
    val relation: Relation<A>
        get() = Relation(Symbols.EQUALS) { a, b -> eq(a, b) }

    companion object {
        fun <A : Any> of(
            eq: Eq<A>
        ): Setoid<A> = object : Setoid<A> {
            override val eq = eq
        }
    }
}
