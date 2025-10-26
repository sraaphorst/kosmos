package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * A [ScalarField] is a function that assigns a scalar from the [Field]
 * to each point of a [VectorSpace].
 *
 * Mathematically: f : V → 𝔽, where V is a vector space over 𝔽.
 */
interface ScalarField<F, V> where F : Any, V : VectorSpace<F, V> {
    val field: Field<F>
    val space: VectorSpace<F, V>
    operator fun invoke(point: V): F
}
