package org.vorpal.kosmos.analysis

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.Vec2RSpace
import org.vorpal.kosmos.linear.Vec2R
import org.vorpal.kosmos.testutils.shouldBeApproximately

/**
 * Basic correctness tests for numerical derivatives and differentials
 * defined in [Derivative].
 *
 * The test verifies that for f(x, y) = x² + y², we have:
 *  ∇f = (2x, 2y)
 *  and  D_v f(p) = ∇f(p) · v
 */
class DerivativeTest : StringSpec({

    val space = Vec2RSpace  // assumed to exist, the usual ℝ² vector space

    val f = ScalarFields.of(space) { p: Vec2R ->
        p.x * p.x + p.y * p.y
    }

    "directional derivative matches analytical gradient" {
        val p = Vec2R(1.0, 2.0)
        val v = Vec2R(3.0, -1.0)

        val expected = 2 * p.x * v.x + 2 * p.y * v.y

        val numeric = Derivative.derivativeAt(
            space,
            f::invoke,
            p,
            v
        )

        numeric shouldBeApproximately expected
    }

    "dReal produces covector field equivalent to gradient" {
        val df = f.dReal()

        val p = Vec2R(1.5, -2.0)
        val v = Vec2R(1.0, 1.0)

        val expected = 2 * p.x * v.x + 2 * p.y * v.y
        val result = df(p)(v)

        result shouldBeApproximately expected
    }

    "gradient corresponds to (2x, 2y)" {
        val df = f.dReal()
        val p = Vec2R(1.0, 2.0)

        val gradX = df(p)(Vec2R(1.0, 0.0))
        val gradY = df(p)(Vec2R(0.0, 1.0))

        gradX shouldBeApproximately (2 * p.x)
        gradY shouldBeApproximately (2 * p.y)
    }
})