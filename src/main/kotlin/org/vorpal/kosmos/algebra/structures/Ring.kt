package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.ops.Add
import org.vorpal.kosmos.algebra.ops.Mul

/** Ring packs two ops with cross-properties (distributivity). */
// Distributivity is a property *between* ops; we encode it as laws (tests),
// (or add marker interfaces if you prefer static “claims”).
interface Ring<A, out M: Monoid<A, Mul>> {
    val add: AbelianGroup<A, Add>
    val mul: M
}

object Rings {
    object IntRing : Ring<Int, Monoid<Int, Mul>> {
        override val add = AbelianGroups.IntAdd
        override val mul = Monoids.IntMul
    }
}
