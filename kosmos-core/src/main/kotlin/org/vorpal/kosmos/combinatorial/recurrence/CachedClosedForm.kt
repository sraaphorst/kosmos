package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

/**
 * Mixin interface providing cached closed-form evaluation.
 *
 * Can be combined with [CachedLinearSequence] or [CachedNonlinearSequence]
 * to allow both recurrence-based and closed-form computations with caching.
 *
 * Example:
 * ```
 * object Fibonacci :
 *     CachedLinearSequence(
 *         initial = listOf(BigInteger.ZERO, BigInteger.ONE),
 *         coefficients = listOf(BigInteger.ONE, BigInteger.ONE)
 *     ),
 *     CachedClosedForm {
 *
 *     override fun closedFormCalculator(n: Int): BigInteger {
 *         val sqrt5 = Math.sqrt(5.0)
 *         val phi = (1 + sqrt5) / 2
 *         val psi = (1 - sqrt5) / 2
 *         val value = (Math.pow(phi, n.toDouble()) - Math.pow(psi, n.toDouble())) / sqrt5
 *         return BigInteger.valueOf(value.roundToLong())
 *     }
 * }
 * ```
 */
interface CachedClosedForm : ClosedForm {

    /** Internal cache for closed-form evaluations. */
    val closedFormCache: MutableMap<Int, BigInteger>
        get() = _closedFormCache

    companion object {
        private val _closedFormCache = ConcurrentHashMap<Int, BigInteger>()
    }

    /** Compute aₙ using the closed-form expression. */
    fun closedFormCalculator(n: Int): BigInteger

    /** Return the cached closed-form result if available. */
    override fun closedForm(n: Int): BigInteger =
        closedFormCache.getOrPut(n) { closedFormCalculator(n) }
}