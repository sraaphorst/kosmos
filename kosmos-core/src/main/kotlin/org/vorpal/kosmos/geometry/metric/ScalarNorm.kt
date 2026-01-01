package org.vorpal.kosmos.geometry.metric

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.math.Real

// Or map to Real via a scalar norm
interface ScalarNorm<F : Field<F>> {
    fun absAsReal(x: F): Real
}
