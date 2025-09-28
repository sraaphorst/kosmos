package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Mul

interface Field<A> : Ring<A> {
    override val mul: AbelianGroup<A, Mul>
}
