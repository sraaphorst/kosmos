package org.vorpal.kosmos.core.render

import org.vorpal.kosmos.core.Eq

/**
 * Utilities for printing **linear combinations** of basis elements.
 *
 * This module is meant to be the common “final step” renderer for many algebraic types that are
 * naturally expressed as:
 *
 *     Σᵢ cᵢ · eᵢ
 *
 * where the coefficients `cᵢ` live in some scalar type `A` (e.g. `Real`, `Rational`, `BigInteger`)
 * and the basis elements `eᵢ` are represented here purely by **labels** (strings).
 *
 * ### Formatting rules (high-level)
 * Given a list of labeled coefficients `(label, coeff)`:
 *
 * - Coefficients equal to `zero` (according to [Eq]) are omitted.
 * - The first printed term is written without a leading `" + "`; a leading `"-"` is allowed.
 * - Subsequent terms are joined with `" + "` or `" - "` depending on sign.
 * - For non-scalar basis terms (`label != ""`), a coefficient equal to `one` is omitted:
 *   e.g. `1·I` prints as `I`, `-1·I` prints as `-I`.
 * - The scalar term is represented using the empty label `""`.
 *
 * This behavior is intentionally shared across:
 * - hypercomplex numbers (complex/quaternion/octonion and friends);
 * - sparse polynomial-like structures;
 * - ad-hoc debug output of linear combinations.
 *
 * ### Cayley–Dickson tower note
 * For Cayley–Dickson constructions, an element at dimension `2^n` is built from a pair of elements
 * at dimension `2^(n-1)`. In practice, printing reduces to:
 *
 * 1. Decompose the value into an ordered list of scalar coefficients `Z -> List<A>`.
 * 2. Zip those coefficients with basis labels `List<String>` of the same length.
 * 3. Render via [linearPrintable].
 *
 * See [basisPrintable] for the main ergonomic entry point.
 */
object LinearCombinationPrintable {

    /**
     * Minimal signed operations required for canonical “±” printing.
     *
     * `SignedOps` does **not** require a total order; it only needs:
     * - [isNeg] to decide whether a coefficient should be printed with `+` or `-`,
     * - [abs] to print magnitudes after the sign has been chosen.
     *
     * Examples:
     * - `Real` can implement this with `x < 0` and `abs(x)`.
     * - `Rational` can implement this with `signum < 0` and `abs()`.
     * - `BigInteger` can implement this with comparison to `0` and `abs()`.
     *
     * Types like `Complex` generally do *not* have a canonical sign, so you print them by first
     * decomposing into signed scalar coefficients (e.g. `(re, im)` in `Real`) and using `SignedOps<Real>`.
     */
    interface SignedOps<A : Any> {
        fun isNeg(x: A): Boolean
        fun abs(x: A): A
    }

    /**
     * Render a linear combination into a string.
     *
     * This is the core formatter used by [basisPrintable] and [linearNPrintable].
     * You can also call it directly as a utility when you already have labeled coefficients.
     *
     * @param terms list of `(label, coefficient)` pairs. The empty label `""` denotes the scalar term.
     * @param signed sign/magnitude operations for coefficients.
     * @param zero additive identity in `A`; coefficients equal to `zero` (via [eqA]) are omitted.
     * @param one multiplicative identity in `A`; non-scalar terms with coefficient `one` omit the magnitude.
     * @param prA printer for coefficients.
     * @param eqA equality used to detect `zero` and `one` cases (defaults to structural equality).
     */
    fun <A : Any> linearPrintable(
        terms: List<Pair<String, A>>,
        signed: SignedOps<A>,
        zero: A,
        one: A,
        prA: Printable<A> = Printable.default(),
        eqA: Eq<A> = Eq.default()
    ): String {
        val nonzero = terms.filter { (_, coeff) -> !eqA(coeff, zero) }
        if (nonzero.isEmpty()) return prA(zero).ifEmpty { "0" }

        fun coeffPrefix(coeff: A): Pair<String, A> {
            val neg = signed.isNeg(coeff)
            val mag = signed.abs(coeff)
            val sign = if (neg) "-" else "+"
            return sign to mag
        }

        val sb = StringBuilder()
        var first = true

        nonzero.forEach { (basis, coeff) ->
            val (sign, mag) = coeffPrefix(coeff)

            val magStr = when {
                basis.isEmpty() -> prA(mag)          // scalar term keeps magnitude
                eqA(mag, one) -> ""                  // omit 1 for basis terms
                else -> prA(mag)
            }

            val termStr = when {
                basis.isEmpty() -> magStr
                magStr.isEmpty() -> basis
                else -> magStr + basis
            }

            if (first) {
                if (sign == "-") sb.append("-")
                sb.append(termStr)
                first = false
            } else {
                sb.append(" ")
                sb.append(sign)
                sb.append(" ")
                sb.append(termStr)
            }
        }

        return sb.toString()
    }

