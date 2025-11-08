package org.vorpal.kosmos.geometry.metric

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.linear.Vector

class EuclideanMetric<F: Field<F>, V: Vector<F, V>> (
    private val dot: (V, V) -> F
) : Metric<F, V> {
    override fun innerProduct(u: V, v: V): F = dot(u, v)
}
