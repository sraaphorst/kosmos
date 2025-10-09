package org.vorpal.kosmos.combinatorial

import org.vorpal.kosmos.memoization.memoize
import java.math.BigInteger
import kotlin.math.pow
import kotlin.math.roundToLong


/* ================================================================
 *  Factorial
 * ================================================================ */

/**
 * Call Factorial(n) to calculate **n!**, which is returned as a `BigInteger`.
 */
object Factorial {
    private val factorial = memoize<Int, BigInteger> { n ->
        when (n) {
            0, 1 -> BigInteger.ONE
            else -> n.toBigInteger() * this(n - 1)
        }
    }

    operator fun invoke(n: Int): BigInteger = factorial(n)
}

/* ================================================================
 *  Binomial Coefficients
 * ================================================================ */

object Binomial {
    private val binomial = memoize<Int, Int, BigInteger> { n, k ->
        when (k) {
            !in 0..n -> BigInteger.ZERO
            0, n           -> BigInteger.ONE
            else           -> this(n - 1, k - 1) + this(n - 1, k)
        }
    }

    operator fun invoke(n: Int, k: Int): BigInteger = binomial(n, k)
}


/* ================================================================
 *  Stirling Numbers of the First Kind (signed)
 * ================================================================ */

/**
 * Represents the **Stirling numbers of the first kind** s(n, k).
 *
 * These count the number of permutations of **n** elements that have exactly
 * **k** disjoint cycles in their cycle decomposition.
 *
 * By convention, s(n, k) may be **signed** or **unsigned**:
 * - The signed Stirling numbers are denoted s(n, k) and alternate in sign.
 * - The unsigned Stirling numbers are denoted |s(n, k)| and are always non-negative.
 *
 * ### Definition
 * s(n, k) = number of permutations of n elements with exactly k cycles.
 *
 * ### Recurrence relation
 * ```
 * s(n, k) = s(n - 1, k - 1) - (n - 1) * s(n - 1, k)
 * ```
 * where:
 * - `s(n - 1, k - 1)` accounts for introducing a new singleton cycle containing element n.
 * - `(n - 1) * s(n - 1, k)` accounts for inserting element n into an existing cycle
 *   of a permutation of n - 1 elements (hence the subtraction for sign consistency).
 *
 * ### Boundary conditions
 * ```
 * s(0, 0) = 1
 * s(n, 0) = 0  for n > 0
 * s(0, k) = 0  for k > 0
 * s(n, n) = 1
 * ```
 *
 * ### Example
 * ```
 * s(4, 2) = 11
 * ```
 * (There are 11 permutations of 4 elements with exactly 2 cycles.)
 *
 * ### Relationship to falling factorials
 * Stirling numbers of the first kind are the coefficients in the expansion:
 * ```
 * x(x - 1)(x - 2)...(x - n + 1) = Σ_{k=0}^{n} s(n, k) * x^k
 * ```
 *
 * ### Sign convention
 * The signed and unsigned forms are related by:
 * ```
 * s(n, k) = (-1)^{n - k} * |s(n, k)|
 * ```
 */
object StirlingFirst : BinaryCombinatorialFunction<BigInteger> {
    private val s = memoize<Int, Int, BigInteger> { n, k ->
        when(k) {
            n -> BigInteger.ONE
            0 -> BigInteger.ZERO
            else -> this(n - 1, k - 1) - (n - 1).toBigInteger() * this(n - 1, k)
        }
    }

    override fun invoke(n: Int, k: Int): BigInteger = s(n, k)
}


/* ================================================================
 *  Stirling Numbers of the Second Kind
 * ================================================================ */

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
object StirlingSecond : BinaryCombinatorialFunction<BigInteger> {
    private val s = memoize<Int, Int, BigInteger> { n, k ->
        when {
            n == k || k == 1 -> BigInteger.ONE
            k == 0 || k > n -> BigInteger.ZERO
            else -> {
                val kFact = Factorial(k).toDouble()
                val sum = (0..k).sumOf { j ->
                    val term = (-1.0).pow((k - j).toDouble()) *
                            Binomial(k, j).toDouble() *
                            j.toDouble().pow(n.toDouble())
                    term
                }
                sum.div(kFact).roundToLong().toBigInteger()
            }
        }
    }

    override fun invoke(n: Int, k: Int): BigInteger = s(n, k)
}


/* ================================================================
 *  Lah Numbers (unsigned)
 * ================================================================ */

