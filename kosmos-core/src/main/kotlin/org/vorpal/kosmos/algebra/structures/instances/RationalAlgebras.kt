package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.std.Rational

object RationalAlgebras {
    val RationalField: Field<Rational> = Field.Companion.of(
        add = AbelianGroup.Companion.of(
            identity = Rational.Companion.ZERO,
            op = BinOp(Symbols.PLUS) { r1, r2 -> r1 + r2 },
            inverse = Endo(Symbols.MINUS) { -it }
        ),
        mul = CommutativeMonoid.Companion.of(
            identity = Rational.Companion.ONE,
            op = BinOp(Symbols.ASTERISK) { r1, r2 -> r1 * r2 }
        ),
        reciprocal = Endo(Symbols.INVERSE) { r ->
            require(r != Rational.Companion.ZERO) { "0 has no reciprocal." }
            r.reciprocal()
        }
    )

    object RationalStarField :
        Field<Rational> by RationalField,
        InvolutiveRing<Rational> {

        override val conj: Endo<Rational> =
            Endo(Symbols.CONJ) { it }

        // Disambiguate.
        override val zero: Rational
            get() = RationalField.zero
    }
}

val eqRational: Eq<Rational> = Eqs.rational
