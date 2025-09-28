package org.vorpal.kosmos.categories

import org.vorpal.kosmos.combinatorial.FiniteSet

/** A general morphism from one type to another. */
fun interface Morphism<A, B> {
    fun apply(a: A): B
}

/** Morphism composition: note (f then g)(x) = g(f(x)). */
infix fun <A, B, C> Morphism<A, B>.then(g: Morphism<B, C>): Morphism<A, C> =
    Morphism { a -> g.apply(this.apply(a)) }

/** Create an equality checker for a given morphism from domain (a set of A) to B.
 * This produces a function that takes another morphism from A to B and determines if they are equal over the domain. */
fun <A, B> Morphism<A, B>.eqOn(domain: FiniteSet<A>, eqB: (B, B) -> Boolean): (Morphism<A, B>) -> Boolean =
    { other -> domain.toList().all { a -> eqB(this.apply(a), other.apply(a)) } }

object Morphisms {
    fun <A> identity(): Morphism<A, A> = Morphism { it }
}