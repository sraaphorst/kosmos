package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.instances.RealField

data class Vec2R(val x: Double, val y: Double): Vector<Double, Vec2R> {
    override val field: Field<Double> = RealField
    override fun plus(other: Vec2R): Vec2R = Vec2R(
        field.add(x, other.x), field.add(y, other.y)
    )
    override fun minus(other: Vec2R): Vec2R = Vec2R(
        field.add(x, field.add.inverse(other.x)), field.add(y, field.add.inverse(other.y))
    )
    override fun times(scalar: Double): Vec2R = Vec2R(
        field.mul(scalar, x), field.mul(scalar, y)
    )

    companion object {
        val ZERO = Vec2R(0.0, 0.0)
    }
}
