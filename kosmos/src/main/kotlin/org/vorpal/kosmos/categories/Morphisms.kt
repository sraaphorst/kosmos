package org.vorpal.kosmos.categories

fun interface Morphism<A, B> {
    fun apply(a: A): B
}

infix fun <A, B, C> Morphism<A, B>.then(g: Morphism<B, C>): Morphism<A, C> =
    Morphism { a -> g.apply(this.apply(a)) }

fun <A, B> Morphism<A, B>.eqOn(domain: FiniteSet<A>, eqB: (B, B) -> Boolean): (Morphism<A, B>) -> Boolean =
    { other -> domain.toList().all { a -> eqB(this.apply(a), other.apply(a)) } }
