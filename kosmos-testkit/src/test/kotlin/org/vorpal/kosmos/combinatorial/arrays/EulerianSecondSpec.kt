package org.vorpal.kosmos.combinatorial.arrays

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.combinatorial.Factorial
import java.math.BigInteger

/**
 * Unit tests for the [EulerianSecond] numbers ⟨n, k⟩.
 *
 * Eulerian numbers of the second kind count permutations of n elements
 * with exactly k ascending runs.
 *
 * This suite checks:
 *  - Base cases ⟨0,0⟩ = 1, ⟨n,0⟩ = 1
 *  - Known triangle values from OEIS A008517
 *  - Recurrence relation: ⟨n,k⟩ = (k+1)·⟨n-1,k⟩ + (2n-k-1)·⟨n-1,k-1⟩
 *  - Row sum identity
 *  - Growth and monotonicity properties
 */
class EulerianSecondSpec : StringSpec({

    // ========== Edge Cases ==========

    "Eulerian second numbers outside valid range are zero" {
        EulerianSecond(4, -1) shouldBe BigInteger.ZERO
        EulerianSecond(4, 4) shouldBe BigInteger.ZERO   // k must be < n
        EulerianSecond(4, 5) shouldBe BigInteger.ZERO
        EulerianSecond(-1, 0) shouldBe BigInteger.ZERO
        EulerianSecond(0, 1) shouldBe BigInteger.ZERO
        EulerianSecond(1, 1) shouldBe BigInteger.ZERO
    }

    "base case ⟨0,0⟩ = 1" {
        EulerianSecond(0, 0) shouldBe BigInteger.ONE
    }

    "first column is always 1" {
        for (n in 0..10) {
            EulerianSecond(n, 0) shouldBe BigInteger.ONE
        }
    }

    // ========== Known Triangle Rows (OEIS A008517) ==========

    "Eulerian second triangle matches first known rows (n=0..8)" {
        val expectedRows = listOf(
            listOf(1),
            listOf(1),
            listOf(1, 2),
            listOf(1, 8, 6),
            listOf(1, 22, 58, 24),
            listOf(1, 52, 328, 444, 120),
            listOf(1, 114, 1452, 4400, 3708, 720),
            listOf(1, 240, 5610, 32120, 58140, 33984, 5040),
            listOf(1, 494, 19950, 195800, 644020, 785304, 341136, 40320)
        ).map { row -> row.map { BigInteger.valueOf(it.toLong()) } }

        expectedRows.forEachIndexed { n, row ->
            val actual = (0 until maxOf(n, 1)).map { k -> EulerianSecond(n, k) }
            actual shouldBe row
        }
    }

    "specific known values from OEIS" {
        EulerianSecond(3, 1) shouldBe BigInteger.valueOf(8)
        EulerianSecond(3, 2) shouldBe BigInteger.valueOf(6)
        EulerianSecond(4, 1) shouldBe BigInteger.valueOf(22)
        EulerianSecond(4, 2) shouldBe BigInteger.valueOf(58)
        EulerianSecond(4, 3) shouldBe BigInteger.valueOf(24)
        EulerianSecond(5, 2) shouldBe BigInteger.valueOf(328)
        EulerianSecond(6, 3) shouldBe BigInteger.valueOf(4400)
    }

    // ========== Recurrence Relation ==========

    "recurrence relation holds: ⟨n,k⟩ = (k+1)·⟨n-1,k⟩ + (2n-k-1)·⟨n-1,k-1⟩" {
        for (n in 1..10) {
            for (k in 0 until n) {
                val left = BigInteger.valueOf((k + 1).toLong()) * EulerianSecond(n - 1, k)
                val right = BigInteger.valueOf((2L * n - k - 1)) * EulerianSecond(n - 1, k - 1)
                EulerianSecond(n, k) shouldBe (left + right)
            }
        }
    }

    "recurrence components are non-negative" {
        for (n in 1..10) {
            for (k in 0 until n) {
                val coeff1 = k + 1
                val coeff2 = 2 * n - k - 1
                (coeff1 > 0) shouldBe true
                (coeff2 > 0) shouldBe true
            }
        }
    }

    // ========== Row Sum Properties ==========

    "row sums equal double factorial (2n-1)!!" {
        // (2n-1)!! = (2n-1)·(2n-3)·...·3·1
        val expectedSums = listOf(
            1,      // n=0: 1!! = 1
            1,      // n=1: 1!! = 1
            3,      // n=2: 3!! = 3
            15,     // n=3: 5!! = 5·3·1 = 15
            105,    // n=4: 7!! = 7·5·3·1 = 105
            945,    // n=5: 9!! = 9·7·5·3·1 = 945
            10395   // n=6: 11!! = 11·9·7·5·3·1 = 10395
        ).map { BigInteger.valueOf(it.toLong()) }

        expectedSums.forEachIndexed { n, expected ->
            val sum = (0 until maxOf(n, 1)).fold(BigInteger.ZERO) { acc, k ->
                acc + EulerianSecond(n, k)
            }
            sum shouldBe expected
        }
    }

    "computed double factorial matches row sum" {
        for (n in 0..8) {
            val rowSum = (0 until maxOf(n, 1)).fold(BigInteger.ZERO) { acc, k ->
                acc + EulerianSecond(n, k)
            }

            // Compute (2n-1)!!
            val doubleFactorial = (1..n).fold(BigInteger.ONE) { acc, i ->
                acc * BigInteger.valueOf((2L * i - 1))
            }

            rowSum shouldBe doubleFactorial
        }
    }

    // ========== Asymmetry ==========

    "triangle is NOT symmetric (unlike first kind)" {
        // Verify explicitly that rows are not symmetric
        for (n in 3..6) {
            val row = (0 until n).map { k -> EulerianSecond(n, k) }
            val reversed = row.reversed()
            (row != reversed) shouldBe true
        }
    }

    "specific asymmetric examples" {
        EulerianSecond(3, 1) shouldBe BigInteger.valueOf(8)
        EulerianSecond(3, 2) shouldBe BigInteger.valueOf(6)
        // 8 ≠ 6, so not symmetric

        EulerianSecond(5, 1) shouldBe BigInteger.valueOf(52)
        EulerianSecond(5, 4) shouldBe BigInteger.valueOf(120)
        // 52 ≠ 120, so not symmetric
    }

    // ========== Growth Properties ==========

    "column values increase down for k >= 1" {
        for (k in 1..5) {
            for (n in (k + 1)..10) {
                (EulerianSecond(n, k) > EulerianSecond(n - 1, k)) shouldBe true
            }
        }
    }

    "values grow exponentially with n for fixed k > 0" {
        for (k in 1..4) {
            for (n in (k + 2)..8) {
                val ratio = EulerianSecond(n, k).toDouble() /
                        EulerianSecond(n - 1, k).toDouble()
                (ratio > 1.5) shouldBe true
            }
        }
    }

    "row maxima increase exponentially" {
        for (n in 2..7) {
            val maxN = (0 until n).maxOf { k -> EulerianSecond(n, k) }
            val maxNplus1 = (0 until n + 1).maxOf { k -> EulerianSecond(n + 1, k) }
            (maxNplus1 > maxN * BigInteger.TWO) shouldBe true
        }
    }

    // ========== Last Column Patterns ==========

    "last valid column ⟨n, n-1⟩ equals n!" {
        for (n in 1..8) {
            val factorial = (1..n).fold(BigInteger.ONE) { acc, i ->
                acc * BigInteger.valueOf(i.toLong())
            }
            EulerianSecond(n, n - 1) shouldBe factorial
        }
    }

    "last column grows as factorial" {
        val factorials = listOf(1, 1, 2, 6, 24, 120, 720, 5040)
            .map(Int::toBigInteger)

        factorials.forEachIndexed { n, expected ->
            if (n > 0) {
                EulerianSecond(n, n - 1) shouldBe expected
            }
        }
    }

    // ========== Column Ratios ==========

    "second column divided by first column grows" {
        for (n in 2..8) {
            val ratio = EulerianSecond(n, 1).toDouble() / EulerianSecond(n, 0).toDouble()
            val prevRatio = EulerianSecond(n - 1, 1).toDouble() / EulerianSecond(n - 1, 0).toDouble()
            (ratio > prevRatio) shouldBe true
        }
    }

    // ========== All Positive ==========

    "all valid elements are positive" {
        for (n in 0..10) {
            for (k in 0 until maxOf(n, 1)) {
                (EulerianSecond(n, k) > BigInteger.ZERO) shouldBe true
            }
        }
    }

    // ========== Row Structure ==========

    "row n has exactly n valid non-zero elements" {
        for (n in 1..10) {
            val validElements = (0 until n).map { k -> EulerianSecond(n, k) }
                .filter { it != BigInteger.ZERO }
            validElements.size shouldBe n
        }
    }

    "elements at position n and beyond are zero" {
        for (n in 1..10) {
            EulerianSecond(n, n) shouldBe BigInteger.ZERO
            EulerianSecond(n, n + 1) shouldBe BigInteger.ZERO
        }
    }

    // ========== Large Values ==========

    "handles large n efficiently" {
        val large = EulerianSecond(12, 6)
        (large > BigInteger.ZERO) shouldBe true
    }

    "computes entire large row" {
        val n = 10
        val row = (0 until n).map { k -> EulerianSecond(n, k) }
        row.size shouldBe 10
        row.first() shouldBe BigInteger.ONE
        // Last element should be 10!
        row.last() shouldBe BigInteger.valueOf(3628800L)
    }

    // ========== Combinatorial Interpretation ==========

    "⟨n, 0⟩ = 1 counts permutations with 1 ascending run (fully decreasing)" {
        for (n in 0..8) {
            EulerianSecond(n, 0) shouldBe BigInteger.ONE
        }
    }

    "⟨n, n-1⟩ = n! counts permutations with n ascending runs (all singletons)" {
        for (n in 1..9) {
            EulerianSecond(n, n - 1) shouldBe Factorial(n)
        }
    }

    "⟨2, k⟩ values sum to 3 = 3!!" {
        val sum = EulerianSecond(2, 0) + EulerianSecond(2, 1)
        sum shouldBe BigInteger.valueOf(3)
    }

    // ========== Special Values ==========

    "values in second column" {
        // ⟨n, 1⟩ for small n
        val secondColumn = listOf(2, 8, 22, 52, 114, 240)
            .map(Int::toBigInteger)

        secondColumn.forEachIndexed { index, expected ->
            val n = index + 2
            EulerianSecond(n, 1) shouldBe expected
        }
    }

    "each row is unimodal (increases then decreases)" {
        for (n in 3..10) {
            val row = (0 until n).map { k -> EulerianSecond(n, k) }
            val maxIdx = row.indices.maxBy { row[it] }
            // nondecreasing up to max
            for (i in 1..maxIdx) (row[i] >= row[i-1]) shouldBe true
            // nonincreasing after max
            for (i in maxIdx+1 until row.size) (row[i] <= row[i-1]) shouldBe true
        }
    }

    "penultimate column dominates right edge for small n" {
        // Empirical: for n = 3..7, ⟨⟨n, n-2⟩⟩ ≥ n!
        for (n in 3..7) {
            val penultimate = EulerianSecond(n, n - 2)
            (penultimate >= Factorial(n)) shouldBe true
        }
    }
})