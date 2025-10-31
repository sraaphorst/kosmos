package org.vorpal.kosmos.functional.typeclasses

/**
 * Functor: something that can be mapped over.
 */
interface Functor<F> {
    fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B>
}
