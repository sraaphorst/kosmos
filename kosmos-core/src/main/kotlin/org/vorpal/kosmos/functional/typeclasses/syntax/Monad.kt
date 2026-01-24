package org.vorpal.kosmos.functional.typeclasses.syntax

import org.vorpal.kosmos.functional.core.Kind
import org.vorpal.kosmos.functional.typeclasses.Monad

inline fun <F, A, B> Monad<F>.bind(
    fa: Kind<F, A>,
    crossinline f:(A) -> Kind<F, B>
): Kind<F, B> = flatMap(fa) { a -> f(a) }

/**
 * In this case, andThen is the "sequence, and ignore the left result" combinator.
 */
fun <F, A, B> Monad<F>.andThen(
    fa: Kind<F, A>,
    fb: Kind<F, B>
): Kind<F, B> = flatMap(fa) { _ -> fb }

/**
 * Compose is just the opposite order of andThen, so perform fb and then fa.
 */
fun <F, A, B> Monad<F>.compose(
    fa: Kind<F, A>,
    fb: Kind<F, B>
): Kind<F, A> = andThen(fb, fa)
