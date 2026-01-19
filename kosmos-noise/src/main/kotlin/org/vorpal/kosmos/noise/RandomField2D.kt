package org.vorpal.kosmos.noise

import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.analysis.ScalarField
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.linear.instances.FixedVectorSpaces
import org.vorpal.kosmos.linear.values.Vec2

fun interface RandomField2D {
    fun sample(x: Real, y: Real): Real
}

fun RandomField2D.asScalarField(): ScalarField<Real, Vec2<Real>> =
    ScalarField.of(FixedVectorSpaces.vec2(RealAlgebras.RealStarField)) { v -> sample(v.x, v.y) }

/**
 * Perlin's 6t^5-15t^4+10t^3.
 */
private fun fade(t: Real): Real =
    t * t * t * (t * (t * 6 - 15) + 10)





