package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace

data class Vec0<F: Any>(override val field: Field<F>): Vector<F, Vec0<F>> {
    override fun plus(other: Vec0<F>): Vec0<F> {
        check(field == other.field) { Vector.FIELD_MATCH_ERROR }
        return this
    }

    override fun minus(other: Vec0<F>): Vec0<F> {
        check(field == other.field) { Vector.FIELD_MATCH_ERROR }
        return this
    }

    override fun times(scalar: F): Vec0<F> = this
}

fun <F: Any, V: Any> FiniteVectorSpace<F, V>.vec(): Vec0<F> =
    Vec0(this.field)
