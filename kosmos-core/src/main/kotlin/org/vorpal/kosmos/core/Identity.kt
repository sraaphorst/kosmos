package org.vorpal.kosmos.core

/**
 * The identity function for a single parameter.
 */
class Identity<T>: (T) -> T {
    override fun invoke(p: T): T = p
}
