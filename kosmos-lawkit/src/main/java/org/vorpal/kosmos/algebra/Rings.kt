package org.vorpal.kosmos.algebra

import org.vorpal.kosmos.algebra.structures.AbelianGroups
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Monoids
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.core.ops.Add
import org.vorpal.kosmos.core.ops.Mul
import org.vorpal.kosmos.core.props.LeftDistributesOver
import org.vorpal.kosmos.core.props.RightDistributesOver

object Rings {
    object IntRing :
        Ring<Int, Monoid<Int, Mul>>,
        LeftDistributesOver<Add>,
        RightDistributesOver<Add> {
            override val add = AbelianGroups.IntAdd
            override val mul = Monoids.IntMul
        }
}