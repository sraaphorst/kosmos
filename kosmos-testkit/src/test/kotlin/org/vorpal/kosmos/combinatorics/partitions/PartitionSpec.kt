package org.vorpal.kosmos.combinatorics.partitions

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigInteger
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.InvolutionLaw
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Unit and property tests for [Partition].
 *
 * Note that [Partition.of] validates but does not sort: callers must supply parts
 * already in nonincreasing order, or construction throws. Several tests below pin
 * down that contract explicitly.
 */
class PartitionSpec : FunSpec({
    lawTests(lawGraph("partitions") {
        // ---- Construction ---------------------------------------------------

        local("of(vararg) builds a partition from already-sorted parts") {
            Partition.of(4, 3, 3, 1).parts shouldBe listOf(4, 3, 3, 1)
        }

        local("of(Iterable) builds a partition from already-sorted parts") {
            Partition.of(listOf(4, 3, 3, 1)).parts shouldBe listOf(4, 3, 3, 1)
        }

        local("of() with no arguments is the unique partition of zero") {
            val empty = Partition.of()
            empty.parts shouldBe emptyList()
            empty.weight shouldBe BigInteger.ZERO
            empty.length shouldBe 0
        }

        local("of rejects zero or negative parts") {
            shouldThrow<IllegalArgumentException> { Partition.of(3, 0, 1) }
            shouldThrow<IllegalArgumentException> { Partition.of(2, -1) }
        }

        local("of rejects parts that are not already in nonincreasing order (no implicit sorting)") {
            shouldThrow<IllegalArgumentException> { Partition.of(1, 2, 3) }
            shouldThrow<IllegalArgumentException> { Partition.of(3, 1, 2) }
        }

        local("arbitrary partitions always satisfy the class invariant") {
            lawCheckAll(ArbPartition.arbSmall()) { p ->
                p.parts.all { it > 0 } shouldBe true
                p.parts.zipWithNext().all { (a, b) -> a >= b } shouldBe true
            }
        }

        local("construction does not retain a mutable input list") {
            val parts = mutableListOf(4, 3, 1)
            val partition = Partition.of(parts)

            parts[0] = 2

            partition.parts shouldBe listOf(4, 3, 1)
        }

        // ---- weight -----------------------------------------------------------

        local("weight is the sum of the parts") {
            Partition.of(4, 3, 3, 1).weight shouldBe BigInteger.valueOf(11)
        }

        local("weight matches summing parts for arbitrary partitions") {
            lawCheckAll(ArbPartition.arbSmall()) { p ->
                p.weight shouldBe p.parts.sumOf(Int::toBigInteger)
            }
        }

        local("weight is never negative") {
            lawCheckAll(ArbPartition.arbSmall()) { p ->
                (p.weight >= BigInteger.ZERO) shouldBe true
            }
        }

        local("weight does not overflow Int") {
            val partition = Partition.of(Int.MAX_VALUE, 1)

            partition.weight shouldBe
                Int.MAX_VALUE.toBigInteger() + BigInteger.ONE
        }

        // ---- length -------------------------------------------------------------

        local("length is the number of parts") {
            Partition.of(4, 3, 3, 1).length shouldBe 4
            Partition.of().length shouldBe 0
        }

        local("length matches parts.size for arbitrary partitions") {
            lawCheckAll(ArbPartition.arbSmall()) { p ->
                p.length shouldBe p.parts.size
            }
        }

        // ---- hasOnlyOddParts ------------------------------------------------------

        local("hasOnlyOddParts is true when every part is odd") {
            Partition.of(5, 3, 1).hasOnlyOddParts shouldBe true
        }

        local("hasOnlyOddParts is false when some part is even") {
            Partition.of(4, 3, 1).hasOnlyOddParts shouldBe false
        }

        local("hasOnlyOddParts is vacuously true for the empty partition") {
            Partition.of().hasOnlyOddParts shouldBe true
        }

        local("hasOnlyOddParts holds for arbitrary odd-part partitions") {
            lawCheckAll(ArbPartition.arbOdd()) { p ->
                p.hasOnlyOddParts shouldBe true
            }
        }

        // ---- hasDistinctParts -----------------------------------------------------

        local("hasDistinctParts is true when all parts differ") {
            Partition.of(4, 3, 1).hasDistinctParts shouldBe true
        }

        local("hasDistinctParts is false when a part repeats") {
            Partition.of(4, 3, 3, 1).hasDistinctParts shouldBe false
        }

        local("hasDistinctParts is vacuously true for the empty partition") {
            Partition.of().hasDistinctParts shouldBe true
        }

        local("hasDistinctParts holds for arbitrary distinct-part partitions") {
            lawCheckAll(ArbPartition.arbDistinct()) { p ->
                p.hasDistinctParts shouldBe true
            }
        }

        local("odd-and-distinct generator produces partitions satisfying both predicates") {
            lawCheckAll(ArbPartition.arbOddDistinct()) { p ->
                p.hasOnlyOddParts shouldBe true
                p.hasDistinctParts shouldBe true
            }
        }

        // ---- hasNoPartDivisibleBy (general k; hasOnlyOddParts is the k = 2 case) ----

        local("hasNoPartDivisibleBy is true when no part is divisible by k") {
            Partition.of(5, 4, 1).hasNoPartDivisibleBy(3) shouldBe true
        }

        local("hasNoPartDivisibleBy is false when some part is divisible by k") {
            Partition.of(6, 4, 1).hasNoPartDivisibleBy(3) shouldBe false
        }

        local("hasNoPartDivisibleBy is vacuously true for the empty partition, for any k") {
            Partition.of().hasNoPartDivisibleBy(3) shouldBe true
            Partition.of().hasNoPartDivisibleBy(5) shouldBe true
        }

        local("hasNoPartDivisibleBy rejects k < 2") {
            shouldThrow<IllegalArgumentException> { Partition.of(3, 1).hasNoPartDivisibleBy(1) }
            shouldThrow<IllegalArgumentException> { Partition.of(3, 1).hasNoPartDivisibleBy(0) }
        }

        local("hasNoPartDivisibleBy(2) agrees with hasOnlyOddParts") {
            lawCheckAll(ArbPartition.arbSmall()) { p ->
                p.hasNoPartDivisibleBy(2) shouldBe p.hasOnlyOddParts
            }
        }

        local("hasNoPartDivisibleBy holds for arbitrary k-regular partitions, for several k") {
            lawCheckAll(GlaisherTestingCombinations.arbKAndKRegularPartition()) { (k, p) ->
                p.hasNoPartDivisibleBy(k) shouldBe true
            }
        }

        // ---- hasMultiplicitiesLessThan (general k; hasDistinctParts is the k = 2 case) --

        local("hasMultiplicitiesLessThan is true when every part occurs fewer than k times") {
            Partition.of(5, 5, 3, 1).hasMultiplicitiesLessThan(3) shouldBe true
        }

        local("hasMultiplicitiesLessThan is false when some part occurs k or more times") {
            Partition.of(5, 5, 5, 3).hasMultiplicitiesLessThan(3) shouldBe false
        }

        local("hasMultiplicitiesLessThan is vacuously true for the empty partition, for any k") {
            Partition.of().hasMultiplicitiesLessThan(3) shouldBe true
            Partition.of().hasMultiplicitiesLessThan(5) shouldBe true
        }

        local("hasMultiplicitiesLessThan rejects k < 2") {
            shouldThrow<IllegalArgumentException> { Partition.of(3, 1).hasMultiplicitiesLessThan(1) }
            shouldThrow<IllegalArgumentException> { Partition.of(3, 1).hasMultiplicitiesLessThan(0) }
        }

        local("hasMultiplicitiesLessThan(2) agrees with hasDistinctParts") {
            lawCheckAll(ArbPartition.arbSmall()) { p ->
                p.hasMultiplicitiesLessThan(2) shouldBe p.hasDistinctParts
            }
        }

        local("hasMultiplicitiesLessThan holds for arbitrary bounded-multiplicity partitions, for several k") {
            lawCheckAll(GlaisherTestingCombinations.arbKAndBoundedMultiplicityPartition()) { (k, q) ->
                q.hasMultiplicitiesLessThan(k) shouldBe true
            }
        }

        // ---- conjugate (transpose of the Ferrers diagram; an involution) -----------

        local("conjugates a known partition") {
            Partition.of(4, 4, 2, 1, 1).conjugate() shouldBe Partition.of(5, 3, 2, 2)
        }

        local("conjugation swaps a single row with a single column") {
            Partition.of(5).conjugate() shouldBe Partition.of(1, 1, 1, 1, 1)
            Partition.of(1, 1, 1, 1, 1).conjugate() shouldBe Partition.of(5)
        }

        local("a self-conjugate partition is fixed by conjugation") {
            // A "staircase" partition (n, n-1, ..., 1) is always self-conjugate.
            Partition.of(3, 2, 1).conjugate() shouldBe Partition.of(3, 2, 1)
            // But self-conjugate partitions aren't only staircases.
            Partition.of(3, 1, 1).conjugate() shouldBe Partition.of(3, 1, 1)
        }

        local("the empty partition is self-conjugate") {
            Partition.of().conjugate() shouldBe Partition.of()
        }

        law("conjugation is an involution", InvolutionLaw(
                Endo("conjugate", Partition::conjugate),
                ArbPartition.arbSmall()
            ))

        local("conjugation preserves weight") {
            lawCheckAll(ArbPartition.arbSmall()) { partition ->
                partition.conjugate().weight shouldBe partition.weight
            }
        }

        local("conjugation exchanges width and height") {
            lawCheckAll(ArbPartition.arbSmall(1..8)) { partition ->
                partition.conjugate().length shouldBe partition.parts.first()
                partition.conjugate().parts.first() shouldBe partition.length
            }
        }

        // ---- durfeeSide -------------------------------------------------------------

        local("durfeeSide of known partitions") {
            Partition.of().durfeeSide shouldBe 0
            Partition.of(5).durfeeSide shouldBe 1
            Partition.of(5, 3, 3, 1).durfeeSide shouldBe 3
            Partition.of(4, 4, 2, 2).durfeeSide shouldBe 2
        }

        local("durfeeSide can equal length, when every part is at least as large as the number of parts") {
            Partition.of(5, 5, 5).durfeeSide shouldBe 3
        }

        local("durfeeSide can equal the largest part, when the largest part is small relative to length") {
            Partition.of(1, 1, 1, 1).durfeeSide shouldBe 1
        }

        local("durfeeSide is invariant under conjugation") {
            lawCheckAll(ArbPartition.arbSmall()) { p ->
                p.durfeeSide shouldBe p.conjugate().durfeeSide
            }
        }

        local("durfeeSide is bounded by both length and the largest part") {
            lawCheckAll(ArbPartition.arbSmall()) { p ->
                (p.durfeeSide in 0..minOf(p.length, p.parts.firstOrNull() ?: 0)) shouldBe true
            }
        }

        // ---- equals / hashCode ------------------------------------------------------

        local("partitions built independently from the same parts are equal") {
            Partition.of(4, 3, 1) shouldBe Partition.of(4, 3, 1)
        }

        local("partitions with different parts are not equal") {
            Partition.of(4, 3, 1) shouldNotBe Partition.of(4, 2, 1)
        }

        local("a partition is not equal to null or to an unrelated type") {
            val p = Partition.of(4, 3, 1)
            (p.equals(null)) shouldBe false
            (p.equals("Partition(4, 3, 1)")) shouldBe false
        }

        local("equals is reflexive for arbitrary partitions") {
            lawCheckAll(ArbPartition.arbSmall()) { p ->
                (p == p) shouldBe true
            }
        }

        local("equal partitions have equal hashCodes") {
            lawCheckAll(ArbPartition.arbSmall()) { p ->
                val q = Partition.of(p.parts)
                p shouldBe q
                p.hashCode() shouldBe q.hashCode()
            }
        }

        // ---- toString -----------------------------------------------------------

        local("toString shows weight and parts") {
            Partition.of(4, 3, 3, 1).toString() shouldBe "Partition(11): [4, 3, 3, 1]"
        }
    })
})
