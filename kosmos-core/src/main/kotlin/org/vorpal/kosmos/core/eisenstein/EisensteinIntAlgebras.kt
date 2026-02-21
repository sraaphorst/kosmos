package org.vorpal.kosmos.core.eisenstein

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.instances.Complex
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger
import kotlin.math.sqrt

object EisensteinIntAlgebras {
    /**
     * Note that this is actually an integral domain, and in fact a Euclidean domain with
     * norm function n(a + bω) = a^2 - ab + b^2, giving gcd and unique factorization.
     */
    val EisensteinIntCommutativeRing: CommutativeRing<EisensteinInt> = CommutativeRing.of(
        add = AbelianGroup.of(
            identity = EisensteinInt.ZERO,
            op = BinOp(Symbols.PLUS, EisensteinInt::plus),
            inverse = Endo(Symbols.MINUS, EisensteinInt::unaryMinus)
        ),
        mul = CommutativeMonoid.of(
            identity = EisensteinInt.ONE,
            op = BinOp(Symbols.ASTERISK, EisensteinInt::times)
        )
    )

    val EisensteinIntNormSq: HasNormSq<EisensteinInt, BigInteger> =
        object : HasNormSq<EisensteinInt, BigInteger> {
            override val normSq: UnaryOp<EisensteinInt, BigInteger> =
                UnaryOp(Symbols.NORM_SQ_SYMBOL) { z ->
                    val a = z.a
                    val b = z.b
                    a * a - a * b + b * b
                }
        }

    object EisensteinIntCommutativeRingWithNorm :
        CommutativeRing<EisensteinInt> by EisensteinIntCommutativeRing,
        HasNormSq<EisensteinInt, BigInteger> by EisensteinIntNormSq

    /**
     * Note: due to rounding in Real, for sufficiently large values, this may not precisely meet the exact
     * definition of a ring homomorphism. BigInteger to Real may become lossy quite quickly.
     */
    private val sqrt3over2 = sqrt(3.0) / 2.0
    val EisensteinIntToCHomomorphism: RingHomomorphism<EisensteinInt, Complex> = RingHomomorphism.of(
        EisensteinIntCommutativeRing,
        ComplexAlgebras.ComplexField,
        UnaryOp { (a, b) ->
            val aReal = a.toReal()
            val bReal = b.toReal()
            Complex(aReal - bReal / 2.0, bReal * sqrt3over2)
        }
    )

    fun EisensteinInt.toComplex(): Complex =
        EisensteinIntToCHomomorphism.apply(this)
}
