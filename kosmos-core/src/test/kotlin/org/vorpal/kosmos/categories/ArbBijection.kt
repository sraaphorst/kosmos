package org.vorpal.kosmos.categories

import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import org.vorpal.kosmos.combinatorial.FiniteSet
import org.vorpal.kosmos.combinatorial.generateArbOrderedFiniteSet
import org.vorpal.kosmos.combinatorial.generateArbOrderedFiniteSetOfSize

/**
 * Generate an Arb<Bijection<A, B>> between two sets of the same size.
 * Creates a random bijection by pairing elements from domain and codomain.
 */
fun <A, B> generateArbBijection(
    arbA: Arb<A>,
    arbB: Arb<B>,
    lowerBound: Int = 1,
    upperBoundInclusive: Int = 10
): Arb<Bijection<A, B>> {
    require(lowerBound >= 0) { "lowerBound must be non-negative" }
    require(upperBoundInclusive >= lowerBound) { "upperBoundInclusive must be >= lowerBound" }

    return Arb.int(lowerBound..upperBoundInclusive).flatMap { size ->
        generateArbOrderedFiniteSetOfSize(arbA, size).flatMap { domain ->
            generateArbOrderedFiniteSetOfSize(arbB, size).flatMap { codomain ->
                // Generate a random permutation of codomain indices
                Arb.shuffle(codomain.order).map { shuffledCodomain ->
                    val mapping = domain.order.zip(shuffledCodomain).toMap()
                    Bijection.of(domain, codomain, mapping)
                }
            }
        }
    }
}

/**
 * Generate an Arb<Bijection<A, A>> (permutation) on a finite set.
 * Creates random permutations of elements in the domain.
 */
fun <A> generateArbPermutation(
    arb: Arb<A>,
    lowerBound: Int = 1,
    upperBoundInclusive: Int = 10
): Arb<Bijection<A, A>> {
    require(lowerBound >= 0) { "lowerBound must be non-negative" }
    require(upperBoundInclusive >= lowerBound) { "upperBoundInclusive must be >= lowerBound" }

    return Arb.int(lowerBound..upperBoundInclusive).flatMap { size ->
        generateArbOrderedFiniteSetOfSize(arb, size).flatMap { domain ->
            // Generate a random permutation of the domain elements
            Arb.shuffle(domain.order).map { shuffled ->
                val mapping = domain.order.zip(shuffled).toMap()
                Bijection.endo(domain, mapping)
            }
        }
    }
}

/**
 * Generate an Arb<Bijection<A, A>> of exact size.
 */
fun <A> generateArbPermutationOfSize(
    arb: Arb<A>,
    exactSize: Int
): Arb<Bijection<A, A>> {
    require(exactSize >= 0) { "exactSize must be non-negative" }

    return generateArbOrderedFiniteSetOfSize(arb, exactSize).flatMap { domain ->
        Arb.shuffle(domain.order).map { shuffled ->
            val mapping = domain.order.zip(shuffled).toMap()
            Bijection.endo(domain, mapping)
        }
    }
}

/**
 * Generate identity bijections for testing.
 */
fun <A> generateArbIdentityBijection(
    arb: Arb<A>,
    lowerBound: Int = 1,
    upperBoundInclusive: Int = 10
): Arb<Bijection<A, A>> {
    return generateArbOrderedFiniteSet(arb, lowerBound, upperBoundInclusive).map { domain ->
        Bijection.id(domain)
    }
}

/**
 * Generate bijections constructed from cycle notation.
 * Useful for testing group properties and cycle decomposition.
 */
fun <A> generateArbCyclicPermutation(
    arb: Arb<A>,
    minCycleLength: Int = 2,
    maxCycleLength: Int = 8
): Arb<Bijection<A, A>> {
    require(minCycleLength >= 2) { "minCycleLength must be at least 2" }
    require(maxCycleLength >= minCycleLength) { "maxCycleLength must be >= minCycleLength" }

    return Arb.int(minCycleLength..maxCycleLength).flatMap { cycleLength ->
        generateArbOrderedFiniteSetOfSize(arb, cycleLength).map { domain ->
            // Create a single cycle through all elements
            Bijection.fromCycles(domain, domain.order)
        }
    }
}

