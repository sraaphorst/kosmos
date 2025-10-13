package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import java.math.BigInteger

/**
 * **Cayley trees** Tₙ — OEIS A000272.
 *
 * Number of labeled trees on n vertices.
 *
 * Closed form:
 * ```
 * Tₙ = n^{n−2},  for n ≥ 1;  T₀ = 1
 * ```
 *
 * Derived from Cayley’s formula (1889) for labeled trees.
 * Used throughout combinatorial species theory, Prüfer codes, and graph enumeration.
 */
object CayleyTrees :
    CachedClosedForm<BigInteger> by CayleyClosedForm

private object CayleyClosedForm : CachedClosedFormImplementation<BigInteger>() {
    override fun closedFormCalculator(n: Int): BigInteger =
        when (n) {
            0, 1 -> BigInteger.ONE
            else -> BigInteger.valueOf(n.toLong()).pow(n - 2)
        }
}
