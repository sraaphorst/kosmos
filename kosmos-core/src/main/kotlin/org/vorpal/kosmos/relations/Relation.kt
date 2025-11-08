package org.vorpal.kosmos.relations

fun interface Relation<A> {
    fun rel(a: A, b: A): Boolean
    operator fun invoke(a: A, b: A) = rel(a, b)
}

// Property markers (purely declarative: laws will test)
interface Reflexive<A> { val R: Relation<A> }
interface Symmetric<A> { val R: Relation<A> }
interface Antisymmetric<A> { val R: Relation<A> }
interface Transitive<A> { val R: Relation<A> }

interface Preorder<A> : Reflexive<A>, Transitive<A>
interface Equivalence<A> : Reflexive<A>, Symmetric<A>, Transitive<A>

// For total order, totality is a law and not a marker.
interface Poset<A> : Reflexive<A>, Antisymmetric<A>, Transitive<A>
interface TotalOrder<A> : Poset<A>
