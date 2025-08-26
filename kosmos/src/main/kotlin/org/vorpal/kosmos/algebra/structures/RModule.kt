package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.ops.Action
import org.vorpal.kosmos.algebra.ops.Add
import org.vorpal.kosmos.algebra.ops.Mul

/** A left R-module over a ring R acting on an abelian group (V,+).
 *  Defined by the ring, abelian group, and the (scalar) action of the ring on the group.
 **/
interface RModule<S, V, out RM> where RM : Monoid<S, Mul> {
    val R: Ring<S, RM>
    val add: AbelianGroup<V, Add>
    val smul: Action<S, V>
}
