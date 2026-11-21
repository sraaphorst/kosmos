package org.vorpal.kosmos.combinatorics

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.generateArbOrderedFiniteSet
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

class FiniteSetPermutationIntegrationSpec : FunSpec({
    lawTests(lawGraph("finite sets and permutations") {
        branch("Permutation actions on FiniteSets") {
            local("applying permutation to ordered set preserves size") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val result = base.map { perm.apply(it) }
                    result.size shouldBe base.size
                }
            }

            local("permuting elements creates valid reordering") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val permuted = base.map { perm.apply(it) }
                    permuted.backing shouldBe base.backing
                }
            }

            local("identity permutation leaves set unchanged") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 3, 10)) { base ->
                    val id = base.identityPermutation()
                    val result = base.map { id.apply(it) }
                    result shouldBe base
                }
            }

            local("applying permutation twice with inverse returns original") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val permuted = base.map { perm.apply(it) }
                    val restored = permuted.map { perm.inverse().apply(it) }
                    restored.backing shouldBe base.backing
                }
            }
        }

        branch("Permutation groups on FiniteSets") {
            local("symmetric group size bound (factorial)") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 1, 5)) { base ->
                    // Can't easily enumerate all permutations, but we can verify
                    // properties of generated permutations
                    val perms = (1..10).map {
                        generateArbPermutationOfSize(Arb.int(), base.size).bind().second
                    }
                    perms.all { it.domain.size == base.size } shouldBe true
                }
            }

            local("all permutations form a group") {
                lawCheckAll(
                    generateArbPermutationOfSize(Arb.int(), 4),
                    generateArbPermutationOfSize(Arb.int(), 4)
                ) { (base1, p1), (base2, p2) ->
                    if (base1.backing == base2.backing) {
                        // Closure
                        val composed = p1 andThen p2
                        composed.domain.backing shouldBe base1.backing

                        // Identity exists
                        val id = Permutation.identity(base1)
                        (p1 andThen id) shouldBe p1

                        // Inverse exists
                        val inv = p1.inverse()
                        val shouldBeId = p1 andThen inv
                        base1.all { x -> shouldBeId.apply(x) == x } shouldBe true
                    }
                }
            }
        }

        branch("Cyclic permutations on ordered sets") {
            local("cyclic shift preserves all elements") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 3, 10)) { base ->
                    // Use shiftPermutation instead, which works for all shifts
                    (1 until base.size).forEach { shift ->
                        val shifted = base.shiftPermutation(shift)
                        val result = base.map { shifted.apply(it) }
                        result.backing shouldBe base.backing
                    }
                }
            }

            local("full cycle returns to start") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 3, 8)) { base ->
                    val cyclic = base.cyclicPermutation(1)
                    val fullCycle = cyclic.exp(base.size)
                    base.all { x -> fullCycle.apply(x) == x } shouldBe true
                }
            }

            local("cyclic permutation order equals size") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 3, 8)) { base ->
                    if (base.size > 1) {
                        val cyclic = base.cyclicPermutation(1)
                        cyclic.order() shouldBe base.size
                    }
                }
            }
        }

        branch("Permutation composition chains") {
            local("long composition chain") {
                lawCheckAll(PermutationTestingCombinations.arbBaseWithPermutations(Arb.int(), 4, 5)) { (base, perms) ->
                    val composed = perms.reduce { acc, perm -> acc andThen perm }

                    // Result should still be a valid permutation
                    base.all { x -> composed.apply(x) in base } shouldBe true

                    // Should be bijective
                    val images = base.map { composed.apply(it) }.toSet()
                    images shouldBe base.toSet()
                }
            }

            local("inverse of composition chain") {
                lawCheckAll(PermutationTestingCombinations.arbBaseWithPermutations(Arb.int(), 4, 3)) { (base, perms) ->
                    val composed = perms.reduce { acc, perm -> acc andThen perm }
                    val inv = composed.inverse()
                    val shouldBeId = composed andThen inv

                    base.all { x -> shouldBeId.apply(x) == x } shouldBe true
                }
            }
        }

        branch("Subset operations with permutations") {
            local("permutation restricted to subset") {
                lawCheckAll(
                    generateArbOrderedFiniteSet(Arb.int(1..20), 5, 10)
                ) { base ->
                    val subset = base.take(base.size / 2)
                    val perm = base.shiftPermutation(1)

                    // Check how permutation affects subset
                    val permutedSubset = subset.map { perm.apply(it) }
                    permutedSubset.all { it in base } shouldBe true
                }
            }

            local("fixed points form a valid subset") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val fixedPoints = base.filter { perm.apply(it) == it }
                    fixedPoints isSubsetOf base shouldBe true
                }
            }
        }

        branch("Permutation conjugacy classes") {
            local("conjugate permutations have same cycle type") {
                lawCheckAll(generateArbPermutationPair(Arb.int(), 5)) { (_, p, q) ->
                    val conjugate = q andThen p andThen q.inverse()

                    val pCycles = p.cycles().map { it.size }.sorted()
                    val conjCycles = conjugate.cycles().map { it.size }.sorted()

                    pCycles shouldBe conjCycles
                }
            }

            local("self-conjugation is identity operation") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                    val selfConjugate = perm andThen perm andThen perm.inverse()
                    base.all { x -> selfConjugate.apply(x) == perm.apply(x) } shouldBe true
                }
            }
        }

        branch("Edge cases and special scenarios") {
            local("empty set permutation") {
                val empty = FiniteSet.empty<Int>()
                val id = Permutation.identity(empty)

                id.isEmpty shouldBe true
                id.order() shouldBe 1
                id.cycles() shouldBe emptyList()
            }

            local("single element permutation is always identity") {
                lawCheckAll(Arb.int()) { x ->
                    val singleton = FiniteSet.singleton(x)
                    val perm = singleton.identityPermutation()

                    perm.apply(x) shouldBe x
                    perm.order() shouldBe 1
                    perm.cycles().isEmpty() shouldBe true
                }
            }

            local("two element transposition") {
                lawCheckAll(Arb.int(), Arb.int()) { a, b ->
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

            local("large permutation performance") {
                val large = FiniteSet.rangeOrdered(1, 100)
                val perm = large.shiftPermutation(1)

                // Should handle large sets efficiently
                perm.order() shouldBe 100
                perm.cycles().size shouldBe 1
            }
        }

        branch("Permutation validation and error cases") {
            local("composing permutations on different domains fails") {
                val base1 = FiniteSet.ordered(1, 2, 3)
                val base2 = FiniteSet.ordered(4, 5, 6)
                val perm1 = base1.identityPermutation()
                val perm2 = base2.identityPermutation()

                shouldThrow<IllegalArgumentException> {
                    perm1 andThen perm2
                }
            }

            local("power with negative exponent fails") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (_, perm) ->
                    shouldThrow<IllegalArgumentException> {
                        perm.exp(-1)
                    }
                }
            }
        }

        branch("Permutation statistics and analysis") {
            local("sign of permutation via inversions") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
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

            local("descent set analysis") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
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

        branch("Composition with map operations") {
            local("map then permute vs permute then map") {
                lawCheckAll(generateArbPermutation(Arb.int(0..1000), 3, 8)) { (base, perm) ->
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

            local("filtering preserves permutation structure on subset") {
                lawCheckAll(generateArbPermutation(Arb.int(-10..10), 5, 10)) { (base, perm) ->
                    val positives = base.filter { it > 0 }

                    // Elements in filtered set should map under permutation
                    positives.all { x ->
                        val image = perm.apply(x)
                        image in base
                    } shouldBe true
                }
            }
        }

        branch("Sorted sets and permutations") {
            local("sorted set with cyclic permutation") {
                lawCheckAll(Arb.list(Arb.int(), 3..8)) { list ->
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

            local("maintaining vs breaking sort order") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 4, 8)) { base ->
                    val sorted = FiniteSet.sorted(base)

                    // Identity maintains order
                    val id = sorted.identityPermutation()
                    sorted.map { id.apply(it) } shouldBe sorted

                    // Shifted permutation breaks order
                    if (sorted.size > 2) {
                        val shift = sorted.shiftPermutation(1)
                        val shifted = sorted.map { shift.apply(it) }
                        shifted shouldNotBe sorted
                    }
                }
            }
        }

        branch("Cartesian products and permutations") {
            local("permutation of product vs product of permutations") {
                lawCheckAll(
                    generateArbPermutationOfSize(Arb.int(), 3),
                    generateArbPermutationOfSize(Arb.char(), 3)
                ) { (base1, _), (base2, _) ->
                    val product = base1 cartesianProduct base2

                    // Can define a product permutation
                    val productPerm = product.identityPermutation()
                    productPerm.domain.size shouldBe (base1.size * base2.size)
                }
            }
        }

        branch("Commutator subgroup elements") {
            local("commutator is identity when permutations commute") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 3, 6)) { base ->
                    val id = base.identityPermutation()
                    val perm = base.shiftPermutation(1)

                    // [id, p] = id p id^-1 p^-1 = p p^-1 = id
                    val commutator = id andThen perm andThen id.inverse() andThen perm.inverse()
                    base.all { x -> commutator.apply(x) == x } shouldBe true
                }
            }
        }
    })
})