/**
 * Generate bijections with multiple disjoint cycles.
 */
fun <A> generateArbMultiCyclePermutation(
    arb: Arb<A>,
    setSize: Int = 10,
    maxCycles: Int = 3
): Arb<Bijection<A, A>> {
    require(setSize >= 2) { "setSize must be at least 2" }
    require(maxCycles >= 1) { "maxCycles must be at least 1" }

    return generateArbOrderedFiniteSetOfSize(arb, setSize).flatMap { domain ->
        Arb.int(1..maxCycles).flatMap { numCycles ->
            // Partition the domain into cycles
            val elements = domain.order.toMutableList()
            Arb.shuffle(elements).flatMap { shuffled ->
                // Randomly partition into cycles
                val cyclesList = mutableListOf<List<A>>()
                var remaining = shuffled

                repeat(numCycles.coerceAtMost(remaining.size)) {
                    if (remaining.size > 1) {
                        val cycleSize = (2..(remaining.size)).random()
                        val cycle = remaining.take(cycleSize)
                        cyclesList.add(cycle)
                        remaining = remaining.drop(cycleSize)
                    }
                }

                // Add remaining elements as fixed points (1-cycles, which are omitted)
                Arb.constant(Bijection.fromCycles(domain, *cyclesList.toTypedArray()))
            }
        }
    }
}

/**
 * Generate involutions (self-inverse permutations).
 * These are permutations where f(f(x)) = x for all x.
 */
fun <A> generateArbInvolution(
    arb: Arb<A>,
    lowerBound: Int = 2,
    upperBoundInclusive: Int = 10
): Arb<Bijection<A, A>> {
    return generateArbOrderedFiniteSet(arb, lowerBound, upperBoundInclusive).flatMap { domain ->
        val elements = domain.order.toMutableList()

        // Pair up elements randomly for 2-cycles, leave the rest as fixed points
        Arb.shuffle(elements).map { shuffled ->
            val mapping = mutableMapOf<A, A>()
            var i = 0

            while (i < shuffled.size - 1 && kotlin.random.Random.nextBoolean()) {
                // Create a 2-cycle (swap)
                mapping[shuffled[i]] = shuffled[i + 1]
                mapping[shuffled[i + 1]] = shuffled[i]
                i += 2
            }

            // Fixed points
            while (i < shuffled.size) {
                mapping[shuffled[i]] = shuffled[i]
                i++
            }

            Bijection.endo(domain, mapping)
        }
    }
}

/**
 * Specialized generators for common types.
 */
object BijectionArbs {

    fun arbIntPermutation(
        lowerBound: Int = 2,
        upperBoundInclusive: Int = 10
    ): Arb<Bijection<Int, Int>> =
        generateArbPermutation(Arb.int(1..100), lowerBound, upperBoundInclusive)

    fun arbStringBijection(
        lowerBound: Int = 2,
        upperBoundInclusive: Int = 8
    ): Arb<Bijection<String, String>> =
        generateArbPermutation(Arb.string(1..5), lowerBound, upperBoundInclusive)

    fun arbSmallPermutation(
        size: Int = 5
    ): Arb<Bijection<Int, Int>> =
        generateArbPermutationOfSize(Arb.int(1..100), size)

    fun arbIntToCharBijection(
        lowerBound: Int = 1,
        upperBoundInclusive: Int = 10
    ): Arb<Bijection<Int, Char>> =
        generateArbBijection(Arb.int(1..100), Arb.char('a'..'z'), lowerBound, upperBoundInclusive)
}

/**
 * Extension functions for easier usage.
 */
