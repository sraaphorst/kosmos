package org.vorpal.kosmos.geometry.metric

import org.vorpal.kosmos.algebra.structures.Field

/**
 * A field that supports a square root operation.
 */
interface SqrtField<F: Field<F>> : Field<F> {
    fun sqrt(x: F): F
}
