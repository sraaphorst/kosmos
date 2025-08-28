package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.OpTag
import org.vorpal.kosmos.core.props.Invertible

/** Group = monoid + inverses for that op. */
interface Group<A, TAG : OpTag> : Monoid<A, TAG>, Loop<A, TAG>, Invertible<A, TAG> {
    override fun ldiv(a: A, b: A): A = combine(inverse(a), b)
    override fun rdiv(b: A, a: A): A = combine(b, inverse(a))
}
