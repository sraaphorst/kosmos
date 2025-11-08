package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace

data class Vec1<F: Any>(val x: F, override val field: Field<F>): Vector<F, Vec1<F>> {
    override fun plus(other: Vec1<F>): Vec1<F> {
        check(field == other.field) { Vector.FIELD_MATCH_ERROR }
        return Vec1(field.add(x, other.x), field)
    }

    override fun minus(other: Vec1<F>): Vec1<F> {
        check(field == other.field) { Vector.FIELD_MATCH_ERROR }
        return Vec1(
            field.add(x, field.add.inverse(other.x)),
            field
        )
    }

    override fun times(scalar: F): Vec1<F> =
        Vec1(field.mul(scalar, x), field)
}

fun <F: Any, V: Any> FiniteVectorSpace<F, V>.vec(x: F): Vec1<F> =
    Vec1(x, this.field)
