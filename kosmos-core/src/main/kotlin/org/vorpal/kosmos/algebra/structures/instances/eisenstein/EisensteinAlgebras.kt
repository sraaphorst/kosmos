package org.vorpal.kosmos.algebra.structures.instances.Eisenstein

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.eisenstein.EisensteinInt
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

object EisensteinAlgebras {

    object EisensteinIntRing:
        InvolutiveRing<EisensteinInt>,
        HasNormSq<EisensteinInt, BigInteger> {

        override val zero: EisensteinInt = EisensteinInt.zero
        override val one: EisensteinInt = EisensteinInt.one

        override val add: AbelianGroup<EisensteinInt> = AbelianGroup.of(
            identity = zero,
            op = BinOp(Symbols.PLUS) { ez1, ez2 ->
                EisensteinInt(ez1.a + ez2.a, ez1.b + ez2.b)
            },
            inverse = Endo(Symbols.MINUS) { ez ->
                EisensteinInt(-ez.a, -ez.b)
            }
        )

        // (a + bω)(c + dω) = (ac - bd) + (ad + bc - bd)ω
        override val mul: Monoid<EisensteinInt> = Monoid.of(
            identity = one,
            op = BinOp(Symbols.ASTERISK) { ez1, ez2 ->
                val ac = ez1.a * ez2.a
                val bd = ez1.b * ez2.b
                val real = ac - bd

                val ad = ez1.a * ez2.b
                val bc = ez1.b * ez2.a
                val omegaCoeff = ad + bc - bd

                EisensteinInt(real, omegaCoeff)
            }
        )

        override fun fromBigInt(n: BigInteger): EisensteinInt =
            EisensteinInt(n, BigInteger.ZERO)

        // conj(a + bω) = (a - b) + (-b)ω
        override val conj: Endo<EisensteinInt> = Endo(Symbols.CONJ) { ez ->
            EisensteinInt(ez.a - ez.b, -ez.b)
        }

        // N(a + bω) = a^2 - ab + b^2
        override val normSq: UnaryOp<EisensteinInt, BigInteger> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { x ->
                x.a * x.a - x.a * x.b + x.b * x.b
            }

        override val normElement: Endo<EisensteinInt> = Endo(Symbols.NORM) { x ->
            mul.op(x, conj(x))
        }
    }

    val eqEisensteinInt: Eq<EisensteinInt> = Eq { x, y -> x == y }
}