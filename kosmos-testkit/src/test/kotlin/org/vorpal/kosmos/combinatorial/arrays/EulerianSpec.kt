package org.vorpal.kosmos.combinatorial.arrays

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.combinatorial.Factorial
import java.math.BigInteger

/**
 * Unit tests for the [Eulerian] numbers A(n, k).
 *
 * Eulerian numbers count the number of permutations of {1..n}
 * having exactly k ascents (or equivalently, k descents depending on convention).
 *
 * This suite checks:
 *  - Base cases A(0, 0) = 1, A(n, 0) = A(n, n-1) = 1
 *  - Known triangle values from OEIS A008292
 *  - Summation identity Σₖ A(n, k) = n!
 *  - Recurrence vs closed form agreement
 */
class EulerianSpec : StringSpec({

    // ========== Edge Cases ==========

    "Eulerian numbers outside valid range are zero" {
        Eulerian(4, -1) shouldBe BigInteger.ZERO
        Eulerian(4, 4) shouldBe BigInteger.ZERO   // k must be < n
        Eulerian(4, 5) shouldBe BigInteger.ZERO
        Eulerian(-1, 0) shouldBe BigInteger.ZERO
        Eulerian(0, 1) shouldBe BigInteger.ZERO
        Eulerian(1, 1) shouldBe BigInteger.ZERO   // k must be < n
    }

    "Eulerian base cases hold" {
        Eulerian(0, 0) shouldBe BigInteger.ONE
        Eulerian(1, 0) shouldBe BigInteger.ONE
        Eulerian(2, 0) shouldBe BigInteger.ONE
        Eulerian(2, 1) shouldBe BigInteger.ONE
    }

    // ========== Known Triangle Rows (OEIS A008292) ==========

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

    "first and last valid elements of each row are 1" {
        for (n in 1..10) {
            Eulerian(n, 0) shouldBe BigInteger.ONE
            Eulerian(n, n - 1) shouldBe BigInteger.ONE
        }
    }

    // ========== Recurrence vs Closed Form ==========

    "recurrence and closed form agree for all small values" {
        for (n in 0..4) {
            for (k in 0..n) {
                Eulerian(n, k) shouldBe Eulerian.closedForm(n, k)
            }
        }
    }

    "recurrence and closed form agree for larger values" {
        for (n in 11..15) {
            for (k in 0 until n) {
                Eulerian(n, k) shouldBe Eulerian.closedForm(n, k)
            }
        }
    }

    "closed form handles edge cases correctly" {
        Eulerian.closedForm(0, 0) shouldBe BigInteger.ONE
        Eulerian.closedForm(4, -1) shouldBe BigInteger.ZERO
        Eulerian.closedForm(4, 4) shouldBe BigInteger.ZERO
        Eulerian.closedForm(-1, 0) shouldBe BigInteger.ZERO
    }

    // ========== Symmetry Properties ==========

    "Eulerian triangle has horizontal symmetry: A(n,k) = A(n,n-1-k)" {
        for (n in 2..10) {
            for (k in 0 until n) {
                Eulerian(n, k) shouldBe Eulerian(n, n - 1 - k)
            }
        }
    }

    "symmetric pairs match for specific values" {
        Eulerian(4, 1) shouldBe Eulerian(4, 2)  // 11 = 11
        Eulerian(5, 1) shouldBe Eulerian(5, 3)  // 26 = 26
        Eulerian(6, 1) shouldBe Eulerian(6, 4)  // 57 = 57
        Eulerian(7, 2) shouldBe Eulerian(7, 4)  // 1191 = 1191
    }

    // ========== Recurrence Relation ==========

    "recurrence relation holds: A(n,k) = (n-k)*A(n-1,k-1) + (k+1)*A(n-1,k)" {
        for (n in 1..10) {
            for (k in 0 until n) {
                val left = BigInteger.valueOf((n - k).toLong()) * Eulerian(n - 1, k - 1)
                val right = BigInteger.valueOf((k + 1).toLong()) * Eulerian(n - 1, k)
                Eulerian(n, k) shouldBe (left + right)
            }
        }
    }

    // ========== Factorial Sum Identity ==========

    "Sum over k gives factorial identity Σₖ A(n, k) = n!" {
        (0..8).forEach { n ->
            val sum = (0 until maxOf(n, 1)).fold(BigInteger.ZERO) { acc, k ->
                acc + Eulerian(n, k)
            }
            val factorial = (1..maxOf(n, 1)).fold(BigInteger.ONE) { acc, i ->
                acc * BigInteger.valueOf(i.toLong())
            }
            sum shouldBe factorial
        }
    }

    "row sums for specific rows equal factorials" {
        val factorials = listOf(1, 1, 2, 6, 24, 120, 720, 5040, 40320)
            .map { BigInteger.valueOf(it.toLong()) }

        factorials.forEachIndexed { n, expected ->
            val sum = (0 until maxOf(n, 1)).fold(BigInteger.ZERO) { acc, k ->
                acc + Eulerian(n, k)
            }
            sum shouldBe expected
        }
    }

    "closed form row sums also equal factorials" {
        val factorials = listOf(1, 1, 2, 6, 24, 120, 720)
            .map { BigInteger.valueOf(it.toLong()) }

        factorials.forEachIndexed { n, expected ->
            val sum = (0 until maxOf(n, 1)).fold(BigInteger.ZERO) { acc, k ->
                acc + Eulerian.closedForm(n, k)
            }
            sum shouldBe expected
        }
    }

    // ========== Unimodality ==========

    "rows are unimodal with maximum near center" {
        for (n in 3..10) {
            val row = (0 until n).map { k -> Eulerian(n, k) }
            val maxIndex = row.indexOf(row.maxOrNull())
            val center = (n - 1) / 2
            // Maximum should be at or very close to center
            (maxIndex in (center - 1)..(center + 1)) shouldBe true
        }
    }

    "values increase then decrease within each row" {
        for (n in 4..10) {
            val midpoint = (n - 1) / 2
            // Values increase up to midpoint
            for (k in 0 until midpoint) {
                (Eulerian(n, k + 1) >= Eulerian(n, k)) shouldBe true
            }
            // Values decrease after midpoint (due to symmetry)
            for (k in midpoint until n - 1) {
                (Eulerian(n, k + 1) <= Eulerian(n, k)) shouldBe true
            }
        }
    }

    // ========== Specific Column Patterns ==========

    "second column follows pattern: A(n, 1) = 2^n - n - 1" {
        for (n in 2..10) {
            val expected = BigInteger.valueOf(2).pow(n) -
                    BigInteger.valueOf(n.toLong()) - BigInteger.ONE
            Eulerian(n, 1) shouldBe expected
        }
    }

    "penultimate column equals second column: A(n, n-2) = A(n, 1)" {
        for (n in 3..10) {
            Eulerian(n, n - 2) shouldBe Eulerian(n, 1)
        }
    }

    // ========== Specific Known Values ==========

    "selected known values from OEIS" {
        Eulerian(3, 1) shouldBe BigInteger.valueOf(4)
        Eulerian(4, 1) shouldBe BigInteger.valueOf(11)
        Eulerian(4, 2) shouldBe BigInteger.valueOf(11)
        Eulerian(5, 2) shouldBe BigInteger.valueOf(66)
        Eulerian(6, 2) shouldBe BigInteger.valueOf(302)
        Eulerian(7, 3) shouldBe BigInteger.valueOf(2416)
        Eulerian(8, 4) shouldBe BigInteger.valueOf(15619)
    }

    // ========== Center Values ==========

    "center values for even n" {
        // For even n, there are two center values (they're equal by symmetry)
        Eulerian(4, 1) shouldBe Eulerian(4, 2)
        Eulerian(6, 2) shouldBe Eulerian(6, 3)
        Eulerian(8, 3) shouldBe Eulerian(8, 4)
    }

    "center value for odd n" {
        // For odd n, single center value
        val n5Center = Eulerian(5, 2)
        n5Center shouldBe BigInteger.valueOf(66)

        val n7Center = Eulerian(7, 3)
        n7Center shouldBe BigInteger.valueOf(2416)
    }

    // ========== Growth Properties ==========

    "Eulerian numbers grow with n for fixed k >= 1" {
        for (k in 1..5) {
            for (n in (k + 1) until 9) {
                if (k < n) {  // Ensure both are valid
                    (Eulerian(n + 1, k) > Eulerian(n, k)) shouldBe true
                }
            }
        }
    }

    "center values grow exponentially with n" {
        for (n in 3..8) {
            val center = (n - 1) / 2
            val ratio = Eulerian(n + 1, center).toDouble() /
                    Eulerian(n, center).toDouble()
            (ratio > 2.0) shouldBe true
        }
    }

    // ========== Combinatorial Interpretation ==========

    "A(n,0) = 1 counts permutations with 0 ascents (only decreasing permutation)" {
        for (n in 1..10) {
            Eulerian(n, 0) shouldBe BigInteger.ONE
        }
    }

    "A(n,n-1) = 1 counts permutations with n-1 ascents (only increasing permutation)" {
        for (n in 1..10) {
            Eulerian(n, n - 1) shouldBe BigInteger.ONE
        }
    }

    "A(2,k) values make sense for 2-element permutations" {
        // [1,2] has 1 ascent, [2,1] has 0 ascents
        Eulerian(2, 0) shouldBe BigInteger.ONE  // [2,1]
        Eulerian(2, 1) shouldBe BigInteger.ONE  // [1,2]
    }

    "A(3,k) values match 3-element permutation counts" {
        // 0 ascents: [3,2,1] = 1 permutation
        // 1 ascent: [2,3,1], [3,1,2], [2,1,3], [1,3,2] = 4 permutations
        // 2 ascents: [1,2,3] = 1 permutation
        Eulerian(3, 0) shouldBe BigInteger.ONE
        Eulerian(3, 1) shouldBe BigInteger.valueOf(4)
        Eulerian(3, 2) shouldBe BigInteger.ONE
    }

    // ========== Large Values ==========

    "handles large n without overflow" {
        val large = Eulerian(15, 7)
        (large > BigInteger.ZERO) shouldBe true
        // Should compute without error
    }

    "handles large row computation efficiently" {
        val n = 12
        val row = (0 until n).map { k -> Eulerian(n, k) }
        row.size shouldBe 12

        // First and last should be 1
        row.first() shouldBe BigInteger.ONE
        row.last() shouldBe BigInteger.ONE

        // Row should sum to 12!
        val sum = row.fold(BigInteger.ZERO) { acc, v -> acc + v }
        val factorial = Factorial(n)
        sum shouldBe factorial
    }

    "closed form handles large values correctly" {
        val large = Eulerian.closedForm(15, 7)
        (large > BigInteger.ZERO) shouldBe true
        large shouldBe Eulerian(15, 7)
    }

    // ========== Monotonicity Within Columns ==========

    "column values increase down" {
        for (k in 1..5) {
            for (n in (k + 1)..10) {
                (Eulerian(n, k) > Eulerian(n - 1, k)) shouldBe true
            }
        }
    }

    // ========== Special Relationships ==========

    "Eulerian numbers increase then decrease across each row" {
        for (n in 3..8) {
            val row = (0 until n).map { k -> Eulerian(n, k) }

            // The first half should be non-decreasing, the second half non-increasing
            val firstHalf = row.take(n / 2 + 1)
            val secondHalf = row.drop(n / 2)

            firstHalf.zipWithNext { a, b -> (b >= a) shouldBe true }
            secondHalf.zipWithNext { a, b -> (b <= a) shouldBe true }

            // Symmetry check
            for (k in 0 until n) {
                row[k] shouldBe row[n - 1 - k]
            }
        }
    }

    // ========== Row Length Properties ==========

    "row n has correct number of valid elements" {
        for (n in 0..10) {
            val validElements = (0..n).map { k -> Eulerian(n, k) }
                .filter { it != BigInteger.ZERO }
            val expectedCount = if (n == 0) 1 else n
            validElements.size shouldBe expectedCount
        }
    }

    "elements at position n and beyond are zero" {
        for (n in 1..10) {
            Eulerian(n, n) shouldBe BigInteger.ZERO
            Eulerian(n, n + 1) shouldBe BigInteger.ZERO
        }
    }

    // ========== All Positive ==========

    "all valid elements are positive" {
        for (n in 0..10) {
            for (k in 0 until maxOf(n, 1)) {
                (Eulerian(n, k) > BigInteger.ZERO) shouldBe true
            }
        }
    }

    // ========== Closed Form Specific Tests ==========

    "closed form produces symmetric rows" {
        for (n in 2..8) {
            for (k in 0 until n) {
                Eulerian.closedForm(n, k) shouldBe Eulerian.closedForm(n, n - 1 - k)
            }
        }
    }

    "closed form matches known values" {
        Eulerian.closedForm(3, 1) shouldBe BigInteger.valueOf(4)
        Eulerian.closedForm(4, 1) shouldBe BigInteger.valueOf(11)
        Eulerian.closedForm(5, 2) shouldBe BigInteger.valueOf(66)
        Eulerian.closedForm(6, 2) shouldBe BigInteger.valueOf(302)
    }
})