package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * A [CovectorField] assigns to each point in a [VectorSpace] a [Covector] on that space.
 * Mathematically: ω : V → V*, where V* is the dual space of linear functionals on V.
 */
interface CovectorField<F, V> where F: Any, V: VectorSpace<F, V> {
    val space: VectorSpace<F, V>
    operator fun invoke(point: V): Covector<F, V>
}
