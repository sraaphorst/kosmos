package org.vorpal.kosmos.categories

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.shuffle
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.vorpal.kosmos.combinatorial.FiniteSet
import org.vorpal.kosmos.combinatorial.generateArbOrderedFiniteSet
import org.vorpal.kosmos.combinatorial.generateArbOrderedFiniteSetOfSize

class BijectionSpec : FunSpec({

    // Test data for concrete examples
    val setA = FiniteSet.Companion.ordered(1, 2, 3)
    val setB = FiniteSet.Companion.ordered("a", "b", "c")
    val setC = FiniteSet.Companion.ordered('x', 'y', 'z')

    context("Bijection construction") {
        test("should create valid bijection from complete forward map") {
            val bij = Bijection.Companion.of(
                setA, setB,
                mapOf(1 to "a", 2 to "b", 3 to "c")
            )

            bij.apply(1) shouldBe "a"
            bij.apply(2) shouldBe "b"
            bij.apply(3) shouldBe "c"
        }

        test("should create valid bijection using build DSL") {
            val bij = Bijection.Companion.build(setA, setB) {
                this[1] = "a"
                this[2] = "b"
                this[3] = "c"
            }

            bij.apply(1) shouldBe "a"
            bij.apply(2) shouldBe "b"
            bij.apply(3) shouldBe "c"
        }

        test("should fail when forward map is not total (missing domain element)") {
            shouldThrow<IllegalArgumentException> {
                Bijection.Companion.of(
                    setA, setB,
                    mapOf(1 to "a", 2 to "b") // missing 3
                )
            }
        }

        test("should fail when forward map has extra keys") {
            shouldThrow<IllegalArgumentException> {
                Bijection.Companion.of(
                    setA, setB,
                    mapOf(1 to "a", 2 to "b", 3 to "c", 4 to "d")
                )
            }
        }

        test("should fail when forward map is not surjective") {
            shouldThrow<IllegalArgumentException> {
                Bijection.Companion.of(
                    setA, setB,
                    mapOf(1 to "a", 2 to "a", 3 to "b") // "c" not in image
                )
            }
        }

        test("should fail when forward map is not injective") {
            val setSmall = FiniteSet.Companion.ordered("a", "b")
            shouldThrow<IllegalArgumentException> {
                Bijection.Companion.of(
                    setA, setSmall,
                    mapOf(1 to "a", 2 to "a", 3 to "b") // collision on "a"
                )
            }
        }

        test("property: all generated bijections are valid") {
            checkAll(100, generateArbBijection(Arb.Companion.int(1..100), Arb.Companion.string(1..5), 2, 10)) { bij ->
                // Domain and codomain same size
                bij.domain.size shouldBe bij.codomain.size

                // All elements map somewhere
                bij.domain.all { a -> bij.apply(a) in bij.codomain } shouldBe true

                // Image equals codomain (surjective)
                val image = bij.domain.map { bij.apply(it) }.toSet()
                image shouldBe bij.codomain.toSet()
            }
        }
    }

    context("Bijection backward (inverse) mapping") {
        test("backward should invert forward mapping") {
            val bij = Bijection.Companion.of(
                setA, setB,
                mapOf(1 to "a", 2 to "b", 3 to "c")
            )

            bij.backward.apply("a") shouldBe 1
            bij.backward.apply("b") shouldBe 2
            bij.backward.apply("c") shouldBe 3
        }

        test("property: forward then backward is identity") {
            checkAll(100, generateArbEndoBijection(Arb.Companion.int(1..100), 2, 15)) { bij ->
                bij.domain.all { a ->
                    bij.backward.apply(bij.forward.apply(a)) == a
                } shouldBe true
            }
        }

        test("property: backward then forward is identity") {
            checkAll(100, generateArbEndoBijection(Arb.Companion.int(1..100), 2, 15)) { bij ->
                bij.codomain.all { b ->
                    bij.forward.apply(bij.backward.apply(b)) == b
                } shouldBe true
            }
        }
    }

    context("Bijection with singleton and empty sets") {
        test("should work with singleton sets") {
            val single1 = FiniteSet.Companion.singleton(42)
            val single2 = FiniteSet.Companion.singleton("answer")

            val bij = Bijection.Companion.of(
                single1, single2,
                mapOf(42 to "answer")
            )

            bij.apply(42) shouldBe "answer"
            bij.backward.apply("answer") shouldBe 42
        }

        test("should work with empty sets") {
            val empty1 = FiniteSet.Companion.empty<Int>()
            val empty2 = FiniteSet.Companion.empty<String>()

            val bij = Bijection.Companion.of(empty1, empty2, emptyMap())

            bij.domain.isEmpty shouldBe true
            bij.codomain.isEmpty shouldBe true
        }
    }

    context("Endomorphic bijections (permutations)") {
        test("should create permutation using endo") {
            val bij = Bijection.Companion.endo(
                setA,
                mapOf(1 to 2, 2 to 3, 3 to 1) // cycle (1 2 3)
            )

            bij.apply(1) shouldBe 2
            bij.apply(2) shouldBe 3
            bij.apply(3) shouldBe 1
        }

        test("identity permutation") {
            val bij = Bijection.Companion.endo(
                setA,
                mapOf(1 to 1, 2 to 2, 3 to 3)
            )

            setA.all { a -> bij.apply(a) == a } shouldBe true
        }

        test("property: identity bijection preserves all elements") {
            checkAll(50, generateArbIdentityBijection(Arb.Companion.int(1..100), 2, 12)) { bij ->
                bij.domain.all { a -> bij.apply(a) == a } shouldBe true
            }
        }

        test("property: identity is self-inverse") {
            checkAll(50, generateArbIdentityBijection(Arb.Companion.int(1..100), 2, 12)) { bij ->
                bij.isIdentity() shouldBe true
                bij.inverse().isIdentity() shouldBe true
            }
        }

        test("property: permutation composition preserves bijection properties") {
            checkAll(50, BijectionTestingCombinations.arbEndoBijectionPair(Arb.Companion.int(1..100), 6)) { (perm1, perm2) ->
                val composed = perm1 then perm2

                // Image equals domain (still a permutation)
                val image = perm1.domain.map { composed.apply(it) }.toSet()
                image shouldBe perm1.domain.toSet()

                // Still bijective
                perm1.domain.all { a ->
                    composed.backward.apply(composed.apply(a)) == a
                } shouldBe true
            }
        }
    }

    context("Bijection inverse operation") {
        test("inverse should swap domain and codomain") {
            val bij = Bijection.Companion.of(
                setA, setB,
                mapOf(1 to "a", 2 to "b", 3 to "c")
            )

            val inv = bij.inverse()

            inv.apply("a") shouldBe 1
            inv.apply("b") shouldBe 2
            inv.apply("c") shouldBe 3
        }

        test("property: double inverse returns to original") {
            checkAll(80, generateArbEndoBijection(Arb.Companion.int(1..100), 2, 12)) { bij ->
                val doubleInv = bij.inverse().inverse()

                bij.domain.all { a -> doubleInv.apply(a) == bij.apply(a) } shouldBe true
            }
        }

        test("property: inverse swaps forward and backward") {
            checkAll(80, generateArbEndoBijection(Arb.Companion.int(1..100), 2, 12)) { bij ->
                val inv = bij.inverse()

                bij.domain.all { a ->
                    inv.backward.apply(a) == bij.forward.apply(a)
                } shouldBe true
            }
        }
    }

    context("Bijection composition") {
        test("should compose two bijections via then") {
            val bij1 = Bijection.Companion.of(
                setA, setB,
                mapOf(1 to "a", 2 to "b", 3 to "c")
            )

            val bij2 = Bijection.Companion.of(
                setB, setC,
                mapOf("a" to 'x', "b" to 'y', "c" to 'z')
            )

            val composed = bij1 then bij2

            composed.apply(1) shouldBe 'x'
            composed.apply(2) shouldBe 'y'
            composed.apply(3) shouldBe 'z'
        }

        test("property: composition maintains inverse property") {
            checkAll(60, BijectionTestingCombinations.arbComposablePair(
                Arb.Companion.int(1..100), Arb.Companion.string(1..5), Arb.Companion.char('a'..'z'), 5
            )) { (bij1, bij2) ->
                val composed = bij1 then bij2

                // Forward then backward is identity
                bij1.domain.all { a ->
                    composed.backward.apply(composed.forward.apply(a)) == a
                } shouldBe true
            }
        }

        test("property: associativity of composition") {
            checkAll(40, generateArbOrderedFiniteSetOfSize(Arb.Companion.int(1..100), 5)) { domain ->
                checkAll(10, Arb.Companion.shuffle(domain.order)) { perm1 ->
                    checkAll(10, Arb.Companion.shuffle(domain.order)) { perm2 ->
                        checkAll(10, Arb.Companion.shuffle(domain.order)) { perm3 ->
                            val f = Bijection.Companion.endo(domain, domain.order.zip(perm1).toMap())
                            val g = Bijection.Companion.endo(domain, domain.order.zip(perm2).toMap())
                            val h = Bijection.Companion.endo(domain, domain.order.zip(perm3).toMap())

                            val left = (f then g) then h
                            val right = f then (g then h)

                            domain.all { a -> left.apply(a) == right.apply(a) } shouldBe true
                        }
                    }
                }
            }
        }
    }

    context("Bijection equality on domain") {
        test("eqOn should detect equal bijections") {
            val bij1 = Bijection.Companion.of(setA, setB, mapOf(1 to "a", 2 to "b", 3 to "c"))
            val bij2 = Bijection.Companion.of(setA, setB, mapOf(1 to "a", 2 to "b", 3 to "c"))

            val eq = bij1.eqOn(setA) { x, y -> x == y }
            eq(bij2) shouldBe true
        }

        test("eqOn should detect different bijections") {
            val bij1 = Bijection.Companion.of(setA, setB, mapOf(1 to "a", 2 to "b", 3 to "c"))
            val bij2 = Bijection.Companion.of(setA, setB, mapOf(1 to "b", 2 to "a", 3 to "c"))

            val eq = bij1.eqOn(setA) { x, y -> x == y }
            eq(bij2) shouldBe false
        }
    }

    context("Orbit and permutation operations") {
        test("orbit should find cycle") {
            val bij = Bijection.Companion.endo(setA, mapOf(1 to 2, 2 to 3, 3 to 1))

            val orbit = bij.orbit(1)
            orbit.toSet() shouldBe setOf(1, 2, 3)
        }

        test("property: orbit is closed under application") {
            checkAll(80, BijectionTestingCombinations.arbEndoBijectionWithElement(Arb.Companion.int(1..100), 2, 10)) { (bij, elem) ->
                val orbit = bij.orbit(elem)

                // All orbit elements map to orbit elements
                orbit.all { x -> bij.apply(x) in orbit } shouldBe true
            }
        }

        test("property: orbits partition the domain") {
            checkAll(80, generateArbEndoBijection(Arb.Companion.int(1..100), 2, 12)) { bij ->
                val allOrbits = bij.domain.map { elem -> bij.orbit(elem).toSet() }.toSet()

                // Each element appears in exactly one orbit
                val unionOfOrbits = allOrbits.flatten().toSet()
                unionOfOrbits shouldBe bij.domain.toSet()

                // Orbits are pairwise disjoint
                allOrbits.forEach { orbit1 ->
                    allOrbits.forEach { orbit2 ->
                        if (orbit1 != orbit2) {
                            (orbit1 intersect orbit2).isEmpty() shouldBe true
                        }
                    }
                }
            }
        }

        test("property: applying orbit order times returns to start") {
            checkAll(80, BijectionTestingCombinations.arbEndoBijectionWithElement(Arb.Companion.int(1..100), 2, 10)) { (bij, elem) ->
                val order = bij.orderOf(elem)
                var current = elem
                repeat(order) { current = bij.apply(current) }
                current shouldBe elem
            }
        }

        test("property: cyclic permutations have single orbit") {
            checkAll(60, generateArbCyclicEndoBijection(Arb.Companion.int(1..100), 3, 10)) { bij ->
                val firstElem = bij.domain.order.first()
                val orbit = bij.orbit(firstElem)

                orbit.size shouldBe bij.domain.size
            }
        }

        test("property: involutions have order 1 or 2") {
            checkAll(60, generateArbInvolutionBijection(Arb.Companion.int(1..100), 2, 12)) { bij ->
                bij.domain.all { elem ->
                    val order = bij.orderOf(elem)
                    order == 1 || order == 2
                } shouldBe true
            }
        }

        test("property: cycle decomposition covers entire domain") {
            checkAll(60, generateArbEndoBijection(Arb.Companion.int(1..100), 3, 10)) { bij ->
                val cycles = bij.cycleDecomposition()
                val allElements = cycles.flatten().toSet()

                // All non-trivial cycles found
                bij.domain.filter { bij.orderOf(it) > 1 }.all { it in allElements } shouldBe true
            }
        }
    }

    context("Automorphism orbit operations") {
        test("automorphism orbit should match bijection orbit") {
            val bij = Bijection.Companion.endo(setA, mapOf(1 to 2, 2 to 3, 3 to 1))
            val auto = bij.toAutomorphism()

            val bijOrbit = bij.orbit(1)
            val autoOrbit = auto.orbit(1, setA)

            bijOrbit.toSet() shouldBe autoOrbit.toSet()
        }

        test("property: automorphism orbits are closed") {
            checkAll(60, generateArbEndoBijection(Arb.Companion.int(1..100), 2, 10)) { bij ->
                val auto = bij.toAutomorphism()

                bij.domain.forEach { elem ->
                    val orbit = auto.orbit(elem, bij.domain)
                    orbit.all { x -> auto.apply(x) in orbit } shouldBe true
                }
            }
        }

        test("automorphism isIdentity should detect identity") {
            val identity = Automorphism.Companion.id<Int>()
            val notIdentity = Automorphism.Companion.of<Int>(
                { when(it) { 1 -> 2; 2 -> 1; else -> it } },
                { when(it) { 1 -> 2; 2 -> 1; else -> it } }
            )

            identity.isIdentity(setA) shouldBe true
            notIdentity.isIdentity(setA) shouldBe false
        }

        test("property: identity automorphism on any domain") {
            checkAll(60, generateArbOrderedFiniteSet(Arb.Companion.int(1..100), 2, 12)) { domain ->
                val identity = Automorphism.Companion.id<Int>()

                identity.isIdentity(domain) shouldBe true
                domain.all { identity.apply(it) == it } shouldBe true
            }
        }
    }

    context("Special bijection factories") {
        test("fromOrdering should create position-based bijection") {
            val domain = FiniteSet.Companion.ordered(1, 2, 3)
            val codomain = FiniteSet.Companion.ordered("x", "y", "z")

            val bij = Bijection.Companion.fromOrdering(domain, codomain)

            bij.apply(1) shouldBe "x"
            bij.apply(2) shouldBe "y"
            bij.apply(3) shouldBe "z"
        }

        test("fromCycles should create valid permutation") {
            val domain = FiniteSet.Companion.ordered(1, 2, 3, 4, 5)
            val bij = Bijection.Companion.fromCycles(domain, listOf(1, 2, 3), listOf(4, 5))

            bij.apply(1) shouldBe 2
            bij.apply(2) shouldBe 3
            bij.apply(3) shouldBe 1
            bij.apply(4) shouldBe 5
            bij.apply(5) shouldBe 4
        }

        test("property: fromCycles produces correct cycle structure") {
            checkAll(60, generateArbOrderedFiniteSet(Arb.Companion.int(1..100), 5, 10)) { domain ->
                val cycle1 = domain.take(3).order
                val cycle2 = domain.drop(3).take(2).order

                if (cycle1.isNotEmpty() && cycle2.isNotEmpty()) {
                    val bij = Bijection.Companion.fromCycles(domain, cycle1, cycle2)

                    // Check cycle 1
                    cycle1.indices.forEach { i ->
                        val next = (i + 1) % cycle1.size
                        bij.apply(cycle1[i]) shouldBe cycle1[next]
                    }

                    // Check cycle 2
                    cycle2.indices.forEach { i ->
                        val next = (i + 1) % cycle2.size
                        bij.apply(cycle2[i]) shouldBe cycle2[next]
                    }
                }
            }
        }
    }
})