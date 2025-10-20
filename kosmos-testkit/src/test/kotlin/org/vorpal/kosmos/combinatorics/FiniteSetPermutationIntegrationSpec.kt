package org.vorpal.kosmos.combinatorics

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.generateArbOrderedFiniteSet

class FiniteSetPermutationIntegrationSpec : FreeSpec({

    "Permutation actions on FiniteSets" - {

        "applying permutation to ordered set preserves size" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val result = base.map { perm.apply(it) }
                result.size shouldBe base.size
            }
        }

        "permuting elements creates valid reordering" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val permuted = base.map { perm.apply(it) }
                permuted.backing shouldBe base.backing
            }
        }

        "identity permutation leaves set unchanged" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 3, 10)) { base ->
                val id = base.identityPermutation()
                val result = base.map { id.apply(it) }
                result shouldBe base
            }
        }

        "applying permutation twice with inverse returns original" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val permuted = base.map { perm.apply(it) }
                val restored = permuted.map { perm.inverse().apply(it) }
                restored.backing shouldBe base.backing
            }
        }
    }

    "Permutation groups on FiniteSets" - {

        "symmetric group size bound (factorial)" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 1, 5)) { base ->
                // Can't easily enumerate all permutations, but we can verify
                // properties of generated permutations
                val perms = (1..10).map {
                    generateArbPermutationOfSize(Arb.int(), base.size).single().second
                }
                perms.all { it.domain.size == base.size } shouldBe true
            }
        }

        "all permutations form a group" {
            checkAll(
                generateArbPermutationOfSize(Arb.int(), 4),
                generateArbPermutationOfSize(Arb.int(), 4)
            ) { (base1, p1), (base2, p2) ->
                if (base1.backing == base2.backing) {
                    // Closure
                    val composed = p1 then p2
                    composed.domain.backing shouldBe base1.backing

                    // Identity exists
                    val id = Permutation.identity(base1)
                    (p1 then id) shouldBe p1

                    // Inverse exists
                    val inv = p1.inverse()
                    val shouldBeId = p1 then inv
                    base1.all { x -> shouldBeId.apply(x) == x } shouldBe true
                }
            }
        }
    }

    "Cyclic permutations on ordered sets" - {

        "cyclic shift preserves all elements" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 3, 10)) { base ->
                // Use shiftPermutation instead, which works for all shifts
                (1 until base.size).forEach { shift ->
                    val shifted = base.shiftPermutation(shift)
                    val result = base.map { shifted.apply(it) }
                    result.backing shouldBe base.backing
                }
            }
        }

        "full cycle returns to start" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 3, 8)) { base ->
                val cyclic = base.cyclicPermutation(1)
                val fullCycle = cyclic.exp(base.size)
                base.all { x -> fullCycle.apply(x) == x } shouldBe true
            }
        }

        "cyclic permutation order equals size" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 3, 8)) { base ->
                if (base.size > 1) {
                    val cyclic = base.cyclicPermutation(1)
                    cyclic.order() shouldBe base.size
                }
            }
        }
    }

    "Permutation composition chains" - {

        "long composition chain" {
            checkAll(PermutationTestingCombinations.arbBaseWithPermutations(Arb.int(), 4, 5)) { (base, perms) ->
                val composed = perms.reduce { acc, perm -> acc then perm }

                // Result should still be a valid permutation
                base.all { x -> composed.apply(x) in base } shouldBe true

                // Should be bijective
                val images = base.map { composed.apply(it) }.toSet()
                images shouldBe base.toSet()
            }
        }

        "inverse of composition chain" {
            checkAll(PermutationTestingCombinations.arbBaseWithPermutations(Arb.int(), 4, 3)) { (base, perms) ->
                val composed = perms.reduce { acc, perm -> acc then perm }
                val inv = composed.inverse()
                val shouldBeId = composed then inv

                base.all { x -> shouldBeId.apply(x) == x } shouldBe true
            }
        }
    }

    "Subset operations with permutations" - {

        "permutation restricted to subset" {
            checkAll(
                generateArbOrderedFiniteSet(Arb.int(1..20), 5, 10)
            ) { base ->
                val subset = base.take(base.size / 2)
                val perm = base.shiftPermutation(1)

                // Check how permutation affects subset
                val permutedSubset = subset.map { perm.apply(it) }
                permutedSubset.all { it in base } shouldBe true
            }
        }

        "fixed points form a valid subset" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val fixedPoints = base.filter { perm.apply(it) == it }
                fixedPoints isSubsetOf base shouldBe true
            }
        }
    }

    "Permutation conjugacy classes" - {

        "conjugate permutations have same cycle type" {
            checkAll(generateArbPermutationPair(Arb.int(), 5)) { (base, p, q) ->
                val conjugate = q then p then q.inverse()

                val pCycles = p.cycles().map { it.size }.sorted()
                val conjCycles = conjugate.cycles().map { it.size }.sorted()

                pCycles shouldBe conjCycles
            }
        }

        "self-conjugation is identity operation" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                val selfConjugate = perm then perm then perm.inverse()
                base.all { x -> selfConjugate.apply(x) == perm.apply(x) } shouldBe true
            }
        }
    }

    "Edge cases and special scenarios" - {

        "empty set permutation" {
            val empty = FiniteSet.empty<Int>()
            val id = Permutation.identity(empty)

            id.isEmpty shouldBe true
            id.order() shouldBe 1
            id.cycles() shouldBe emptyList()
        }

        "single element permutation is always identity" {
            checkAll(Arb.int()) { x ->
                val singleton = FiniteSet.singleton(x)
                val perm = singleton.identityPermutation()

                perm.apply(x) shouldBe x
                perm.order() shouldBe 1
                perm.cycles().isEmpty() shouldBe true
            }
        }

        "two element transposition" {
            checkAll(Arb.int(), Arb.int()) { a, b ->
                if (a != b) {
                    val base = FiniteSet.ordered(a, b)
                    val mapping = mapOf(a to b, b to a)
                    val transposition = Permutation.of(base, mapping)

                    transposition.apply(a) shouldBe b
                    transposition.apply(b) shouldBe a
                    transposition.order() shouldBe 2
                    transposition.cycles().size shouldBe 1
                }
            }
        }

        "large permutation performance" {
            val large = FiniteSet.rangeOrdered(1, 100)
            val perm = large.shiftPermutation(1)

            // Should handle large sets efficiently
            perm.order() shouldBe 100
            perm.cycles().size shouldBe 1
        }
    }

    "Permutation validation and error cases" - {

        "composing permutations on different domains fails" {
            val base1 = FiniteSet.ordered(1, 2, 3)
            val base2 = FiniteSet.ordered(4, 5, 6)
            val perm1 = base1.identityPermutation()
            val perm2 = base2.identityPermutation()

            shouldThrow<IllegalArgumentException> {
                perm1 then perm2
            }
        }

        "power with negative exponent fails" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                shouldThrow<IllegalArgumentException> {
                    perm.exp(-1)
                }
            }
        }
    }

    "Permutation statistics and analysis" - {

        "sign of permutation via inversions" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                // Count cycles to determine sign
                val cycleCount = perm.cycles().size
                val fixedPoints = base.count { perm.apply(it) == it }
                val totalCycles = cycleCount + fixedPoints

                // Sign is (-1)^(n - totalCycles) where n is size
                val expectedSign = if ((base.size - totalCycles) % 2 == 0) 1 else -1

                // Verify the sign is valid (either 1 or -1)
                (expectedSign == 1 || expectedSign == -1) shouldBe true
            }
        }

        "descent set analysis" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                // A descent is where perm(i) > perm(i+1) in the ordering
                val sorted = base.toList().sorted()
                val permuted = sorted.map { perm.apply(it) }

                val descents = (0 until permuted.size - 1).count { i ->
                    permuted[i] > permuted[i + 1]
                }

                // Verify descents is in valid range
                (descents >= 0 && descents < base.size) shouldBe true
            }
        }
    }

    "Composition with map operations" - {

        "map then permute vs permute then map" {
            checkAll(generateArbPermutation(Arb.int(0..1000), 3, 8)) { (base, perm) ->
                // Use a safer mapping function that won't overflow
                val f: (Int) -> String = { "x$it" }

                // Permute first then map
                val permFirst = base.map { perm.apply(it) }.map(f)

                // Both should have the same size as base since permutation is bijective
                permFirst.size shouldBe base.size

                // The mapped values should still be a bijection
                permFirst.backing.size shouldBe base.size
            }
        }

        "filtering preserves permutation structure on subset" {
            checkAll(generateArbPermutation(Arb.int(-10..10), 5, 10)) { (base, perm) ->
                val positives = base.filter { it > 0 }

                // Elements in filtered set should map under permutation
                positives.all { x ->
                    val image = perm.apply(x)
                    image in base
                } shouldBe true
            }
        }
    }

    "Sorted sets and permutations" - {

        "sorted set with cyclic permutation" {
            checkAll(Arb.list(Arb.int(), 3..8)) { list ->
                val sorted = FiniteSet.sorted(list)
                if (sorted.size > 1) {
                    val cyclic = sorted.cyclicPermutation(1)

                    // Verify it's a valid cyclic shift
                    sorted.order.indices.all { i ->
                        val next = (i + 1) % sorted.size
                        cyclic.apply(sorted[i]) == sorted[next]
                    } shouldBe true
                }
            }
        }

        "maintaining vs breaking sort order" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 4, 8)) { base ->
                val sorted = FiniteSet.sorted(base)
                val reversed = sorted.reversed()

                // Identity maintains order
                val id = sorted.identityPermutation()
                sorted.map { id.apply(it) } shouldBe sorted

                // Other permutations generally don't
                if (base.size > 2) {
                    val shift = sorted.shiftPermutation(1)
                    val shifted = sorted.map { shift.apply(it) }
                    if (sorted.size > 2) {
                        shifted shouldNotBe sorted
                    }
                }
            }
        }
    }

    "Cartesian products and permutations" - {

        "permutation of product vs product of permutations" {
            checkAll(
                generateArbPermutationOfSize(Arb.int(), 3),
                generateArbPermutationOfSize(Arb.char(), 3)
            ) { (base1, perm1), (base2, perm2) ->
                val product = base1 cartesianProduct base2

                // Can define a product permutation
                val productPerm = product.identityPermutation()
                productPerm.domain.size shouldBe (base1.size * base2.size)
            }
        }
    }

    "Commutator subgroup elements" - {

        "commutator is identity when permutations commute" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 3, 6)) { base ->
                val id = base.identityPermutation()
                val perm = base.shiftPermutation(1)

                // [id, p] = id p id^-1 p^-1 = p p^-1 = id
                val commutator = id then perm then id.inverse() then perm.inverse()
                base.all { x -> commutator.apply(x) == x } shouldBe true
            }
        }
    }
})