package org.vorpal.kosmos.algebra.innerproduct

import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.Vec2RInnerProductSpace
import org.vorpal.kosmos.analysis.arbFieldDouble
import org.vorpal.kosmos.analysis.arbVec2R
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.laws.algebra.InnerProductSpaceLaws
import org.vorpal.kosmos.linear.Vec2R

// plus your Vec2R / Vec3R and arbitraries / Eq, etc.

object RealInnerProductFixtures {

    val realEq = Eqs.doubleUlps() /* Eq<Real> */
    val vecEq: Eq<Vec2R> = Eq { x, y -> realEq.eqv(x.x, y.x) && realEq.eqv(x.y, y.y) }
    val vecArb = arbVec2R()
    val scalarArb = arbFieldDouble()

    val vec2RealInnerProductSpaceLaws =
        InnerProductSpaceLaws(
            space = Vec2RInnerProductSpace,
            scalarArb = scalarArb,
            vectorArb = vecArb,
            scalarEq = realEq,
            vectorEq = vecEq,
            isNonNegative = { r -> r >= -1e-10 },
        )
}