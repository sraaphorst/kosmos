package org.vorpal.kosmos.core

import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import org.vorpal.kosmos.core.finiteset.FiniteSet

/**
 * Generate an Arb<FiniteSet.Ordered<A>> with size bounds.
 * The final set size may be smaller than requested if duplicate elements are generated.
 */
fun <A> generateArbOrderedFiniteSet(
    arb: Arb<A>,
    lowerBound: Int = 0,
    upperBoundInclusive: Int = 20
): Arb<FiniteSet.Ordered<A>> {
    require(lowerBound >= 0) { "lowerBound must be non-negative" }
    require(upperBoundInclusive >= lowerBound) { "upperBoundInclusive must be >= lowerBound" }

    return Arb.list(arb, lowerBound..upperBoundInclusive)
        .map { FiniteSet.ordered(it) }
}

/**
 * Generate an Arb<FiniteSet.Unordered<A>> with size bounds.
 */
fun <A> generateArbUnorderedFiniteSet(
    arb: Arb<A>,
    lowerBound: Int = 0,
    upperBoundInclusive: Int = 20
): Arb<FiniteSet.Unordered<A>> {
    require(lowerBound >= 0) { "lowerBound must be non-negative" }
    require(upperBoundInclusive >= lowerBound) { "upperBoundInclusive must be >= lowerBound" }

    return Arb.set(arb, lowerBound..upperBoundInclusive)
        .map { FiniteSet.unordered(it) }
}

/**
 * Generate an Arb<FiniteSet.Ordered<A>> with exactly the specified size.
 * Uses a retry mechanism to ensure the target size is achieved.
 */
fun <A> generateArbOrderedFiniteSetOfSize(
    arb: Arb<A>,
    exactSize: Int,
): Arb<FiniteSet.Ordered<A>> {
    require(exactSize >= 0) { "exactSize must be non-negative" }

    return if (exactSize == 0) {
        Arb.constant(FiniteSet.empty())
    } else {
        Arb.set(arb, exactSize..exactSize).map { set ->
            // If we don't get exactly the right size, pad with more elements
            if (set.size == exactSize) {
                FiniteSet.ordered(set)
            } else {
                // Generate additional unique elements to reach exact size
                val additional = generateSequence { arb.single() }
                    .filter { it !in set }
                    .take(exactSize - set.size)
                    .toList()
                FiniteSet.ordered(set + additional)
            }
        }
    }
}

/**
 * Generate an Arb<FiniteSet.Unordered<A>> with exactly the specified size.
 */
fun <A> generateArbUnorderedFiniteSetOfSize(
    arb: Arb<A>,
    exactSize: Int
): Arb<FiniteSet.Unordered<A>> {
    require(exactSize >= 0) { "exactSize must be non-negative" }

    return if (exactSize == 0) {
        Arb.constant(FiniteSet.unordered(emptySet<A>()))
    } else {
        generateArbOrderedFiniteSetOfSize(arb, exactSize)
            .map { it.toUnordered() }
    }
}

/**
 * Generate an Arb<FiniteSet<A>> that randomly chooses between Ordered and Unordered.
 */
fun <A> generateArbFiniteSet(
    arb: Arb<A>,
    lowerBound: Int = 0,
    upperBoundInclusive: Int = 20
): Arb<FiniteSet<A>> {
    return Arb.choice(
        generateArbOrderedFiniteSet(arb, lowerBound, upperBoundInclusive),
        generateArbUnorderedFiniteSet(arb, lowerBound, upperBoundInclusive)
    )
}

/**
 * Generate non-empty FiniteSets
 */
fun <A> generateArbNonEmptyOrderedFiniteSet(
    arb: Arb<A>,
    upperBoundInclusive: Int = 20
): Arb<FiniteSet.Ordered<A>> {
    return generateArbOrderedFiniteSet(arb, 1, upperBoundInclusive)
}

fun <A> generateArbNonEmptyUnorderedFiniteSet(
    arb: Arb<A>,
    upperBoundInclusive: Int = 20
): Arb<FiniteSet.Unordered<A>> {
    return generateArbUnorderedFiniteSet(arb, 1, upperBoundInclusive)
}

/**
 * Generate FiniteSets with elements from multiple Arbs (for heterogeneous testing)
 */
fun <A> generateArbOrderedFiniteSetFromMultiple(
    arb: Arb<A>,
    vararg arbs: Arb<A>,
    lowerBound: Int = 0,
    upperBoundInclusive: Int = 20
): Arb<FiniteSet.Ordered<A>> {
    require(arbs.isNotEmpty()) { "Must provide at least one Arb" }

    return Arb.list(Arb.choice(arb, *arbs), lowerBound..upperBoundInclusive)
        .map { FiniteSet.ordered(it) }
}

/**
 * Specialized generators for common types
 */
object ArbFiniteSet {

    // Common integer sets
    fun arbSmallIntSet(
        lowerBound: Int = 0,
        upperBoundInclusive: Int = 10
    ): Arb<FiniteSet.Ordered<Int>> =
        generateArbOrderedFiniteSet(Arb.int(1..100), lowerBound, upperBoundInclusive)

    fun arbPositiveIntSet(
        lowerBound: Int = 0,
        upperBoundInclusive: Int = 20
    ): Arb<FiniteSet.Ordered<Int>> =
        generateArbOrderedFiniteSet(Arb.positiveInt(), lowerBound, upperBoundInclusive)

