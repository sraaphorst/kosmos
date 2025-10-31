package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp

object IntegerAlgebras {
    object Z2AdditiveGroup : AbelianGroup<Int> {
        override val identity: Int = 0
        override val inverse: (Int) -> Int = Identity()
        override val op: BinOp<Int> = BinOp(Symbols.PLUS){ a, b -> (a + b) % 2 }
    }

    object Z2MultiplicativeGroup : AbelianGroup<Int> {
        override val identity: Int = 1
        override val inverse: (Int) -> Int = Identity()
        override val op: BinOp<Int> = BinOp(Symbols.ASTERISK){ a, b -> (a * b) % 2}
    }

    val F2 : Field<Int> = object : Field<Int> {
        override val mul: AbelianGroup<Int> = Z2AdditiveGroup
        override val add: AbelianGroup<Int> = Z2MultiplicativeGroup
    }
}
