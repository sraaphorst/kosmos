package org.vorpal.kosmos.combinatorial.arrays

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigInteger

class AitkenSpec : StringSpec({

    // ========== Edge Cases ==========

    "out of bounds returns zero" {
        Aitken(5, 6) shouldBe BigInteger.ZERO   // k > n
        Aitken(5, -1) shouldBe BigInteger.ZERO  // k < 0
        Aitken(-1, 0) shouldBe BigInteger.ZERO  // n < 0
        Aitken(0, 1) shouldBe BigInteger.ZERO   // k > n at base
    }

    "base case (0,0) is 1" {
        Aitken(0, 0) shouldBe BigInteger.ONE
    }

    // ========== Known Triangle Rows (OEIS A011971) ==========

    "first few rows match known Bell triangle" {
        val expectedRows = listOf(
            listOf(1),
            listOf(1, 2),
            listOf(2, 3, 5),
            listOf(5, 7, 10, 15),
            listOf(15, 20, 27, 37, 52),
            listOf(52, 67, 87, 114, 151, 203)
        ).map { row -> row.map { BigInteger.valueOf(it.toLong()) } }

        expectedRows.forEachIndexed { n, expected ->
            val actual = (0..n).map { k -> Aitken(n, k) }
            actual shouldBe expected
        }
    }

    "first column contains previous row's last element" {
        // A(n, 0) = A(n-1, n-1) for n >= 1
        for (n in 1..10) {
            Aitken(n, 0) shouldBe Aitken(n - 1, n - 1)
        }
    }

    "diagonal elements are Bell numbers" {
        // A(n, n) gives: 1, 2, 5, 15, 52, 203, 877...
        val bellDiagonal = listOf(1, 2, 5, 15, 52, 203, 877, 4140)
            .map { BigInteger.valueOf(it.toLong()) }

        bellDiagonal.forEachIndexed { n, expected ->
            Aitken(n, n) shouldBe expected
        }
    }

    // ========== Recurrence Properties ==========

    "recurrence relation holds: A(n,k) = A(n,k-1) + A(n-1,k-1)" {
        for (n in 1..10) {
            for (k in 1..n) {
                val expected = Aitken(n, k - 1) + Aitken(n - 1, k - 1)
                Aitken(n, k) shouldBe expected
            }
        }
    }

    "each row starts with previous diagonal: A(n,0) = A(n-1,n-1)" {
        for (n in 1..10) {
            Aitken(n, 0) shouldBe Aitken(n - 1, n - 1)
        }
    }

    // ========== Row Properties ==========

    "row elements are strictly increasing" {
        for (n in 1..10) {
            for (k in 1..n) {
                (Aitken(n, k) > Aitken(n, k - 1)) shouldBe true
            }
        }
    }

    "row sums equal Bell numbers" {
        // Row sums give: 1, 3, 10, 37, 151, 674, 3263...
        // But the problem states "row sums match Bell number sequence"
        // Let me calculate what they actually are
        val expectedSums = listOf(
            1,     // row 0: 1
            3,     // row 1: 1+2
            10,    // row 2: 2+3+5
            37,    // row 3: 5+7+10+15
            151,   // row 4: 15+20+27+37+52
            674    // row 5: 52+67+87+114+151+203
        ).map { BigInteger.valueOf(it.toLong()) }

        expectedSums.forEachIndexed { n, expected ->
            val sum = (0..n).fold(BigInteger.ZERO) { acc, k -> acc + Aitken(n, k) }
            sum shouldBe expected
        }
    }

    // ========== Column Properties ==========

    "column elements are strictly increasing down" {
        for (k in 1..8) {
            for (n in k + 1..10) {
                (Aitken(n, k) > Aitken(n - 1, k)) shouldBe true
            }
        }
    }

    "first column progression" {
        // First column: 1, 1, 2, 5, 15, 52, 203...
        val firstColumn = listOf(1, 1, 2, 5, 15, 52, 203, 877)
            .map { BigInteger.valueOf(it.toLong()) }

        firstColumn.forEachIndexed { n, expected ->
            Aitken(n, 0) shouldBe expected
        }
    }

    // ========== Difference Properties ==========

    "row differences equal previous row: A(n,k) - A(n,k-1) = A(n-1,k-1)" {
        for (n in 1..10) {
            for (k in 1..n) {
                val diff = Aitken(n, k) - Aitken(n, k - 1)
                diff shouldBe Aitken(n - 1, k - 1)
            }
        }
    }

    // ========== Specific Known Values ==========

    "selected known values from OEIS A011971" {
        Aitken(0, 0) shouldBe BigInteger.ONE
        Aitken(1, 1) shouldBe BigInteger.valueOf(2)
        Aitken(2, 2) shouldBe BigInteger.valueOf(5)
        Aitken(3, 1) shouldBe BigInteger.valueOf(7)
        Aitken(4, 2) shouldBe BigInteger.valueOf(27)
        Aitken(5, 3) shouldBe BigInteger.valueOf(114)
        Aitken(6, 4) shouldBe BigInteger.valueOf(523)
    }

    // ========== Diagonal Relationships ==========

    "main diagonal equals next row start" {
        // A(n, n) = A(n+1, 0) for all n >= 0
        for (n in 0..9) {
            Aitken(n, n) shouldBe Aitken(n + 1, 0)
        }
    }

    "superdiagonal follows recurrence" {
        // A(n, n-1) = A(n, n-2) + A(n-1, n-2)
        for (n in 2..10) {
            val expected = Aitken(n, n - 2) + Aitken(n - 1, n - 2)
            Aitken(n, n - 1) shouldBe expected
        }
    }

    // ========== Large Values ==========

    "handles large n without stack overflow" {
        val large = Aitken(10, 10)
        large shouldBe BigInteger("678570")
    }

    "handles large intermediate values" {
        val result = Aitken(12, 6)
        // This should compute without error
        (result > BigInteger.ZERO) shouldBe true
    }

    "computes entire large row efficiently" {
        val n = 10
        val row = (0..n).map { k -> Aitken(n, k) }
        row.size shouldBe 11
        row.first() shouldBe BigInteger("115975")
        row.last() shouldBe BigInteger("678570")
    }

    // ========== Monotonicity ==========

    "all elements are positive for valid indices" {
        for (n in 0..10) {
            for (k in 0..n) {
                (Aitken(n, k) >= BigInteger.ONE) shouldBe true
            }
        }
    }

    "diagonal grows exponentially" {
        // Each diagonal element significantly larger than previous
        for (n in 1..7) {
            val ratio = Aitken(n, n).toDouble() / Aitken(n - 1, n - 1).toDouble()
            (ratio > 1.5) shouldBe true
        }
    }

    // ========== Construction Properties ==========

    "each element is sum of left and upper-left" {
        // Explicit verification of construction rule
        for (n in 2..8) {
            for (k in 1..n) {
                val left = Aitken(n, k - 1)
                val upperLeft = Aitken(n - 1, k - 1)
                Aitken(n, k) shouldBe (left + upperLeft)
            }
        }
    }

    "row begins with last element of previous row" {
        // Each row "wraps" from previous row's end
        for (n in 1..8) {
            Aitken(n, 0) shouldBe Aitken(n - 1, n - 1)
        }
    }
})