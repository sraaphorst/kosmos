package org.vorpal.kosmos.functional.typeclasses

import org.vorpal.kosmos.functional.core.Kind

/**
 * Apply adds function application inside the context.
 *
 * Think: we have `F<(A)->B>` and `F<A>`, and we want `F<B>`.
 */
interface Apply<F>: Functor<F> {
    fun <A, B> ap(ff: Kind<F, (A) -> B>, fa: Kind<F, A>): Kind<F, B>

    fun <A, B, C> map2(
        fa: Kind<F, A>,
        fb: Kind<F, B>,
        f: (A, B) -> C
    ): Kind<F, C> =
        ap(map(fa) { a -> { b: B -> f(a, b) } }, fb)

    fun <A, B, C, D> map3(
        fa: Kind<F, A>,
        fb: Kind<F, B>,
        fc: Kind<F, C>,
        f: (A, B, C) -> D
    ): Kind<F, D> =
        ap(map2(fa, fb) { a, b -> { c: C -> f(a, b, c) } }, fc)
}
