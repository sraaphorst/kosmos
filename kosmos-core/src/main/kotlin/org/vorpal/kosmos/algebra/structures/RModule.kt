package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.Add
import org.vorpal.kosmos.core.ops.Mul

/** A left R-module over a ring R acting on an abelian group (V,+).
 *  Defined by the ring, abelian group, and the (scalar) action of the ring on the group.
 **/
interface RModule<S, V, out Rng> where Rng : Ring<S, Monoid<S, Mul>> {
    val R: Rng
    val add: AbelianGroup<V, Add>
    val smul: Action<S, V>
}