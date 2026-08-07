package org.vorpal.kosmos.combinatorics.partitions

class Partition private constructor(
    val parts: List<Int>
) {
    init {
        require(parts.all { it > 0 }) {
            "An integer partition must have only positive parts: $parts"
        }
        require(parts.zipWithNext().all { (a, b) -> a >= b }) {
            "An integer partition must have parts in strictly nonincreasing order: $parts"
        }
    }

    // Note: Kotlin's by lazy defaults to LazyThreadSafetyMode.SYNCHRONIZED, which pays a lock on every first access.
    // If we end up generating large numbers of Partitions in tight loops (partitionsOf(n) for largeish n) and it's
    // single-threaded, by lazy(LazyThreadSafetyMode.NONE) { ... } avoids that cost.
    val weight: Int by lazy {
        parts.sum()
    }

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
            Partition(parts.sortedDescending())

        fun of(vararg parts: Int): Partition =
            of(parts.asIterable())
    }
}
