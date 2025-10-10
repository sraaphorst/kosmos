package org.vorpal.kosmos.combinatorial.arrays

import org.vorpal.kosmos.combinatorial.recurrence.BivariateRecurrence
import java.math.BigInteger
import org.vorpal.kosmos.memoization.recursiveMemoize2

/**
 * **Aitken's Array** (aka Bell's Triangle, Peirce Triangle).
 *
 * This array generates the **Bell numbers** — the number of ways to partition
 * a set of *n* distinct elements into non-empty, unlabeled subsets.
 *
 * For example:
 * - B₀ = 1 (the empty set has one partition)
 * - B₁ = 1 ({a})
 * - B₂ = 2 ({a,b}, {a}{b})
 * - B₃ = 5 ({{a,b,c}}, {{a}{b,c}}, {{b}{a,c}}, {{c}{a,b}}, {{a}{b}{c}})
 *
 * Each entry `Bell(n, k)` in the triangle is defined recursively as:
 *
 * ```
 * B(0, 0) = 1
 * B(n, 0) = B(n - 1, n - 1)
 * B(n, k) = B(n, k - 1) + B(n - 1, k - 1)
 * ```
 *
 * where `n ≥ 0` and `0 ≤ k ≤ n`.
 *
 * The triangle is constructed row by row:
 *
 * 1. Each row begins with `B(n, 0) = B(n - 1, n - 1)`.
 * 2. Each subsequent entry is the sum of the entry to its immediate left
 *    and the one up-left in the previous row.
 *
 * The **Bell numbers** themselves appear as the *first element* of each row:
 *
 * ```
 * Bell(n) = B(n, 0)
 * ```
 *
 * ---
 *
 * ### Example
 * The first few rows of the triangle:
 * ```
 * n=0: 1
 * n=1: 1 2
 * n=2: 2 3 5
 * n=3: 5 7 10 15
 * n=4: 15 20 27 37 52
 * ```
 *
 * The leftmost elements (1, 1, 2, 5, 15, 52, …) are the **Bell numbers** — A000110 in the OEIS.
 *
 * ---
 *
 * ### Combinatorial interpretation
 *
 * - `B(n, k)` counts the number of set partitions of a size-`n` set
 *   in which the *(k+1)ᵗʰ* element (if elements are labeled 1..n)
 *   is in a subset that also contains the element `n`.
 * - The Bell numbers `B(n, 0)` count *all* partitions of an `n`-element set.
 *
 * ---
 *
 * See [Bell][org.vorpal.kosmos.combinatorial.sequences.Bell] for the corresponding 1D sequence of Bell numbers.
 *
 * See [StirlingSecond] for the related partition counts S(n, k),
 *      where S(n, k) counts the number of partitions of an n-set into exactly k blocks.
 *
 * OEIS A011971
 */
object Aitken : BivariateRecurrence<BigInteger> {

    private val recursiveCache = recursiveMemoize2<Int, Int, BigInteger> { self, n, k ->
        when {
            n == 0 && k == 0 -> BigInteger.ONE
            k !in 0..n -> BigInteger.ZERO
            k == 0 -> self(n - 1, n - 1)
            else -> self(n, k - 1) + self(n - 1, k - 1)
        }
    }

    override fun invoke(n: Int, k: Int): BigInteger = recursiveCache(n, k)
}