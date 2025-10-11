package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger

/**
 * A cached sequence defined by a **nonlinear recurrence relation**:
 *
 * The next term aₙ is computed by applying [next] to the list of previous terms.
 */
open class CachedNonlinearSequence(
    override val initial: List<BigInteger>,
    override val next: (List<BigInteger>) -> BigInteger
) : CachedRecursiveSequence(), NonlinearRecurrence {

    init {
        require(initial.isNotEmpty()) { "Initial terms cannot be empty." }
    }

    override fun recursiveCalculator(n: Int): BigInteger {
        if (n < initial.size) return initial[n]
        val prefix = (0 until n).map(::invoke)
        return next(prefix)
    }
}
