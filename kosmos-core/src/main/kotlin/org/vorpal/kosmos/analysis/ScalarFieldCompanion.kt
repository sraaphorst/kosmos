package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace

interface ScalarFieldCompanion {
    fun <F, V> of(
        field: Field<F>,
        space: VectorSpace<F, V>,
        f: (V) -> F
    ): ScalarField<F, V> where F : Any, V : VectorSpace<F, V> =
        object : BaseScalarField<F, V>(field, space) {
            override fun invoke(point: V): F = f(point)
        }

    fun <F, V> constant(
        field: Field<F>,
        space: VectorSpace<F, V>,
        value: F
    ): ScalarField<F, V> where F : Any, V : VectorSpace<F, V> =
        object : BaseScalarField<F, V>(field, space) {
            override fun invoke(point: V): F = value
        }

    fun <F, V> zero(
        field: Field<F>,
        space: VectorSpace<F, V>
    ): ScalarField<F, V> where F : Any, V : VectorSpace<F, V> =
        constant(field, space, field.add.identity)

    fun <F, V> one(
        field: Field<F>,
        space: VectorSpace<F, V>
    ): ScalarField<F, V> where F : Any, V : VectorSpace<F, V> =
        constant(field, space, field.mul.identity)
}

// Global companion for easy static creation
object ScalarFields : ScalarFieldCompanion

