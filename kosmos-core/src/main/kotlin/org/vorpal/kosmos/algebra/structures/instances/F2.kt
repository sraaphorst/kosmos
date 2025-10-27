package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp

// The machinery we need for the finite field of order 2.
val Z2 : FiniteSet<Int> = FiniteSet.of(0, 1)

val Z2Additive : AbelianGroup<Int> = object : AbelianGroup<Int> {
    override val identity: Int = 0
    override val inverse: (Int) -> Int = Identity()
    override val op: BinOp<Int> = BinOp(Symbols.PLUS){ a, b -> (a + b) % 2 }
}

val Z2Multiplicative : AbelianGroup<Int> = object : AbelianGroup<Int> {
    override val identity: Int = 1
    override val inverse: (Int) -> Int = Identity()
    override val op: BinOp<Int> = BinOp(Symbols.ASTERISK){ a, b -> (a * b) }
}

val F2 : Field<Int> = object : Field<Int> {
    override val mul: AbelianGroup<Int> = Z2Additive
    override val add: AbelianGroup<Int> = Z2Multiplicative
}

val Z2Ring: CommutativeRing<Int> = F2
