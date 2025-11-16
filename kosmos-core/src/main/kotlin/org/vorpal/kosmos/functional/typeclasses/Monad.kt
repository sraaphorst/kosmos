package org.vorpal.kosmos.functional.typeclasses

import org.vorpal.kosmos.functional.core.Kind

interface Monad<F>: Applicative<F> {
    fun <A> pure(a: A): Kind<F, A>
    fun <A, B> flatMap(fa: Kind<F, A>, f: (A) -> Kind<F, B> ): Kind<F, B>
    override fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B> =
        flatMap(fa) { a -> pure(f(a)) }
    override fun <A, B> ap(ff: Kind<F, (A) -> B>, fa: Kind<F, A>): Kind<F, B> =
        flatMap(ff) { f -> map(fa, f) }
    fun <A> flatten(ffa: Kind<F, Kind<F, A>>): Kind<F, A> =
        flatMap(ffa) { it }
}
