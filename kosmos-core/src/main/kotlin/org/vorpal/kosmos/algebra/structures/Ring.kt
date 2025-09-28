package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Add
import org.vorpal.kosmos.core.ops.Mul
import org.vorpal.kosmos.core.ops.OpTag
import org.vorpal.kosmos.core.props.Distributivity

/** Ring packs two ops with cross-properties (distributivity). */
interface Ring<A> {
    val add: AbelianGroup<A, Add>
    val mul: Monoid<A, Mul>
}
