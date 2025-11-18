package org.vorpal.kosmos.functional.typeclasses

import org.vorpal.kosmos.functional.core.Kind

/**
 * Apply adds function application inside the context.
 *
 * Think: we have `F<(A)->B>` and `F<A>`, and we want `F<B>`.
 */
interface Apply<F>: Functor<F> {
    fun <A, B> ap(ff: Kind<F, (A) -> B>, fa: Kind<F, A>): Kind<F, B>

    fun <F, A, B, C> Apply<F>.map2(
        fa: Kind<F, A>,
        fb: Kind<F, B>,
        f: (A, B) -> C
    ): Kind<F, C> =
        ap(map(fa) { a -> { b: B -> f(a, b) } }, fb)

    fun <F, A, B, C, D> Apply<F>.map3(
        fa: Kind<F, A>,
        fb: Kind<F, B>,
        fc: Kind<F, C>,
        f: (A, B, C) -> D
    ): Kind<F, D> =
        ap(map2(fa, fb) { a, b -> { c: C -> f(a, b, c) } }, fc)

    // Keep the left value, drop the right (Haskell <* , Cats productL)
    fun <A, B> productL(
        fa: Kind<F, A>,
        fb: Kind<F, B>
    ): Kind<F, A> =
        map2(fa, fb) { a, _ -> a }

    // Drop the left value, keep the right (Haskell *> , Cats productR)
    fun <A, B> productR(
        fa: Kind<F, A>,
        fb: Kind<F, B>
    ): Kind<F, B> =
        map2(fa, fb) { _, b -> b }
}
