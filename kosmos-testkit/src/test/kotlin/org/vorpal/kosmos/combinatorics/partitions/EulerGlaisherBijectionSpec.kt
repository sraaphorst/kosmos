package org.vorpal.kosmos.combinatorics.partitions

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

/**
 * Tests for [EulerGlaisherBijection]: the correspondence between partitions into odd
 * parts and partitions into distinct parts of the same weight.
 *
 * Property tests use [ArbPartition.arbOdd] / [ArbPartition.arbDistinct] to check the
 * general laws (domain-checking, weight preservation, landing in the right predicate,
 * and round-tripping as mutual inverses). Fixed examples pin down a few small,
 * hand-verified cases (including one, n = 15, where a part's multiplicity spans more
 * than one binary bit) so the property tests aren't the only line of defense.
 */
class EulerGlaisherBijectionSpec : StringSpec({

    // ---- domain checking ---------------------------------------------------

    "oddPartitionToDistinctPartition rejects a partition with an even part" {
        shouldThrow<IllegalArgumentException> {
            EulerGlaisherBijection.oddPartitionToDistinctPartition(Partition.of(4, 3, 1))
        }
    }

    "distinctPartitionToOddPartition rejects a partition with a repeated part" {
        shouldThrow<IllegalArgumentException> {
            EulerGlaisherBijection.distinctPartitionToOddPartition(Partition.of(3, 3, 1))
        }
    }

    // ---- lands in the right codomain ---------------------------------------

    "oddPartitionToDistinctPartition always produces a partition with distinct parts" {
        checkAll(ArbPartition.arbOdd()) { p ->
            EulerGlaisherBijection.oddPartitionToDistinctPartition(p).hasDistinctParts shouldBe true
        }
    }

    "distinctPartitionToOddPartition always produces a partition with only odd parts" {
        checkAll(ArbPartition.arbDistinct()) { q ->
            EulerGlaisherBijection.distinctPartitionToOddPartition(q).hasOnlyOddParts shouldBe true
        }
    }

    // ---- weight preservation ------------------------------------------------

    "oddPartitionToDistinctPartition preserves weight" {
        checkAll(ArbPartition.arbOdd()) { p ->
            EulerGlaisherBijection.oddPartitionToDistinctPartition(p).weight shouldBe p.weight
        }
    }

    "distinctPartitionToOddPartition preserves weight" {
        checkAll(ArbPartition.arbDistinct()) { q ->
            EulerGlaisherBijection.distinctPartitionToOddPartition(q).weight shouldBe q.weight
        }
    }

    // ---- mutual inverses (round-trip) ---------------------------------------

    "distinctPartitionToOddPartition undoes oddPartitionToDistinctPartition" {
        checkAll(ArbPartition.arbOdd()) { p ->
            val roundTrip = EulerGlaisherBijection.distinctPartitionToOddPartition(
                EulerGlaisherBijection.oddPartitionToDistinctPartition(p)
            )
            roundTrip shouldBe p
        }
    }

    "oddPartitionToDistinctPartition undoes distinctPartitionToOddPartition" {
        checkAll(ArbPartition.arbDistinct()) { q ->
            val roundTrip = EulerGlaisherBijection.oddPartitionToDistinctPartition(
                EulerGlaisherBijection.distinctPartitionToOddPartition(q)
            )
            roundTrip shouldBe q
        }
    }

    // ---- the empty partition is a (trivial) fixed point ---------------------

    "the empty partition maps to itself in both directions" {
        val empty = Partition.of()
        EulerGlaisherBijection.oddPartitionToDistinctPartition(empty) shouldBe empty
        EulerGlaisherBijection.distinctPartitionToOddPartition(empty) shouldBe empty
    }

    // ---- odd-and-distinct partitions are fixed points of both directions ------

    "partitions that are both odd and distinct are fixed points of both directions" {
        checkAll(ArbPartition.arbOddDistinct()) { p ->
            EulerGlaisherBijection.oddPartitionToDistinctPartition(p) shouldBe p
            EulerGlaisherBijection.distinctPartitionToOddPartition(p) shouldBe p
        }
    }

    // ---- fixed, hand-verified examples ---------------------------------------

    "n = 1: {1} <-> {1}" {
        val odd = Partition.of(1)
        val distinct = Partition.of(1)
        EulerGlaisherBijection.oddPartitionToDistinctPartition(odd) shouldBe distinct
        EulerGlaisherBijection.distinctPartitionToOddPartition(distinct) shouldBe odd
    }

    "n = 3: {3} <-> {3}, and {1,1,1} <-> {2,1}" {
        EulerGlaisherBijection.oddPartitionToDistinctPartition(Partition.of(3)) shouldBe Partition.of(3)
        EulerGlaisherBijection.distinctPartitionToOddPartition(Partition.of(3)) shouldBe Partition.of(3)

        EulerGlaisherBijection.oddPartitionToDistinctPartition(Partition.of(1, 1, 1)) shouldBe Partition.of(2, 1)
        EulerGlaisherBijection.distinctPartitionToOddPartition(Partition.of(2, 1)) shouldBe Partition.of(1, 1, 1)
    }

    "n = 4: {3,1} <-> {3,1} (multiplicities all 1, so the map is the identity here)" {
        val p = Partition.of(3, 1)
        EulerGlaisherBijection.oddPartitionToDistinctPartition(p) shouldBe p
        EulerGlaisherBijection.distinctPartitionToOddPartition(p) shouldBe p
    }

    "n = 4: {1,1,1,1} <-> {4}" {
        EulerGlaisherBijection.oddPartitionToDistinctPartition(Partition.of(1, 1, 1, 1)) shouldBe Partition.of(4)
        EulerGlaisherBijection.distinctPartitionToOddPartition(Partition.of(4)) shouldBe Partition.of(1, 1, 1, 1)
    }

    "n = 15: {3,3,3,3,3} <-> {12,3} (multiplicity 5 = 0b101 spans two bits)" {
        val odd = Partition.of(3, 3, 3, 3, 3)
        val distinct = Partition.of(12, 3)
        EulerGlaisherBijection.oddPartitionToDistinctPartition(odd) shouldBe distinct
        EulerGlaisherBijection.distinctPartitionToOddPartition(distinct) shouldBe odd
    }

    "mixed odd parts are mapped canonically" {
        val odd = Partition.of(5, 5, 5, 3, 3, 1)
        val distinct = Partition.of(10, 6, 5, 1)

        EulerGlaisherBijection
            .oddPartitionToDistinctPartition(odd) shouldBe distinct

        EulerGlaisherBijection
            .distinctPartitionToOddPartition(distinct) shouldBe odd
    }
})