fun <A> Arb<A>.toPermutationArb(
    lowerBound: Int = 1,
    upperBoundInclusive: Int = 10
): Arb<Bijection<A, A>> = generateArbPermutation(this, lowerBound, upperBoundInclusive)

fun <A> Arb<A>.toPermutationOfSize(exactSize: Int): Arb<Bijection<A, A>> =
    generateArbPermutationOfSize(this, exactSize)

fun <A, B> Arb<A>.toBijectionArb(
    arbB: Arb<B>,
    lowerBound: Int = 1,
    upperBoundInclusive: Int = 10
): Arb<Bijection<A, B>> = generateArbBijection(this, arbB, lowerBound, upperBoundInclusive)

/**
 * Useful combinations for testing bijection operations.
 */
object BijectionTestingCombinations {

    /**
     * Generate pairs of composable bijections: (A → B) and (B → C).
     */
    fun <A, B, C> arbComposablePair(
        arbA: Arb<A>,
        arbB: Arb<B>,
        arbC: Arb<C>,
        size: Int = 5
    ): Arb<Pair<Bijection<A, B>, Bijection<B, C>>> {
        return generateArbOrderedFiniteSetOfSize(arbA, size).flatMap { domainA ->
            generateArbOrderedFiniteSetOfSize(arbB, size).flatMap { domainB ->
                generateArbOrderedFiniteSetOfSize(arbC, size).flatMap { domainC ->
                    val bij1Arb = Arb.shuffle(domainB.order).map { shuffledB ->
                        Bijection.of(domainA, domainB, domainA.order.zip(shuffledB).toMap())
                    }
                    val bij2Arb = Arb.shuffle(domainC.order).map { shuffledC ->
                        Bijection.of(domainB, domainC, domainB.order.zip(shuffledC).toMap())
                    }
                    Arb.pair(bij1Arb, bij2Arb)
                }
            }
        }
    }

    /**
     * Generate pairs of permutations for testing composition.
     */
    fun <A> arbPermutationPair(
        arb: Arb<A>,
        size: Int = 5
    ): Arb<Pair<Bijection<A, A>, Bijection<A, A>>> {
        return generateArbOrderedFiniteSetOfSize(arb, size).flatMap { domain ->
            val perm1 = Arb.shuffle(domain.order).map { shuffled ->
                Bijection.endo(domain, domain.order.zip(shuffled).toMap())
            }
            val perm2 = Arb.shuffle(domain.order).map { shuffled ->
                Bijection.endo(domain, domain.order.zip(shuffled).toMap())
            }
            Arb.pair(perm1, perm2)
        }
    }

    /**
     * Generate a bijection and an element from its domain.
     */
    fun <A> arbBijectionWithElement(
        arb: Arb<A>,
        lowerBound: Int = 2,
        upperBoundInclusive: Int = 10
    ): Arb<Pair<Bijection<A, A>, A>> {
        return generateArbPermutation(arb, lowerBound, upperBoundInclusive).flatMap { bijection ->
            Arb.element(bijection.domain.order).map { element ->
                bijection to element
            }
        }
    }
}

// Usage examples in comments:
/*
// Basic usage:
val permArb = generateArbPermutation(Arb.int(), 3, 8)
val bijArb = Arb.int().toBijectionArb(Arb.string(), 2, 6)

// For property testing:
checkAll(permArb) { perm ->
    perm.domain.all { a ->
        perm.backward.apply(perm.forward.apply(a)) == a
    }
}

// Testing composition:
checkAll(BijectionTestingCombinations.arbPermutationPair(Arb.int(), 5)) { (f, g) ->
    val composed = f then g
    // Test properties of composed bijections
}

// Testing orbits:
checkAll(BijectionTestingCombinations.arbBijectionWithElement(Arb.int())) { (bij, elem) ->
    val orbit = bij.orbit(elem)
    orbit.all { it in bij.domain }
}
*/