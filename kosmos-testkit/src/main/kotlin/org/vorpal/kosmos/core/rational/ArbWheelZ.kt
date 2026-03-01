package org.vorpal.kosmos.core.rational

import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.Sample
import io.kotest.property.asSample

/**
 * Arbitrary for [WheelZ], for use in property-based testing of CarlstromWheel,
 * and also usable in Wheel.
 */
object ArbWheelZ : Arb<WheelZ>() {

    /**
     * Edge cases should be *common bug magnets*:
     * kotest will inject these with EdgeConfig.edgecasesGenerationProbability.
     */
    private val edgecases: List<WheelZ> = listOf(
        WheelZ.BOTTOM,
        WheelZ.ZERO,
        WheelZ.ONE,
        WheelZ.INF,

        WheelZ.of(1, 2),
        WheelZ.of(-1, 2),
        WheelZ.of(2, 1),
        WheelZ.of(-2, 1),
        WheelZ.of(1, -2),  // should normalize sign
        WheelZ.of(2, 4)    // should reduce
    )

    override fun edgecase(rs: RandomSource): Sample<WheelZ>? {
        val idx = rs.random.nextInt(edgecases.size)
        return edgecases[idx].asSample()
    }

    /**
     * We bias towards special values even in random sampling:
     * - `35%`: [WheelZ.INF]
     * - `20%`: [WheelZ.ZERO]
     * - `10%`: [WheelZ.ONE]
     * - `10%`: [WheelZ.BOTTOM]
     * - `25%`: random finite, modest magnitudes in range `[-200, 200]`.
     */
    override fun sample(rs: RandomSource): Sample<WheelZ> {
        val roll = rs.random.nextInt(100)

        val value = when {
            roll < 35 -> WheelZ.INF      // 35%
            roll < 55 -> WheelZ.ZERO     // 20%
            roll < 65 -> WheelZ.ONE      // 10%
            roll < 75 -> WheelZ.BOTTOM   // 10%
            else -> {
                // 25%: random finite
                // Keep magnitudes modest for shrinking and speed.
                val n = rs.random.nextInt(-200, 201)
                var d = rs.random.nextInt(-200, 201)
                while (d == 0) {
                    d = rs.random.nextInt(-200, 201)
                }
                WheelZ.of(n, d)
            }
        }

        return value.asSample()
    }
}
