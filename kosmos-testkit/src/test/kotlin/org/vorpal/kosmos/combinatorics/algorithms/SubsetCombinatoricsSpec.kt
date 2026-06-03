package org.vorpal.kosmos.combinatorics.algorithms

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.core.finiteset.FiniteSet

/**
 * Tests for subset combinatorics:
 *
 *  - [generateAllSubsets] (Ordered / Unordered)
 *  - [generateSubsetsInRange]
 *  - [generateAllKSubsets] (Ordered / Unordered)
 *  - [generateAllSubsetsGraded]
 *  - [generateKSubsetsInRange]
 *  - [powerSet] (Ordered / Unordered)
 *aaaaa
 * Since the tested domains are intentionally tiny and finite, these tests use
 * deterministic bounded loops instead of property-test sampling.
 */
class SubsetCombinatoricsSpec : FunSpec({

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Convenient small ordered set used throughout: {a, b, c, d}. */
    val abcd = FiniteSet.ordered('a', 'b', 'c', 'd')

    /** Build a canonical Ordered set {0, 1, ..., n-1}. */
    fun orderedRange(n: Int): FiniteSet.Ordered<Int> =
        FiniteSet.ordered((0 until n).toList())

    /** Build a canonical Unordered set {0, 1, ..., n-1}. */
    fun unorderedRange(n: Int): FiniteSet.Unordered<Int> =
        FiniteSet.unordered((0 until n).toSet())

    /** Encode a subset of {0, ..., n-1} as its bitmask. */
    fun FiniteSet.Ordered<Int>.toMask(): Int =
        fold(0) { acc, i -> acc or (1 shl i) }

    /** Encode a sequence of ordered subsets of {0, ..., n-1} as bitmasks. */
    fun Sequence<FiniteSet.Ordered<Int>>.toMaskList(): List<Int> =
        map { it.toMask() }.toList()

    /**
     * Assert that a list of masks contains every mask in [0, 2^n) exactly once.
     */
    fun assertCoversPowerSetExactlyOnce(
        n: Int,
        masks: List<Int>,
    ) {
        val expectedSize = 1 shl n
        val expected = (0 until expectedSize).toSet()
        masks shouldHaveSize expectedSize
        masks.toSet() shouldBe expected
    }

    /**
     * Full Gosper-compatible k-mask range for k-subsets of an n-element set.
     *
     * For k = 0, the only valid mask is 0.
     * For k > 0:
     *
     *   first = 00...0011...1
     *   last  = 11...1100...0
     */
    fun kMaskRange(
        n: Int,
        k: Int,
    ): Pair<Int, Int> {
        require(k in 0..n)
        if (k == 0) return 0 to 0
        val start = (1 shl k) - 1
        val end = start shl (n - k)
        return start to end
    }

    // ── generateAllSubsets (Ordered) ──────────────────────────────────────

    context("generateAllSubsets (Ordered)") {
        test("empty set produces exactly the empty subset") {
            val subsets = orderedRange(0)
                .generateAllSubsets()
                .toList()
            subsets shouldHaveSize 1
            subsets[0].toSet() shouldBe emptySet()
        }

        test("singleton produces {∅, {x}}") {
            val subsets = FiniteSet.ordered(7)
                .generateAllSubsets()
                .toList()

            subsets.map { it.toSet() } shouldContainExactlyInAnyOrder
                listOf(emptySet(), setOf(7),)
        }

        test("|S| = 4 produces all 16 subsets") {
            val subsets = abcd.generateAllSubsets()
                .map { it.toSet() }
                .toList()

            subsets shouldHaveSize 16

            subsets shouldContainExactlyInAnyOrder
                listOf(
                    emptySet(),
                    setOf('a'),
                    setOf('b'),
                    setOf('c'),
                    setOf('d'),
                    setOf('a', 'b'),
                    setOf('a', 'c'),
                    setOf('a', 'd'),
                    setOf('b', 'c'),
                    setOf('b', 'd'),
                    setOf('c', 'd'),
                    setOf('a', 'b', 'c'),
                    setOf('a', 'b', 'd'),
                    setOf('a', 'c', 'd'),
                    setOf('b', 'c', 'd'),
                    setOf('a', 'b', 'c', 'd'),
                )
        }

        test("count is 2^n") {
            for (n in 0..8) {
                orderedRange(n)
                    .generateAllSubsets()
                    .count() shouldBe (1 shl n)
            }
        }

        test("every subset is a subset of the original") {
            for (n in 0..7) {
                val s =
                    orderedRange(n)

                s.generateAllSubsets().forEach { sub ->
                    (sub isSubsetOf s) shouldBe true
                }
            }
        }

        test("covers the power set exactly once") {
            for (n in 0..7) {
                val masks = orderedRange(n)
                    .generateAllSubsets()
                    .toMaskList()
                assertCoversPowerSetExactlyOnce(n, masks)
            }
        }

        test("rejects sets of size > 30 on iteration") {
            val tooBig = FiniteSet.ordered((0..30).toList())
            shouldThrow<IllegalArgumentException> {
                tooBig.generateAllSubsets().first()
            }
        }
    }

    // ── generateAllSubsets (Unordered) ────────────────────────────────────

    context("generateAllSubsets (Unordered)") {

        test("count is 2^n") {
            for (n in 0..6)
                unorderedRange(n).generateAllSubsets().count() shouldBe (1 shl n)
        }

        test("produces the same subsets as the ordered version") {
            val unordered = FiniteSet.unordered('x', 'y', 'z')
            val ordered = FiniteSet.ordered('x', 'y', 'z')
            unordered.generateAllSubsets()
                .map { it.toSet() }
                .toList() shouldContainExactlyInAnyOrder
                ordered.generateAllSubsets()
                    .map { it.toSet() }
                    .toList()
        }
    }

    // ── generateSubsetsInRange ────────────────────────────────────────────

    context("generateSubsetsInRange") {

        test("range [0, 2^n - 1] enumerates all subsets") {
            for (n in 0..6) {
                val s = orderedRange(n)
                val all = s.generateAllSubsets()
                    .map { it.toSet() }
                    .toList()
                val ranged = s.generateSubsetsInRange(0, (1 shl n) - 1)
                    .map { it.toSet() }
                    .toList()
                ranged shouldContainExactly all
            }
        }

        test("disjoint mask partitions cover everything exactly once") {
            for (n in 2..6) {
                val s = orderedRange(n)
                val maxMask = (1 shl n) - 1
                val mid = maxMask / 2
                val firstHalf = s.generateSubsetsInRange(0, mid)
                    .map { it.toSet() }
                    .toList()
                val secondHalf = s.generateSubsetsInRange(mid + 1, maxMask)
                    .map { it.toSet() }
                    .toList()
                val all = s.generateAllSubsets()
                    .map { it.toSet() }
                    .toList()
                (firstHalf + secondHalf) shouldContainExactly all
            }
        }

        test("singleton range [m, m] yields exactly one subset") {
            val s = orderedRange(4)
            for (mask in 0..15)
                s.generateSubsetsInRange(mask, mask)
                    .toList() shouldHaveSize 1
        }

        test("rejects negative maskStart") {
            shouldThrow<IllegalArgumentException> {
                abcd.generateSubsetsInRange(-1, 5)
            }
        }

        test("rejects maskStart > maskEndInclusive") {
            shouldThrow<IllegalArgumentException> {
                abcd.generateSubsetsInRange(5, 3)
            }
        }

        test("rejects maskEndInclusive >= 2^n") {
            shouldThrow<IllegalArgumentException> {
                abcd.generateSubsetsInRange(0, 16)
            }
        }

        test("rejects size > 30") {
            val tooBig = FiniteSet.ordered((0..30).toList())
            shouldThrow<IllegalArgumentException> {
                tooBig.generateSubsetsInRange(0, 0)
            }
        }
    }

    // ── generateAllKSubsets (Ordered) ─────────────────────────────────────

    context("generateAllKSubsets (Ordered)") {

        test("k = 0 returns the singleton {∅}") {
            val subs = abcd.generateAllKSubsets(0).toList()
            subs shouldHaveSize 1
            subs[0].toSet() shouldBe emptySet()
        }

        test("k = n returns the singleton {S}") {
            val subs = abcd.generateAllKSubsets(abcd.size).toList()
            subs shouldHaveSize 1
            subs[0].toSet() shouldBe abcd.toSet()
        }

        test("k = 2 over {a, b, c, d} gives all six 2-element subsets") {
            abcd.generateAllKSubsets(2)
                .map { it.toSet() }
                .toList() shouldContainExactlyInAnyOrder
                listOf(
                    setOf('a', 'b'),
                    setOf('a', 'c'),
                    setOf('a', 'd'),
                    setOf('b', 'c'),
                    setOf('b', 'd'),
                    setOf('c', 'd'),
                )
        }

        test("count is C(n, k) for all k") {
            for (n in 0..7) {
                val s = orderedRange(n)
                for (k in 0..n) {
                    s.generateAllKSubsets(k)
                        .count()
                        .toBigInteger() shouldBe Binomial(n, k)
                }
            }
        }

        test("every k-subset has exactly k elements") {
            for (n in 0..7) {
                val s = orderedRange(n)
                for (k in 0..n)
                    s.generateAllKSubsets(k).forEach { sub ->
                        sub.size shouldBe k
                    }
            }
        }

        test("every k-subset is a subset of the original") {
            for (n in 0..7) {
                val s = orderedRange(n)
                for (k in 0..n)
                    s.generateAllKSubsets(k).forEach { sub ->
                        (sub isSubsetOf s) shouldBe true
                }
            }
        }

        test("k-subsets are pairwise distinct") {
            for (n in 0..7) {
                val s = orderedRange(n)
                for (k in 0..n) {
                    val masks = s.generateAllKSubsets(k).toMaskList()
                    masks.toSet() shouldHaveSize masks.size
                }
            }
        }

        test("Σ_k C(n, k) = 2^n") {
            for (n in 0..8) {
                val s = orderedRange(n)
                val total = (0..n).sumOf { k ->
                    s.generateAllKSubsets(k).count()
                }
                total shouldBe (1 shl n)
            }
        }

        test("rejects k < 0") {
            shouldThrow<IllegalArgumentException> {
                abcd.generateAllKSubsets(-1)
            }
        }

        test("rejects k > n") {
            shouldThrow<IllegalArgumentException> {
                abcd.generateAllKSubsets(abcd.size + 1)
            }
        }

        test("rejects size > 30") {
            val tooBig =
                FiniteSet.ordered((0..30).toList())
            shouldThrow<IllegalArgumentException> {
                tooBig.generateAllKSubsets(2)
            }
        }
    }

    context("generateAllKSubsets (Unordered)") {

        test("matches the ordered version up to ordering") {
            val unordered =
                FiniteSet.unordered('x', 'y', 'z')

            val ordered =
                FiniteSet.ordered('x', 'y', 'z')

            for (k in 0..3) {
                unordered.generateAllKSubsets(k)
                    .map { it.toSet() }
                    .toList() shouldContainExactlyInAnyOrder
                    ordered.generateAllKSubsets(k)
                        .map { it.toSet() }
                        .toList()
            }
        }

        test("count is C(n, k)") {
            for (n in 0..6) {
                val s = unorderedRange(n)
                for (k in 0..n) {
                    s.generateAllKSubsets(k)
                        .count()
                        .toBigInteger() shouldBe Binomial(n, k)
                }
            }
        }
    }


    context("generateAllSubsetsGraded") {

        test("count is 2^n") {
            for (n in 0..7) {
                orderedRange(n)
                    .generateAllSubsetsGraded()
                    .count() shouldBe (1 shl n)
            }
        }

        test("subsets appear in non-decreasing size order") {
            for (n in 0..6) {
                val sizes =
                    orderedRange(n)
                        .generateAllSubsetsGraded()
                        .map { it.size }
                        .toList()

                sizes shouldBe sizes.sorted()
            }
        }

        test("first subset is empty, last is the full set when |S| > 0") {
            for (n in 1..6) {
                val s =
                    orderedRange(n)

                val subs =
                    s.generateAllSubsetsGraded()
                        .toList()

                subs.first().toSet() shouldBe emptySet()
                subs.last().toSet() shouldBe s.toSet()
            }
        }

        test("graded subset generation covers the power set exactly once") {
            for (n in 0..6) {
                val masks =
                    orderedRange(n)
                        .generateAllSubsetsGraded()
                        .toMaskList()

                assertCoversPowerSetExactlyOnce(n, masks)
            }
        }
    }

    // ── generateKSubsetsInRange ───────────────────────────────────────────

    context("generateKSubsetsInRange") {

        test("every yielded subset has size k") {
            val s =
                orderedRange(5)

            val k =
                3

            val (maskStart, maskEnd) =
                kMaskRange(s.size, k)

            s.generateKSubsetsInRange(k, maskStart, maskEnd).forEach { sub ->
                sub.size shouldBe k
            }
        }

        test("over the full k-mask range, count is C(n, k)") {
            for (n in 0..6) {
                val s = orderedRange(n)
                for (k in 0..n) {
                    val (maskStart, maskEnd) = kMaskRange(n, k)
                    s.generateKSubsetsInRange(k, maskStart, maskEnd)
                        .count()
                        .toBigInteger() shouldBe Binomial(n, k)
                }
            }
        }

        test("rejects invalid mask range") {
            shouldThrow<IllegalArgumentException> {
                abcd.generateKSubsetsInRange(2, -1, 5)
            }

            shouldThrow<IllegalArgumentException> {
                abcd.generateKSubsetsInRange(2, 5, 3)
            }

            shouldThrow<IllegalArgumentException> {
                abcd.generateKSubsetsInRange(2, 0, 16)
            }
        }

        test("rejects maskStart whose popcount is not k") {
            shouldThrow<IllegalArgumentException> {
                abcd.generateKSubsetsInRange(2, 0, 3)
            }
        }

        test("rejects maskEndInclusive whose popcount is not k") {
            shouldThrow<IllegalArgumentException> {
                abcd.generateKSubsetsInRange(2, 3, 15)
            }
        }
    }

    // ── powerSet (Ordered) ────────────────────────────────────────────────

    context("powerSet (Ordered)") {

        test("size is 2^n") {
            for (n in 0..6) {
                orderedRange(n).powerSet()
                    .size shouldBe (1 shl n)
            }
        }

        test("equals generateAllSubsetsGraded as a sequence") {
            for (n in 0..6) {
                val s = orderedRange(n)
                val power = s.powerSet()
                    .toList()
                    .map { it.toSet() }
                val graded = s.generateAllSubsetsGraded()
                    .map { it.toSet() }
                    .toList()
                power shouldContainExactly graded
            }
        }

        test("contains the empty subset and the full subset") {
            for (n in 1..6) {
                val s = orderedRange(n)
                val asSets = s.powerSet()
                    .toList()
                    .map { it.toSet() }
                asSets shouldHaveSize (1 shl n)
                (emptySet<Int>() in asSets) shouldBe true
                (s.toSet() in asSets) shouldBe true
            }
        }

        test("rejects sets of size > 20") {
            shouldThrow<IllegalArgumentException> {
                FiniteSet.ordered((0..20).toList()).powerSet()
            }
        }
    }

    // ── powerSet (Unordered) ──────────────────────────────────────────────

    context("powerSet (Unordered)") {

        test("size is 2^n") {
            for (n in 0..6) {
                unorderedRange(n)
                    .powerSet()
                    .size shouldBe (1 shl n)
            }
        }

        test("contains the same subsets as the ordered version") {
            for (n in 0..5) {
                val unordered = unorderedRange(n)
                val ordered = orderedRange(n)
                unordered.powerSet()
                    .toList()
                    .map { it.toSet() } shouldContainExactlyInAnyOrder
                    ordered.powerSet()
                        .toList()
                        .map { it.toSet() }
            }
        }

        test("rejects sets of size > 20") {
            shouldThrow<IllegalArgumentException> {
                FiniteSet.unordered((0..20).toSet()).powerSet()
            }
        }
    }
})
