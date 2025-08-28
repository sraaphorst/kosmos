package org.vorpal.kosmos.algebra

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.ops.Mul

object Monoids {
    object IntMul : Monoid<Int, Mul> {
        override val identity = 1
        override fun combine(a: Int, b: Int) = a * b
    }
}