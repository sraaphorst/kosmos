package org.vorpal.kosmos.algebra

/**
 * Abstract functor/monad for index morphisms.
 */
interface IndexFunction<T : IndexFunction<T>> {
    fun run(n: Int): Int
    infix fun andThen(other: T): T
    fun flatMap(f: (Int) -> T): T
    fun repeat(k: Int): T
}
