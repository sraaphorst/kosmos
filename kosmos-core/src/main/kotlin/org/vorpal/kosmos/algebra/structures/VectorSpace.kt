package org.vorpal.kosmos.algebra.structures

/** A vector space is a module where the scalars form a field. */
interface VectorSpace<F: Any, V: Any> : RModule<F, V> {
    override val ring: Field<F>

    // For convenience, we allow the ring to be referred to as a field or a ring.
    val field: Field<F>
        get() = ring
}
