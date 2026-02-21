package org.vorpal.kosmos.core.eisenstein

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.Complex
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.geometry.lattices.EuclideanLattice
import org.vorpal.kosmos.linear.values.Vec2
import java.math.BigInteger
import kotlin.math.sqrt

object EisensteinIntAlgebras {
    private val sqrt3over2 = sqrt(3.0) / 2.0

    /**
     * Note that this is actually an integral domain, and in fact a Euclidean domain with
     * norm function n(a + bω) = a^2 - ab + b^2, giving gcd and unique factorization.
     */
    object EisensteinIntCommutativeRing :
        CommutativeRing<EisensteinInt>, // by EisensteinIntCommutativeRing,
        HasNormSq<EisensteinInt, BigInteger>,
        InvolutiveRing<EisensteinInt> {
        override val add: AbelianGroup<EisensteinInt> = AbelianGroup.of(
            identity = EisensteinInt.ZERO,
            op = BinOp(Symbols.PLUS, EisensteinInt::plus),
            inverse = Endo(Symbols.MINUS, EisensteinInt::unaryMinus)
        )

        override val mul: CommutativeMonoid<EisensteinInt> = CommutativeMonoid.of(
            identity = EisensteinInt.ONE,
            op = BinOp(Symbols.ASTERISK, EisensteinInt::times)
        )

        override val zero: EisensteinInt = EisensteinInt.ZERO
        override val one: EisensteinInt = EisensteinInt.ONE

        override val normSq: UnaryOp<EisensteinInt, BigInteger> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { z ->
                val a = z.a
                val b = z.b
                a * a - a * b + b * b
            }

        override val normElement: Endo<EisensteinInt> = Endo(Symbols.NORM) { x ->
            mul.op(x, conj(x))
        }

        override fun fromBigInt(n: BigInteger): EisensteinInt =
            EisensteinInt(n, BigInteger.ZERO)

        // conj(a + bω) = (a - b) + (-b)ω
        override val conj: Endo<EisensteinInt> = Endo(Symbols.CONJ) { ez ->
            EisensteinInt(ez.a - ez.b, -ez.b)
        }
    }

    object EisensteinIntLattice: EuclideanLattice<EisensteinInt, Rational> {
        private val ring = EisensteinIntCommutativeRing

        override val dot: (EisensteinInt, EisensteinInt) -> Rational = { x, y ->
            Rational.of(ring.normSq((ring.add(x, y))) - ring.normSq(x) - ring.normSq(y), BigInteger.TWO)
        }

        override val rank: Int = 2

        /**
         * The basis is {1, ω}.
         */
        override val basis: List<EisensteinInt> = listOf(
            EisensteinInt.ONE,
            EisensteinInt.OMEGA
        )

        override val addV: AbelianGroup<EisensteinInt> = ring.add
        override val scale: LeftAction<BigInteger, EisensteinInt> = LeftAction(Symbols.TRIANGLE_RIGHT) { s, (a, b) ->
            EisensteinInt(s * a, s * b)
        }

        val embed: UnaryOp<EisensteinInt, Vec2<Real>> = UnaryOp { (a, b) ->
            val aReal = a.toReal()
            val bReal = b.toReal()
            Vec2(aReal - bReal / 2.0, bReal * sqrt3over2)
        }
    }

    /**
     * Note: due to rounding in Real, for sufficiently large values, this may not precisely meet the exact
     * definition of a ring homomorphism. BigInteger to Real may become lossy quite quickly.
     */
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

    val eqEisensteinInt: Eq<EisensteinInt> = Eq { x, y -> x == y }
}
