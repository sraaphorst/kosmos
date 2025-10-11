package org.vorpal.kosmos.combinatorial.recurrence

import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

/**
 * Base interface for all cached univariate sequences.
 *
 * Every implementing sequence acts as a [Sequence] of [BigInteger]
 * and supports indexed access via [invoke].
 */
interface CachedSequence : Sequence<BigInteger> {
    fun createCache() = ConcurrentHashMap<Int, BigInteger>()

    /** The initial seed values (a₀, a₁, …). */
    val initial: List<BigInteger>

    /** Retrieve or compute the nth term aₙ. */
    operator fun invoke(n: Int): BigInteger

    /** Clear all caches. */
    fun clear()
}
