package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp

object BooleanAlgebras {
    object BooleanXORGroup: AbelianGroup<Boolean> {
        override val identity: Boolean = false
        override val op: BinOp<Boolean> =
            BinOp(symbol = Symbols.O_PLUS) { x, y -> x xor y}
        override val inverse: (Boolean) -> Boolean = Identity()
    }

    object BooleanANDMonoid: CommutativeMonoid<Boolean> {
        override val identity: Boolean = true
        override val op: BinOp<Boolean> =
            BinOp(symbol = Symbols.WEDGE) { x, y -> x and y }
    }

    object BooleanORMonoid: CommutativeMonoid<Boolean> {
        override val identity: Boolean = false
        override val op: BinOp<Boolean> =
            BinOp(symbol = Symbols.VEE) { x, y -> x or y }
    }

    /**
     * Field isomorphic to GF(2).
     */
    object BooleanField: Field<Boolean> {
        override val add: AbelianGroup<Boolean> = BooleanXORGroup

        // Since the multiplicative group here just contains true, it does form a group.
        override val mul: AbelianGroup<Boolean> = object : AbelianGroup<Boolean> {
            override val identity: Boolean = true
            override val op: BinOp<Boolean> = BinOp(symbol = Symbols.WEDGE) { x, y -> x and y }
            override val inverse: (Boolean) -> Boolean = Identity()
        }
    }
}