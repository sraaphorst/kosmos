package org.vorpal.kosmos.geometry.metric

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.linear.Vector

/**
 * A bilinear form g: V × V → F defining an inner product on a vector space.
 */
interface Metric<F: Field<F>, V: Vector<F, V>> {
    fun innerProduct(u: V, v: V): F
}
