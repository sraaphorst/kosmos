package org.vorpal.kosmos.laws.core

import org.vorpal.kosmos.algebra.IndexFunction
import org.vorpal.kosmos.algebra.Indexable
import org.vorpal.kosmos.laws.core.IndexFunctionLaws.Context
import java.math.BigInteger

/**
 * Aggregated law suite for any Indexable sequence and its IndexFunction companion.
 *
 * Combines:
 *  - IndexFunctionLaws: functor / monad / composition consistency
 *  - IndexableFixedPointLaws: fixed-point structure
 *
 * Works with self-indexing number-theoretic structures such as:
 *  - PrimeLattice
 *  - FibonacciLattice
 *  - PerfectLattice (future)
 */
object IndexableLaws {

    data class Config<
            T : IndexFunction<T>,
            S : Indexable<S, BigInteger>
            >(
        val name: String,
        val indexable: S,
        val ctx: Context<T>,
        val checkFixedPoints: Boolean = true,
        val expectNone: Boolean = false,
        val expectedFixedPoints: List<Int> = emptyList(),
        val range: IntRange = 1..200
    )

    /**
     * Run both IndexFunctionLaws and IndexableFixedPointLaws.
     */
    fun <
            T : IndexFunction<T>,
            S : Indexable<S, BigInteger>
            > sanity(cfg: Config<T, S>) {
        println("Running IndexableLaws for ${cfg.name}...")

        // Composition / Monad / Functor laws
        IndexFunctionLaws.sanity(cfg.ctx)

        // Fixed point checks
        if (cfg.checkFixedPoints) {
            val fpCtx = IndexableFixedPointLaws.Context(
                instance = cfg.indexable,
                range = cfg.range,
                expectedFixedPoints = cfg.expectedFixedPoints,
                expectNone = cfg.expectNone
            )
            IndexableFixedPointLaws.verifyFixedPoints(fpCtx)
            if (!cfg.expectNone) IndexableFixedPointLaws.divergenceAfterLastFixedPoint(fpCtx)
        }

        println("✓ ${cfg.name} passed IndexableLaws")
    }
}