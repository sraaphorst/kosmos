package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.core.ops.Action

// TODO: These need to be cleaned up properly to reflect more general F^n for fields F.
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

/**
 * Simple 2D vector for testing.
 */
data class Vec2D(val x: Double, val y: Double) {
    override fun toString(): String = "($x, $y)"

    companion object {
        val ZERO = Vec2D(0.0, 0.0)
        val UNIT_X = Vec2D(1.0, 0.0)
        val UNIT_Y = Vec2D(0.0, 1.0)
    }
}

/**
 * 2D vector space over doubles.
 */
object Vec2DSpace : VectorSpace<Double, Vec2D> {
    override val ring: Field<Double> = DoubleField

    override val group: AbelianGroup<Vec2D> = AbelianGroup.of(
        op = { a, b -> Vec2D(a.x + b.x, a.y + b.y) },
        identity = Vec2D.ZERO,
        inverse = { Vec2D(-it.x, -it.y) }
    )

    override val action: Action<Double, Vec2D> = Action { scalar, vec ->
        Vec2D(scalar * vec.x, scalar * vec.y)
    }
}

// ============================================================================
// Helper Extensions
// ============================================================================

/**
 * Dot product for Vec2D (not part of VectorSpace interface, but useful for testing).
 */
infix fun Vec2D.dot(other: Vec2D): Double = this.x * other.x + this.y * other.y

/**
 * Magnitude (norm) of Vec2D.
 */
val Vec2D.magnitude: Double
    get() = kotlin.math.sqrt(x * x + y * y)

/**
 * Normalized version of Vec2D.
 */
fun Vec2D.normalize(): Vec2D {
    val mag = magnitude
    return if (mag > 0.0) Vec2D(x / mag, y / mag) else Vec2D.ZERO
}
