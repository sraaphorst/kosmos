package org.vorpal.kosmos.combinatorics.meta

import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.std.Rational
import java.util.concurrent.ConcurrentHashMap

/**
 * **Bernoulli polynomials** Bₙ(x):
 * generalize Bernoulli numbers by introducing a variable x.
 *
 * Generating function:
 * ```
 * (t eˣᵗ) / (eᵗ − 1) = Σₙ₌₀^∞ Bₙ(x) tⁿ / n!
 * ```
 *
 * Recurrence (explicit formula):
 * ```
 * Bₙ(x) = Σₖ₌₀ⁿ binom(n, k) · Bₖ⁻ · xⁿ⁻ᵏ
 * ```
 *
 * Properties:
 * - Bₙ(0) = Bₙ⁻  (classical Bernoulli numbers)
 * - Bₙ(1) = Bₙ⁺  (alternate convention)
 * - Bₙ′(x) = n·Bₙ₋₁(x)
 * - Bₙ(x + 1) − Bₙ(x) = n·xⁿ⁻¹
 *
 * OEIS:
 * - A099673 (triangular array of coefficients)
 * - A027641 (Bₙ⁻ at x=0)
 * - A164555 (Bₙ⁺ at x=1)
 *
 * Right now, we treat the analytic family B_n(x) as a "polynomial" before formally defining polynomial rings.
 * That is because these are not "true" polynomials: they are evaluators.
 * 	- It returns the value of B_n(x) at a given x;
 * 	- It doesn’t store or represent the symbolic coefficients;
 * 	- You can’t do algebraic operations like addition, multiplication, or differentiation on the polynomials themselves.
 * 	Thus, technically, it's not a polynomial in the algebraic sense: it's a function parameterized by n.
 *
 * Once we define a proper Polynomial<C> type (say over Rational), we can redefine BernoulliPolynomial as:
 * object BernoulliPolynomialRing :
 *     CachedRecurrence<Polynomial<Rational>> by ...
 *
 * and then the Faulhaber formula becomes:
 *
 * S_p(n) = \frac{B_{p+1}(x+1) - B_{p+1}(x)}{p+1}
 * as an identity of polynomials, not just numbers.
 */
object BernoulliPolynomial {
    private val cache = ConcurrentHashMap<Pair<Int, Rational>, Rational>()

    operator fun invoke(n: Int, x: Rational): Rational =
        cache.getOrPut(n to x) {
            when (n) {
                0 -> Rational.ONE
                1 -> x - Rational.of(1, 2)
                else -> (0..n).fold(Rational.ZERO) { acc, k ->
                    acc + Rational.of(Binomial(n, k)) *
                            BernoulliMinus(k) *
                            x.pow(n - k)
                }
            }
        }

    /** Return the polynomial evaluated at x = 0 (classical Bernoulli number). */
    fun atZero(n: Int): Rational = this(n, Rational.ZERO)

    /** Return the polynomial evaluated at x = 1 (alternate Bernoulli number). */
    fun atOne(n: Int): Rational = this(n, Rational.ONE)

    fun clear() = cache.clear()
}
