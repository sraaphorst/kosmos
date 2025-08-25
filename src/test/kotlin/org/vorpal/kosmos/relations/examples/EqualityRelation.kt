package org.vorpal.kosmos.relations.examples

import org.vorpal.org.vorpal.kosmos.relations.Equivalence
import org.vorpal.org.vorpal.kosmos.relations.Relation

class EqRelation<A> : Equivalence<A> {
    override val R = Relation<A> { a, b -> a == b }
}
