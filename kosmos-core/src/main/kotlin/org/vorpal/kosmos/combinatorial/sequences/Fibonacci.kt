package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.recurrence.LinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import java.math.BigInteger

/**
 * Infinite sequence of **Fibonacci numbers** Fₙ.
 *
 * Defined by:
 *   F(0) = 0, F(1) = 1,
 *   F(n) = F(n−1) + F(n−2)
 *
 * Recurrence coefficients: [1, 1]
 * Initial values: [0, 1]
 *
 * First few terms:
 *   0, 1, 1, 2, 3, 5, 8, 13, ...
 *
 * Properties:
 * - Linear recurrence with constant coefficients.
 * - Appears in nature and mathematics via the golden ratio φ.
 * - Exponential generating function: (eˣ - 1) / (eˣ + 1)
 */
object Fibonacci : Recurrence<BigInteger> by LinearRecurrence.forBigInt(
    initial = listOf(0, 1),
    coeffs = listOf(1, 1)
)

/** Lightweight 64-bit Fibonacci implementation (overflows around 92). */
object Fibonacci64 : Recurrence<Long> by LinearRecurrence.forLong(
    initial = listOf(0, 1),
    coeffs = listOf(1, 1)
)
