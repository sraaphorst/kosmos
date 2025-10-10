package org.vorpal.kosmos.combinatorial.arrays

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigInteger

/**
 * Unit tests for the [Eulerian] numbers A(n, k).
 *
 * Eulerian numbers count the number of permutations of {1..n}
 * having exactly k ascents (or equivalently, k descents depending on convention).
 *
 * This suite checks:
 *  - Base cases A(0, 0) = 1, A(n, 0) = A(n, n-1) = 1
 *  - Known triangle values from OEIS A173018
 *  - Summation identity Σₖ A(n, k) = n!
 */
class EulerianSpec : StringSpec({

    "Eulerian base cases hold" {
        Eulerian(0, 0) shouldBe BigInteger.ONE
        Eulerian(1, 0) shouldBe BigInteger.ONE
        Eulerian(2, 0) shouldBe BigInteger.ONE
        Eulerian(2, 1) shouldBe BigInteger.ONE
    }

    "Eulerian triangle matches first known rows (n=0..9)" {
        val expectedRows = listOf(
            listOf(1),
            listOf(1),
            listOf(1, 1),
            listOf(1, 4, 1),
            listOf(1, 11, 11, 1),
            listOf(1, 26, 66, 26, 1),
            listOf(1, 57, 302, 302, 57, 1),
            listOf(1, 120, 1191, 2416, 1191, 120, 1),
            listOf(1, 247, 4293, 15619, 15619, 4293, 247, 1),
            listOf(1, 502, 14608, 88234, 156190, 88234, 14608, 502, 1)
        ).map { row -> row.map { BigInteger.valueOf(it.toLong()) } }

        expectedRows.forEachIndexed { n, row ->
            val actual = (0..n).map { k -> Eulerian(n, k) }.dropLastWhile { it == BigInteger.ZERO }
            actual shouldBe row
        }
    }

    "Sum over k gives factorial identity Σₖ A(n, k) = n!" {
        (0..8).forEach { n ->
            val sum = (0..n).fold(BigInteger.ZERO) { acc, k -> acc + Eulerian(n, k) }
            val factorial = (1..n).fold(BigInteger.ONE) { acc, i -> acc * BigInteger.valueOf(i.toLong()) }
            sum shouldBe factorial
        }
    }

    "Eulerian numbers outside valid range are zero" {
        Eulerian(4, -1) shouldBe BigInteger.ZERO
        Eulerian(4, 4) shouldBe BigInteger.ZERO
        Eulerian(-1, 0) shouldBe BigInteger.ZERO
    }
})