/**
 * Represents the **Lah numbers** L(n, k).
 *
 * These count the number of ways to partition a set of **n** elements
 * into **k** non-empty **linearly ordered** subsets (i.e., ordered lists rather than sets).
 *
 * In other words, Lah numbers count the number of ways to arrange n labeled objects
 * into k non-empty ordered blocks, where both the order of blocks and the order
 * of elements within each block matter.
 *
 * ### Definition
 * L(n, k) = number of ways to partition n elements into k ordered subsets.
 *
 * ### Closed form
 * ```
 * L(n, k) = (n! / k!) * (n - 1 choose k - 1)
 * ```
 *
 * ### Recurrence relation
 * ```
 * L(n, k) = L(n - 1, k - 1) + (n + k - 1) * L(n - 1, k)
 * ```
 * where:
 * - `L(n - 1, k - 1)` accounts for starting a new list with element n.
 * - `(n + k - 1) * L(n - 1, k)` accounts for inserting element n into
 *   any position among the existing k lists.
 *
 * ### Boundary conditions
 * ```
 * L(0, 0) = 1
 * L(n, 0) = 0  for n > 0
 * L(0, k) = 0  for k > 0
 * L(n, n) = 1
 * ```
 *
 * ### Example
 * ```
 * L(4, 2) = 36
 * ```
 * meaning there are 36 ways to arrange 4 labeled elements into 2 ordered lists.
 */
object Lah : BinaryCombinatorialFunction<BigInteger> {
    override fun invoke(n: Int, k: Int): BigInteger {
        require(k in 1..n)
        val bin = Binomial(n - 1, k - 1)
        return bin * Factorial(n) / Factorial(k)
    }
}


/* ================================================================
 *  Linear Recurrence Generator
 * ================================================================ */

/**
 * Generic linear recurrence generator over an arbitrary numeric type T.
 * Generates any linear recurrence sequence such as Fibonacci, Lucas, Tribonacci, etc.
 *
 * This form is compatible with a future Ring<T> abstraction, but can also be
 * constructed directly from Int, Long, or BigInteger sequences by providing
 * addition and multiplication lambdas.
 *
 * Example:
 * ```
 * Fibonacci.take(10).toList() // [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]
 * ```
 *
 * TODO: Create a Ring<T>.toLinearRecurrence(initial: List<T>, coeffs: List<T>) which passes in the AbelianGroup's zero,
 * plus, and Monoid's times.
 */
data class LinearRecurrence<T>(
    val initial: List<T>,
    val coeffs: List<T>,
    val zero: T,
    val add: (T, T) -> T,
    val multiply: (T, T) -> T
) : Sequence<T> {

    init {
        require(initial.isNotEmpty()) { "Initial terms cannot be empty." }
        require(initial.size == coeffs.size) {
            "Initial term list and coefficient list must have the same size."
        }
    }

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private val window = ArrayDeque(initial)

        override fun hasNext(): Boolean = true  // Infinite sequence

        override fun next(): T {
            val nextValue = window.first()
            val next = coeffs.zip(window).fold(zero) { acc, (c, v) ->
                add(acc, multiply(c, v))
            }
            window.removeFirst()
            window.addLast(next)
            return nextValue
        }
    }

    companion object {
        fun forInt(initial: List<Int>, coeffs: List<Int>) =
            LinearRecurrence(initial, coeffs, 0, Int::plus, Int::times)

        fun forLong(initial: List<Long>, coeffs: List<Long>) =
            LinearRecurrence(initial, coeffs, 0L, Long::plus, Long::times)

        fun forBigInt(initial: List<BigInteger>, coeffs: List<BigInteger>) =
            LinearRecurrence(initial, coeffs, BigInteger.ZERO, BigInteger::add, BigInteger::multiply)

        fun forBigInt(initial: List<Int>, coeffs: List<Int>) =
            forBigInt(initial.map(Int::toBigInteger), coeffs.map(Int::toBigInteger))

        fun forBigInt(initial: List<Long>, coeffs: List<Long>) =
            forBigInt(initial.map(Long::toBigInteger), coeffs.map(Long::toBigInteger))
    }
}


/**
 * The Lucas sequence, defined by base cases L(0) = 2, L(1) = 1 and then L(n) = L(n-1) + L(n-2).
 */
val Lucas = LinearRecurrence.forInt(listOf(2, 1), listOf(1, 1))

