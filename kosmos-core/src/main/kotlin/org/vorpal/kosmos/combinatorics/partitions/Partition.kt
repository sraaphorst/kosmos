package org.vorpal.kosmos.combinatorics.partitions

import java.math.BigInteger

/**
 * An integer partition represented canonically as a finite nonincreasing
 * list of positive parts.
 *
 * Its weight is the sum of its parts. The empty list is the unique
 * partition of zero.
 */
class Partition private constructor(
    val parts: List<Int>
) {
    init {
        require(parts.all { it > 0 }) {
            "An integer partition must have only positive parts: $parts"
        }
        require(parts.zipWithNext().all { (a, b) -> a >= b }) {
            "An integer partition must have parts in nonincreasing order: $parts"
        }
    }

    val weight: BigInteger = parts.sumOf(Int::toBigInteger)
    val length: Int = parts.size

    /**
     * True if no part of this partition is divisible by [k], i.e. this is a
     * *k-regular* partition. Vacuously true for the empty partition.
     *
     * This is the domain restriction for [GlaisherBijection.kRegularToBoundedMultiplicities]:
     * for k = 2 it specializes to [hasOnlyOddParts].
     *
     * @throws IllegalArgumentException if [k] is less than 2.
     */
    fun hasNoPartDivisibleBy(k: Int): Boolean {
        require(k >= 2) {
            "k must be at least 2, but got: $k"
        }
        return parts.none { it % k == 0 }
    }

    /** True if every part is odd. Equivalent to [hasNoPartDivisibleBy] with k = 2. */
    val hasOnlyOddParts: Boolean
        get() = hasNoPartDivisibleBy(2)

    /**
     * True if no part of this partition occurs [k] or more times, i.e. every
     * part's multiplicity is strictly less than [k]. Vacuously true for the
     * empty partition.
     *
     * This is the domain restriction for [GlaisherBijection.boundedMultiplicitiesToKRegular]:
     * for k = 2 it specializes to [hasDistinctParts].
     *
     * @throws IllegalArgumentException if [k] is less than 2.
     */
    fun hasMultiplicitiesLessThan(k: Int): Boolean {
        require(k >= 2) {
            "k must be at least 2, but got: $k"
        }
        return parts
            .groupingBy { it }
            .eachCount()
            .values
            .all { multiplicity -> multiplicity < k }
    }

    /** True if all parts are distinct, i.e. no part is repeated. Equivalent to [hasMultiplicitiesLessThan] with k = 2. */
    val hasDistinctParts: Boolean
        get() = hasMultiplicitiesLessThan(2)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Partition) return false

        if (parts != other.parts) return false

        return true
    }

    override fun hashCode(): Int =
        parts.hashCode()

    override fun toString() =
        "Partition($weight): $parts"

    companion object {
        fun of(parts: Iterable<Int>): Partition =
            Partition(parts.toList())

        fun of(vararg parts: Int): Partition =
            of(parts.asIterable())
    }
}
