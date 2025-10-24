package org.vorpal.kosmos.categories

/**
 * Marker interface for higher-kinded types.
 * This acts as the "witness" that F<_> exists, like Haskell's type constructor.
 */
interface Kind<F, out A>

@Suppress("UNCHECKED_CAST")
fun <F, A> Kind<F, A>.fix(): A =
    this as A
