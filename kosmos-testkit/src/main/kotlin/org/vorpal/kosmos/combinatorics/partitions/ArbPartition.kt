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

// ---------------------------------------------------------------------------
// General, k-parameterized generators for GlaisherBijection.
// ---------------------------------------------------------------------------

/**
 * An `Arb<Int>` of positive integers that are not divisible by [k] (i.e. "k-regular"
 * integers), by construction rather than by filtering: draws a quotient in
 * `0..maxQuotient` and a nonzero residue in `1 until k`, and returns `quotient*k + residue`.
 */
fun kRegularIntArb(k: Int, maxQuotient: Int = 14): Arb<Int> {
    require(k >= 2) { "k must be at least 2, but was: $k" }
    return Arb.bind(Arb.int(0..maxQuotient), Arb.int(1 until k)) { quotient, residue ->
        quotient * k + residue
    }
}

/**
 * Generate an arbitrary [Partition] whose parts are all "k-regular": none divisible
 * by [k]. This is the domain of [GlaisherBijection.kRegularToBoundedMultiplicities].
 * [partArb] must only ever produce k-regular values; the default is [kRegularIntArb].
 */
fun generateArbKRegularPartition(
    k: Int,
    partArb: Arb<Int> = kRegularIntArb(k),
    sizeRange: IntRange = 0..8
): Arb<Partition> =
    Arb.list(partArb, sizeRange).map { Partition.of(it.sortedDescending()) }

/**
 * Generate an arbitrary [Partition] in which every part occurs fewer than [k] times.
 * This is the domain of [GlaisherBijection.boundedMultiplicitiesToKRegular].
 *
 * Rather than filtering or retrying, any group of parts drawn from [partArb] that
 * would repeat [k] or more times is simply truncated down to `k - 1` copies, so this
 * terminates promptly regardless of [k] or [sizeRange].
 */
fun generateArbBoundedMultiplicityPartition(
    k: Int,
    partArb: Arb<Int> = Arb.int(1..30),
    sizeRange: IntRange = 0..8
): Arb<Partition> {
    require(k >= 2) { "k must be at least 2, but was: $k" }
    return Arb.list(partArb, sizeRange).map { raw ->
        val truncated = raw
            .groupingBy { it }
            .eachCount()
            .flatMap { (value, count) -> List(minOf(count, k - 1)) { value } }
        Partition.of(truncated.sortedDescending())
    }
}

/**
 * Generate an arbitrary [Partition] that is both k-regular and has multiplicities
 * strictly less than [k] (built from [generateArbBoundedMultiplicityPartition] with
 * a k-regular [partArb], so both restrictions hold simultaneously by construction).
 *
 * A part with multiplicity `m < k` has a single-digit base-k expansion (`m` itself),
 * so its `m` copies map to `m` copies of `part * k^0 = part` - unchanged. Combined
 * with k-regularity making the reverse direction's factor-stripping loop a no-op,
 * every partition here is a fixed point of *both* directions of [GlaisherBijection]
 * (see `GlaisherBijectionSpec`) - and this is the *exact* characterization, not just
 * a sufficient one: multiplicity need only be below [k], not equal to 1. For k = 2
 * those coincide (m < 2 forces m = 1), which is why the earlier, k=2-only version of
 * this generator used to draw from a [Arb.set] and no one noticed the difference.
 */
fun generateArbKRegularBoundedPartition(
    k: Int,
    partArb: Arb<Int> = kRegularIntArb(k),
    sizeRange: IntRange = 0..8
): Arb<Partition> =
    generateArbBoundedMultiplicityPartition(
        k = k,
        partArb = partArb,
        sizeRange = sizeRange
    )

// ---------------------------------------------------------------------------
// k = 2 aliases: odd parts / distinct parts (Euler's theorem is Glaisher's at k = 2).
// ---------------------------------------------------------------------------

/**
 * Generate an arbitrary [Partition] whose parts are all odd. Equivalent to
 * [generateArbKRegularPartition] with k = 2.
 * [oddPartArb] must only ever produce odd values; the default draws odd values in 1..29.
 */
