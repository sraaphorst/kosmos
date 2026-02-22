package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Natural
import org.vorpal.kosmos.core.ops.BinOp

object NaturalAlgebras {
    val additiveCommutativeMonoid: CommutativeMonoid<Natural> = CommutativeMonoid.of(
        identity = Natural.ZERO,
        op = BinOp(Symbols.PLUS, Natural::plus),
    )

    val multiplicativeCommutativeMonoid: CommutativeMonoid<Natural> = CommutativeMonoid.of(
        identity = Natural.ONE,
        op = BinOp(Symbols.ASTERISK, Natural::times)
    )

    val commutativeSemiring: CommutativeSemiring<Natural> = CommutativeSemiring.of(
        add = additiveCommutativeMonoid,
        mul = multiplicativeCommutativeMonoid
    )
}
