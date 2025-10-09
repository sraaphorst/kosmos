package org.vorpal.kosmos.memoization

import java.util.concurrent.ConcurrentHashMap

/**
 * A thread-safe memoizer that returns the cached version of a single parameter function.
 */
fun <A : Any, R : Any> memoize(f: (A) -> R): (A) -> R {
    val cache = ConcurrentHashMap<A, R>()
    return { a -> cache.computeIfAbsent(a, f) }
}

/**
 * A thread-safe memoizer that return the cached version of a two parameter function.
 */
fun <A : Any, B : Any, R : Any> memoize(f: (A, B) -> R): (A, B) -> R {
    val cache = ConcurrentHashMap<Pair<A, B>, R>()
    return { a, b -> cache.computeIfAbsent(a to b) { (x, y) -> f(x, y) } }
}
