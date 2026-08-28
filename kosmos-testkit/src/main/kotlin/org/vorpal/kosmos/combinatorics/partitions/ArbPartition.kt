package org.vorpal.kosmos.combinatorics.partitions

import io.kotest.property.Arb
import io.kotest.property.arbitrary.*

/**
 * Generate an arbitrary [Partition] by drawing parts from [partArb] and sorting them
 * into the canonical nonincreasing form [Partition] requires.
 *
 * Note: [Partition.of] no longer sorts its input itself, so every generator in this
 * file sorts before construction.
 */
fun generateArbPartition(
    partArb: Arb<Int> = Arb.int(1..30),
    sizeRange: IntRange = 0..8
): Arb<Partition> =
    Arb.list(partArb, sizeRange).map { Partition.of(it.sortedDescending()) }

/**
 * Generate an arbitrary [Partition] whose parts are all odd.
 * [oddPartArb] must only ever produce odd values; the default draws
 * 2k+1 for k in 0..14, i.e. odd values in 1..29.
 */
fun generateArbOddPartition(
    oddPartArb: Arb<Int> = Arb.int(0..14).map { 2 * it + 1 },
    sizeRange: IntRange = 0..8
): Arb<Partition> =
    Arb.list(oddPartArb, sizeRange).map { Partition.of(it.sortedDescending()) }

/**
 * Generate an arbitrary [Partition] whose parts are all distinct.
 * Draws from a set (rather than a list) so duplicates are impossible by construction;
 * as with other set-based generators in this module, the resulting size may be smaller
 * than requested if [partArb]'s range is too small relative to [sizeRange].
 */
fun generateArbDistinctPartition(
    partArb: Arb<Int> = Arb.int(1..50),
    sizeRange: IntRange = 0..8
): Arb<Partition> =
    Arb.set(partArb, sizeRange).map { Partition.of(it.sortedDescending()) }

/**
 * Generate an arbitrary [Partition] whose parts are all odd AND distinct
 * (i.e. lies in the intersection of both restricted domains).
 *
 * Every part here has multiplicity 1, and multiplicity 1 in binary is just `1`,
 * so both Euler-Glaisher directions map any such partition to itself: these are
 * fixed points of *both* directions, not neither. Useful precisely for pinning
 * that identity behavior down explicitly, in addition to satisfying both
 * `require` guards simultaneously.
 */
fun generateArbOddDistinctPartition(
    sizeRange: IntRange = 0..8
): Arb<Partition> =
    Arb.set(Arb.int(0..24).map { 2 * it + 1 }, sizeRange).map { Partition.of(it.sortedDescending()) }

/**
 * Specialized generators for common cases, mirroring the style of
 * [org.vorpal.kosmos.combinatorics.ArbPermutation] / `ArbBijection`.
 */
object ArbPartition {
    /** Any valid partition, no restriction on parts. */
    fun arbSmall(sizeRange: IntRange = 0..8): Arb<Partition> =
        generateArbPartition(sizeRange = sizeRange)

    /** A partition into odd parts only. */
    fun arbOdd(sizeRange: IntRange = 0..8): Arb<Partition> =
        generateArbOddPartition(sizeRange = sizeRange)

    /** A partition into distinct parts only. */
    fun arbDistinct(sizeRange: IntRange = 0..8): Arb<Partition> =
        generateArbDistinctPartition(sizeRange = sizeRange)

    /** A partition into parts that are both odd and distinct. */
    fun arbOddDistinct(sizeRange: IntRange = 0..8): Arb<Partition> =
        generateArbOddDistinctPartition(sizeRange = sizeRange)

    /** The unique partition of zero (the empty partition). */
    fun arbEmpty(): Arb<Partition> =
        Arb.constant(Partition.of())
}

/**
 * Extension functions for easier usage, mirroring `Arb<A>.toPermutationArb` etc.
 */
fun Arb<Int>.toPartitionArb(sizeRange: IntRange = 0..8): Arb<Partition> =
    generateArbPartition(this, sizeRange)

fun Arb<Int>.toDistinctPartitionArb(sizeRange: IntRange = 0..8): Arb<Partition> =
    generateArbDistinctPartition(this, sizeRange)
