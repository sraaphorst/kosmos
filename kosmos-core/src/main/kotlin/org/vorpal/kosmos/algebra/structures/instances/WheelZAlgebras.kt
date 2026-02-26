package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.WheelInf
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.rational.WheelZ

object WheelZAlgebras {
    fun wheel(): WheelInf<WheelZ> {
        return object : WheelInf<WheelZ> {
            override val zero: WheelZ = WheelZ.ZERO
            override val one: WheelZ = WheelZ.ONE
            override val bottom: WheelZ = WheelZ.BOTTOM
            override val posInf: WheelZ = WheelZ.POS_INF
            override val negInf: WheelZ = WheelZ.NEG_INF
            override val add: AbelianGroup<WheelZ> = AbelianGroup.of(
                WheelZ.ZERO,
                BinOp(Symbols.PLUS, WheelZ::plus),
                Endo(Symbols.MINUS, WheelZ::unaryMinus)
            )
            override val mul: CommutativeMonoid<WheelZ> = CommutativeMonoid.of(
                WheelZ.ONE,
                BinOp(Symbols.ASTERISK, WheelZ::times)
            )
            override val inv: Endo<WheelZ> = Endo(Symbols.INVERSE, WheelZ::inv)
        }
    }
}