fun generateArbOddPartition(
    oddPartArb: Arb<Int> = kRegularIntArb(2),
    sizeRange: IntRange = 0..8
): Arb<Partition> =
    generateArbKRegularPartition(2, oddPartArb, sizeRange)

/**
 * Generate an arbitrary [Partition] whose parts are all distinct. Equivalent to
 * [generateArbBoundedMultiplicityPartition] with k = 2, which truncates any group of
 * duplicate values down to `k - 1 = 1` copy, so duplicates are removed rather than
 * avoided; as a result the resulting size may be smaller than requested if [partArb]'s
 * range is too small relative to [sizeRange] (many draws collide and get truncated away).
 */
fun generateArbDistinctPartition(
    partArb: Arb<Int> = Arb.int(1..50),
    sizeRange: IntRange = 0..8
): Arb<Partition> =
    generateArbBoundedMultiplicityPartition(2, partArb, sizeRange)

/**
 * Generate an arbitrary [Partition] whose parts are all odd AND distinct. Equivalent to
 * [generateArbKRegularBoundedPartition] with k = 2: a fixed point of both Euler-Glaisher
 * directions (see the note on [generateArbKRegularBoundedPartition]).
 */
fun generateArbOddDistinctPartition(
    sizeRange: IntRange = 0..8
): Arb<Partition> =
    generateArbKRegularBoundedPartition(2, sizeRange = sizeRange)

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

    /** A k-regular partition: no part divisible by [k]. */
    fun arbKRegular(k: Int, sizeRange: IntRange = 0..8): Arb<Partition> =
        generateArbKRegularPartition(k, sizeRange = sizeRange)

    /** A partition whose multiplicities are bounded by [k]. */
    fun arbBoundedMultiplicity(k: Int, sizeRange: IntRange = 0..8): Arb<Partition> =
        generateArbBoundedMultiplicityPartition(k, sizeRange = sizeRange)

    /** A partition that is both k-regular and has multiplicities bounded by [k]. */
    fun arbKRegularBounded(k: Int, sizeRange: IntRange = 0..8): Arb<Partition> =
        generateArbKRegularBoundedPartition(k, sizeRange = sizeRange)
}

/**
 * Extension functions for easier usage, mirroring `Arb<A>.toPermutationArb` etc.
 */
fun Arb<Int>.toPartitionArb(sizeRange: IntRange = 0..8): Arb<Partition> =
    generateArbPartition(this, sizeRange)

fun Arb<Int>.toDistinctPartitionArb(sizeRange: IntRange = 0..8): Arb<Partition> =
    generateArbDistinctPartition(this, sizeRange)

/**
 * Combinations pairing a randomly chosen modulus with a matching partition, for
 * property tests over [GlaisherBijection] that want to exercise more than one k
 * rather than a single hardcoded modulus.
 */
object GlaisherTestingCombinations {
    /** A modulus k in [kRange] (k >= 2) paired with a k-regular partition for that k. */
    fun arbKAndKRegularPartition(
        kRange: IntRange = 2..6,
        sizeRange: IntRange = 0..8
    ): Arb<Pair<Int, Partition>> =
        Arb.int(kRange).flatMap { k ->
            generateArbKRegularPartition(k, sizeRange = sizeRange).map { p -> k to p }
        }

    /** A modulus k in [kRange] (k >= 2) paired with a partition whose multiplicities are bounded by that k. */
    fun arbKAndBoundedMultiplicityPartition(
        kRange: IntRange = 2..6,
        sizeRange: IntRange = 0..8
    ): Arb<Pair<Int, Partition>> =
        Arb.int(kRange).flatMap { k ->
            generateArbBoundedMultiplicityPartition(k, sizeRange = sizeRange).map { p -> k to p }
        }

    /** A modulus k in [kRange] (k >= 2) paired with a partition that is a fixed point of both directions for that k. */
    fun arbKAndKRegularBoundedPartition(
        kRange: IntRange = 2..6,
        sizeRange: IntRange = 0..8
    ): Arb<Pair<Int, Partition>> =
        Arb.int(kRange).flatMap { k ->
            generateArbKRegularBoundedPartition(k, sizeRange = sizeRange).map { p -> k to p }
        }
}
