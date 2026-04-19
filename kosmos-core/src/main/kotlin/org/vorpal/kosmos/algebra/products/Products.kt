package org.vorpal.kosmos.algebra.products

object Products {
    fun <A : Any> repeat(a: A): Pair<A, A> = Pair(a, a)

    fun <A : Any, B : Any> leftProjection(): (Pair<A, B>) -> A = Pair<A, B>::first

    fun <A : Any, B : Any> rightProjection(): (Pair<A, B>) -> B = Pair<A, B>::second

    fun <A : Any> diagonal(): (A) -> Pair<A, A> =
        { a -> Pair(a, a) }
}
