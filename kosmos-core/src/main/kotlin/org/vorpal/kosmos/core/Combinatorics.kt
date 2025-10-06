package org.vorpal.kosmos.core

import java.math.BigInteger

/**
 * Compute n! (factorial) as a [Long].
 * Uses a fold expression over primitive range; efficient and allocation-free.
 */
fun Int.factorial(): Long {
    require(this >= 0) { "factorial: n must be non-negative (was $this)" }
    return when (this) {
        0, 1 -> 1L
        else -> (2..this).fold(1L) { acc, i -> acc * i }
    }
}

/**
 * Compute n! (factorial) as a [BigInteger].
 * Suitable for large n where overflow would occur.
 */
fun Int.bigFactorial(): BigInteger {
    require(this >= 0) { "factorial: n must be non-negative (was $this)" }
    return when (this) {
        0, 1 -> BigInteger.ONE
        else -> (2..this).fold(BigInteger.ONE) { acc, i ->
            acc * BigInteger.valueOf(i.toLong())
        }
    }
}

/**
 * Falling factorial: n·(n−1)·...·(n−k+1)
 */
fun fallingFactorial(n: Int, k: Int): Long {
    require(n >= 0 && k >= 0) { "fallingFactorial: n,k must be ≥ 0 (were $n,$k)" }
    require(k <= n) { "fallingFactorial: k cannot exceed n (n=$n, k=$k)" }
    return if (k == 0) 1L else (0 until k).fold(1L) { acc, i -> acc * (n - i) }
}

/**
 * Falling factorial (BigInteger version)
 */
fun bigFallingFactorial(n: Int, k: Int): BigInteger {
    require(n >= 0 && k >= 0) { "fallingFactorial: n,k must be ≥ 0 (were $n,$k)" }
    require(k <= n) { "fallingFactorial: k cannot exceed n (n=$n, k=$k)" }
    return if (k == 0) BigInteger.ONE else (0 until k).fold(BigInteger.ONE) { acc, i ->
        acc * BigInteger.valueOf((n - i).toLong())
    }
}

/**
 * Binomial coefficient "n choose k" = n! / (k! * (n - k)!).
 * Uses multiplicative form to avoid overflow for moderate n.
 */
fun binomial(n: Int, k: Int): Long {
    require(n >= 0 && k >= 0 && k <= n) { "binomial: require 0 ≤ k ≤ n (n=$n, k=$k)" }
    val m = minOf(k, n - k)
    return (1..m).fold(1L) { acc, i ->
        acc * (n - m + i) / i
    }
}

/**
 * Binomial coefficient as a [BigInteger].
 */
fun bigBinomial(n: Int, k: Int): BigInteger {
    require(n >= 0 && k >= 0 && k <= n) { "binomial: require 0 ≤ k ≤ n (n=$n, k=$k)" }
    val m = minOf(k, n - k)
    return (1..m).fold(BigInteger.ONE) { acc, i ->
        acc * BigInteger.valueOf((n - m + i).toLong()) / BigInteger.valueOf(i.toLong())
    }
}

// This allows us to call factorial as an extension method or as a function.
object Combinatorics {
    fun factorial(n: Int): Long = n.factorial()
    fun bigFactorial(n: Int): BigInteger = n.bigFactorial()
}
