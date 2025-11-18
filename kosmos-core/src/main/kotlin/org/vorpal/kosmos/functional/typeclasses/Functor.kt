package org.vorpal.kosmos.functional.typeclasses

import org.vorpal.kosmos.functional.core.Kind

/**
 * A Functor lets you map a pure function over a context F<_>.
 *
 * Laws (for all fa, f, g):
 *  1) Identity: map(fa, ::identity) == fa
 *  2) Composition: map(map(fa, f), g) == map(fa) { a -> g(f(a)) }
 */
interface Functor<F> {
    fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B>
}
