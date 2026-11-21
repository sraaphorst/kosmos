package org.vorpal.kosmos.combinatorics

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.generateArbOrderedFiniteSet
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

class PermutationSpec : FunSpec({
    lawTests(lawGraph("permutations") {
        branch("Permutation construction") {
            local("of creates valid permutation from total bijective mapping") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                    base.all { x -> perm.apply(x) in base } shouldBe true
                }
            }

            local("identity permutation maps everything to itself") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                    val id = Permutation.identity(base)
                    base.all { x -> id.apply(x) == x } shouldBe true
                }
            }

            local("identity permutation is its own inverse") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                    val id = Permutation.identity(base)
                    id.inverse() shouldBe id
                }
            }

            local("constructed permutation preserves bijectivity") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val images = base.map { perm.apply(it) }.toSet()
                    images shouldBe base.toSet()
                }
            }
        }

        branch("Permutation application") {
            local("apply and get operator are equivalent") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    base.all { x -> perm.apply(x) == perm[x] } shouldBe true
                }
            }

            local("forward then backward is identity") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    base.all { x -> perm.backward.apply(perm.apply(x)) == x } shouldBe true
                }
            }

            local("backward then forward is identity") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    base.all { x -> perm.apply(perm.backward.apply(x)) == x } shouldBe true
                }
            }
        }

        branch("Permutation composition") {
            local("composition preserves domain") {
                lawCheckAll(generateArbPermutationPair(Arb.int(), 5)) { (base, p1, p2) ->
                    val composed = p1 andThen p2
                    composed.domain shouldBe base
                }
            }

            local("composition is associative") {
                lawCheckAll(PermutationTestingCombinations.arbBaseWithPermutations(Arb.int(), 5, 3)) { (base, perms) ->
                    val (p1, p2, p3) = perms
                    val left = (p1 andThen p2) andThen p3
                    val right = p1 andThen (p2 andThen p3)

                    base.all { x -> left.apply(x) == right.apply(x) } shouldBe true
                }
            }

            local("identity is left identity for composition") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                    val id = Permutation.identity(base)
                    val composed = id andThen perm
                    base.all { x -> composed.apply(x) == perm.apply(x) } shouldBe true
                }
            }

            local("identity is right identity for composition") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                    val id = Permutation.identity(base)
                    val composed = perm andThen id
                    base.all { x -> composed.apply(x) == perm.apply(x) } shouldBe true
                }
            }

            local("composition with inverse gives identity") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val composed = perm andThen perm.inverse()
                    base.all { x -> composed.apply(x) == x } shouldBe true
                }
            }

            local("inverse then original gives identity") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val composed = perm.inverse() andThen perm
                    base.all { x -> composed.apply(x) == x } shouldBe true
                }
            }
        }

        branch("Permutation inverse") {
            local("inverse swaps forward and backward") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
                    val inv = perm.inverse()
                    val domain = perm.domain.toList()

                    // forward of inverse == backward of original
                    domain.forEach { a ->
                        inv.apply(a) shouldBe perm.applyInverse(a)
                    }

                    // backward of inverse == forward of original
                    domain.forEach { a ->
                        inv.applyInverse(a) shouldBe perm.apply(a)
                    }
                }
            }

            local("inverse twice is identity") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
                    perm.inverse().inverse() shouldBe perm
                }
            }

            local("inverse of identity is identity") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                    val id = Permutation.identity(base)
                    id.inverse() shouldBe id
                }
            }

            local("composition inverse formula: (fg)^-1 = g^-1 f^-1") {
                lawCheckAll(generateArbPermutationPair(Arb.int(), 5)) { (base, p1, p2) ->
                    val composed = p1 andThen p2
                    val invComposed = composed.inverse()
                    val invFormula = p2.inverse() andThen p1.inverse()

                    base.all { x -> invComposed.apply(x) == invFormula.apply(x) } shouldBe true
                }
            }
        }

        branch("Permutation cycles") {
            local("identity has no non-trivial cycles") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                    val id = Permutation.identity(base)
                    id.cycles().isEmpty() shouldBe true
                }
            }

            local("cycles partition the domain") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val cycles = perm.cycles()
                    val cycleElements = cycles.flatten().toSet()
                    val fixedPoints = base.filter { perm.apply(it) == it }.toSet()
                    (cycleElements + fixedPoints) shouldBe base.toSet()
                }
            }

            local("cycles are disjoint") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
                    val cycles = perm.cycles()
                    for (i in cycles.indices) {
                        for (j in i + 1 until cycles.size) {
                            val cycle1 = cycles[i].toSet()
                            val cycle2 = cycles[j].toSet()
                            (cycle1 intersect cycle2).isEmpty() shouldBe true
                        }
                    }
                }
            }

            local("cyclic permutation has one cycle") {
                lawCheckAll(generateArbCyclicPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                    val cycles = perm.cycles()
                    cycles.size shouldBe 1
                    cycles.first().toSet() shouldBe base.toSet()
                }
            }

            local("each cycle element maps to next in cycle") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
                    val cycles = perm.cycles()
                    cycles.all { cycle ->
                        cycle.indices.all { i ->
                            val next = (i + 1) % cycle.size
                            perm.apply(cycle[i]) == cycle[next]
                        }
                    } shouldBe true
                }
            }
        }

        branch("Permutation order") {
            local("identity has order 1") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                    val id = Permutation.identity(base)
                    id.order() shouldBe 1
                }
            }

            local("cyclic permutation order equals cycle length") {
                lawCheckAll(generateArbCyclicPermutation(Arb.int(), 2, 8)) { (base, perm) ->
                    perm.order() shouldBe base.size
                }
            }

            local("order is positive") {
                lawCheckAll(generateArbPermutation(Arb.int(), 2, 10)) { (_, perm) ->
                    perm.order() shouldBeGreaterThan 0
                }
            }

            local("permutation to the power of its order is identity") {
                lawCheckAll(generateArbPermutation(Arb.int(), 2, 8)) { (base, perm) ->
                    val order = perm.order()
                    val powerN = perm.exp(order)
                    base.all { x -> powerN.apply(x) == x } shouldBe true
                }
            }

            local("order divides any power that gives identity") {
                lawCheckAll(generateArbPermutation(Arb.int(), 2, 6)) { (base, perm) ->
                    val order = perm.order()
                    val multiple = order * 2
                    val powerMultiple = perm.exp(multiple)
                    base.all { x -> powerMultiple.apply(x) == x } shouldBe true
                }
            }
        }

        branch("Permutation powers") {
            local("power 0 is identity") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                    val power0 = perm.exp(0)
                    base.all { x -> power0.apply(x) == x } shouldBe true
                }
            }

            local("power 1 is self") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                    val power1 = perm.exp(1)
                    base.all { x -> power1.apply(x) == perm.apply(x) } shouldBe true
                }
            }

            local("power 2 is composition with self") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                    val power2 = perm.exp(2)
                    val composed = perm andThen perm
                    base.all { x -> power2.apply(x) == composed.apply(x) } shouldBe true
                }
            }

            local("power law: p^(m+n) = p^m . p^n") {
                lawCheckAll(generateArbPermutation(Arb.int(), 2, 6)) { (base, perm) ->
                    val m = 2
                    val n = 3
                    val powerSum = perm.exp(m + n)
                    val powerProduct = perm.exp(m) andThen perm.exp(n)
                    base.all { x -> powerSum.apply(x) == powerProduct.apply(x) } shouldBe true
                }
            }

            local("power law: (p^m)^n = p^(m*n)") {
                lawCheckAll(generateArbPermutation(Arb.int(), 2, 5)) { (base, perm) ->
                    val m = 2
                    val n = 2
                    val powerOfPower = perm.exp(m).exp(n)
                    val powerProduct = perm.exp(m * n)
                    base.all { x -> powerOfPower.apply(x) == powerProduct.apply(x) } shouldBe true
                }
            }
        }

        branch("FiniteSet extension functions") {
            local("identityPermutation is identity") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                    val id = base.identityPermutation()
                    base.all { x -> id.apply(x) == x } shouldBe true
                }
            }

            local("cyclicPermutation creates valid cyclic permutation") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 3, 8)) { base ->
                    if (base.size > 1) {
                        val cyclic = base.cyclicPermutation(1)
                        cyclic.cycles().size shouldBe 1
                    }
                }
            }

            local("cyclicPermutation with shift 0 is identity") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 3, 8)) { base ->
                    val cyclic = base.cyclicPermutation(0)
                    base.all { x -> cyclic.apply(x) == x } shouldBe true
                }
            }

            local("shiftPermutation shifts by k positions") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 5, 8)) { base ->
                    val shift = 2
                    val shifted = base.shiftPermutation(shift)
                    base.order.indices.all { i ->
                        val expected = base.order[(i + shift) % base.size]
                        shifted.apply(base.order[i]) == expected
                    } shouldBe true
                }
            }

            local("shift by size is identity") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 3, 8)) { base ->
                    val shifted = base.shiftPermutation(base.size)
                    base.all { x -> shifted.apply(x) == x } shouldBe true
                }
            }
        }

        branch("Permutation group properties") {
            local("closure: composition of two permutations is a permutation") {
                lawCheckAll(generateArbPermutationPair(Arb.int(), 5)) { (base, p1, p2) ->
                    val composed = p1 andThen p2
                    base.all { x -> composed.apply(x) in base } shouldBe true
                }
            }

            local("every permutation has an inverse") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val inv = perm.inverse()
                    val composed = perm andThen inv
                    base.all { x -> composed.apply(x) == x } shouldBe true
                }
            }

            local("conjugation preserves order") {
                lawCheckAll(generateArbPermutationPair(Arb.int(), 5)) { (_, p, q) ->
                    val conjugate = q andThen p andThen q.inverse()
                    conjugate.order() shouldBe p.order()
                }
            }

            local("conjugation preserves cycle type") {
                lawCheckAll(generateArbPermutationPair(Arb.int(), 5)) { (_, p, q) ->
                    val conjugate = q andThen p andThen q.inverse()

                    val pCycles = p.cycles().map { it.size }.sorted()
                    val conjCycles = conjugate.cycles().map { it.size }.sorted()

                    pCycles shouldBe conjCycles
                }
            }

            local("commutator of identity is identity") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                    val id = Permutation.identity(base)
                    val commutator = perm andThen id andThen perm.inverse() andThen id.inverse()
                    base.all { x -> commutator.apply(x) == x } shouldBe true
                }
            }

            local("commutator with self is identity") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                    val commutator = perm andThen perm andThen perm.inverse() andThen perm.inverse()
                    base.all { x -> commutator.apply(x) == x } shouldBe true
                }
            }
        }

        branch("Permutation sign and parity") {
            local("identity has positive sign") {
                lawCheckAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                    val id = Permutation.identity(base)
                    id.sign() shouldBe 1
                }
            }

            local("transposition has negative sign") {
                lawCheckAll(Arb.int(), Arb.int()) { a, b ->
                    if (a != b) {
                        val base = FiniteSet.ordered(a, b)
                        val mapping = mapOf(a to b, b to a)
                        val transposition = Permutation.of(base, mapping)
                        transposition.sign() shouldBe -1
                    }
                }
            }

            local("sign is multiplicative: sign(p1 * p2) = sign(p1) * sign(p2)") {
                lawCheckAll(generateArbPermutationPair(Arb.int(), 5)) { (_, p1, p2) ->
                    val composed = p1 andThen p2
                    composed.sign() shouldBe (p1.sign() * p2.sign())
                }
            }

            local("inverse has same sign: sign(p^-1) = sign(p)") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
                    perm.inverse().sign() shouldBe perm.sign()
                }
            }

            local("sign is either 1 or -1") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
                    val sign = perm.sign()
                    (sign == 1 || sign == -1) shouldBe true
                }
            }

            local("even length cycle has odd sign") {
                lawCheckAll(generateArbCyclicPermutation(Arb.int(), 2, 8)) { (base, perm) ->
                    if (base.size % 2 == 0) {
                        perm.sign() shouldBe -1
                    } else {
                        perm.sign() shouldBe 1
                    }
                }
            }
        }

        branch("Permutation statistics") {
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

            local("inversion count is well-defined") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                    val sorted = base.toList().sorted()
                    val permuted = sorted.map { perm.apply(it) }

                    // Count inversions: pairs (i,j) where i < j but permuted[i] > permuted[j]
                    var inversions = 0
                    for (i in permuted.indices) {
                        for (j in i + 1 until permuted.size) {
                            if (permuted[i] > permuted[j]) {
                                inversions++
                            }
                        }
                    }

                    // Verify inversions is non-negative and bounded
                    (inversions >= 0 && inversions <= (base.size * (base.size - 1)) / 2) shouldBe true
                }
            }
        }

        branch("Permutation isEmpty/isNotEmpty") {
            local("isEmpty is true for empty domain") {
                val empty = FiniteSet.empty<Int>()
                val id = Permutation.identity(empty)
                id.isEmpty shouldBe true
                id.isNotEmpty shouldBe false
            }

            local("isEmpty is false for non-empty domain") {
                lawCheckAll(generateArbPermutation(Arb.int(), 1, 10)) { (_, perm) ->
                    perm.isEmpty shouldBe false
                    perm.isNotEmpty shouldBe true
                }
            }
        }

        branch("Identity permutation properties") {
            local("generateArbIdentityPermutation creates identity") {
                lawCheckAll(generateArbIdentityPermutation(Arb.int(), 1, 10)) { (base, perm) ->
                    base.all { x -> perm.apply(x) == x } shouldBe true
                }
            }

            local("identity has order 1") {
                lawCheckAll(generateArbIdentityPermutation(Arb.int(), 1, 10)) { (_, perm) ->
                    perm.order() shouldBe 1
                }
            }

            local("identity has no cycles") {
                lawCheckAll(generateArbIdentityPermutation(Arb.int(), 1, 10)) { (_, perm) ->
                    perm.cycles().isEmpty() shouldBe true
                }
            }
        }

        branch("Bijection properties") {
            local("permutation is surjective") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val images = base.map { perm.apply(it) }.toSet()
                    images shouldBe base.toSet()
                }
            }

            local("permutation is injective") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val images = base.map { perm.apply(it) }
                    images.toSet().size shouldBe base.size
                }
            }

            local("permutation domain equals codomain") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
                    perm.domain shouldBe perm.codomain
                }
            }
        }

        branch("Cycle decomposition properties") {
            local("cycle lengths sum to size of non-fixed elements") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val cycles = perm.cycles()
                    val cycleElementsCount = cycles.sumOf { it.size }
                    val fixedPointsCount = base.count { perm.apply(it) == it }
                    cycleElementsCount + fixedPointsCount shouldBe base.size
                }
            }

            local("permutation from cycles reconstruction") {
                lawCheckAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                    val cycles = perm.cycles()
                    val reconstructed = mutableMapOf<Any?, Any?>()

                    for (element in base) {
                        reconstructed[element] = perm.apply(element)
                    }

                    // Verify cycles match the mapping
                    for (cycle in cycles) {
                        for (i in cycle.indices) {
                            val next = (i + 1) % cycle.size
                            reconstructed[cycle[i]] shouldBe cycle[next]
                        }
                    }
                }
            }
        }
    })
})