package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.Rational

object RationalAlgebras {
    val RationalField: Field<Rational> = Field.of(
        add = AbelianGroup.of(
            identity = Rational.ZERO,
            op = BinOp(Symbols.PLUS) { a, b -> a + b },
            inverse = Endo(Symbols.MINUS) { -it }
        ),
        mul = CommutativeMonoid.of(
            identity = Rational.ONE,
            op = BinOp(Symbols.ASTERISK) { a, b -> a * b }
        ),
        reciprocal = Endo(Symbols.INVERSE) { r ->
            require(r != Rational.ZERO) { "0 has no reciprocal." }
            r.reciprocal()
        }
    )

    object RationalStarField :
        Field<Rational> by RationalField,
        InvolutiveRing<Rational>,
        NormedDivisionAlgebra<Rational, Rational> {

        override val conj: Endo<Rational> =
            Endo(Symbols.CONJ) { it }

        override val normSq: UnaryOp<Rational, Rational> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { a -> a * a }

        override val zero: Rational
            get() = RationalField.zero

        override val one: Rational
            get() = RationalField.one
    }
}

val eqRational: Eq<Rational> = Eqs.rational