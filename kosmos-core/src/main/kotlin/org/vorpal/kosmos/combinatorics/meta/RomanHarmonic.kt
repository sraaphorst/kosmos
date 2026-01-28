package org.vorpal.kosmos.combinatorics.meta

import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.math.bigIntSgn

/**
 * **Roman harmonic numbers** Cₙ^(k):
 * generalization of harmonic numbers introduced by Sandro Roman (1984).
 *
 * Defined by:
 * ```
 * Cₙ^(k) = Σ_{j=1..n} (-1)^(j-1) * binom(n, j) / j^k
 * ```
 *
 * Properties:
 * - Cₙ^(1) = Hₙ (ordinary harmonic numbers)
 * - limₙ→∞ Cₙ^(k) = ζ(k)
 * - Appears in expansions of polygamma and Hurwitz zeta functions
 *
 * They are not naturally recursive, and are computed directly by definition.
 */
object RomanHarmonic {
    operator fun invoke(n: Int, k: Int): Rational =
        (1..n).fold(Rational.ZERO) { acc, j ->
            acc + Rational.of(bigIntSgn(j - 1) * Binomial(n, j), j.toBigInteger().pow(k))
        }
}
