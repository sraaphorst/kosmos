package org.vorpal.kosmos.algebra

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.core.ops.Add
import org.vorpal.kosmos.core.ops.Mul
import org.vorpal.kosmos.std.Rational

object AbelianGroups {
    object IntAdd : AbelianGroup<Int, Add> {
        override val identity = 0
        override fun combine(a: Int, b: Int) = a + b
        override fun inverse(a: Int) = -a
    }

    object RationalAdd: AbelianGroup<Rational, Add> {
        override val identity = Rational.zero
        override fun combine(a: Rational, b: Rational) = a + b
        override fun inverse(a: Rational) = -a
    }

    object RationalMul: AbelianGroup<Rational, Mul> {
        override val identity = Rational.one
        override fun combine(a: Rational, b: Rational) = a * b
        override fun inverse(a: Rational) = a.reciprocal()
    }
}