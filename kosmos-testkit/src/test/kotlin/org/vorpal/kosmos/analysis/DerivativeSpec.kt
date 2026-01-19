package org.vorpal.kosmos.analysis

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.analysis.Derivative.dReal
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.linear.values.Vec2
import org.vorpal.kosmos.testutils.shouldBeApproximately

/**
 * Basic correctness tests for numerical derivatives and differentials
 * defined in [Derivative].
 *
 * The test verifies that for f(x, y) = x² + y², we have:
 *  ∇f = (2x, 2y)
 *  and  D_v f(p) = ∇f(p) · v
 */
class DerivativeSpec : StringSpec({

    val space = vec2Space  // assumed to exist, the usual ℝ² vector space

    val f = ScalarField.of(space) { p: Vec2<Real> ->
        p.x * p.x + p.y * p.y
    }

    "directional derivative matches analytical gradient" {
        val p = Vec2(1.0, 2.0)
        val v = Vec2(3.0, -1.0)

        val expected = 2 * p.x * v.x + 2 * p.y * v.y

        val numeric = Derivative.derivativeAt(
            space,
            f::invoke,
            p,
            v,
            1e-6
        )

        numeric shouldBeApproximately expected
    }

    "dReal produces covector field equivalent to gradient" {
        val df = f.dReal()

        val p = Vec2(1.5, -2.0)
        val v = Vec2(1.0, 1.0)

        val expected = 2 * p.x * v.x + 2 * p.y * v.y
        val result = df(p)(v)

        result shouldBeApproximately expected
    }

    "gradient corresponds to (2x, 2y)" {
        val df = f.dReal()
        val p = Vec2(1.0, 2.0)

        val gradX = df(p)(Vec2(1.0, 0.0))
        val gradY = df(p)(Vec2(0.0, 1.0))

        gradX shouldBeApproximately (2 * p.x)
        gradY shouldBeApproximately (2 * p.y)
    }
})