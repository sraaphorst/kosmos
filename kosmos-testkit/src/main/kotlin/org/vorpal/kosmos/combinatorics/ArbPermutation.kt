package org.vorpal.kosmos.combinatorics

import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.generateArbOrderedFiniteSet
import org.vorpal.kosmos.core.generateArbOrderedFiniteSetOfSize

/**
 * Generate an arbitrary permutation on a finite ordered set.
 * Uses Fisher-Yates shuffle for uniform distribution.
 */
fun <A: Any> generateArbPermutation(
    arb: Arb<A>,
    lowerBound: Int = 1,
    upperBoundInclusive: Int = 10
): Arb<Pair<FiniteSet.Ordered<A>, Permutation<A>>> {
    require(lowerBound >= 0) { "lowerBound must be non-negative" }
    require(upperBoundInclusive >= lowerBound) { "upperBoundInclusive must be >= lowerBound" }

    return Arb.int(lowerBound..upperBoundInclusive).flatMap { size ->
        generateArbOrderedFiniteSetOfSize(arb, size).flatMap { base ->
            Arb.shuffle(base.order).map { shuffled ->
                val mapping = base.order.zip(shuffled).toMap()
                base to Permutation.of(base, mapping)
            }
        }
    }
}

/**
 * Generate an arbitrary permutation of exact size.
 */
fun <A: Any> generateArbPermutationOfSize(
    arb: Arb<A>,
    exactSize: Int
): Arb<Pair<FiniteSet.Ordered<A>, Permutation<A>>> {
    require(exactSize >= 0) { "exactSize must be non-negative" }

    return generateArbOrderedFiniteSetOfSize(arb, exactSize).flatMap { base ->
        Arb.shuffle(base.order).map { shuffled ->
            val mapping = base.order.zip(shuffled).toMap()
            base to Permutation.of(base, mapping)
        }
    }
}

/**
 * Generate the identity permutation on a finite ordered set.
 */
fun <A: Any> generateArbIdentityPermutation(
    arb: Arb<A>,
    lowerBound: Int = 1,
    upperBoundInclusive: Int = 10
): Arb<Pair<FiniteSet.Ordered<A>, Permutation<A>>> {
    return generateArbOrderedFiniteSet(arb, lowerBound, upperBoundInclusive).map { base ->
        val mapping = base.associateWith { it }
        base to Permutation.of(base, mapping)
    }
}

/**
 * Generate a permutation with a specific cycle structure.
 * The cycle lengths are randomly distributed but sum to the base size.
 */
fun <A: Any> generateArbCyclicPermutation(
    arb: Arb<A>,
    minCycleLength: Int = 2,
    maxCycleLength: Int = 8
): Arb<Pair<FiniteSet.Ordered<A>, Permutation<A>>> {
    require(minCycleLength >= 2) { "minCycleLength must be at least 2" }
    require(maxCycleLength >= minCycleLength) { "maxCycleLength must be >= minCycleLength" }

    return Arb.int(minCycleLength..maxCycleLength).flatMap { cycleLength ->
        generateArbOrderedFiniteSetOfSize(arb, cycleLength).map { base ->
            // Create a single cycle through all elements
            val mapping = mutableMapOf<A, A>()
            for (i in base.order.indices) {
                val next = (i + 1) % base.order.size
                mapping[base[i]] = base[next]
            }
            base to Permutation.of(base, mapping)
        }
    }
}

/**
 * Generate a pair of permutations on the same base.
 * Useful for testing composition and other binary operations.
 */
fun <A: Any> generateArbPermutationPair(
    arb: Arb<A>,
    size: Int = 5
): Arb<Triple<FiniteSet.Ordered<A>, Permutation<A>, Permutation<A>>> {
    return generateArbOrderedFiniteSetOfSize(arb, size).flatMap { base ->
        val perm1Arb = Arb.shuffle(base.order).map { shuffled ->
            val mapping = base.order.zip(shuffled).toMap()
            Permutation.of(base, mapping)
        }

        val perm2Arb = Arb.shuffle(base.order).map { shuffled ->
            val mapping = base.order.zip(shuffled).toMap()
            Permutation.of(base, mapping)
        }

        Arb.bind(perm1Arb, perm2Arb) { p1, p2 ->
            Triple(base, p1, p2)
        }
    }
}

/**
 * Specialized generators for common types.
 */
object ArbPermutation {

    fun arbIntPermutation(
        lowerBound: Int = 2,
        upperBoundInclusive: Int = 8
    ): Arb<Pair<FiniteSet.Ordered<Int>, Permutation<Int>>> =
        generateArbPermutation(Arb.int(1..100), lowerBound, upperBoundInclusive)

    fun arbSmallPermutation(
        size: Int = 5
    ): Arb<Pair<FiniteSet.Ordered<Int>, Permutation<Int>>> =
        generateArbPermutationOfSize(Arb.int(1..100), size)

    fun arbCharPermutation(
        lowerBound: Int = 2,
        upperBoundInclusive: Int = 10
    ): Arb<Pair<FiniteSet.Ordered<Char>, Permutation<Char>>> =
        generateArbPermutation(Arb.char('a'..'z'), lowerBound, upperBoundInclusive)
}

/**
 * Extension functions for easier usage.
 */
fun <A: Any> Arb<A>.toPermutationArb(
    lowerBound: Int = 1,
    upperBoundInclusive: Int = 10
): Arb<Pair<FiniteSet.Ordered<A>, Permutation<A>>> =
    generateArbPermutation(this, lowerBound, upperBoundInclusive)

fun <A: Any> Arb<A>.toPermutationOfSize(exactSize: Int): Arb<Pair<FiniteSet.Ordered<A>, Permutation<A>>> =
    generateArbPermutationOfSize(this, exactSize)

/**
 * Useful combinations for testing permutation operations.
 */
object PermutationTestingCombinations {

    /**
     * Generate a base and a permutation for testing algorithms that need both.
     */
    fun <A: Any> arbBaseAndPermutation(
        arb: Arb<A>,
        size: Int = 5
    ): Arb<Pair<FiniteSet.Ordered<A>, Permutation<A>>> =
        generateArbPermutationOfSize(arb, size)

    /**
     * Generate a base with multiple permutations for testing sequences.
     */
    fun <A: Any> arbBaseWithPermutations(
        arb: Arb<A>,
        size: Int = 5,
        count: Int = 3
    ): Arb<Pair<FiniteSet.Ordered<A>, List<Permutation<A>>>> {
        return generateArbOrderedFiniteSetOfSize(arb, size).flatMap { base ->
            Arb.list(
                Arb.shuffle(base.order).map { shuffled ->
                    val mapping = base.order.zip(shuffled).toMap()
                    Permutation.of(base, mapping)
                },
                count..count
            ).map { perms ->
                base to perms
            }
        }
    }
}