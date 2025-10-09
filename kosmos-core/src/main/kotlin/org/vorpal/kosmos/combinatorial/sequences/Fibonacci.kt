package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.LinearRecurrence

/**
 * The Fibonacci sequence, defined by base cases F(0) = 0, F(1) = 1, and then F(n) = F(n-1) + F(n-2).
 */
val Fibonacci = LinearRecurrence.forInt(listOf(0, 1), listOf(1, 1))
