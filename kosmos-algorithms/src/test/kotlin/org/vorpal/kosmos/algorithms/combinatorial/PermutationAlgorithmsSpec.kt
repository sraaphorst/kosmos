package org.vorpal.kosmos.algorithms.combinatorial

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.combinatorics.Permutation
import org.vorpal.kosmos.combinatorics.generateArbOrderedFiniteSetOfSize
import org.vorpal.kosmos.combinatorics.generateArbPermutationOfSize
import org.vorpal.kosmos.core.bigFactorial
import java.math.BigInteger
import java.util.Random

class PermutationAlgorithmsSpec : FunSpec({

    // Test data
    val smallBase = FiniteSet.ordered(1, 2, 3)
    val mediumBase = FiniteSet.ordered('a', 'b', 'c', 'd')

    context("permutations generator") {
        test("generates correct number of permutations") {
            val base = FiniteSet.ordered(1, 2, 3, 4)
            val allPerms = permutations(base).toList()

            allPerms shouldHaveSize 24 // 4!
        }

        test("generates all unique permutations") {
            val base = FiniteSet.ordered(1, 2, 3)
            val allPerms = permutations(base).toList()

            // Check uniqueness by converting each to list representation
            val asLists = allPerms.map { perm ->
                base.order.map { elem -> perm[elem] }
            }

            asLists.toSet() shouldHaveSize 6 // All unique
        }

        test("starts with identity permutation") {
            val base = FiniteSet.ordered(1, 2, 3)
            val first = permutations(base).first()

            base.order.all { elem -> first[elem] == elem } shouldBe true
        }

        test("generates permutations in lexicographic order") {
            val base = FiniteSet.ordered(1, 2, 3)
            val allPerms = permutations(base).toList()

            // Convert to list of lists for easy comparison
            val asLists = allPerms.map { perm -> base.order.map { perm[it] } }

            asLists shouldContainExactly listOf(
                listOf(1, 2, 3),
                listOf(1, 3, 2),
                listOf(2, 1, 3),
                listOf(2, 3, 1),
                listOf(3, 1, 2),
                listOf(3, 2, 1)
            )
        }

        test("works with empty set") {
            val empty = FiniteSet.ordered<Int>()
            val allPerms = permutations(empty).toList()

            allPerms shouldHaveSize 1 // Identity only
        }

        test("works with singleton set") {
            val single = FiniteSet.ordered(42)
            val allPerms = permutations(single).toList()

            allPerms shouldHaveSize 1
            allPerms.first()[42] shouldBe 42
        }

        test("property: generates n! permutations") {
            checkAll(30, generateArbOrderedFiniteSetOfSize(Arb.int(1..100), 6)) { base ->
                val count = permutations(base).count()
                val expected = base.size.bigFactorial().toInt()

                count shouldBe expected
            }
        }
    }

    context("Lehmer code") {
        test("identity permutation has all-zero Lehmer code") {
            val base = FiniteSet.ordered(1, 2, 3, 4)
            val identity = permutations(base).first()
            val code = lehmerCode(identity, base)

            code.toList() shouldContainExactly listOf(0, 0, 0, 0)
        }

        test("last permutation has descending Lehmer code") {
            val base = FiniteSet.ordered(1, 2, 3, 4)
            val last = permutations(base).last()
            val code = lehmerCode(last, base)

            code.toList() shouldContainExactly listOf(3, 2, 1, 0)
        }

        test("Lehmer code components are bounded correctly") {
            val base = FiniteSet.ordered(1, 2, 3, 4, 5)

            checkAll(50, generateArbPermutationOfSize(Arb.int(1..100), 5)) { (testBase, perm) ->
                // Use the standard base for consistency
                val mappedPerm = permutations(base).find { p ->
                    base.order.map { p[it] } == testBase.order.map { perm[it] }
                }

                if (mappedPerm != null) {
                    val code = lehmerCode(mappedPerm, base)

                    code.withIndex().all { (i, c) ->
                        c >= 0 && c < base.size - i
                    } shouldBe true
                }
            }
        }

        test("property: different permutations have different Lehmer codes") {
            checkAll(30, generateArbOrderedFiniteSetOfSize(Arb.int(1..100), 4)) { base ->
                val perms = permutations(base).take(10).toList()
                val codes = perms.map { lehmerCode(it, base).toList() }

                codes.toSet().size shouldBe codes.size
            }
        }
    }

    context("rank and unrank") {
        test("rank of identity is zero") {
            val base = FiniteSet.ordered(1, 2, 3, 4)
            val identity = permutations(base).first()

            rankPermutation(identity, base) shouldBe BigInteger.ZERO
        }

        test("rank of last permutation is n!-1") {
            val base = FiniteSet.ordered(1, 2, 3, 4)
            val last = permutations(base).last()
            val rank = rankPermutation(last, base)

            rank shouldBe 4.bigFactorial() - BigInteger.ONE
        }

        test("unrank(rank(p)) = p for all permutations") {
            val base = FiniteSet.ordered(1, 2, 3, 4)

            permutations(base).forEachIndexed { idx, perm ->
                val rank = rankPermutation(perm, base)
                val unranked = unrankPermutation(base, rank)

                // Check that unranked produces the same mapping
                base.order.all { elem -> unranked[elem] == perm[elem] } shouldBe true
            }
        }

        test("rank(unrank(r)) = r for valid ranks") {
            val base = FiniteSet.ordered(1, 2, 3, 4)
            val maxRank = 4.bigFactorial()

            for (r in 0 until minOf(24, maxRank.toInt())) {
                val perm = unrankPermutation(base, r.toBigInteger())
                val ranked = rankPermutation(perm, base)

                ranked shouldBe r.toBigInteger()
            }
        }

        test("unrank throws for negative rank") {
            val base = FiniteSet.ordered(1, 2, 3)

            shouldThrow<IllegalArgumentException> {
                unrankPermutation(base, BigInteger.valueOf(-1))
            }
        }

        test("unrank throws for rank >= n!") {
            val base = FiniteSet.ordered(1, 2, 3)

            shouldThrow<IllegalArgumentException> {
                unrankPermutation(base, 6.toBigInteger()) // 3! = 6
            }
        }

        test("property: ranking is bijective") {
            checkAll(40, generateArbOrderedFiniteSetOfSize(Arb.int(1..100), 5)) { base ->
                checkAll(10, generateArbPermutationOfSize(Arb.int(1..100), 5)) { (testBase, perm) ->
                    // Map the permutation to our standard base
                    val mapping = testBase.order.map { perm[it] }
                    val standardPerm = permutations(base).find { p ->
                        base.order.map { p[it] } == mapping
                    }

                    if (standardPerm != null) {
                        val rank = rankPermutation(standardPerm, base)
                        val unranked = unrankPermutation(base, rank)

                        base.order.all { elem ->
                            unranked[elem] == standardPerm[elem]
                        } shouldBe true
                    }
                }
            }
        }

        test("sequential unranking produces permutations in lexicographic order") {
            val base = FiniteSet.ordered(1, 2, 3)
            val generated = (0 until 6).map { r ->
                val perm = unrankPermutation(base, r.toBigInteger())
                base.order.map { perm[it] }
            }

            generated shouldContainExactly listOf(
                listOf(1, 2, 3),
                listOf(1, 3, 2),
                listOf(2, 1, 3),
                listOf(2, 3, 1),
                listOf(3, 1, 2),
                listOf(3, 2, 1)
            )
        }
    }

    context("lexicographic successor") {
        test("identity has a successor") {
            val base = FiniteSet.ordered(1, 2, 3)
            val identity = permutations(base).first()
            val successor = lexicographicSuccessor(identity, base)

            successor shouldNotBe null
            successor!!.let { s ->
                base.order.map { s[it] } shouldBe listOf(1, 3, 2)
            }
        }

        test("last permutation has no successor") {
            val base = FiniteSet.ordered(1, 2, 3)
            val last = permutations(base).last()

            lexicographicSuccessor(last, base) shouldBe null
        }

        test("iterating successors generates all permutations") {
            val base = FiniteSet.ordered(1, 2, 3)
            val generated = mutableListOf<List<Int>>()

            var current: Permutation<Int>? = permutations(base).first()
            while (current != null) {
                generated.add(base.order.map { current[it] })
                current = lexicographicSuccessor(current, base)
            }

            generated shouldContainExactly listOf(
                listOf(1, 2, 3),
                listOf(1, 3, 2),
                listOf(2, 1, 3),
                listOf(2, 3, 1),
                listOf(3, 1, 2),
                listOf(3, 2, 1)
            )
        }

        test("successor produces correct permutation for various cases") {
            val base = FiniteSet.ordered(1, 2, 3, 4)

            // Test case: [1,2,3,4] -> [1,2,4,3]
            val p1 = permutations(base).first()
            val s1 = lexicographicSuccessor(p1, base)!!
            base.order.map { s1[it] } shouldBe listOf(1, 2, 4, 3)

            // Test case: [1,4,3,2] -> [2,1,3,4]
            val p2 = permutations(base).toList()[5] // [1,4,3,2]
            val s2 = lexicographicSuccessor(p2, base)!!
            base.order.map { s2[it] } shouldBe listOf(2, 1, 3, 4)
        }

        test("property: successor matches generated sequence") {
            checkAll(20, generateArbOrderedFiniteSetOfSize(Arb.int(1..100), 5)) { base ->
                val allPerms = permutations(base).toList()

                for (i in 0 until allPerms.size - 1) {
                    val current = allPerms[i]
                    val expected = allPerms[i + 1]
                    val successor = lexicographicSuccessor(current, base)

                    successor shouldNotBe null
                    base.order.all { elem ->
                        successor!![elem] == expected[elem]
                    } shouldBe true
                }
            }
        }
    }

    context("random permutation") {
        test("generates valid permutation") {
            val base = FiniteSet.ordered(1, 2, 3, 4, 5)
            val perm = randomPermutation(base)

            // Check it's a valid permutation (bijection)
            val image = base.order.map { perm[it] }.toSet()
            image shouldBe base.toSet()
        }

        test("is deterministic with same seed") {
            val base = FiniteSet.ordered(1, 2, 3, 4, 5)
            val seed = 42L

            val perm1 = randomPermutation(base, Random(seed))
            val perm2 = randomPermutation(base, Random(seed))

            base.order.all { elem -> perm1[elem] == perm2[elem] } shouldBe true
        }

        test("produces different results with different seeds") {
            val base = FiniteSet.ordered(1, 2, 3, 4, 5)

            val perm1 = randomPermutation(base, Random(1))
            val perm2 = randomPermutation(base, Random(2))

            // Very unlikely to be the same
            base.order.any { elem -> perm1[elem] != perm2[elem] } shouldBe true
        }

        test("works with singleton set") {
            val single = FiniteSet.ordered(42)
            val perm = randomPermutation(single)

            perm[42] shouldBe 42
        }

        test("works with empty set") {
            val empty = FiniteSet.ordered<Int>()
            val perm = randomPermutation(empty)

            // Should not throw, identity permutation
            perm.isEmpty shouldBe true
        }

        test("property: all elements appear exactly once in image") {
            checkAll(50, generateArbOrderedFiniteSetOfSize(Arb.int(1..100), 8)) { base ->
                val perm = randomPermutation(base, Random())

                val image = base.order.map { perm[it] }
                image.toSet() shouldBe base.toSet()
                image.size shouldBe base.size
            }
        }

        test("property: generates diverse permutations") {
            val base = FiniteSet.ordered(1, 2, 3, 4, 5)
            val generated = (1..100).map {
                val perm = randomPermutation(base, Random())
                base.order.map { perm[it] }
            }.toSet()

            // Should generate many different permutations
            generated.size shouldBeGreaterThan 50
        }
    }

    context("integration tests") {
        test("all permutations can be ranked and unranked") {
            val base = FiniteSet.ordered(1, 2, 3, 4)

            permutations(base).forEachIndexed { expectedRank, perm ->
                val rank = rankPermutation(perm, base)
                rank.toInt() shouldBe expectedRank

                val unranked = unrankPermutation(base, rank)
                base.order.all { elem -> unranked[elem] == perm[elem] } shouldBe true
            }
        }

        test("Lehmer code matches rank") {
            val base = FiniteSet.ordered(1, 2, 3, 4)

            permutations(base).forEach { perm ->
                val rank = rankPermutation(perm, base)
                val code = lehmerCode(perm, base)

                // Manually compute rank from Lehmer code
                val computedRank = code.withIndex().fold(BigInteger.ZERO) { acc, (i, ci) ->
                    acc + ci.toBigInteger() * (base.size - 1 - i).bigFactorial()
                }

                computedRank shouldBe rank
            }
        }

        test("random permutations can be ranked") {
            val base = FiniteSet.ordered(1, 2, 3, 4, 5)

            repeat(20) {
                val perm = randomPermutation(base, Random())
                val rank: BigInteger = rankPermutation(perm, base)

                rank shouldBeGreaterThanOrEqualTo BigInteger.ZERO
                rank shouldBeLessThan base.size.bigFactorial()
            }
        }

        test("lexicographic iteration matches rank sequence") {
            val base = FiniteSet.ordered(1, 2, 3)

            var current: org.vorpal.kosmos.combinatorics.Permutation<Int>? = permutations(base).first()
            var expectedRank = BigInteger.ZERO

            while (current != null) {
                val actualRank = rankPermutation(current, base)
                actualRank shouldBe expectedRank

                current = lexicographicSuccessor(current, base)
                expectedRank += BigInteger.ONE
            }
        }
    }
})