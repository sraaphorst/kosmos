package org.vorpal.kosmos.combinatorial.arrays

import org.vorpal.kosmos.combinatorial.Binomial
import org.vorpal.kosmos.combinatorial.Factorial
import org.vorpal.kosmos.combinatorial.recurrence.BivariateClosedForm
import org.vorpal.kosmos.combinatorial.recurrence.BivariateRecurrence
import org.vorpal.kosmos.std.bigIntSgn
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents the **Stirling numbers of the second kind** S(n, k).
 *
 * These count the number of ways to partition a set of **n** distinct elements
 * into exactly **k** non-empty, unlabeled subsets.
 *
 * ### Definition
 * S(n, k) = number of ways to divide {1, 2, …, n} into k non-empty subsets.
 *
 * ### Recurrence relation
 * ```
 * S(n, k) = S(n - 1, k - 1) + k * S(n - 1, k)
 * ```
 * where:
 * - `S(n - 1, k - 1)` corresponds to the case where the n-th element forms a singleton subset.
 * - `k * S(n - 1, k)` corresponds to the case where the n-th element joins one of the k existing subsets.
 *
 * ### Boundary conditions
 * ```
 * S(0, 0) = 1
 * S(n, 0) = 0  for n > 0
 * S(0, k) = 0  for k > 0
 * S(n, k) = 0  for k > n
 * S(n, n) = 1
 * ```
 *
 * ### Example
 * ```
 * S(3, 2) = 3
 * ```
 * because {1, 2, 3} can be partitioned into 2 non-empty subsets as:
 * { {1,2}, {3} }, { {1,3}, {2} }, { {2,3}, {1} }.
 *
 * ### Closed form
 * The closed-form inclusion–exclusion expression is:
 * ```
 * S(n, k) = 1/k! * Σ_{j=0}^{k} (-1)^{k-j} * (k choose j) * j^n
 * ```
 */
object StirlingSecond : BivariateRecurrence<BigInteger>, BivariateClosedForm<BigInteger> {
    private val recursiveCache = ConcurrentHashMap<Pair<Int, Int>, BigInteger>()
    private val closedFormCache = ConcurrentHashMap<Pair<Int, Int>, BigInteger>()

    override fun invoke(n: Int, k: Int): BigInteger {
        val key = n to k
        recursiveCache[key]?.let { return it }

        val result = when {
            n == 0 && k == 0  -> BigInteger.ONE
            k == 0 || k > n   -> BigInteger.ZERO
            n == k            -> BigInteger.ONE
            else              -> invoke(n - 1, k - 1) + BigInteger.valueOf(k.toLong()) * invoke(n - 1, k)
        }

        recursiveCache[key] = result
        return result
    }
    override fun closedForm(n: Int, k: Int): BigInteger {
        val key = n to k
        closedFormCache[key]?.let { return it }

        val result = when {
            n == 0 && k == 0 -> BigInteger.ONE
            k == 0 || k > n  -> BigInteger.ZERO
            n == k           -> BigInteger.ONE
            else ->
                (0..k).fold(BigInteger.ZERO) { acc, j ->
                    acc + bigIntSgn(k - j) * Binomial(k, j) * j.toBigInteger().pow(n)
                } / Factorial(k)
            }

        closedFormCache[key] = result
        return result
    }
}
