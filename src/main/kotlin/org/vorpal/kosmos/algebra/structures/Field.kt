package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.ops.Mul
import org.vorpal.kosmos.std.Rational

interface Field<A> : Ring<A, AbelianGroup<A, Mul>>

object Fields {
    object RationalField : Field<Rational> {
        override val add = AbelianGroups.RationalAdd
        override val mul = AbelianGroups.RationalMul
    }
}