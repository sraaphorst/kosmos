package org.vorpal.kosmos.functional.typeclasses

/**
 * Marker interface for higher-kinded types.
 * This acts as the "witness" that F<_> exists, like Haskell's type constructor.
 */
interface Kind<F, out A>