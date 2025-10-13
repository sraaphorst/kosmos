package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.combinatorics.arrays.Delannoy
import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import java.math.BigInteger

/**
 * **Central Delannoy numbers** Dₙ = D(n, n).
 *
 * Counts the number of lattice paths from `(0, 0)` to `(n, n)` using
 * only the steps:
 *
 * - East `(1, 0)`
 * - North `(0, 1)`
 * - Diagonal `(1, 1)`
 *
 * These correspond to the diagonal entries of the [Delannoy] array,
 * i.e. `Dₙ = D(n, n)`.
 *
 * ---
 *
 * ### Recurrence
 * ```
 * D₀ = 1
 * Dₙ = 3·Dₙ₋₁ + 2·Σₖ₌₀^{n−2} Dₖ
 * ```
 *
 * This formulation expresses each term as a weighted sum of all previous
 * central Delannoy numbers.
 *
 * ---
 *
 * ### Closed form
 * ```
 * Dₙ = Σₖ₌₀ⁿ C(n, k)² · 2ᵏ
 * ```
 *
 * where `C(a, b)` denotes the binomial coefficient.
 *
 * Equivalently:
 * ```
 * Dₙ = D(n, n)
 * ```
 * as computed by the full [Delannoy] array.
 *
 * ---
 *
 * ### First values
 * ```
 * n : 0, 1, 2, 3, 4, 5, 6, 7, 8
 * Dₙ : 1, 3, 13, 63, 321, 1683, 8989, 48639, 265729
 * ```
 *
 * OEIS [A001850](https://oeis.org/A001850)
 *
 * ---
 *
 * ### Asymptotic growth
 * \[
 * Dₙ ∼ c · (3 + 2√2)ⁿ / √n,
 * \quad c = (\sqrt2 + 1)/(4√π)
 * \]
 *
 * ---
 *
 * ### Related
 * - [Delannoy] — general 2D array D(m, n)
 * - [org.vorpal.kosmos.combinatorics.arrays.Pascal] triangle — binomial coefficients
 * - [org.vorpal.kosmos.combinatorics.arrays.Trinomial] triangle — 3-term analog
 * - [Catalan]
 * - [Motzkin]
 * - [Schroder]
 *
 * ---
 *
 * ### References
 * - Riordan, *Combinatorial Identities* (1968), § 3.7
 * - Comtet, *Advanced Combinatorics* (1974), § 3.6
 * - OEIS [A001850](https://oeis.org/A001850)
 */
object CentralDelannoy :
    CachedRecurrence<BigInteger> by CentralDelannoyRecurrence,
    CachedClosedForm<BigInteger> by CentralDelannoyClosedForm

private object CentralDelannoyRecurrence : CachedRecurrenceImplementation<BigInteger>() {
    private val THREE = BigInteger.valueOf(3L)
    override fun recursiveCalculator(n: Int): BigInteger = when {
        n < 0 -> BigInteger.ZERO
        n == 0 -> BigInteger.ONE
        else -> THREE * this(n - 1) * BigInteger.TWO *
                (0..(n - 2)).fold(BigInteger.ZERO) { acc, k ->
                    acc + this(k)
                }
    }
}

private object CentralDelannoyClosedForm : CachedClosedFormImplementation<BigInteger>() {
    override fun closedFormCalculator(n: Int): BigInteger =
        Delannoy(n, n)
}
