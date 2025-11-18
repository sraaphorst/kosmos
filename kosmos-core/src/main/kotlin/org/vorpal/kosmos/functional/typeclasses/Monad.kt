package org.vorpal.kosmos.functional.typeclasses

import org.vorpal.kosmos.functional.core.Kind
import org.vorpal.kosmos.functional.datastructures.Either

// Monad — derive map/ap from flatMap + pure for coherence
interface Monad<F>: Applicative<F> {
    fun <A, B> flatMap(fa: Kind<F, A>, f: (A) -> Kind<F, B> ): Kind<F, B>

    override fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B> =
        flatMap(fa) { a -> pure(f(a)) }

    override fun <A, B> ap(ff: Kind<F, (A) -> B>, fa: Kind<F, A>): Kind<F, B> =
        flatMap(ff) { f -> map(fa, f) }

    fun <A> flatten(ffa: Kind<F, Kind<F, A>>): Kind<F, A> =
        flatMap(ffa) { it }

    // Tail recursion helper (for stack-safety in deep chains).
    fun <A, B> tailRecM(
        a: A,
        f: (A) -> Kind<F, Either<A, B>>
    ): Kind<F, B> =
        // default, not stack-safe; instances can override with stack-safe impl
        flatMap(f(a)) { e ->
            when (e) {
                is Either.Left -> tailRecM(e.value, f)
                is Either.Right -> pure(e.value)
            }
        }
}
