package org.vorpal.kosmos.combinatorics.partitions

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.checkAll
import java.math.BigInteger

/**
 * Unit and property tests for [Partition].
 *
 * Note that [Partition.of] validates but does not sort: callers must supply parts
 * already in nonincreasing order, or construction throws. Several tests below pin
 * down that contract explicitly.
 */
class PartitionSpec : StringSpec({

    // ---- Construction ---------------------------------------------------

    "of(vararg) builds a partition from already-sorted parts" {
        Partition.of(4, 3, 3, 1).parts shouldBe listOf(4, 3, 3, 1)
    }

    "of(Iterable) builds a partition from already-sorted parts" {
        Partition.of(listOf(4, 3, 3, 1)).parts shouldBe listOf(4, 3, 3, 1)
    }

    "of() with no arguments is the unique partition of zero" {
        val empty = Partition.of()
        empty.parts shouldBe emptyList()
        empty.weight shouldBe BigInteger.ZERO
        empty.length shouldBe 0
    }

    "of rejects zero or negative parts" {
        shouldThrow<IllegalArgumentException> { Partition.of(3, 0, 1) }
        shouldThrow<IllegalArgumentException> { Partition.of(2, -1) }
    }

    "of rejects parts that are not already in nonincreasing order (no implicit sorting)" {
        shouldThrow<IllegalArgumentException> { Partition.of(1, 2, 3) }
        shouldThrow<IllegalArgumentException> { Partition.of(3, 1, 2) }
    }

    "arbitrary partitions always satisfy the class invariant" {
        checkAll(ArbPartition.arbSmall()) { p ->
            p.parts.all { it > 0 } shouldBe true
            p.parts.zipWithNext().all { (a, b) -> a >= b } shouldBe true
        }
    }

    "construction does not retain a mutable input list" {
        val parts = mutableListOf(4, 3, 1)
        val partition = Partition.of(parts)

        parts[0] = 2

        partition.parts shouldBe listOf(4, 3, 1)
    }

    // ---- weight -----------------------------------------------------------

    "weight is the sum of the parts" {
        Partition.of(4, 3, 3, 1).weight shouldBe BigInteger.valueOf(11)
    }

    "weight matches summing parts for arbitrary partitions" {
        checkAll(ArbPartition.arbSmall()) { p ->
            p.weight shouldBe p.parts.sumOf(Int::toBigInteger)
        }
    }

    "weight is never negative" {
        checkAll(ArbPartition.arbSmall()) { p ->
            (p.weight >= BigInteger.ZERO) shouldBe true
        }
    }

    "weight does not overflow Int" {
        val partition = Partition.of(Int.MAX_VALUE, 1)

        partition.weight shouldBe
            Int.MAX_VALUE.toBigInteger() + BigInteger.ONE
    }

    // ---- length -------------------------------------------------------------

    "length is the number of parts" {
        Partition.of(4, 3, 3, 1).length shouldBe 4
        Partition.of().length shouldBe 0
    }

    "length matches parts.size for arbitrary partitions" {
        checkAll(ArbPartition.arbSmall()) { p ->
            p.length shouldBe p.parts.size
        }
    }

    // ---- hasOnlyOddParts ------------------------------------------------------

    "hasOnlyOddParts is true when every part is odd" {
        Partition.of(5, 3, 1).hasOnlyOddParts shouldBe true
    }

    "hasOnlyOddParts is false when some part is even" {
        Partition.of(4, 3, 1).hasOnlyOddParts shouldBe false
    }

    "hasOnlyOddParts is vacuously true for the empty partition" {
        Partition.of().hasOnlyOddParts shouldBe true
    }

    "hasOnlyOddParts holds for arbitrary odd-part partitions" {
        checkAll(ArbPartition.arbOdd()) { p ->
            p.hasOnlyOddParts shouldBe true
        }
    }

    // ---- hasDistinctParts -----------------------------------------------------

    "hasDistinctParts is true when all parts differ" {
        Partition.of(4, 3, 1).hasDistinctParts shouldBe true
    }

    "hasDistinctParts is false when a part repeats" {
        Partition.of(4, 3, 3, 1).hasDistinctParts shouldBe false
    }

    "hasDistinctParts is vacuously true for the empty partition" {
        Partition.of().hasDistinctParts shouldBe true
    }

    "hasDistinctParts holds for arbitrary distinct-part partitions" {
        checkAll(ArbPartition.arbDistinct()) { p ->
            p.hasDistinctParts shouldBe true
        }
    }

    "odd-and-distinct generator produces partitions satisfying both predicates" {
        checkAll(ArbPartition.arbOddDistinct()) { p ->
            p.hasOnlyOddParts shouldBe true
            p.hasDistinctParts shouldBe true
        }
    }

    // ---- hasNoPartDivisibleBy (general k; hasOnlyOddParts is the k = 2 case) ----

    "hasNoPartDivisibleBy is true when no part is divisible by k" {
        Partition.of(5, 4, 1).hasNoPartDivisibleBy(3) shouldBe true
    }

    "hasNoPartDivisibleBy is false when some part is divisible by k" {
        Partition.of(6, 4, 1).hasNoPartDivisibleBy(3) shouldBe false
    }

    "hasNoPartDivisibleBy is vacuously true for the empty partition, for any k" {
        Partition.of().hasNoPartDivisibleBy(3) shouldBe true
        Partition.of().hasNoPartDivisibleBy(5) shouldBe true
    }

    "hasNoPartDivisibleBy rejects k < 2" {
        shouldThrow<IllegalArgumentException> { Partition.of(3, 1).hasNoPartDivisibleBy(1) }
        shouldThrow<IllegalArgumentException> { Partition.of(3, 1).hasNoPartDivisibleBy(0) }
    }

    "hasNoPartDivisibleBy(2) agrees with hasOnlyOddParts" {
        checkAll(ArbPartition.arbSmall()) { p ->
            p.hasNoPartDivisibleBy(2) shouldBe p.hasOnlyOddParts
        }
    }

    "hasNoPartDivisibleBy holds for arbitrary k-regular partitions, for several k" {
        checkAll(GlaisherTestingCombinations.arbKAndKRegularPartition()) { (k, p) ->
            p.hasNoPartDivisibleBy(k) shouldBe true
        }
    }

    // ---- hasMultiplicitiesLessThan (general k; hasDistinctParts is the k = 2 case) --

    "hasMultiplicitiesLessThan is true when every part occurs fewer than k times" {
        Partition.of(5, 5, 3, 1).hasMultiplicitiesLessThan(3) shouldBe true
    }

    "hasMultiplicitiesLessThan is false when some part occurs k or more times" {
        Partition.of(5, 5, 5, 3).hasMultiplicitiesLessThan(3) shouldBe false
    }

    "hasMultiplicitiesLessThan is vacuously true for the empty partition, for any k" {
        Partition.of().hasMultiplicitiesLessThan(3) shouldBe true
        Partition.of().hasMultiplicitiesLessThan(5) shouldBe true
    }

    "hasMultiplicitiesLessThan rejects k < 2" {
        shouldThrow<IllegalArgumentException> { Partition.of(3, 1).hasMultiplicitiesLessThan(1) }
        shouldThrow<IllegalArgumentException> { Partition.of(3, 1).hasMultiplicitiesLessThan(0) }
    }

    "hasMultiplicitiesLessThan(2) agrees with hasDistinctParts" {
        checkAll(ArbPartition.arbSmall()) { p ->
            p.hasMultiplicitiesLessThan(2) shouldBe p.hasDistinctParts
        }
    }

    "hasMultiplicitiesLessThan holds for arbitrary bounded-multiplicity partitions, for several k" {
        checkAll(GlaisherTestingCombinations.arbKAndBoundedMultiplicityPartition()) { (k, q) ->
            q.hasMultiplicitiesLessThan(k) shouldBe true
        }
    }

    // ---- equals / hashCode ------------------------------------------------------

    "partitions built independently from the same parts are equal" {
        Partition.of(4, 3, 1) shouldBe Partition.of(4, 3, 1)
    }

    "partitions with different parts are not equal" {
        Partition.of(4, 3, 1) shouldNotBe Partition.of(4, 2, 1)
    }

    "a partition is not equal to null or to an unrelated type" {
        val p = Partition.of(4, 3, 1)
        (p.equals(null)) shouldBe false
        (p.equals("Partition(4, 3, 1)")) shouldBe false
    }

    "equals is reflexive for arbitrary partitions" {
        checkAll(ArbPartition.arbSmall()) { p ->
            (p == p) shouldBe true
        }
    }

    "equal partitions have equal hashCodes" {
        checkAll(ArbPartition.arbSmall()) { p ->
            val q = Partition.of(p.parts)
            p shouldBe q
            p.hashCode() shouldBe q.hashCode()
        }
    }

    // ---- toString -----------------------------------------------------------

    "toString shows weight and parts" {
        Partition.of(4, 3, 3, 1).toString() shouldBe "Partition(11): [4, 3, 3, 1]"
    }
})
