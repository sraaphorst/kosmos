package org.vorpal.kosmos.combinatorics.partitions

/**
 * The Euler–Glaisher bijection between partitions into odd parts and partitions
 * into distinct parts of the same weight: interpreting each odd part's multiplicity
 * as a binary expansion (forward), or writing every part as `2^k * q` with `q` odd
 * (backward).
 *
 * This is exactly [GlaisherBijection] at `k = 2` (odd parts are the k-regular parts,
 * distinct parts are the parts with multiplicities bounded by k, for k = 2); this
 * object is now a thin, named delegator to it, kept for the more familiar odd/distinct
 * vocabulary. See [GlaisherBijection] for the general theorem, the algorithm, and the
 * `@throws` documentation that applies equally here.
 */
object EulerGlaisherBijection {
    /**
     * Given a partition into odd parts, convert it to the corresponding partition
     * into distinct parts. Delegates to [GlaisherBijection.kRegularToBoundedMultiplicities]
     * with k = 2.
     */
    fun oddPartitionToDistinctPartition(partition: Partition): Partition =
        GlaisherBijection.kRegularToBoundedMultiplicities(partition, 2)

    /**
     * Given a partition into distinct parts, convert it to the corresponding partition
     * into odd parts. Delegates to [GlaisherBijection.boundedMultiplicitiesToKRegular]
     * with k = 2.
     */
    fun distinctPartitionToOddPartition(partition: Partition): Partition =
        GlaisherBijection.boundedMultiplicitiesToKRegular(partition, 2)
}
