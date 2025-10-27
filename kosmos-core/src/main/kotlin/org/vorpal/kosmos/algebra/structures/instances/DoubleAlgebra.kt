package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.core.ops.Action


object DoubleField : Field<Double> {
    override val add: AbelianGroup<Double> = AbelianGroup.of(
        op = Double::plus,
        identity = 0.0,
        inverse = Double::unaryMinus
    )

    override val mul: AbelianGroup<Double> = AbelianGroup.of(
        op = Double::times,
        identity = 1.0,
        inverse = { 1.0 / it }
    )
}

/**
 * Simple 2D vector for testing.
 */
data class Vec2D(val x: Double, val y: Double) {
    companion object {
        val ZERO = Vec2D(0.0, 0.0)
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