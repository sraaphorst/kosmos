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

    fun <A, B> traverse(list: List<A>, f: (A) -> Kind<F, B>): Kind<F, List<B>> {
        val initial = pure(emptyList<B>())
        return list.foldRight(initial) { a, acc ->
            map2(f(a), acc) { head, tail -> listOf(head) + tail }
        }
    }

    fun <A> sequence(list: List<Kind<F, A>>): Kind<F, List<A>> =
        traverse(list) { it }
}
