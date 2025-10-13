package org.vorpal.kosmos.combinatorics.arrays

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.combinatorics.Factorial
import java.math.BigInteger

/**
 * Tests for [Eulerian] numbers A(n, k) — OEIS A008292.
 *
 * Eulerian numbers count the permutations of {1,…,n} with exactly k ascents.
 *
 * This suite verifies:
 *  - Base and boundary cases.
 *  - Agreement with known OEIS rows.
 *  - Recurrence ↔ closed-form consistency.
 *  - Symmetry A(n,k) = A(n,n−1−k).
 *  - Factorial sum identity Σₖ A(n,k) = n!.
 *  - Column and unimodality patterns.
 *  - Combinatorial interpretation and performance sanity.
 */
class EulerianSpec : StringSpec({

    // ---------- Base & Edge Cases ----------

    "base and boundary cases" {
        Eulerian(0, 0) shouldBe BigInteger.ONE
        Eulerian(1, 0) shouldBe BigInteger.ONE
        Eulerian(1, 1) shouldBe BigInteger.ZERO
        Eulerian(4, -1) shouldBe BigInteger.ZERO
        Eulerian(4, 4) shouldBe BigInteger.ZERO
        Eulerian(0, 1) shouldBe BigInteger.ZERO
    }

    // ---------- Known Values (OEIS A008292) ----------

    "matches known rows (n=0..6)" {
        val expected = listOf(
            listOf(1),
            listOf(1),
            listOf(1, 1),
            listOf(1, 4, 1),
            listOf(1, 11, 11, 1),
            listOf(1, 26, 66, 26, 1),
            listOf(1, 57, 302, 302, 57, 1)
        ).map { row -> row.map(Int::toBigInteger) }

        expected.forEachIndexed { n, row ->
            val actual = (0 until n.coerceAtLeast(1)).map { k -> Eulerian(n, k) }
            actual shouldBe row
        }
    }

    // ---------- Symmetry ----------

    "triangle is symmetric: A(n,k) = A(n,n−1−k)" {
        for (n in 2..10)
            for (k in 0 until n)
                Eulerian(n, k) shouldBe Eulerian(n, n - 1 - k)
    }

    // ---------- Recurrence vs Closed Form ----------

    "recurrence and closed form agree (n=0..12)" {
        for (n in 0..12)
            for (k in 0 until n.coerceAtLeast(1))
                Eulerian(n, k) shouldBe Eulerian.closedForm(n, k)
    }

    // ---------- Factorial Identity ----------

    "row sums equal n! (recurrence and closed form)" {
        for (n in 0..8) {
            val recurrenceSum = (0 until n.coerceAtLeast(1))
                .fold(BigInteger.ZERO) { acc, k -> acc + Eulerian(n, k) }
            val closedSum = (0 until n.coerceAtLeast(1))
                .fold(BigInteger.ZERO) { acc, k -> acc + Eulerian.closedForm(n, k) }

            recurrenceSum shouldBe Factorial(n)
            closedSum shouldBe Factorial(n)
        }
    }

    // ---------- Unimodality ----------

    "each row is unimodal (increases then decreases)" {
        for (n in 3..10) {
            val row = (0 until n).map { k -> Eulerian(n, k) }
            val maxIndex = row.indexOf(row.maxOrNull())
            for (i in 1..maxIndex) (row[i] >= row[i - 1]) shouldBe true
            for (i in maxIndex + 1 until row.size) (row[i] <= row[i - 1]) shouldBe true
        }
    }

    // ---------- Column Patterns ----------

    "second column follows A(n,1) = 2^n − n − 1" {
        for (n in 2..10) {
            val expected = BigInteger.valueOf(2).pow(n) -
                    BigInteger.valueOf(n.toLong()) - BigInteger.ONE
            Eulerian(n, 1) shouldBe expected
        }
    }

    "penultimate column equals second column" {
        for (n in 3..10)
            Eulerian(n, n - 2) shouldBe Eulerian(n, 1)
    }

    // ---------- Combinatorial Meaning ----------

    "A(n,0)=1 and A(n,n−1)=1 for all n" {
        for (n in 1..10) {
            Eulerian(n, 0) shouldBe BigInteger.ONE
            Eulerian(n, n - 1) shouldBe BigInteger.ONE
        }
    }

    "A(3,k) matches 3-element permutation counts" {
        Eulerian(3, 0) shouldBe BigInteger.ONE
        Eulerian(3, 1) shouldBe BigInteger.valueOf(4)
        Eulerian(3, 2) shouldBe BigInteger.ONE
    }

    // ---------- Performance & Consistency ----------

    "large value matches closed form and remains positive" {
        val n = 15; val k = 7
        val recurrence = Eulerian(n, k)
        val closed = Eulerian.closedForm(n, k)
        recurrence shouldBe closed
        (recurrence > BigInteger.ZERO) shouldBe true
    }
})