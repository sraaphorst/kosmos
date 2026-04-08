package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Natural
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable

/**
 * Main structures:
 * - [NaturalAdditiveCommutativeMonoid]
 * - [NaturalMultiplicativeCommutativeMonoid]
 * - [NaturalCommutativeSemiring]
 *
 * Eqs:
 * - [eqNatural]
 *
 * Printables:
 * - [printableNatural]
 * - [printableNaturalPretty]
 */
object NaturalAlgebras {
    object NaturalAdditiveCommutativeMonoid: CommutativeMonoid<Natural> {
        override val identity: Natural = Natural.ZERO
        override val op: BinOp<Natural> = BinOp(Symbols.PLUS, Natural::plus)
    }

    object NaturalMultiplicativeCommutativeMonoid: CommutativeMonoid<Natural> {
        override val identity: Natural = Natural.ONE
        override val op: BinOp<Natural> = BinOp(Symbols.ASTERISK, Natural::times)
    }

    object NaturalCommutativeSemiring: CommutativeSemiring<Natural> {
        override val add: CommutativeMonoid<Natural> = NaturalAdditiveCommutativeMonoid
        override val mul: CommutativeMonoid<Natural> = NaturalMultiplicativeCommutativeMonoid
    }

    val eqNatural: Eq<Natural> = Eq.default()

    val printableNatural: Printable<Natural> = Printable.default()
    val printableNaturalPretty = printableNatural
}
