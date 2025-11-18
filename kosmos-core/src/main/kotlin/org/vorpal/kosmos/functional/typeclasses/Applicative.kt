package org.vorpal.kosmos.functional.typeclasses

import org.vorpal.kosmos.functional.core.Kind

/**
 * Applicative adds the ability to inject a pure value with `pure`.
 *
 * Key intuition:
 *  - `pure(x)` lifts a pure value into the context
 *  - `map` can be derived from `ap` + `just`
 *
 * Laws (in addition to Functor/Apply laws):
 *  Identity, Homomorphism, Interchange, Composition.
 */
interface Applicative<F>: Apply<F> {
    fun <A> pure(a: A): Kind<F, A>

    override fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B> =
        ap(pure(f), fa)
}
