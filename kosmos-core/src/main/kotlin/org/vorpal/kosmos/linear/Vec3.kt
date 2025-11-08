package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace

/**
 * A general 3D [Vector] with entries from a [Field].
 */
data class Vec3<F: Any>(
    val x: F,
    val y: F,
    val z: F,
    override val field: Field<F>
) : Vector<F, Vec3<F>> {

    override fun plus(other: Vec3<F>): Vec3<F> = Vec3(
        field.add(x, other.x),
        field.add(y, other.y),
        field.add(z, other.z),
        field
    )

    override fun minus(other: Vec3<F>): Vec3<F> = Vec3(
        field.add(x, field.add.inverse(other.x)),
        field.add(y, field.add.inverse(other.y)),
        field.add(z, field.add.inverse(other.z)),
        field
    )

    override fun times(scalar: F): Vec3<F> = Vec3(
        field.mul(scalar, x),
        field.mul(scalar, y),
        field.mul(scalar, z),
        field
    )

    companion object {
        fun <F: Any> of(x: F, y: F, z: F, field: Field<F>): Vec3<F> =
            Vec3(x, y, z, field)

        fun <F: Any> constant(f: F, field: Field<F>): Vec3<F> =
            Vec3(f, f, f, field)

        fun <F: Any> zero(field: Field<F>): Vec3<F> =
            Vec3(field.add.identity, field.add.identity, field.add.identity, field)
    }
}

fun <F: Any, V: Any> FiniteVectorSpace<F, V>.vec(x: F, y: F, z: F): Vec3<F> {
    check(this.dimension == 3) { "Trying to create a 3-dim vector from a $dimension-dim vector space."}
    return Vec3(x, y, z, this.field)
}
