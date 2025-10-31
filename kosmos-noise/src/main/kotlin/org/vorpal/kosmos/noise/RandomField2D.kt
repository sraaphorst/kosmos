package org.vorpal.kosmos.noise

import org.vorpal.kosmos.algebra.structures.instances.Real
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.Vec2RSpace
import org.vorpal.kosmos.analysis.ScalarField
import org.vorpal.kosmos.analysis.ScalarFields
import org.vorpal.kosmos.linear.Vec2R

fun interface RandomField2D {
    fun sample(x: Double, y: Double): Double
}

fun RandomField2D.asScalarField(): ScalarField<Real, Vec2R> =
    ScalarFields.of(Vec2RSpace) { v -> sample(v.x, v.y) }

/**
 * Perlin's 6t^5-15t^4+10t^3.
 */
private fun fade(t: Double): Double =
    t * t * t * (t * (t * 6 - 15) + 10)





