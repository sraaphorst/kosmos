package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.BooleanAlgebra
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

// Note: we call concrete algebraic structures over Booleans BoolStructure to avoid name clashes.
object BooleanAlgebras {
    val BoolXorAbelianGroup: AbelianGroup<Boolean> = AbelianGroup.of(
        identity = false,
        op = BinOp(Symbols.O_PLUS) { x, y -> x xor y },
        inverse = Endo(Symbols.NOTHING, Identity())
    )

    val BoolAndCommutativeMonoid: CommutativeMonoid<Boolean> = CommutativeMonoid.of(
        identity = true,
        op = BinOp(Symbols.WEDGE) { x, y -> x and y }
    )

    val BoolOrCommutativeMonoid: CommutativeMonoid<Boolean> = CommutativeMonoid.of(
        identity = false,
        op = BinOp(Symbols.VEE) { x, y -> x or y }
    )

    /**
     * Field isomorphic to GF(2).
     */
    val BoolField: Field<Boolean> = Field.of(
        add = BoolXorAbelianGroup,
        mul = BoolAndCommutativeMonoid,
        reciprocal = Endo(Symbols.NOTHING, Identity())
    )

    /**
     * Boolean algebra on Kotlin Boolean:
     *  - ⊥ = false, ⊤ = true
     *  - ∨ = OR, ∧ = AND, ¬ = NOT
     */
    val BoolAlgebra: BooleanAlgebra<Boolean> = BooleanAlgebra.of(
        join = BinOp(symbol = Symbols.BOOL_OR) { a, b -> a || b },
        meet = BinOp(symbol = Symbols.BOOL_AND) { a, b -> a && b },
        bottom = false,
        top = true,
        not = Endo(symbol = Symbols.NOT) { a -> !a }
    )
}
