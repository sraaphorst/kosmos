package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.Field

/**
 * A general 2D [Vector] with entries from a [Field].
 */
data class Vec2<F: Any>(
    val x: F,
    val y: F,
    override val field: Field<F>
) : Vector<F, Vec2<F>> {

    override fun plus(other: Vec2<F>): Vec2<F> = Vec2(
        field.add(x, other.x),
        field.add(y, other.y),
        field
    )

    override fun minus(other: Vec2<F>): Vec2<F> = Vec2(
        field.add(x, field.add.inverse(other.x)),
        field.add(y, field.add.inverse(other.y)),
        field
    )

    override fun times(scalar: F): Vec2<F> = Vec2(
        field.mul(scalar, x),
        field.mul(scalar, y),
        field
    )

    companion object {
        fun <F: Any> of(x: F, y: F, field: Field<F>): Vec2<F> = Vec2(
            x, y, field
        )

        fun <F: Any> zero(field: Field<F>): Vec2<F> = Vec2(
            field.add.identity, field.add.identity, field
        )
    }
}
