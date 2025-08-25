package org.vorpal.org.vorpal.kosmos.relations

fun interface StrictRelation<A> {
    fun lt(a: A, b: A): Boolean
}

interface Irreflexive<A> { val LT: StrictRelation<A> }
interface Asymmetric<A> { val LT: StrictRelation<A> }
interface TransitiveS<A> { val LT: StrictRelation<A> }
interface TotalOnInequality<A> { val LT: StrictRelation<A> }

// This is a more strict form of total order: for all a ≠ b, a < b or b < a.
interface StrictTotalOrder<A> : Irreflexive<A>, Asymmetric<A>, TransitiveS<A>, TotalOnInequality<A>
