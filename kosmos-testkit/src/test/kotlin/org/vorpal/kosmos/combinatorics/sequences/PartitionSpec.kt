package org.vorpal.kosmos.combinatorics.sequences

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigInteger

/**
 * Unit tests for the [Partition] recurrence using Kotest.
 *
 * Verifies the first few values of the integer partition function p(n)
 * against the canonical sequence (OEIS A000041).
 */
class PartitionSpec : StringSpec({
    val expected = listOf(
        1, 1, 2, 3, 5, 7, 11, 15, 22, 30, 42, 56, 77, 101, 135, 176, 231, 297, 385, 490, 627, 792, 1002, 1255, 1575,
        1958, 2436, 3010, 3718, 4565, 5604, 6842, 8349, 10143, 12310, 14883, 17977, 21637, 26015, 31185, 37338, 44583,
        53174, 63261, 75175, 89134, 105558, 124754, 147273, 173525
    ).map(Int::toBigInteger)

    "Partition numbers match the first known values" {
        expected.forEachIndexed { n, expectedPn ->
            Partition(n) shouldBe expectedPn
        }
    }

    "p(0) is 1 and p(n) increases monotonically for n <= 10" {
        Partition(0) shouldBe BigInteger.ONE
        (1..10).zipWithNext { a, b ->
            (Partition(a) <= Partition(b)) shouldBe true
        }
    }

    "negative n yields zero" {
        Partition(-1) shouldBe BigInteger.ZERO
        Partition(-5) shouldBe BigInteger.ZERO
    }

    "iterator produces correct prefix of sequence" {
        val values = Partition.take(expected.size).toList()
        values shouldBe expected
    }
})
