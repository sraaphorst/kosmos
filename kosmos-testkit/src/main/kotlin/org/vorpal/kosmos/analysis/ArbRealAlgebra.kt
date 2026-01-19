package org.vorpal.kosmos.analysis

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.linear.instances.FixedVectorSpaces
import org.vorpal.kosmos.linear.values.Vec2
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin

// We need to use the same space to avoid throwing when checking space compatibility.
val vec2Space = FixedVectorSpaces.vec2(RealAlgebras.RealField)

/**
 * Arbitrary for finite, non-NaN Reals suitable for field operations.
 */
fun arbFieldReal(): Arb<Real> =
    Arb.double(-1000.0, 1000.0)
        .filter { it.isFinite() && !it.isNaN() }

/**
 * Arbitrary for non-zero Reals (for division tests).
 */
fun arbNonZeroReal(): Arb<Real> =
    arbFieldReal().filter { abs(it) > 1e-6 }

/**
 * Arbitrary for Vec2<Real> vectors.
 */
fun arbVec2Real(): Arb<Vec2<Real>> = arbitrary {
    val x = arbFieldReal().filter { abs(it) > 1e-300 }.bind()
    val y = arbFieldReal().filter { abs(it) > 1e-300 }.bind()
    Vec2(x, y)
}

/**
 * Arbitrary for scalar fields over Vec2D.
 */
fun arbScalarField(): Arb<ScalarField<Real, Vec2<Real>>> = arbitrary {
    val a = arbFieldReal().bind()
    val b = arbFieldReal().bind()
    val c = arbFieldReal().bind()

    // Create linear scalar field: f(x, y) = a*x + b*y + c
    ScalarField.of(vec2Space) { v -> a * v.x + b * v.y + c }
}

/**
 * Arbitrary for non-zero scalar fields (for division tests).
 */
fun arbNonZeroScalarField(): Arb<ScalarField<Real, Vec2<Real>>> = arbitrary {
    val a = arbNonZeroReal().bind()
    val offset = arbNonZeroReal().bind()

    // Create field that's always non-zero: f(x, y) = a*(x² + y² + 1) + offset
    ScalarField.of(vec2Space) { v ->
        a * (v.x * v.x + v.y * v.y + 1.0) + offset
    }
}

/**
 * Arbitrary for unary functions on Reals. These do not have to be continuous, but should be
 * defined on each Real.
 */
fun arbRealFunction(): Arb<(Real) -> Real> = arbitrary {
    listOf<(Real) -> Real>(
        { it * 2.0 },
        { it + 10.0 },
        { it * it },
        { abs(it) },
        { if (it > 0) it else -it },
        { sin(it) },
        { exp(it) }
    ).random()
}

/**
 * Arbitrary for vector fields over Vec2<Real>.
 * Creates linear vector fields: F(x, y) = (ax + by + c, dx + ey + f)
 */


fun arbVectorField(): Arb<VectorField<Real, Vec2<Real>>> = arbitrary {
    val a = arbFieldReal().bind()
    val b = arbFieldReal().bind()
    val c = arbFieldReal().bind()
    val d = arbFieldReal().bind()
    val e = arbFieldReal().bind()
    val f = arbFieldReal().bind()

    VectorField.of(vec2Space) { v ->
        Vec2(
            x = a * v.x + b * v.y + c,
            y = d * v.x + e * v.y + f
        )
    }
}

/**
 * Arbitrary for rotation vector fields (useful for testing composition).
 * Creates fields like F(x, y) = (-y, x) scaled by a factor.
 */
fun arbRotationVectorField(): Arb<VectorField<Real, Vec2<Real>>> = arbitrary {
    val scale = arbFieldReal().bind()

    VectorField.of(vec2Space) { v ->
        Vec2(x = -v.y * scale, y = v.x * scale)
    }
}

/**
 * Arbitrary for vector-to-vector transformations.
 */
fun arbVectorTransform(): Arb<(Vec2<Real>) -> Vec2<Real>> = arbitrary {
    listOf<(Vec2<Real>) -> Vec2<Real>>(
        { v -> Vec2(v.x * 2.0, v.y * 2.0) },
        { v -> Vec2(v.x + 1.0, v.y + 1.0) },
        { v -> Vec2(-v.y, v.x) },
        { v -> Vec2(v.x, -v.y) },
        { v -> Vec2(v.y, v.x) }
    ).random()
}
