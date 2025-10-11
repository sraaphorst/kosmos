package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger

/**
 * A cached sequence defined by a **linear recurrence relation**:
 *
 * aₙ = Σᵢ cᵢ * a_{n−i−1}
 *
 * where [coefficients] = [c₀, c₁, …].
 */
open class CachedLinearSequence(
    override val initial: List<BigInteger>,
    override val coefficients: List<BigInteger>
) : CachedRecursiveSequence(), LinearRecurrence {

    init {
        require(initial.isNotEmpty()) { "Initial terms cannot be empty." }
        require(initial.size == coefficients.size) {
            "Initial terms and coefficients must have the same size."
        }
    }

    override fun recursiveCalculator(n: Int): BigInteger {
        if (n < initial.size) return initial[n]
        return coefficients.indices.fold(BigInteger.ZERO) { acc, i ->
            acc + coefficients[i] * invoke(n - i - 1)
        }
    }
}