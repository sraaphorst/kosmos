package org.vorpal.kosmos.combinatorics.sequences

import org.vorpal.kosmos.frameworks.sequence.CachedClosedForm
import org.vorpal.kosmos.frameworks.sequence.CachedClosedFormImplementation
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrence
import org.vorpal.kosmos.frameworks.sequence.CachedRecurrenceImplementation
import org.vorpal.kosmos.numerical.quasirandom.HaltonSequence
import org.vorpal.kosmos.core.Rational
import org.vorpal.kosmos.core.toRational

/**
 * **Van der Corput sequence** in a fixed integer base.
 *
 * This class implements the classic one–dimensional low–discrepancy sequence
 * introduced by J. G. van der Corput. For a fixed base `b ≥ 2`, the sequence
 * is defined on non-negative integers `n` by the **radical inverse function**
 * in base `b`:
 *
 * ```
 * n = a₀ + a₁ b + a₂ b² + ⋯ + aₘ bᵐ        with digits aᵢ ∈ {0,…,b−1}
 *
 * φ_b(n) = a₀ b⁻¹ + a₁ b⁻² + a₂ b⁻³ + ⋯ + aₘ b⁻(m+1)
 * ```
 *
 * In other words, write `n` in base `b`, then reverse the digit string and
 * place it after the radix point. For example, in base `b = 4`:
 *
 * - `n = 0`  → digits `0`     → `φ₄(0)  = 0`
 * - `n = 1`  → digits `1`     → `φ₄(1)  = 1/4`
 * - `n = 2`  → digits `2`     → `φ₄(2)  = 1/2`
 * - `n = 3`  → digits `3`     → `φ₄(3)  = 3/4`
 * - `n = 4`  → digits `10₄`   → `φ₄(4)  = 1/16`
 * - `n = 5`  → digits `11₄`   → `φ₄(5)  = 5/16`
 * - `n = 6`  → digits `12₄`   → `φ₄(6)  = 9/16`
 * - `n = 7`  → digits `13₄`   → `φ₄(7)  = 13/16`
 *
 * Kosmos represents each term **exactly** as a [Rational], rather than as a
 * floating-point approximation. For each `n`, `φ_b(n)` is a rational in `[0,1]`
 * whose denominator is a power of `base`.
 *
 * ### Recurrence
 *
 * The Van der Corput sequence satisfies a very simple digit recurrence. If we
 * write
 *
 * ```
 * n = bq + a,   with 0 ≤ a < b
 * ```
 *
 * then the radical inverse obeys:
 *
 * ```
 * φ_b(0)    = 0
 * φ_b(n)    = (a + φ_b(q)) / b   where n = bq + a
 * ```
 *
 * This implementation’s [recursiveTerm] uses exactly this recurrence:
 *
 * - For `n = 0`, it returns `Rational.ZERO`.
 * - For `n > 0`, it:
 *   1. extracts the least significant digit `a = n % base`,
 *   2. forms the quotient `q = n / base`,
 *   3. recursively combines `a` and `φ_b(q)` as `(a + φ_b(q)) / base`.
 *
 * The recursion is memoized via [CachedRecurrenceImplementation], so repeated
 * queries are efficient even though the definition is given recursively.
 *
 * ### Closed form
 *
 * The [closedForm] implementation computes the radical inverse by iteratively
 * peeling off base-`b` digits of `n` and accumulating their contributions:
 *
 * ```
 * n₀ = n
 * k  = 1
 * x  = 0
 * while nₖ₋₁ > 0:
 *     digit     = nₖ₋₁ mod b
 *     x        += digit / bᵏ
 *     nₖ        = nₖ₋₁ / b
 *     k        += 1
 * ```
 *
 * This is encoded as a tail-recursive helper that:
 *
 * - starts with `denom = b` (so the first digit uses weight `1 / b`),
 * - multiplies `denom` by `b` each step,
 * - adds `digit / denom` to the accumulating [Rational].
 *
 * The closed form and recurrence agree for all `n ≥ 0`; both are cached
 * separately via [CachedClosedFormImplementation] and [CachedRecurrenceImplementation].
 *
 * ### Usage
 *
 * The typical usage pattern is:
 *
 * ```kotlin
 * val v4 = VanDerCorput(base = 4)
 *
 * // Exact rational values
 * val x0: Rational = v4.closedForm(0)     // 0
 * val x1: Rational = v4.closedForm(1)     // 1/4
 * val x2: Rational = v4.recursiveTerm(2)  // 1/2
 *
 * // Convert to Real if needed
 * val x2Real = x2.toReal()            // 0.5
 * ```
 *
 * In higher-level numerical code (e.g. quasi-Monte Carlo integration) you’ll
 * typically convert terms to `Real` or to vector types; in combinatorics,
 * the exact [Rational] representation is often preferable.
 *
 * ### Low–discrepancy properties
 *
 * The sequence `{ φ_b(n) }` has **low discrepancy** on `[0,1]`, meaning that
 * its empirical distribution converges to the uniform distribution in a
 * much more uniform/structured way than pseudorandom sequences. In practice,
 * this makes it useful as a building block for:
 *
 * - Quasi-Monte Carlo integration,
 * - Stratified sampling,
 * - Sampling patterns in graphics / numerical experiments.
 *
 * In Kosmos, [VanDerCorput] is a core, exact, one–dimensional primitive that
 * higher-level quasi-random constructions (such as [HaltonSequence] in the
 * numerical layer) can build upon.
 *
 * @property base the radix `b ≥ 2` used for the radical inverse map.
 * Every term of the sequence is a rational number in `[0,1]` whose exact
 * denominator is a power of `base`.
 *
 * @see HaltonSequence for a higher-dimensional construction built from multiple
 * bases and multiple Van der Corput sequences.
 */
class VanDerCorput(val base: Int)
    : CachedRecurrence<Rational> by VanDerCorputRecurrence(base),
    CachedClosedForm<Rational> by VanDerCorputClosedForm(base) {
    init {
        require(base >= 2) { "Base must be at least 2, but was $base." }
    }
}

private class VanDerCorputRecurrence(
    private val base: Int
) : CachedRecurrenceImplementation<Rational>() {
    private val baseRational = base.toRational()

    override fun recursiveCalculator(n: Int): Rational {
        require(n >= 0) { "Index n must be non-negative, but was $n." }
        return when (n) {
            0 -> Rational.ZERO
            else -> {
                val a = n % base
                val q = n / base
                (a.toRational() + this(q)) / baseRational
            }
        }
    }
}

private class VanDerCorputClosedForm(
    private val base: Int
) : CachedClosedFormImplementation<Rational>() {
    private val baseRational = base.toRational()

    override fun closedFormCalculator(n: Int): Rational {
        require(n >= 0) { "Index n must be non-negative, but was $n." }

        tailrec fun aux(
            x: Rational = Rational.ZERO,
            denom: Rational = baseRational,
            k: Int = n
        ): Rational = when (k) {
            0 -> x
            else ->
                aux(
                    x + (k % base).toRational() / denom,
                    denom * baseRational,
                    k / base
                )
        }
        return aux()
    }
}
