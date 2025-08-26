package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.ops.Mul

/** A vector space is a module where the scalars form a field. */
interface VectorSpace<S, V> : RModule<S, V, AbelianGroup<S, Mul>> {
    override val R: Field<S>
}