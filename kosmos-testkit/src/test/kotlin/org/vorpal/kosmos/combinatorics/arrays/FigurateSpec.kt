package org.vorpal.kosmos.combinatorics.arrays

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigInteger

class FigurateSpec : StringSpec({

    // ========== Edge Cases ==========

    "invalid inputs return zero" {
        Figurate(2, 5) shouldBe BigInteger.ZERO  // s < 3
        Figurate(1, 5) shouldBe BigInteger.ZERO  // s < 3
        Figurate(5, 0) shouldBe BigInteger.ZERO  // n < 1
        Figurate(5, -1) shouldBe BigInteger.ZERO // n < 1
        Figurate(2, 0) shouldBe BigInteger.ZERO  // both invalid
    }

    "all figurate numbers start at 1" {
        for (s in 3..10) {
            Figurate(s, 1) shouldBe BigInteger.ONE
        }
    }

    // ========== Known OEIS Sequences ==========

    "triangular numbers (A000217)" {
        val expected = listOf(1, 3, 6, 10, 15, 21, 28, 36, 45, 55)
        for (n in 1..expected.size) {
            Figurate(3, n).toInt() shouldBe expected[n - 1]
        }
    }

    "square numbers (A000290)" {
        val expected = listOf(1, 4, 9, 16, 25, 36, 49, 64, 81, 100)
        for (n in 1..expected.size) {
            Figurate(4, n).toInt() shouldBe expected[n - 1]
        }
    }

    "pentagonal numbers (A000326)" {
        val expected = listOf(1, 5, 12, 22, 35, 51, 70, 92, 117, 145)
        for (n in 1..expected.size) {
            Figurate(5, n).toInt() shouldBe expected[n - 1]
        }
    }

    "hexagonal numbers (A000384)" {
        val expected = listOf(1, 6, 15, 28, 45, 66, 91, 120, 153, 190)
        for (n in 1..expected.size) {
            Figurate(6, n).toInt() shouldBe expected[n - 1]
        }
    }

    "heptagonal numbers (A000566)" {
        val expected = listOf(1, 7, 18, 34, 55, 81, 112, 148, 189, 235)
        for (n in 1..expected.size) {
            Figurate(7, n).toInt() shouldBe expected[n - 1]
        }
    }

    "octagonal numbers (A000567)" {
        val expected = listOf(1, 8, 21, 40, 65, 96, 133, 176, 225, 280)
        for (n in 1..expected.size) {
            Figurate(8, n).toInt() shouldBe expected[n - 1]
        }
    }

    // ========== Recurrence vs Closed Form ==========

    "recurrence and closed form agree for small values" {
        for (s in 3..10) {
            for (n in 1..10) {
                Figurate(s, n) shouldBe Figurate.closedForm(s, n)
            }
        }
    }

    "recurrence and closed form agree for larger values" {
        for (s in 3..20) {
            for (n in listOf(50, 100, 200)) {
                Figurate(s, n) shouldBe Figurate.closedForm(s, n)
            }
        }
    }

    // ========== Difference Properties ==========

    "differences follow recurrence pattern: P(s,n) - P(s,n-1) = (s-2)(n-1) + 1" {
        for (s in 3..10) {
            for (n in 2..20) {
                val diff = Figurate(s, n) - Figurate(s, n - 1)
                val expected = BigInteger.valueOf((s - 2L) * (n - 1L) + 1L)
                diff shouldBe expected
            }
        }
    }

    "triangular number differences are sequential integers" {
        for (n in 2..10) {
            val diff = Figurate(3, n) - Figurate(3, n - 1)
            diff shouldBe BigInteger.valueOf(n.toLong())
        }
    }

    "square number differences are odd numbers" {
        for (n in 2..10) {
            val diff = Figurate(4, n) - Figurate(4, n - 1)
            diff shouldBe BigInteger.valueOf(2L * n - 1)
        }
    }

    // ========== Special Formulas ==========

    "triangular numbers match n(n+1)/2" {
        for (n in 1..20) {
            val expected = BigInteger.valueOf(n.toLong() * (n + 1L) / 2L)
            Figurate(3, n) shouldBe expected
        }
    }

    "square numbers match n^2" {
        for (n in 1..20) {
            val expected = BigInteger.valueOf(n.toLong() * n)
            Figurate(4, n) shouldBe expected
        }
    }

    // ========== Relationships Between Sequences ==========

    "hexagonal numbers are triangular numbers at odd indices: H(n) = T(2n-1)" {
        for (n in 1..10) {
            Figurate(6, n) shouldBe Figurate(3, 2 * n - 1)
        }
    }

    "every hexagonal number is triangular" {
        for (n in 1..10) {
            val hex = Figurate(6, n)
            // Check if there exists k such that T(k) = hex
            val k = 2 * n - 1
            Figurate(3, k) shouldBe hex
        }
    }

    // ========== Large Value Tests ==========

    "handles large n values correctly" {
        val large = Figurate(5, 1000)
        val expected = BigInteger.valueOf((5 - 2L) * 1000L * 1000 - (5 - 4L) * 1000)
            .divide(BigInteger.TWO)
        large shouldBe expected
    }

    "handles large s values correctly" {
        val large = Figurate(100, 50)
        val expected = BigInteger.valueOf((100 - 2L) * 50L * 50 - (100 - 4L) * 50)
            .divide(BigInteger.TWO)
        large shouldBe expected
    }

    // ========== Monotonicity ==========

    "figurate numbers are strictly increasing in n for fixed s" {
        for (s in 3..10) {
            for (n in 1..19) {
                Figurate(s, n + 1) shouldBe Figurate(s, n) +
                        BigInteger.valueOf((s - 2L) * n + 1L)
                (Figurate(s, n + 1) > Figurate(s, n)) shouldBe true
            }
        }
    }

    "figurate numbers are strictly increasing in s for fixed n > 1" {
        for (n in 2..10) {
            for (s in 3..9) {
                (Figurate(s + 1, n) > Figurate(s, n)) shouldBe true
            }
        }
    }
})