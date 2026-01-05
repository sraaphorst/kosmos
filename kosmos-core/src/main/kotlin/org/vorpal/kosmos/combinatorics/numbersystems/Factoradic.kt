package org.vorpal.kosmos.combinatorics.numbersystems

import org.vorpal.kosmos.combinatorics.Factorial
import org.vorpal.kosmos.core.render.Printable
import java.math.BigInteger

/**
 * Factoradic (factorial number system) encoding and decoding.
 *
 * The factoradic system is a mixed-radix numeral system where position [i] (0-indexed)
 * has base [i]! and can contain digits from 0 to [i]. This representation is particularly
 * useful in combinatorics for:
 * - Ranking and unranking permutations (Lehmer codes)
 * - Enumerating combinatorial objects
 * - Efficient permutation generation
 *
 * ## Representation Convention
 *
 * This implementation uses a **least-significant-digit-first** ordering where:
 * - Position 0 contains the coefficient of 0! (always 0 in canonical form)
 * - Position 1 contains the coefficient of 1! (digit ∈ {0, 1})
 * - Position 2 contains the coefficient of 2! (digit ∈ {0, 1, 2})
 * - Position [i] contains the coefficient of [i]! (digit ∈ {0, 1, ..., i})
 *
 * ### Example
 * ```
 * 5 in factoradic = [0, 1, 2]
 * Meaning: 0×0! + 1×1! + 2×2! = 0 + 1 + 4 = 5
 * ```
 *
 * ## Canonical Form
 *
 * - Zero is represented as an empty list `[]`
 * - Non-zero numbers must not have trailing zeros (the most significant digit must be non-zero)
 * - This ensures a unique representation for each non-negative integer
 *
 * @see <a href="https://en.wikipedia.org/wiki/Factorial_number_system">Factorial number system</a>
 */
@ConsistentCopyVisibility
data class Factoradic internal constructor(val digits: List<Int>) {
    init {
        require(isCanonical(digits)) { "Illegal factoradic representation: $digits" }
    }

    /**
     * Decodes the factoradic representation into its integer value.
     *
     * Computes the weighted sum of digits where position [i] contributes `digit[i] × i!`.
     * The input must be a valid factoradic representation in canonical form.
     *
     * ### Validation Rules
     * 1. Each digit at position [i] must satisfy: `0 ≤ digit ≤ i`
     * 2. Non-empty representations must not end with zero (no trailing zeros)
     * 3. Empty list represents zero
     *
     * ### Examples
     * ```
     * decode([]) = 0
     * decode([1]) = 1
     * decode([0, 1, 2]) = 0×0! + 1×1! + 2×2! = 5
     * decode([0, 0, 2, 1]) = 0×0! + 0×1! + 2×2! + 1×3! = 10
     * decode([0, 1, 0, 1, 4, 3]) = 0×0! + 1×1! + 0×2! + 1×3! + 4×4! + 3×5! = 463
     * ```
     *
     * ### Time Complexity
     * O(n) where n is the length of the input list, assuming memoized factorial lookups.
     *
     * @return The integer value represented by the factoradic encoding
     * @throws IllegalArgumentException if [input] violates the digit range constraint (digit > position)
     * @throws IllegalArgumentException if [input] has trailing zeros (non-canonical form)
     */
    fun decode(): BigInteger =
        digits.withIndex().fold(BigInteger.ZERO) { sum, (idx, digit) ->
            sum + digit.toBigInteger() * Factorial(idx)
        }

    val size: Int = digits.size

    override fun toString(): String =
        if (digits.isEmpty()) "0"
        else digits.asReversed().joinToString(separator = ",")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Factoradic) return false

        if (digits != other.digits) return false

        return true
    }

    override fun hashCode(): Int {
        return digits.hashCode()
    }

    companion object {
        /**
         * The representation of zero as a factoradic is simply an empty list.
         */
        val ZERO = Factoradic(listOf())

        /**
         * One for convenience.
         */
        val ONE = Factoradic(listOf(0, 1))

        /**
         * Encodes a non-negative integer into its factoradic representation.
         *
         * The encoding process repeatedly extracts digits by computing remainders modulo
         * increasing factorial values, producing a canonical representation with no trailing zeros.
         *
         * ### Algorithm
         * Starting with k = 1! = 1, the algorithm:
         * 1. Computes digit = (n mod k) where k represents the current factorial base
         * 2. Divides n by k to get the remainder for the next position
         * 3. Increments k to the next factorial value
         * 4. Repeats until n = 0
         *
         * ### Examples
         * ```
         * encode(0) = []
         * encode(1) = [1]
         * encode(5) = [0, 1, 2]
         * encode(10) = [0, 0, 2, 1]
         * encode(463) = [1, 1, 0, 3, 3, 3]
         * ```
         *
         * ### Time Complexity
         * O(d) where d is the number of digits in the result, which is O(log n / log log n).
         * Note: Factorial lookups are assumed to be O(1) via memoization.
         *
         * @param x The non-negative integer to encode
         * @return A list of integers representing the factoradic digits in least-significant-first order.
         *         Returns an empty list for x = 0.
         * @throws IllegalArgumentException if [x] is negative
         */
        fun encode(x: BigInteger): Factoradic {
            require(x >= BigInteger.ZERO) {
                "Factoradic representations of negative numbers are not allowed: $x"
            }
            if (x == BigInteger.ZERO) return ZERO

            tailrec fun aux(remainder: BigInteger, k: BigInteger, acc: MutableList<Int>): List<Int> {
                if (remainder == BigInteger.ZERO) return acc.toList()
                val digit = remainder.mod(k).intValueExact()
                acc.add(digit)
                return aux(remainder.divide(k), k + BigInteger.ONE, acc)
            }

            val digits = aux(x, BigInteger.ONE, mutableListOf())
            return Factoradic(digits)
        }

        /**
         * Create a [Factoradic] using digits with the ith place representing the ith digit of the factoradic.
         *
         * For example, a list [0, 1, 0 2] would represent:
         *
         *    0 * 0! + 1 * 1! + 0 * 2! + 2 * 3! = 7
         */
        fun fromDigitsLs(digits: List<Int>): Factoradic =
            Factoradic(digits)

        /**
         * Create a [Factoradic] using digits with the representation from left to right, i.e. with the first
         * digit being the multiplier by `(n-1)!`.
         *
         * For example, a list [2, 0, 1 0] would represent:
         *
         *    2 * 3! + 0 * 2! + 1 * 1! + 0 * 0! = 7
         */
        fun fromDigitsMs(digits: List<Int>): Factoradic =
            fromDigitsLs(digits.asReversed())

        private fun isCanonical(digits: List<Int>): Boolean =
            (digits.isEmpty() || digits.last() != 0) &&
                digits.withIndex().all { (idx, value) -> value in 0..idx }
    }
}
