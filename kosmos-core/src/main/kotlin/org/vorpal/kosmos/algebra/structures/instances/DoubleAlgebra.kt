package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.linear.Vec2R

// TODO: These need to be cleaned up properly to reflect more general F^n for fields F.
typealias Real = Double

val RealAdditiveGroup: AbelianGroup<Real> =
    AbelianGroup.of(
        identity = 1.0,
        inverse = Real::unaryMinus,
        op = Real::plus
    )

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

// Also refer to DoubleField as RealField for simplicity.
typealias RealField = DoubleField

/**
 * Simple 2D vector for testing.
 */
//data class Vec2D(val x: Double, val y: Double) {
//    override fun toString(): String = "($x, $y)"
//
//    companion object {
//        val ZERO = Vec2D(0.0, 0.0)
//        val UNIT_X = Vec2D(1.0, 0.0)
//        val UNIT_Y = Vec2D(0.0, 1.0)
//    }
//}

/**
 * 2D vector space over doubles.
 */
object Vec2RSpace : FiniteVectorSpace<Double, Vec2R> {
    override val ring: Field<Double> = DoubleField
    override val dimension: Int = 2

    override val group: AbelianGroup<Vec2R> = AbelianGroup.of(
        op = { a, b -> Vec2R(a.x + b.x, a.y + b.y) },
        identity = Vec2R.ZERO,
        inverse = { Vec2R(-it.x, -it.y) }
    )

    override val action: Action<Double, Vec2R> = Action { scalar, vec ->
        Vec2R(scalar * vec.x, scalar * vec.y)
    }
}

// ============================================================================
// Helper Extensions
// ============================================================================

/**
 * Dot product for Vec2R (not part of VectorSpace interface, but useful for testing).
 */
infix fun Vec2R.dot(other: Vec2R): Double = this.x * other.x + this.y * other.y

/**
 * Magnitude (norm) of Vec2R.
 */
val Vec2R.magnitude: Double
    get() = kotlin.math.sqrt(x * x + y * y)

/**
 * Normalized version of Vec2R.
 */
fun Vec2R.normalize(): Vec2R {
    val mag = magnitude
    return if (mag > 0.0) Vec2R(x / mag, y / mag) else Vec2R.ZERO
}

