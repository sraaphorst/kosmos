package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Action

/** A vector space is a module where the scalars form a field. */
interface VectorSpace<F: Any, V: Any> : RModule<F, V> {
    override val ring: Field<F>

    // For convenience, we allow the ring to be referred to as a field or a ring.
    val field: Field<F>
        get() = ring

    companion object {
        fun <F: Any, V: Any> of(
            field: Field<F>,
            vectorGroup: AbelianGroup<V>,
            action: Action<F, V>
        ): VectorSpace<F, V> = object : VectorSpace<F, V> {
            override val ring = field
            override val group = vectorGroup
            override val action = action
        }
    }
}

/**
 * A vector space which has a non-zero, well-defined, finite dimensionality.
 */
interface FiniteVectorSpace<F: Any, V: Any> : VectorSpace<F, V>, Dimensionality

