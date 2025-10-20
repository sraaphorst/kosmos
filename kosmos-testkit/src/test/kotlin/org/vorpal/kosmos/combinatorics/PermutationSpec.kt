package org.vorpal.kosmos.combinatorics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.generateArbOrderedFiniteSet

class PermutationSpec : FreeSpec({

    "Permutation construction" - {

        "of creates valid permutation from total bijective mapping" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                base.all { x -> perm.apply(x) in base } shouldBe true
            }
        }

        "identity permutation maps everything to itself" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                val id = Permutation.identity(base)
                base.all { x -> id.apply(x) == x } shouldBe true
            }
        }

        "identity permutation is its own inverse" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                val id = Permutation.identity(base)
                id.inverse() shouldBe id
            }
        }

        "constructed permutation preserves bijectivity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val images = base.map { perm.apply(it) }.toSet()
                images shouldBe base.toSet()
            }
        }
    }

    "Permutation application" - {

        "apply and get operator are equivalent" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                base.all { x -> perm.apply(x) == perm[x] } shouldBe true
            }
        }

        "forward then backward is identity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                base.all { x -> perm.backward.apply(perm.forward.apply(x)) == x } shouldBe true
            }
        }

        "backward then forward is identity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                base.all { x -> perm.forward.apply(perm.backward.apply(x)) == x } shouldBe true
            }
        }
    }

    "Permutation composition" - {

        "composition preserves domain" {
            checkAll(generateArbPermutationPair(Arb.int(), 5)) { (base, p1, p2) ->
                val composed = p1 then p2
                composed.domain shouldBe base
            }
        }

        "composition is associative" {
            checkAll(PermutationTestingCombinations.arbBaseWithPermutations(Arb.int(), 5, 3)) { (base, perms) ->
                val (p1, p2, p3) = perms
                val left = (p1 then p2) then p3
                val right = p1 then (p2 then p3)

                base.all { x -> left.apply(x) == right.apply(x) } shouldBe true
            }
        }

        "identity is left identity for composition" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                val id = Permutation.identity(base)
                val composed = id then perm
                base.all { x -> composed.apply(x) == perm.apply(x) } shouldBe true
            }
        }

        "identity is right identity for composition" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                val id = Permutation.identity(base)
                val composed = perm then id
                base.all { x -> composed.apply(x) == perm.apply(x) } shouldBe true
            }
        }

        "composition with inverse gives identity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val composed = perm then perm.inverse()
                base.all { x -> composed.apply(x) == x } shouldBe true
            }
        }

        "inverse then original gives identity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val composed = perm.inverse() then perm
                base.all { x -> composed.apply(x) == x } shouldBe true
            }
        }
    }

    "Permutation inverse" - {

        "inverse swaps forward and backward" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val inv = perm.inverse()
                inv.forward shouldBe perm.backward
                inv.backward shouldBe perm.forward
            }
        }

        "inverse twice is identity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                perm.inverse().inverse() shouldBe perm
            }
        }

        "inverse of identity is identity" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                val id = Permutation.identity(base)
                id.inverse() shouldBe id
            }
        }

        "composition inverse formula: (fg)^-1 = g^-1 f^-1" {
            checkAll(generateArbPermutationPair(Arb.int(), 5)) { (base, p1, p2) ->
                val composed = p1 then p2
                val invComposed = composed.inverse()
                val invFormula = p2.inverse() then p1.inverse()

                base.all { x -> invComposed.apply(x) == invFormula.apply(x) } shouldBe true
            }
        }
    }

    "Permutation cycles" - {

        "identity has no non-trivial cycles" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                val id = Permutation.identity(base)
                id.cycles().isEmpty() shouldBe true
            }
        }

        "cycles partition the domain" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val cycles = perm.cycles()
                val cycleElements = cycles.flatten().toSet()
                val fixedPoints = base.filter { perm.apply(it) == it }.toSet()
                (cycleElements + fixedPoints) shouldBe base.toSet()
            }
        }

        "cycles are disjoint" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
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

        "cyclic permutation has one cycle" {
            checkAll(generateArbCyclicPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                val cycles = perm.cycles()
                cycles.size shouldBe 1
                cycles.first().toSet() shouldBe base.toSet()
            }
        }

        "each cycle element maps to next in cycle" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
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

    "Permutation order" - {

        "identity has order 1" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                val id = Permutation.identity(base)
                id.order() shouldBe 1
            }
        }

        "cyclic permutation order equals cycle length" {
            checkAll(generateArbCyclicPermutation(Arb.int(), 2, 8)) { (base, perm) ->
                perm.order() shouldBe base.size
            }
        }

        "order is positive" {
            checkAll(generateArbPermutation(Arb.int(), 2, 10)) { (base, perm) ->
                perm.order() shouldBeGreaterThan 0
            }
        }

        "permutation to the power of its order is identity" {
            checkAll(generateArbPermutation(Arb.int(), 2, 8)) { (base, perm) ->
                val order = perm.order()
                val powerN = perm.exp(order)
                base.all { x -> powerN.apply(x) == x } shouldBe true
            }
        }

        "order divides any power that gives identity" {
            checkAll(generateArbPermutation(Arb.int(), 2, 6)) { (base, perm) ->
                val order = perm.order()
                val multiple = order * 2
                val powerMultiple = perm.exp(multiple)
                base.all { x -> powerMultiple.apply(x) == x } shouldBe true
            }
        }
    }

    "Permutation powers" - {

        "power 0 is identity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                val power0 = perm.exp(0)
                base.all { x -> power0.apply(x) == x } shouldBe true
            }
        }

        "power 1 is self" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                val power1 = perm.exp(1)
                base.all { x -> power1.apply(x) == perm.apply(x) } shouldBe true
            }
        }

        "power 2 is composition with self" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                val power2 = perm.exp(2)
                val composed = perm then perm
                base.all { x -> power2.apply(x) == composed.apply(x) } shouldBe true
            }
        }

        "power law: p^(m+n) = p^m . p^n" {
            checkAll(generateArbPermutation(Arb.int(), 2, 6)) { (base, perm) ->
                val m = 2
                val n = 3
                val powerSum = perm.exp(m + n)
                val powerProduct = perm.exp(m) then perm.exp(n)
                base.all { x -> powerSum.apply(x) == powerProduct.apply(x) } shouldBe true
            }
        }

        "power law: (p^m)^n = p^(m*n)" {
            checkAll(generateArbPermutation(Arb.int(), 2, 5)) { (base, perm) ->
                val m = 2
                val n = 2
                val powerOfPower = perm.exp(m).exp(n)
                val powerProduct = perm.exp(m * n)
                base.all { x -> powerOfPower.apply(x) == powerProduct.apply(x) } shouldBe true
            }
        }
    }

    "FiniteSet extension functions" - {

        "identityPermutation is identity" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                val id = base.identityPermutation()
                base.all { x -> id.apply(x) == x } shouldBe true
            }
        }

        "cyclicPermutation creates valid cyclic permutation" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 3, 8)) { base ->
                if (base.size > 1) {
                    val cyclic = base.cyclicPermutation(1)
                    cyclic.cycles().size shouldBe 1
                }
            }
        }

        "cyclicPermutation with shift 0 is identity" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 3, 8)) { base ->
                val cyclic = base.cyclicPermutation(0)
                base.all { x -> cyclic.apply(x) == x } shouldBe true
            }
        }

        "shiftPermutation shifts by k positions" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 5, 8)) { base ->
                val shift = 2
                val shifted = base.shiftPermutation(shift)
                base.order.indices.all { i ->
                    val expected = base.order[(i + shift) % base.size]
                    shifted.apply(base.order[i]) == expected
                } shouldBe true
            }
        }

        "shift by size is identity" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 3, 8)) { base ->
                val shifted = base.shiftPermutation(base.size)
                base.all { x -> shifted.apply(x) == x } shouldBe true
            }
        }
    }

    "Permutation group properties" - {

        "closure: composition of two permutations is a permutation" {
            checkAll(generateArbPermutationPair(Arb.int(), 5)) { (base, p1, p2) ->
                val composed = p1 then p2
                base.all { x -> composed.apply(x) in base } shouldBe true
            }
        }

        "every permutation has an inverse" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val inv = perm.inverse()
                val composed = perm then inv
                base.all { x -> composed.apply(x) == x } shouldBe true
            }
        }

        "conjugation preserves order" {
            checkAll(generateArbPermutationPair(Arb.int(), 5)) { (base, p, q) ->
                val conjugate = q then p then q.inverse()
                conjugate.order() shouldBe p.order()
            }
        }

        "commutator of identity is identity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                val id = Permutation.identity(base)
                val commutator = perm then id then perm.inverse() then id.inverse()
                base.all { x -> commutator.apply(x) == x } shouldBe true
            }
        }
    }

    "Permutation isEmpty/isNotEmpty" - {

        "isEmpty is true for empty domain" {
            val empty = FiniteSet.empty<Int>()
            val id = Permutation.identity(empty)
            id.isEmpty shouldBe true
            id.isNotEmpty shouldBe false
        }

        "isEmpty is false for non-empty domain" {
            checkAll(generateArbPermutation(Arb.int(), 1, 10)) { (base, perm) ->
                perm.isEmpty shouldBe false
                perm.isNotEmpty shouldBe true
            }
        }
    }

    "Identity permutation properties" - {

        "generateArbIdentityPermutation creates identity" {
            checkAll(generateArbIdentityPermutation(Arb.int(), 1, 10)) { (base, perm) ->
                base.all { x -> perm.apply(x) == x } shouldBe true
            }
        }

        "identity has order 1" {
            checkAll(generateArbIdentityPermutation(Arb.int(), 1, 10)) { (base, perm) ->
                perm.order() shouldBe 1
            }
        }

        "identity has no cycles" {
            checkAll(generateArbIdentityPermutation(Arb.int(), 1, 10)) { (base, perm) ->
                perm.cycles().isEmpty() shouldBe true
            }
        }
    }

    "Bijection properties" - {

        "permutation is surjective" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val images = base.map { perm.apply(it) }.toSet()
                images shouldBe base.toSet()
            }
        }

        "permutation is injective" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val images = base.map { perm.apply(it) }
                images.toSet().size shouldBe base.size
            }
        }

        "permutation domain equals codomain" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                perm.domain shouldBe perm.codomain
            }
        }
    }

    "Cycle decomposition properties" - {

        "cycle lengths sum to size of non-fixed elements" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val cycles = perm.cycles()
                val cycleElementsCount = cycles.sumOf { it.size }
                val fixedPointsCount = base.count { perm.apply(it) == it }
                cycleElementsCount + fixedPointsCount shouldBe base.size
            }
        }

        "permutation from cycles reconstruction" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
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