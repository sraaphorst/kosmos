package org.vorpal.kosmos.combinatorics.partitions

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Tests for [GlaisherBijection]: the general k >= 2 correspondence between k-regular
 * partitions (no part divisible by k) and partitions with multiplicities bounded by k
 * (no part repeated k or more times), of which [EulerGlaisherBijection] is the k = 2
 * special case.
 *
 * Property tests exercise a range of moduli via [GlaisherTestingCombinations] rather
 * than a single hardcoded k, so the laws are checked for the general theorem and not
 * just its k = 2 instance. Fixed examples pin down a couple of small, hand-verified
 * cases at k = 2 and k = 3, including one where a multiplicity's base-k expansion has
 * more than one nonzero digit.
 *
 * This supersedes `EulerGlaisherBijectionSpec`, which tested only the k = 2 case; see
 * the "agrees with EulerGlaisherBijection at k = 2" section below for the check tying
 * the two together.
 */
class GlaisherBijectionSpec : StringSpec({

    // ---- k must be at least 2 ------------------------------------------------

    "kRegularToBoundedMultiplicities rejects k < 2" {
        shouldThrow<IllegalArgumentException> {
            GlaisherBijection.kRegularToBoundedMultiplicities(Partition.of(), 1)
        }
        shouldThrow<IllegalArgumentException> {
            GlaisherBijection.kRegularToBoundedMultiplicities(Partition.of(), 0)
        }
    }

    "boundedMultiplicitiesToKRegular rejects k < 2" {
        shouldThrow<IllegalArgumentException> {
            GlaisherBijection.boundedMultiplicitiesToKRegular(Partition.of(), 1)
        }
        shouldThrow<IllegalArgumentException> {
            GlaisherBijection.boundedMultiplicitiesToKRegular(Partition.of(), 0)
        }
    }

    // ---- domain checking ------------------------------------------------------

    "kRegularToBoundedMultiplicities rejects a partition with a part divisible by k" {
        shouldThrow<IllegalArgumentException> {
            GlaisherBijection.kRegularToBoundedMultiplicities(Partition.of(6, 2, 1), 3)
        }
    }

    "boundedMultiplicitiesToKRegular rejects a partition with a part occurring k or more times" {
        shouldThrow<IllegalArgumentException> {
            GlaisherBijection.boundedMultiplicitiesToKRegular(Partition.of(2, 2, 2), 3)
        }
    }

    // ---- lands in the right codomain, across several moduli --------------------

    "kRegularToBoundedMultiplicities always produces multiplicities bounded by k" {
        checkAll(GlaisherTestingCombinations.arbKAndKRegularPartition()) { (k, p) ->
            GlaisherBijection.kRegularToBoundedMultiplicities(p, k).hasMultiplicitiesLessThan(k) shouldBe true
        }
    }

    "boundedMultiplicitiesToKRegular always produces a k-regular partition" {
        checkAll(GlaisherTestingCombinations.arbKAndBoundedMultiplicityPartition()) { (k, q) ->
            GlaisherBijection.boundedMultiplicitiesToKRegular(q, k).hasNoPartDivisibleBy(k) shouldBe true
        }
    }

    // ---- weight preservation, across several moduli -----------------------------

    "kRegularToBoundedMultiplicities preserves weight" {
        checkAll(GlaisherTestingCombinations.arbKAndKRegularPartition()) { (k, p) ->
            GlaisherBijection.kRegularToBoundedMultiplicities(p, k).weight shouldBe p.weight
        }
    }

    "boundedMultiplicitiesToKRegular preserves weight" {
        checkAll(GlaisherTestingCombinations.arbKAndBoundedMultiplicityPartition()) { (k, q) ->
            GlaisherBijection.boundedMultiplicitiesToKRegular(q, k).weight shouldBe q.weight
        }
    }

    // ---- mutual inverses (round-trip), across several moduli --------------------

    "boundedMultiplicitiesToKRegular undoes kRegularToBoundedMultiplicities" {
        checkAll(GlaisherTestingCombinations.arbKAndKRegularPartition()) { (k, p) ->
            val roundTrip = GlaisherBijection.boundedMultiplicitiesToKRegular(
                GlaisherBijection.kRegularToBoundedMultiplicities(p, k),
                k
            )
            roundTrip shouldBe p
        }
    }

    "kRegularToBoundedMultiplicities undoes boundedMultiplicitiesToKRegular" {
        checkAll(GlaisherTestingCombinations.arbKAndBoundedMultiplicityPartition()) { (k, q) ->
            val roundTrip = GlaisherBijection.kRegularToBoundedMultiplicities(
                GlaisherBijection.boundedMultiplicitiesToKRegular(q, k),
                k
            )
            roundTrip shouldBe q
        }
    }

    // ---- the empty partition is a (trivial) fixed point, for any k --------------

    "the empty partition maps to itself in both directions, for any k" {
        checkAll(Arb.int(2..8)) { k ->
            val empty = Partition.of()
            GlaisherBijection.kRegularToBoundedMultiplicities(empty, k) shouldBe empty
            GlaisherBijection.boundedMultiplicitiesToKRegular(empty, k) shouldBe empty
        }
    }

    // ---- k-regular-and-bounded partitions are fixed points of both directions ---

    "partitions that are k-regular and k-bounded are fixed points of both directions" {
        checkAll(GlaisherTestingCombinations.arbKAndKRegularBoundedPartition()) { (k, p) ->
            GlaisherBijection.kRegularToBoundedMultiplicities(p, k) shouldBe p
            GlaisherBijection.boundedMultiplicitiesToKRegular(p, k) shouldBe p
        }
    }

    // ---- agrees with EulerGlaisherBijection at k = 2 ----------------------------

    "kRegularToBoundedMultiplicities at k = 2 agrees with EulerGlaisherBijection" {
        checkAll(ArbPartition.arbOdd()) { p ->
            GlaisherBijection.kRegularToBoundedMultiplicities(p, 2) shouldBe
                EulerGlaisherBijection.oddPartitionToDistinctPartition(p)
        }
    }

    "boundedMultiplicitiesToKRegular at k = 2 agrees with EulerGlaisherBijection" {
        checkAll(ArbPartition.arbDistinct()) { q ->
            GlaisherBijection.boundedMultiplicitiesToKRegular(q, 2) shouldBe
                EulerGlaisherBijection.distinctPartitionToOddPartition(q)
        }
    }

    // ---- fixed, hand-verified examples ------------------------------------------

    "k = 2, n = 15: {3,3,3,3,3} <-> {12,3} (multiplicity 5 = 0b101 spans two bits)" {
        val kRegular = Partition.of(3, 3, 3, 3, 3)
        val bounded = Partition.of(12, 3)
        GlaisherBijection.kRegularToBoundedMultiplicities(kRegular, 2) shouldBe bounded
        GlaisherBijection.boundedMultiplicitiesToKRegular(bounded, 2) shouldBe kRegular
    }

    "k = 2, mixed odd parts are mapped canonically" {
        val kRegular = Partition.of(5, 5, 5, 3, 3, 1)
        val bounded = Partition.of(10, 6, 5, 1)
        GlaisherBijection.kRegularToBoundedMultiplicities(kRegular, 2) shouldBe bounded
        GlaisherBijection.boundedMultiplicitiesToKRegular(bounded, 2) shouldBe kRegular
    }

    "k = 3, n = 20: {5,5,5,5} <-> {15,5} (multiplicity 4 = 12 in base 3 spans two digits)" {
        val kRegular = Partition.of(5, 5, 5, 5)
        val bounded = Partition.of(15, 5)
        GlaisherBijection.kRegularToBoundedMultiplicities(kRegular, 3) shouldBe bounded
        GlaisherBijection.boundedMultiplicitiesToKRegular(bounded, 3) shouldBe kRegular
    }

    "k = 3, mixed 3-regular parts are mapped canonically" {
        val kRegular = Partition.of(2, 2, 2, 2, 2, 1)
        val bounded = Partition.of(6, 2, 2, 1)
        GlaisherBijection.kRegularToBoundedMultiplicities(kRegular, 3) shouldBe bounded
        GlaisherBijection.boundedMultiplicitiesToKRegular(bounded, 3) shouldBe kRegular
    }
})
