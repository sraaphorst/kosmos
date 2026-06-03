package org.vorpal.kosmos.combinatorics.algorithms

import org.vorpal.kosmos.core.finiteset.FiniteSet

/**
 * Fast-path extraction: parses a bitmask and extracts elements into a precisely sized ArrayList.
 *
 * Note: Not inlined, as the JIT compiler handles standard loops efficiently without any
 * bytecode bloat.
 */
private fun <A> extractMask(
    pool: List<A>,
    mask: Int,
    size: Int = Integer.bitCount(mask),
): List<A> {
    val out = ArrayList<A>(size)
    var bits = mask

    // Uses Brian Kernighan's algorithm for counting set bits.
    while (bits != 0) {
        val i = Integer.numberOfTrailingZeros(bits)
        out.add(pool[i])
        bits = bits and (bits - 1)
    }

    return out
}

/**
 * Checks that the given mask range is valid.
 */
private fun requireMaskRange(
    n: Int,
    maskStart: Int,
    maskEndInclusive: Int,
) {
    require(n <= 30) {
        "Set size $n is too large for 32-bit bitmask generation."
    }

    val limit = 1 shl n

    require(maskStart >= 0) {
        "maskStart ($maskStart) must be nonnegative"
    }

    require(maskEndInclusive >= 0) {
        "maskEndInclusive ($maskEndInclusive) must be nonnegative"
    }

    require(maskStart <= maskEndInclusive) {
        "maskStart ($maskStart) must be <= maskEndInclusive ($maskEndInclusive)"
    }

    require(maskEndInclusive < limit) {
        "maskEndInclusive ($maskEndInclusive) must be less than 2^$n ($limit)"
    }
}

// ============================================================================
// LAZY GENERATION (Sequences)
// ============================================================================

/**
 * Lazy generation of all subsets of the given [FiniteSet] as a [Sequence].
 */
fun <A> FiniteSet.Ordered<A>.generateAllSubsets(): Sequence<FiniteSet.Ordered<A>> = sequence {
    val n = size
    require(n <= 30) { "Set size $n is too large for 32-bit bitmask generation." }

    val pool = order
    val total = 1 shl n
    for (mask in 0 until total)
        yield(FiniteSet.ordered(extractMask(pool, mask)))
}

fun <A> FiniteSet.Unordered<A>.generateAllSubsets(): Sequence<FiniteSet.Unordered<A>> =
    toOrdered().generateAllSubsets().map { FiniteSet.unordered(it.order) }

/**
 * Generates all subsets within a specific bitmask range.
 * Useful for partitioning combinatorial workloads across multiple threads.
 */
fun <A> FiniteSet.Ordered<A>.generateSubsetsInRange(maskStart: Int, maskEndInclusive: Int): Sequence<FiniteSet.Ordered<A>> {
    requireMaskRange(size, maskStart, maskEndInclusive)
    val pool = order
    return sequence {
        for (mask in maskStart..maskEndInclusive)
            yield(FiniteSet.ordered(extractMask(pool, mask, Integer.bitCount(mask))))
    }
}

fun <A> FiniteSet.Ordered<A>.generateAllKSubsets(k: Int): Sequence<FiniteSet.Ordered<A>> {
    val n = size
    require(k in 0..n) { "k ($k) must be between 0 and $n" }
    require(n <= 30) { "Set size $n is too large for 32-bit bitmask generation." }

    if (k == 0) return sequenceOf(FiniteSet.ordered(emptyList()))
    if (k == n) return sequenceOf(this)

    val pool = order
    return sequence {
        val limit = 1 shl n
        var mask = (1 shl k) - 1

        while (mask < limit) {
            yield(FiniteSet.ordered(extractMask(pool, mask, k)))

            // Gosper's Hack
            val c = mask and -mask
            val r = mask + c
            mask = (((r xor mask) ushr 2) / c) or r
        }
    }
}

fun <A> FiniteSet.Unordered<A>.generateAllKSubsets(k: Int): Sequence<FiniteSet.Unordered<A>> =
    toOrdered().generateAllKSubsets(k).map { FiniteSet.unordered(it.order) }

fun <A> FiniteSet.Ordered<A>.generateAllSubsetsGraded(): Sequence<FiniteSet.Ordered<A>> =
    sequence {
        for (k in 0..size)
            yieldAll(generateAllKSubsets(k))
    }

/**
 * Generates k-subsets whose masks lie in increasing integer mask order.
 *
 * This is the natural colex/combinadic-style order induced by bit positions.
 */
fun <A> FiniteSet.Ordered<A>.generateKSubsetsInRange(
    k: Int,
    maskStart: Int,
    maskEndInclusive: Int,
): Sequence<FiniteSet.Ordered<A>> {
    val n = size
    require(k in 0..n) {
        "k ($k) must be between 0 and $n"
    }
    requireMaskRange(n, maskStart, maskEndInclusive)
    require(Integer.bitCount(maskStart) == k) {
        "maskStart must have exactly $k bits set, but had ${Integer.bitCount(maskStart)}"
    }
    require(Integer.bitCount(maskEndInclusive) == k) {
        "maskEndInclusive must have exactly $k bits set, but had ${Integer.bitCount(maskEndInclusive)}"
    }

    if (k == 0) {
        return sequenceOf(FiniteSet.ordered(emptyList()))
    }
    val pool = order
    return sequence {
        var mask = maskStart
        while (mask <= maskEndInclusive) {
            yield(FiniteSet.ordered(extractMask(pool, mask, k)))
            if (mask == maskEndInclusive) break
            val c = mask and -mask
            val r = mask + c
            mask = (((r xor mask) ushr 2) / c) or r
        }
    }
}

// ============================================================================
// EAGER GENERATION (Power Sets)
// ============================================================================

fun <A> FiniteSet.Ordered<A>.powerSet(): FiniteSet.Ordered<FiniteSet.Ordered<A>> {
    val n = size

    require(n <= 20) {
        "Set size $n is too large for eager power set generation. Use sequence equivalents."
    }

    return FiniteSet.ordered(generateAllSubsetsGraded().toList())
}

fun <A> FiniteSet.Unordered<A>.powerSet(): FiniteSet.Unordered<FiniteSet.Unordered<A>> {
    val pool = this.toOrdered().order
    val n = pool.size
    require(n <= 20) { "Set size $n is too large for eager power set generation. Use sequence equivalents." }

    val total = 1 shl n
    val out = buildList(total) {
        for (mask in 0 until total) {
            add(FiniteSet.unordered(extractMask(pool, mask)))
        }
    }
    return FiniteSet.unordered(out)
}
