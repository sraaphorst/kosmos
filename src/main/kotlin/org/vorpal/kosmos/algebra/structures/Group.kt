package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.ops.Add
import org.vorpal.kosmos.algebra.ops.OpTag
import org.vorpal.kosmos.algebra.props.Invertible

/** Group = monoid + inverses for that op. */
interface Group<A, TAG : OpTag> : Monoid<A, TAG>, Invertible<A, TAG>

object Groups {
}
