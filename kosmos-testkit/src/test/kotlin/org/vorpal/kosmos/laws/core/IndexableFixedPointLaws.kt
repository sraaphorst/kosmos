package org.vorpal.kosmos.laws.core

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.Indexable
import java.math.BigInteger

/**
 * Generic law tests for fixed points of self-indexing sequences.
 *
 * Given an Indexable<S, N>, we can test:
 *  - existence/nonexistence of fixed points (index(n) == n)
 *  - that known fixed points match expected values
 *  - monotonic divergence beyond a threshold
 */
object IndexableFixedPointLaws {

    data class Context<S, N>(
        val instance: Indexable<S, N>,
        val range: IntRange = 1..200,
        val expectedFixedPoints: List<Int> = emptyList(),
        val expectNone: Boolean = false
    )

    /**
     * Find all fixed points in the given integer range.
     */
    fun <S> findFixedPoints(indexable: Indexable<S, BigInteger>, range: IntRange): List<Int> =
        range.filter { n ->
            indexable.index(n) == BigInteger.valueOf(n.toLong())
        }

    /**
     * Verify the expected set of fixed points (if any).
     */
    fun <S> verifyFixedPoints(ctx: Context<S, BigInteger>) {
        val found = findFixedPoints(ctx.instance, ctx.range)

        when {
            ctx.expectNone -> found.shouldBeEmpty()
            else -> found shouldBe ctx.expectedFixedPoints
        }
    }

    /**
     * Optional: check that the sequence diverges monotonically after the last fixed point.
     * For strictly increasing indexables like primes or Fibonacci, this ensures only finite fixed points.
     */
    fun <S> divergenceAfterLastFixedPoint(ctx: Context<S, BigInteger>) {
        val fixed = findFixedPoints(ctx.instance, ctx.range)
        if (fixed.isNotEmpty()) {
            val start = fixed.max() + 1
            val lastVal = ctx.instance.index(start)
            var prev = lastVal
            for (n in (start + 1)..ctx.range.last) {
                val current = ctx.instance.index(n)
                // As long as index(n) > n eventually holds, we consider it diverging
                if (current > prev && current > BigInteger.valueOf(n.toLong())) return
                prev = current
            }
            error("Sequence did not diverge beyond its last fixed point within range ${ctx.range}")
        }
    }
}