    /**
     * Build a [Printable] for a **fixed ordered basis**.
     *
     * This is the main ergonomic entry point for hypercomplex numbers and any structure where:
     * - there is a canonical ordered basis,
     * - decomposition naturally yields an ordered coefficient list.
     *
     * Example (complex-like):
     * ```kotlin
     * basisPrintable(
     *   labels = listOf("", "I"),
     *   decompose = { z -> listOf(z.re, z.im) },
     *   ...
     * )
     * ```
     *
     * @param labels ordered basis labels. Use `""` for the scalar component.
     * @param decompose returns coefficients in the same order as [labels].
     *
     * @throws IllegalArgumentException if the coefficient list length differs from [labels] length.
     */
    fun <A : Any, Z : Any> basisPrintable(
        labels: List<String>,
        decompose: (Z) -> List<A>,
        signed: SignedOps<A>,
        zero: A,
        one: A,
        prA: Printable<A> = Printable.default(),
        eqA: Eq<A> = Eq.default()
    ): Printable<Z> = Printable { z ->
        val coeffs = decompose(z)
        require(coeffs.size == labels.size) {
            "expected ${labels.size} coefficients, got ${coeffs.size}"
        }

        linearPrintable(
            terms = labels.zip(coeffs),
            signed = signed,
            zero = zero,
            one = one,
            prA = prA,
            eqA = eqA
        )
    }

    /**
     * Build a [Printable] from **explicit labeled terms**.
     *
     * This is useful when the underlying value `Z` is naturally represented as a sparse set of terms,
     * e.g.:
     * - sparse polynomials / monomials (`Map<Exponent, Coeff>`),
     * - graded sums,
     * - dynamically chosen bases / labels,
     * - debug printing where labels are computed on the fly.
     *
     * In contrast to [basisPrintable], this function does not enforce a fixed basis ordering;
     * the caller is responsible for the ordering and labeling of terms.
     *
     * Tip: if you want stable output, sort the produced list before returning it.
     */
    fun <A : Any, Z : Any> linearNPrintable(
        terms: (Z) -> List<Pair<String, A>>,
        signed: SignedOps<A>,
        zero: A,
        one: A,
        prA: Printable<A> = Printable.default(),
        eqA: Eq<A> = Eq.default()
    ): Printable<Z> =
        Printable { z ->
            linearPrintable(
                terms = terms(z),
                signed = signed,
                zero = zero,
                one = one,
                prA = prA,
                eqA = eqA
            )
        }

    /**
     * Deterministically sort labeled terms for printing.
     *
     * Default ordering:
     * 1) scalar term first (label == "")
     * 2) then lexicographic by label
     *
     * If you want a different order (e.g., graded lex for polynomials), pass your own comparator.
     *
     * Example usage:
     * ```kotlin
     * LinearCombinationPrintable.linearNPrintable(
     *     terms = { z -> buildTerms(z).sortedTerms() },
     *     signed = ...,
     *     zero = ...,
     *     one = ...,
     *     prA = ...,
     *     eqA = ...
     * )
     * ```
     */
    fun <A : Any> List<Pair<String, A>>.sortedTerms(
        comparator: Comparator<Pair<String, A>> = compareBy<Pair<String, A>>(
            { (label, _) -> label.isNotEmpty() },  // false (scalar) first
            { (label, _) -> label }               // then by label
        )
    ): List<Pair<String, A>> =
        this.sortedWith(comparator)
}