    // String sets
    fun arbStringSet(
        lowerBound: Int = 0,
        upperBoundInclusive: Int = 15
    ): Arb<FiniteSet.Ordered<String>> =
        generateArbOrderedFiniteSet(Arb.string(1..10), lowerBound, upperBoundInclusive)

    // Character sets
    fun arbCharSet(
        lowerBound: Int = 0,
        upperBoundInclusive: Int = 26
    ): Arb<FiniteSet.Ordered<Char>> =
        generateArbOrderedFiniteSet(Arb.char('a'..'z'), lowerBound, upperBoundInclusive)

    // Boolean sets (naturally limited to size 0-2)
    fun arbBooleanSet(): Arb<FiniteSet.Ordered<Boolean>> =
        generateArbOrderedFiniteSet(Arb.boolean(), 0, 2)

    // Pair sets
    fun <A, B> arbPairSet(
        arbA: Arb<A>,
        arbB: Arb<B>,
        lowerBound: Int = 0,
        upperBoundInclusive: Int = 15
    ): Arb<FiniteSet.Ordered<Pair<A, B>>> =
        generateArbOrderedFiniteSet(Arb.pair(arbA, arbB), lowerBound, upperBoundInclusive)

    // Sets of sets (nested)
    fun <A> arbNestedSet(
        innerArb: Arb<A>,
        maxInnerSize: Int = 5,
        maxOuterSize: Int = 5
    ): Arb<FiniteSet.Ordered<FiniteSet.Ordered<A>>> =
        generateArbOrderedFiniteSet(
            generateArbOrderedFiniteSet(innerArb, 0, maxInnerSize),
            0,
            maxOuterSize
        )
}

/**
 * Extension functions for easier usage
 */
fun <A> Arb<A>.toOrderedFiniteSetArb(
    lowerBound: Int = 0,
    upperBoundInclusive: Int = 20
): Arb<FiniteSet.Ordered<A>> = generateArbOrderedFiniteSet(this, lowerBound, upperBoundInclusive)

fun <A> Arb<A>.toUnorderedFiniteSetArb(
    lowerBound: Int = 0,
    upperBoundInclusive: Int = 20
): Arb<FiniteSet.Unordered<A>> = generateArbUnorderedFiniteSet(this, lowerBound, upperBoundInclusive)

fun <A> Arb<A>.toFiniteSetArb(
    lowerBound: Int = 0,
    upperBoundInclusive: Int = 20
): Arb<FiniteSet<A>> = generateArbFiniteSet(this, lowerBound, upperBoundInclusive)

// Specific size variants
fun <A> Arb<A>.toOrderedFiniteSetOfSize(exactSize: Int): Arb<FiniteSet.Ordered<A>> =
    generateArbOrderedFiniteSetOfSize(this, exactSize)

fun <A> Arb<A>.toUnorderedFiniteSetOfSize(exactSize: Int): Arb<FiniteSet.Unordered<A>> =
    generateArbUnorderedFiniteSetOfSize(this, exactSize)

/**
 * Useful combinations for testing set operations
 */
object TestingCombinations {

    /**
     * Generate pairs of sets for testing binary operations (union, intersection, etc.)
     */
    fun <A> arbSetPair(
        arb: Arb<A>,
        maxSize: Int = 10
    ): Arb<Pair<FiniteSet.Ordered<A>, FiniteSet.Ordered<A>>> =
        Arb.pair(
            generateArbOrderedFiniteSet(arb, 0, maxSize),
            generateArbOrderedFiniteSet(arb, 0, maxSize)
        )

    /**
     * Generate triples of sets for testing associativity
     */
    fun <A> arbSetTriple(
        arb: Arb<A>,
        maxSize: Int = 8
    ): Arb<Triple<FiniteSet.Ordered<A>, FiniteSet.Ordered<A>, FiniteSet.Ordered<A>>> =
        Arb.triple(
            generateArbOrderedFiniteSet(arb, 0, maxSize),
            generateArbOrderedFiniteSet(arb, 0, maxSize),
            generateArbOrderedFiniteSet(arb, 0, maxSize)
        )

    /**
     * Generate a set and a subset for testing subset relationships
     */
    fun <A> arbSetAndSubset(
        arb: Arb<A>,
        maxSize: Int = 15
    ): Arb<Pair<FiniteSet.Ordered<A>, FiniteSet.Ordered<A>>> =
        generateArbOrderedFiniteSet(arb, 1, maxSize).flatMap { parentSet ->
            val subsetArb = Arb.list(Arb.element(parentSet.order), 0..parentSet.size)
                .map { FiniteSet.ordered(it) }
            Arb.pair(
                Arb.constant(parentSet),
                subsetArb
            )
        }
}

// Usage examples in comments:
/*
// Basic usage:
val intSetArb = generateArbOrderedFiniteSet(Arb.int(), 0, 10)
val stringSetArb = Arb.string().toOrderedFiniteSetArb(1, 5)

// For property testing:
checkAll(intSetArb) { set ->
    set.size <= 10
    set.toSet() == set.backing
}

// Testing set operations:
checkAll(TestingCombinations.arbSetPair(Arb.int())) { (setA, setB) ->
    (setA union setB).size >= maxOf(setA.size, setB.size)
}

// Exact size generation:
val exactlyFiveElements = Arb.int().toOrderedFiniteSetOfSize(5)
*/