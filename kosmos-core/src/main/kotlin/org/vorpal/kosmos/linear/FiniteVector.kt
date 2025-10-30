package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.Field

/**
 * A finite dimensional vector that holds its components and the [Field] from which they come.
 */
interface FiniteVector<F : Any, V : FiniteVector<F, V>> : Vector<F, V> {
    val components: List<F>
    override val field: Field<F>
}
