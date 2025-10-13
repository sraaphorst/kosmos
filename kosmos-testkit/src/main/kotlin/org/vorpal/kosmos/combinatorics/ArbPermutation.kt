package org.vorpal.kosmos.combinatorics

import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import org.vorpal.kosmos.categories.Morphism

/**
 * Generate an arbitrary permutation on a finite ordered set.
 * Uses Fisher-Yates shuffle for uniform distribution.
 */
fun <A> generateArbPermutation(
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
                val inv = shuffled.zip(base.order).toMap()
                val f = Morphism<A, A> { mapping.getValue(it) }
                val g = Morphism<A, A> { inv.getValue(it) }
                base to Permutation(base, f, g)
            }
        }
    }
}

/**
 * Generate an arbitrary permutation of exact size.
 */
fun <A> generateArbPermutationOfSize(
    arb: Arb<A>,
    exactSize: Int
): Arb<Pair<FiniteSet.Ordered<A>, Permutation<A>>> {
    require(exactSize >= 0) { "exactSize must be non-negative" }

    return generateArbOrderedFiniteSetOfSize(arb, exactSize).flatMap { base ->
        Arb.shuffle(base.order).map { shuffled ->
            val mapping = base.order.zip(shuffled).toMap()
            val inv = shuffled.zip(base.order).toMap()
            val f = Morphism<A, A> { mapping.getValue(it) }
            val g = Morphism<A, A> { inv.getValue(it) }
            base to Permutation(base, f, g)
        }
    }
}

/**
 * Generate the identity permutation on a finite ordered set.
 */
fun <A> generateArbIdentityPermutation(
    arb: Arb<A>,
    lowerBound: Int = 1,
    upperBoundInclusive: Int = 10
): Arb<Pair<FiniteSet.Ordered<A>, Permutation<A>>> {
    return generateArbOrderedFiniteSet(arb, lowerBound, upperBoundInclusive).map { base ->
        val mapping = base.associateWith { it }
        val f = Morphism<A, A> { it }
        val g = Morphism<A, A> { it }
        base to Permutation(base, f, g)
    }
}

/**
 * Generate a permutation with a specific cycle structure.
 * The cycle lengths are randomly distributed but sum to the base size.
 */
fun <A> generateArbCyclicPermutation(
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
            val inv = mapping.entries.associate { (k, v) -> v to k }
            val f = Morphism<A, A> { mapping.getValue(it) }
            val g = Morphism<A, A> { inv.getValue(it) }
            base to Permutation(base, f, g)
        }
    }
}

/**
 * Generate a pair of permutations on the same base.
 * Useful for testing composition and other binary operations.
 */
fun <A> generateArbPermutationPair(
    arb: Arb<A>,
    size: Int = 5
): Arb<Triple<FiniteSet.Ordered<A>, Permutation<A>, Permutation<A>>> {
    return generateArbOrderedFiniteSetOfSize(arb, size).flatMap { base ->
        val perm1Arb = Arb.shuffle(base.order).map { shuffled ->
            val mapping = base.order.zip(shuffled).toMap()
            val inv = shuffled.zip(base.order).toMap()
            val f = Morphism<A, A> { mapping.getValue(it) }
            val g = Morphism<A, A> { inv.getValue(it) }
            Permutation(base, f, g)
        }

        val perm2Arb = Arb.shuffle(base.order).map { shuffled ->
            val mapping = base.order.zip(shuffled).toMap()
            val inv = shuffled.zip(base.order).toMap()
            val f = Morphism<A, A> { mapping.getValue(it) }
            val g = Morphism<A, A> { inv.getValue(it) }
            Permutation(base, f, g)
        }

        Arb.bind(perm1Arb, perm2Arb) { p1, p2 ->
            Triple(base, p1, p2)
        }
    }
}

/**
 * Specialized generators for common types.
 */
object PermutationArbs {

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
fun <A> Arb<A>.toPermutationArb(
    lowerBound: Int = 1,
    upperBoundInclusive: Int = 10
): Arb<Pair<FiniteSet.Ordered<A>, Permutation<A>>> =
    generateArbPermutation(this, lowerBound, upperBoundInclusive)

fun <A> Arb<A>.toPermutationOfSize(exactSize: Int): Arb<Pair<FiniteSet.Ordered<A>, Permutation<A>>> =
    generateArbPermutationOfSize(this, exactSize)

/**
 * Useful combinations for testing permutation operations.
 */
object PermutationTestingCombinations {

    /**
     * Generate a base and a permutation for testing algorithms that need both.
     */
    fun <A> arbBaseAndPermutation(
        arb: Arb<A>,
        size: Int = 5
    ): Arb<Pair<FiniteSet.Ordered<A>, Permutation<A>>> =
        generateArbPermutationOfSize(arb, size)

    /**
     * Generate a base with multiple permutations for testing sequences.
     */
    fun <A> arbBaseWithPermutations(
        arb: Arb<A>,
        size: Int = 5,
        count: Int = 3
    ): Arb<Pair<FiniteSet.Ordered<A>, List<Permutation<A>>>> {
        return generateArbOrderedFiniteSetOfSize(arb, size).flatMap { base ->
            Arb.list(
                Arb.shuffle(base.order).map { shuffled ->
                    val mapping = base.order.zip(shuffled).toMap()
                    val inv = shuffled.zip(base.order).toMap()
                    val f = Morphism<A, A> { mapping.getValue(it) }
                    val g = Morphism<A, A> { inv.getValue(it) }
                    Permutation(base, f, g)
                },
                count..count
            ).map { perms ->
                base to perms
            }
        }
    }
}

// Usage examples in comments:
/*
// Basic usage:
val permArb = generateArbPermutation(Arb.int(), 3, 8)
val (base, perm) = permArb.single()

// For property testing:
checkAll(permArb) { (base, perm) ->
    // Test properties
}

// Testing with specific size:
val fixed = Arb.int().toPermutationOfSize(5)
checkAll(fixed) { (base, perm) ->
    base.size shouldBe 5
}
*/