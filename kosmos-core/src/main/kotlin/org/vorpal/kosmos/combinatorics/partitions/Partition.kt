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
     * Determines, using lazy computation, if the parts comprise only odd elements.
     */
    val hasOnlyOddParts: Boolean by lazy {
        parts.all { it % 2 == 1 }
    }

    /**
     * This requires parts to be sorted by non-increasing order, which is required by the contract
     * for constructing a Partition.
     */
    val hasDistinctParts: Boolean by lazy {
        parts.zipWithNext().all { (a, b) -> a != b }
    }

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
