package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.ops.Mul
import org.vorpal.kosmos.algebra.ops.OpTag
import org.vorpal.kosmos.algebra.props.Unital

interface Monoid<A, TAG: OpTag> : Semigroup<A>, Unital<A, TAG>

object Monoids {
    object IntMul : Monoid<Int, Mul> {
        override val identity = 1
        override fun combine(a: Int, b: Int) = a * b
    }
}
