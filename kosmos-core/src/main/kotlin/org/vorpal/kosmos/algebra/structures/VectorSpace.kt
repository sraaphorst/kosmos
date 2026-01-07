package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.LeftAction

/** A vector space is a module where the scalars form a field. */
interface VectorSpace<F : Any, V : Any> : RModule<F, V> {
    override val scalars: Field<F>

    // For convenience, we allow the ring to be referred to as a field or a ring.
    val field: Field<F>
        get() = scalars

    companion object {
        fun <F : Any, V : Any> of(
            field: Field<F>,
            vectorGroup: AbelianGroup<V>,
            leftAction: LeftAction<F, V>
        ): VectorSpace<F, V> = object : VectorSpace<F, V> {
            override val scalars = field
            override val group = vectorGroup
            override val leftAction = leftAction
        }
    }
}

/**
 * A vector space which has a non-zero, well-defined, finite dimensionality.
 */
interface FiniteVectorSpace<F: Any, V: Any> : VectorSpace<F, V>, Dimensionality

