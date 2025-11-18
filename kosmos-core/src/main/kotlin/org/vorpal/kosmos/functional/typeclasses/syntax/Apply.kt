package org.vorpal.kosmos.functional.typeclasses.syntax

import org.vorpal.kosmos.functional.core.Kind
import org.vorpal.kosmos.functional.typeclasses.Apply

fun <F, A, B> Apply<F>.product(
    fa: Kind<F, A>,
    fb: Kind<F, B>
): Kind<F, Pair<A, B>> =
    map2(fa, fb) { a, b -> a to b }

fun <F, A, B, C> Apply<F>.liftA2(
    f: (A, B) -> C
): (Kind<F, A>, Kind<F, B>) -> Kind<F, C> =
    { fa, fb -> map2(fa, fb, f) }

fun <F, A, B, C, D> Apply<F>.liftA3(
    f: (A, B, C) -> D
): (Kind<F, A>, Kind<F, B>, Kind<F, C>) -> Kind<F, D> =
    { fa, fb, fc -> map3(fa, fb, fc, f) }

