package org.vorpal.kosmos.memoization

/**
 * Safely memoize a recursive single-argument function.
 *
 * Example:
 * ```
 * val fib = recursiveMemoize<Int, BigInteger> { self, n ->
 *     if (n < 2) BigInteger.valueOf(n.toLong())
 *     else self(n - 1) + self(n - 2)
 * }
 * ```
 */
fun <A, R> recursiveMemoize(f: (self: (A) -> R, A) -> R): (A) -> R {
    val cache = mutableMapOf<A, R>()
    lateinit var selfRef: (A) -> R
    selfRef = { a -> cache.getOrPut(a) { f(selfRef, a) } }
    return selfRef
}

/**
 * Safely memoize a recursive two-argument function.
 *
 * Example:
 * ```
 * val stirling = recursiveMemoize2<Int, Int, BigInteger> { self, n, k ->
 *     when {
 *         n == 0 && k == 0 -> BigInteger.ONE
 *         k == 0 || k > n -> BigInteger.ZERO
 *         else -> self(n - 1, k - 1) + BigInteger.valueOf(k.toLong()) * self(n - 1, k)
 *     }
 * }
 * ```
 */
fun <A, B, R> recursiveMemoize2(f: (self: (A, B) -> R, A, B) -> R): (A, B) -> R {
    val cache = mutableMapOf<Pair<A, B>, R>()
    lateinit var selfRef: (A, B) -> R
    selfRef = { a, b -> cache.getOrPut(a to b) { f(selfRef, a, b) } }
    return selfRef
}