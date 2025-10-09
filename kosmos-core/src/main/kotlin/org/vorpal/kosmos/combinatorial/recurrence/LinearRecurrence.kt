package org.vorpal.kosmos.combinatorial.recurrence

import org.vorpal.kosmos.combinatorial.LinearRecurrence
import java.math.BigInteger
import kotlin.collections.ArrayDeque

/**
 * Linear recurrence:
 *  - a_{n+1} = c1*a_n + c2*a_{n-1} + ... + ck*a_{n-k+1}
 *
 * @param initial initial window (size k)
 * @param coeffs  coefficients (size k), aligned with most-recent-first window
 * @param zero    additive identity of T
 * @param add     addition in T
 * @param multiply multiplication in T
 */
data class LinearRecurrence<T>(
    override val initial: List<T>,
    val coeffs: List<T>,
    val zero: T,
    val add: (T, T) -> T,
    val multiply: (T, T) -> T
) : Recurrence<T> {

    init {
        require(initial.isNotEmpty()) { "initial cannot be empty" }
        require(initial.size == coeffs.size) { "initial and coeffs must have equal size" }
    }

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private val window = ArrayDeque(initial)

        override fun hasNext(): Boolean = true

        override fun next(): T {
            val out = window.first()
            val next = coeffs.zip(window).fold(zero) { acc, (c, v) ->
                add(acc, multiply(c, v))
            }
            window.removeFirst()
            window.addLast(next)
            return out
        }
    }

    companion object {

        fun forInt(initial: List<Int>, coeffs: List<Int>) =
            LinearRecurrence(initial, coeffs, 0, Int::plus, Int::times)

        fun forLong(initial: List<Long>, coeffs: List<Long>) =
            LinearRecurrence(initial, coeffs, 0L, Long::plus, Long::times)

        fun forBigInt(initial: List<BigInteger>, coeffs: List<BigInteger>) =
            LinearRecurrence(initial, coeffs, BigInteger.ZERO, BigInteger::add, BigInteger::multiply)

        fun forBigInt(initial: List<Int>, coeffs: List<Int>) =
            forBigInt(initial.map(Int::toBigInteger), coeffs.map(Int::toBigInteger))

        fun forBigInt(initial: List<Long>, coeffs: List<Long>) =
            forBigInt(initial.map(Long::toBigInteger), coeffs.map(Long::toBigInteger))
    }
}