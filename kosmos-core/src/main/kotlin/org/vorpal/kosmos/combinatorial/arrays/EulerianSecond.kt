package org.vorpal.kosmos.combinatorial.arrays

import org.vorpal.kosmos.combinatorial.Binomial
import org.vorpal.kosmos.combinatorial.recurrence.BivariateClosedForm
import org.vorpal.kosmos.combinatorial.recurrence.BivariateRecurrence
import org.vorpal.kosmos.std.bigIntSgn
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

/**
 * **Eulerian numbers of the second kind** ⟨n,k⟩:
 * number of permutations of n elements with *k* ascending runs.
 *
 * Recurrence:
 * ```
 * ⟨0,0⟩=1
 * ⟨n,k⟩=(k+1)·⟨n−1,k⟩+(n−k)·⟨n−1,k−1⟩
 * ```
 *
 * OEIS A008517
 */
object EulerianSecond : BivariateRecurrence<BigInteger>, BivariateClosedForm<BigInteger> {

    private val recursiveCache = ConcurrentHashMap<Pair<Int, Int>, BigInteger>()
    private val closedFormCache = ConcurrentHashMap<Pair<Int, Int>, BigInteger>()

    override fun invoke(n: Int, k: Int): BigInteger {
        val key = n to k
        recursiveCache[key]?.let { return it }

        val result = when {
                n == 0 && k == 0 -> BigInteger.ONE
                k !in 0 until n -> BigInteger.ZERO
                else -> {
                    val a = BigInteger.valueOf((k + 1L)) * invoke(n - 1, k)
                    val b = BigInteger.valueOf((2L * n - k - 1L)) * invoke(n - 1, k - 1)
                    a + b
                }
            }

        recursiveCache[key] = result
        return result
    }

    override fun closedForm(n: Int, k: Int): BigInteger {
        val key = n to k
        closedFormCache[key]?.let { return it }

        val result = when {
            n == 0 && k == 0 -> BigInteger.ONE
            k !in 0 until n -> BigInteger.ZERO
            else -> {
                (0..k).fold(BigInteger.ZERO) { acc, j ->
                    acc + bigIntSgn(j) * Binomial(2 * n + 1, j) * BigInteger.valueOf((k + 1L - j)).pow(n)
                }
                var sum = BigInteger.ZERO
                val upper = n - k - 1
                for (r in 0..upper) {
                    val sign = if (((n + r) and 1) == 0) BigInteger.ONE else BigInteger.valueOf(-1)
                    val bin  = Binomial(2 * n + 1, r)                       // C(2n+1, r)
                    val s    = StirlingFirst(2 * n - k - r, n - k - r)      // signed s(·,·)
                    sum += sign * bin * s
                }
                sum
            }
        }

        closedFormCache[key] = result

        // We want positive results since StirlingFirst is signed which leads to sign flipping.
        return result.abs()
    }
}