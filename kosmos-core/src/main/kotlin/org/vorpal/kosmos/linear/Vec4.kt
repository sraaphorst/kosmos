package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace

/**
 * A general 4D [Vector] with entries from a [Field].
 */
data class Vec4<F: Any>(
    val x: F,
    val y: F,
    val z: F,
    val w: F,
    override val field: Field<F>
) : Vector<F, Vec4<F>> {

    override fun plus(other: Vec4<F>): Vec4<F> = Vec4(
        field.add(x, other.x),
        field.add(y, other.y),
        field.add(z, other.z),
        field.add(w, other.w),
        field
    )

    override fun minus(other: Vec4<F>): Vec4<F> = Vec4(
        field.add(x, field.add.inverse(other.x)),
        field.add(y, field.add.inverse(other.y)),
        field.add(z, field.add.inverse(other.z)),
        field.add(w, field.add.inverse(other.w)),
        field
    )

    override fun times(scalar: F): Vec4<F> = Vec4(
        field.mul(scalar, x),
        field.mul(scalar, y),
        field.mul(scalar, z),
        field.mul(scalar, w),
        field
    )

    companion object {
        fun <F: Any> of(x: F, y: F, z: F, w: F, field: Field<F>): Vec4<F> =
            Vec4(x, y, z, w, field)

        fun <F: Any> constant(f: F, field: Field<F>): Vec4<F> =
            Vec4(f, f, f, f, field)

        fun <F: Any> zero(field: Field<F>): Vec4<F> =
            Vec4(field.add.identity, field.add.identity, field.add.identity, field.add.identity, field)
    }
}

fun <F: Any, V: Any> FiniteVectorSpace<F, V>.vec(x: F, y: F, z: F, w: F): Vec4<F> {
    check(this.dimension == 4) { "Trying to create a 4-dim vector from a $dimension-dim vector space."}
    return Vec4(x, y, z, w, this.field)
}
