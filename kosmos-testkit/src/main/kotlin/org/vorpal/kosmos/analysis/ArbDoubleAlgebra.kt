package org.vorpal.kosmos.analysis

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import org.vorpal.kosmos.algebra.structures.instances.Vec2RSpace
import org.vorpal.kosmos.linear.Vec2R
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin

/**
 * Arbitrary for finite, non-NaN doubles suitable for field operations.
 */
fun arbFieldDouble(): Arb<Double> =
    Arb.double(-1000.0, 1000.0)
        .filter { it.isFinite() && !it.isNaN() }

/**
 * Arbitrary for non-zero doubles (for division tests).
 */
fun arbNonZeroDouble(): Arb<Double> =
    arbFieldDouble().filter { abs(it) > 1e-6 }

/**
 * Arbitrary for Vec2R vectors.
 */
fun arbVec2R(): Arb<Vec2R> = arbitrary {
    val x = arbFieldDouble().filter { abs(it) > 1e-300 }.bind()
    val y = arbFieldDouble().filter { abs(it) > 1e-300 }.bind()
    Vec2R(x, y)
}

/**
 * Arbitrary for scalar fields over Vec2D.
 */
fun arbScalarField(): Arb<ScalarField<Double, Vec2R>> = arbitrary {
    val a = arbFieldDouble().bind()
    val b = arbFieldDouble().bind()
    val c = arbFieldDouble().bind()

    // Create linear scalar field: f(x, y) = a*x + b*y + c
    ScalarFields.of(Vec2RSpace) { v -> a * v.x + b * v.y + c }
}

/**
 * Arbitrary for non-zero scalar fields (for division tests).
 */
fun arbNonZeroScalarField(): Arb<ScalarField<Double, Vec2R>> = arbitrary {
    val a = arbNonZeroDouble().bind()
    val offset = arbNonZeroDouble().bind()

    // Create field that's always non-zero: f(x, y) = a*(x² + y² + 1) + offset
    ScalarFields.of(Vec2RSpace) { v ->
        a * (v.x * v.x + v.y * v.y + 1.0) + offset
    }
}

/**
 * Arbitrary for unary functions on doubles. These do not have to be continuous, but should be
 * defined on each Double.
 */
fun arbDoubleFunction(): Arb<(Double) -> Double> = arbitrary {
    listOf<(Double) -> Double>(
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
 * Arbitrary for vector fields over Vec2R.
 * Creates linear vector fields: F(x, y) = (ax + by + c, dx + ey + f)
 */
fun arbVectorField(): Arb<VectorField<Double, Vec2R>> = arbitrary {
    val a = arbFieldDouble().bind()
    val b = arbFieldDouble().bind()
    val c = arbFieldDouble().bind()
    val d = arbFieldDouble().bind()
    val e = arbFieldDouble().bind()
    val f = arbFieldDouble().bind()

    VectorFields.of(Vec2RSpace) { v ->
        Vec2R(
            x = a * v.x + b * v.y + c,
            y = d * v.x + e * v.y + f
        )
    }
}

/**
 * Arbitrary for rotation vector fields (useful for testing composition).
 * Creates fields like F(x, y) = (-y, x) scaled by a factor.
 */
fun arbRotationVectorField(): Arb<VectorField<Double, Vec2R>> = arbitrary {
    val scale = arbFieldDouble().bind()

    VectorFields.of(Vec2RSpace) { v ->
        Vec2R(
            x = -v.y * scale,
            y = v.x * scale
        )
    }
}

/**
 * Arbitrary for vector-to-vector transformations.
 */
fun arbVectorTransform(): Arb<(Vec2R) -> Vec2R> = arbitrary {
    listOf<(Vec2R) -> Vec2R>(
        { v -> Vec2R(v.x * 2.0, v.y * 2.0) },
        { v -> Vec2R(v.x + 1.0, v.y + 1.0) },
        { v -> Vec2R(-v.y, v.x) },
        { v -> Vec2R(v.x, -v.y) },
        { v -> Vec2R(v.y, v.x) }
    ).random()
}
