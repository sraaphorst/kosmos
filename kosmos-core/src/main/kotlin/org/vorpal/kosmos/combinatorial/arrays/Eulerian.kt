package org.vorpal.kosmos.combinatorial.arrays

import org.vorpal.kosmos.combinatorial.Binomial
import org.vorpal.kosmos.combinatorial.recurrence.BivariateClosedForm
import org.vorpal.kosmos.combinatorial.recurrence.BivariateRecurrence
import org.vorpal.kosmos.std.bigIntSgn
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

/**
 * **Eulerian numbers** A(n, k):
 * number of permutations of {1,…,n} with exactly *k* ascents.
 *
 * Recurrence:
 * ```
 * A(0,0)=1
 * A(n,k)=(n−k)·A(n−1,k−1)+(k+1)·A(n−1,k)
 * ```
 *
 * Closed form:
 * ```
 * A(n,k)=Σⱼ₌₀ᵏ(−1)ʲ·C(n+1,j)·(k+1−j)ⁿ
 * ```
 *
 * OEIS A008292
 */
object Eulerian : BivariateRecurrence<BigInteger>, BivariateClosedForm<BigInteger> {

    private val recursiveCache = ConcurrentHashMap<Pair<Int, Int>, BigInteger>()
    private val closedFormCache = ConcurrentHashMap<Pair<Int, Int>, BigInteger>()

    override fun invoke(n: Int, k: Int): BigInteger {
        val key = n to k

        // check first to avoid double recursion
        recursiveCache[key]?.let { return it }

        val result = when {
            n == 0 && k == 0 -> BigInteger.ONE
            k !in 0 until n -> BigInteger.ZERO
            else -> {
                val a = BigInteger.valueOf((n - k).toLong()) * invoke(n - 1, k - 1)
                val b = BigInteger.valueOf((k + 1).toLong()) * invoke(n - 1, k)
                a + b
            }
        }

        recursiveCache[key] = result
        return result
    }

    /** Closed-form computation (memoized). */
    override fun closedForm(n: Int, k: Int): BigInteger {
        val key = n to k
        closedFormCache[key]?.let { return it }

        val result = when {
            n == 0 && k == 0 -> BigInteger.ONE
            k !in 0 until n -> BigInteger.ZERO
            else -> {
                (0..k).fold(BigInteger.ZERO) { acc, j ->
                    val term = Binomial(n + 1, j) *
                            BigInteger.valueOf((k + 1L - j)).pow(n)
                    acc + bigIntSgn(j) * term
                }
            }
        }

        closedFormCache[key] = result
        return result
    }
}