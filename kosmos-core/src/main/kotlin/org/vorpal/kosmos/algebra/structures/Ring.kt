package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.Add
import org.vorpal.kosmos.core.ops.Mul

/** Ring packs two ops with cross-properties (distributivity). */
interface Ring<A, out M : Monoid<A, Mul>> {
    val add: AbelianGroup<A, Add>
    val mul: M
}
