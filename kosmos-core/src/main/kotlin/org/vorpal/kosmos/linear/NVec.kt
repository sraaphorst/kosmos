package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace

data class NVec<F: Any>(val values: List<F>, override val field: Field<F>): Vector<F, NVec<F>> {
    override fun plus(other: NVec<F>): NVec<F> {
        check(values.size == other.values.size) { "Incompatible vector dimensions: ${values.size} != ${other.values.size}" }
        return NVec(values.zip(other.values).map { (v1, v2) -> field.add(v1, v2) }, field)
    }

    override fun minus(other: NVec<F>): NVec<F> {
        check(values.size == other.values.size) { "Incompatible vector dimensions: ${values.size} != ${other.values.size}" }
        return NVec(values.zip(other.values).map { (v1, v2) -> field.add(v1, field.add.inverse(v2)) }, field)
    }

    override fun times(scalar: F): NVec<F> =
        NVec(values.map { field.mul(scalar, it) }, field)
}

fun <F: Any, V: Any> FiniteVectorSpace<F, V>.vec(vararg xs: F): NVec<F> {
    check(this.dimension == xs.size) { "Trying to create a ${xs.size}-dim vector from a $dimension-dim vector space."}
    return NVec(xs.toList(), this.field)
}
