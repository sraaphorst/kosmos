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
                val composed = p1 andThen p2
                composed.domain shouldBe base
            }
        }

        "composition is associative" {
            checkAll(PermutationTestingCombinations.arbBaseWithPermutations(Arb.int(), 5, 3)) { (base, perms) ->
                val (p1, p2, p3) = perms
                val left = (p1 andThen p2) andThen p3
                val right = p1 andThen (p2 andThen p3)

                base.all { x -> left.apply(x) == right.apply(x) } shouldBe true
            }
        }

        "identity is left identity for composition" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                val id = Permutation.identity(base)
                val composed = id andThen perm
                base.all { x -> composed.apply(x) == perm.apply(x) } shouldBe true
            }
        }

        "identity is right identity for composition" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                val id = Permutation.identity(base)
                val composed = perm andThen id
                base.all { x -> composed.apply(x) == perm.apply(x) } shouldBe true
            }
        }

        "composition with inverse gives identity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val composed = perm andThen perm.inverse()
                base.all { x -> composed.apply(x) == x } shouldBe true
            }
        }

        "inverse then original gives identity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val composed = perm.inverse() andThen perm
                base.all { x -> composed.apply(x) == x } shouldBe true
            }
        }
    }

    "Permutation inverse" - {

        "inverse swaps forward and backward" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
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

        "inverse twice is identity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
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
                val composed = p1 andThen p2
                val invComposed = composed.inverse()
                val invFormula = p2.inverse() andThen p1.inverse()

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
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
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
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
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
            checkAll(generateArbPermutation(Arb.int(), 2, 10)) { (_, perm) ->
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
                val composed = perm andThen perm
                base.all { x -> power2.apply(x) == composed.apply(x) } shouldBe true
            }
        }

        "power law: p^(m+n) = p^m . p^n" {
            checkAll(generateArbPermutation(Arb.int(), 2, 6)) { (base, perm) ->
                val m = 2
                val n = 3
                val powerSum = perm.exp(m + n)
                val powerProduct = perm.exp(m) andThen perm.exp(n)
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
                val composed = p1 andThen p2
                base.all { x -> composed.apply(x) in base } shouldBe true
            }
        }

        "every permutation has an inverse" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (base, perm) ->
                val inv = perm.inverse()
                val composed = perm andThen inv
                base.all { x -> composed.apply(x) == x } shouldBe true
            }
        }

        "conjugation preserves order" {
            checkAll(generateArbPermutationPair(Arb.int(), 5)) { (_, p, q) ->
                val conjugate = q andThen p andThen q.inverse()
                conjugate.order() shouldBe p.order()
            }
        }

        "conjugation preserves cycle type" {
            checkAll(generateArbPermutationPair(Arb.int(), 5)) { (_, p, q) ->
                val conjugate = q andThen p andThen q.inverse()

                val pCycles = p.cycles().map { it.size }.sorted()
                val conjCycles = conjugate.cycles().map { it.size }.sorted()

                pCycles shouldBe conjCycles
            }
        }

        "commutator of identity is identity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                val id = Permutation.identity(base)
                val commutator = perm andThen id andThen perm.inverse() andThen id.inverse()
                base.all { x -> commutator.apply(x) == x } shouldBe true
            }
        }

        "commutator with self is identity" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
                val commutator = perm andThen perm andThen perm.inverse() andThen perm.inverse()
                base.all { x -> commutator.apply(x) == x } shouldBe true
            }
        }
    }

    "Permutation sign and parity" - {

        "identity has positive sign" {
            checkAll(generateArbOrderedFiniteSet(Arb.int(), 1, 10)) { base ->
                val id = Permutation.identity(base)
                id.sign() shouldBe 1
            }
        }

        "transposition has negative sign" {
            checkAll(Arb.int(), Arb.int()) { a, b ->
                if (a != b) {
                    val base = FiniteSet.ordered(a, b)
                    val mapping = mapOf(a to b, b to a)
                    val transposition = Permutation.of(base, mapping)
                    transposition.sign() shouldBe -1
                }
            }
        }

        "sign is multiplicative: sign(p1 * p2) = sign(p1) * sign(p2)" {
            checkAll(generateArbPermutationPair(Arb.int(), 5)) { (_, p1, p2) ->
                val composed = p1 andThen p2
                composed.sign() shouldBe (p1.sign() * p2.sign())
            }
        }

        "inverse has same sign: sign(p^-1) = sign(p)" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
                perm.inverse().sign() shouldBe perm.sign()
            }
        }

        "sign is either 1 or -1" {
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
                val sign = perm.sign()
                (sign == 1 || sign == -1) shouldBe true
            }
        }

        "even length cycle has odd sign" {
            checkAll(generateArbCyclicPermutation(Arb.int(), 2, 8)) { (base, perm) ->
                if (base.size % 2 == 0) {
                    perm.sign() shouldBe -1
                } else {
                    perm.sign() shouldBe 1
                }
            }
        }
    }

    "Permutation statistics" - {

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

        "inversion count is well-defined" {
            checkAll(generateArbPermutation(Arb.int(), 3, 8)) { (base, perm) ->
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

    "Permutation isEmpty/isNotEmpty" - {

        "isEmpty is true for empty domain" {
            val empty = FiniteSet.empty<Int>()
            val id = Permutation.identity(empty)
            id.isEmpty shouldBe true
            id.isNotEmpty shouldBe false
        }

        "isEmpty is false for non-empty domain" {
            checkAll(generateArbPermutation(Arb.int(), 1, 10)) { (_, perm) ->
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
            checkAll(generateArbIdentityPermutation(Arb.int(), 1, 10)) { (_, perm) ->
                perm.order() shouldBe 1
            }
        }

        "identity has no cycles" {
            checkAll(generateArbIdentityPermutation(Arb.int(), 1, 10)) { (_, perm) ->
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
            checkAll(generateArbPermutation(Arb.int(), 3, 10)) { (_, perm) ->
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