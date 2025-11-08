package org.vorpal.kosmos.geometry.metric

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.linear.Vector

/** No sqrt here */
interface InnerProductSpace<F: Field<F>, V: Vector<F, V>> : VectorSpace<F, V> {
    val metric: Metric<F, V>
    fun squaredNorm(v: V): F = metric.innerProduct(v, v)
    fun squaredDistance(u: V, v: V): F = squaredNorm(v - u)
}

// Norm only when available
fun <F, V> InnerProductSpace<F, V>.norm(v: V, sqrt: SqrtField<F>): F where F: Field<F>, V: Vector<F, V> =
    sqrt.sqrt(squaredNorm(v))

fun <F: Field<F>, V: Vector<F, V>> InnerProductSpace<F, V>.normAsDouble(
    v: V, abs: ScalarNorm<F>
): Double = kotlin.math.sqrt(abs.absAsDouble(squaredNorm(v)))