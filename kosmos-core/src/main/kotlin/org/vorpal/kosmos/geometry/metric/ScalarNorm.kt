package org.vorpal.kosmos.geometry.metric

import org.vorpal.kosmos.algebra.structures.Field

// Or map to Double via a scalar norm
interface ScalarNorm<F: Field<F>> {
    fun absAsDouble(x: F): Double
}