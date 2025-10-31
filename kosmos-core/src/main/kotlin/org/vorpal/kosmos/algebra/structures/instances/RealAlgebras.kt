package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.core.math.clamp
import org.vorpal.kosmos.core.math.lerp
import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.linear.Vec2R
import org.vorpal.kosmos.linear.Vec2R_ZERO

typealias Real = Double

object RealAlgebras {
    object DoubleField : Field<Double> {
        override val add: AbelianGroup<Double> = AbelianGroup.of(
            op = Double::plus,
            identity = 0.0,
            inverse = Double::unaryMinus
        )

        override val mul: AbelianGroup<Double> = AbelianGroup.of(
            op = Double::times,
            identity = 1.0,
            inverse = { if (it != 0.0) 1.0 / it else Double.NaN }
        )
    }

    val RealField = DoubleField

    /**
     * 2D vector space over doubles.
     */
    object Vec2RSpace : FiniteVectorSpace<Real, Vec2R> {
        override val ring: Field<Real> = RealField
        override val dimension: Int = 2

        override val group: AbelianGroup<Vec2R> = AbelianGroup.of(
            op = { a, b -> Vec2R(a.x + b.x, a.y + b.y) },
            identity = Vec2R_ZERO,
            inverse = { Vec2R(-it.x, -it.y) }
        )

        override val action: Action<Real, Vec2R> = Action { scalar, vec ->
            Vec2R(scalar * vec.x, scalar * vec.y)
        }
    }
}

// ============================================================================
// Helper Extensions
// ============================================================================

/**
 * Dot product for Vec2R (not part of VectorSpace interface, but useful for testing).
 */
infix fun Vec2R.dot(other: Vec2R): Real =
    this.x * other.x + this.y * other.y

/**
 * Magnitude (norm) of Vec2R.
 */
val Vec2R.magnitude: Real
    get() = kotlin.math.sqrt(x * x + y * y)

/**
 * Normalized version of Vec2R.
 */
fun Vec2R.normalize(): Vec2R {
    val mag = magnitude
    return if (mag > 0.0) Vec2R(x / mag, y / mag) else Vec2R_ZERO
}

/**
 * Linearly interpolate from the calling vector to the to vector for t.
 * If t is in [0, 1], the value will range linearly between this and to.
 * If t < 0 or t > 1, the values will fall outside the vector range.
 */
fun Vec2R.lerp(to: Vec2R, t: Double): Vec2R =
    Vec2R(lerp(x, to.x, t), lerp(y, to.y, t))

/**
 * Linearly interpolate from the calling vector to the [to] vector for [t] in [0, 1].
 * The value of t is clamped, so the vector will always be between this and [to].
 */
fun Vec2R.lerpClamped(to: Vec2R, t: Double): Vec2R =
    lerp(to, clamp(t, 0.0, 1.0))
