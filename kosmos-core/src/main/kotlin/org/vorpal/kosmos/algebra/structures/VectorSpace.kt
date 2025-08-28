package org.vorpal.kosmos.algebra.structures

/** A vector space is a module where the scalars form a field. */
interface VectorSpace<S, V> : RModule<S, V, Field<S>> {
    override val R: Field<S>
}