package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.LinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import java.math.BigInteger

/**
 * Infinite sequence of **Tribonacci numbers** Tₙ.
 *
 * Defined by:
 *   T(0) = 0, T(1) = 0, T(2) = 1
 *   T(n) = T(n−1) + T(n−2) + T(n-3)
 *
 * Recurrence coefficients: [1, 1, 1]
 * Initial values: [0, 0, 1]
 *
 * First few terms:
 *   0, 0, 1, 1, 2, 4, 7, 13, 24, 44, 81, 149, ...
 */
object Tribonacci : Recurrence<BigInteger> by LinearRecurrence.forBigIntFromLong(
    initial = listOf(0, 0, 1),
    coeffs = listOf(1, 1, 1)
)

/** Lightweight 64-bit Tribonacci implementation. */
object Tribonacci64 : Recurrence<Long> by LinearRecurrence.forLong(
    initial = listOf(0, 0, 1),
    coeffs = listOf(1, 1, 1)
)
