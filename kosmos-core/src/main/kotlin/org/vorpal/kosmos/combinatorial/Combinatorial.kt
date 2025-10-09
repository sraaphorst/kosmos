package org.vorpal.kosmos.combinatorial

import org.vorpal.kosmos.memoization.memoize
import java.math.BigInteger


/* ================================================================
 *  Factorial
 * ================================================================ */

/**
 * Call Factorial(n) to calculate **n!**, which is returned as a `BigInteger`.
 */
object Factorial {
    private val cache = memoize<Int, BigInteger> { n ->
        when (n) {
            0, 1 -> BigInteger.ONE
            else -> n.toBigInteger() * this(n - 1)
        }
    }

    operator fun invoke(n: Int): BigInteger = cache(n)
}

/**
 * Calculator for the binomial coefficients.
 * This can also be done using Pascal in the arrays package.
 */
object Binomial {
    private val cache = memoize<Int, Int, BigInteger> { n, k ->
        when (k) {
            !in 0..n -> BigInteger.ZERO
            0, n           -> BigInteger.ONE
            else           -> this(n - 1, k - 1) + this(n - 1, k)
        }
    }

    operator fun invoke(n: Int, k: Int): BigInteger = cache(n, k)
}
