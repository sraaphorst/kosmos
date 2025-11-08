package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.frameworks.sequence.CachedLinearRecurrenceImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
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
object Tribonacci :
    CachedRecurrence<BigInteger> by TribonacciRecurrence

private object TribonacciRecurrence: CachedLinearRecurrenceImplementation<BigInteger, Int>(
    initialValues = listOf(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE),
    selectors = listOf(-1, -2, -3),
    coefficients = listOf(1, 1, 1),
    constantTerm = BigInteger.ZERO,
    multiply = Action { s, t -> s.toBigInteger() * t },
    add = BinOp(BigInteger::add)
)
