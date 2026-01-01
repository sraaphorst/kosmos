package org.vorpal.kosmos.algebra.innerproduct

import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.Vec2RInnerProductSpace
import org.vorpal.kosmos.analysis.arbFieldReal
import org.vorpal.kosmos.analysis.arbVec2R
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.laws.algebra.InnerProductSpaceLaws
import org.vorpal.kosmos.linear.Vec2R

// plus your Vec2R / Vec3R and arbitraries / Eq, etc.

object RealInnerProductFixtures {

    val realEq = Eqs.realApprox()
    val vecEq: Eq<Vec2R> = Eq { x, y -> realEq(x.x, y.x) && realEq(x.y, y.y) }
    val vecArb = arbVec2R()
    val scalarArb = arbFieldReal()

    val vec2RealInnerProductSpaceLaws =
        InnerProductSpaceLaws(
            space = Vec2RInnerProductSpace,
            scalarArb = scalarArb,
            vectorArb = vecArb,
            eqReal = Eqs.realApprox(),
            eqV = vecEq
        )
}
