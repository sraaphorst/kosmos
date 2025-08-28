package org.vorpal.kosmos.algebra

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.std.Rational

object Fields {
    object RationalField : Field<Rational> {
        override val add = AbelianGroups.RationalAdd
        override val mul = AbelianGroups.RationalMul
    }
}
