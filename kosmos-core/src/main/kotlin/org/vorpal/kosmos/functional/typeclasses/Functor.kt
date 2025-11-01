package org.vorpal.kosmos.functional.typeclasses

/**
 * Functor: something that can be mapped over.
 */
interface Functor<F> {
    fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B> = fa.maps(f)
    fun <A, B> Kind<F, A>.maps(f: (A) -> B): Kind<F, B> //= map(this, f)
}
