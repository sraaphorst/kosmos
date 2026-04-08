package org.vorpal.kosmos.numberfields.quadratic

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.quadratic.quadraticRank2MatrixEmbedding
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.EuclideanDomain
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.bridge.ZModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.math.toReal
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.BinaryOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.syntax.toNearestInt
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.geometry.lattices.EuclideanLattice
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ComplexPrintable
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.Vec2
import java.math.BigInteger
import kotlin.math.sqrt

/**
 * Main structures:
 * - [EisensteinIntCommutativeRing]: the Eisenstein integers.
 * - [EisensteinIntEuclideanDomain]: the Eisenstein integers as a Euclidean domain.
 *
 * Lattice structures:
 * - [EisensteinIntLattice]: the Eisenstein integers as a lattice.
 *
 * Vector spaces and modules:
 * - [ZModuleEisensteinInt]: the Eisenstein integers as a ZModule.
 *
 * Homomorphisms:
 * - [EisensteinIntRank2ZMatrixEmbedding]: a ring monomorphism from the Eisenstein ring to the rank 2 Z matrix ring.
 * - [EisensteinIntToCHomomorphism]: a ring homomorphism from the Eisenstein ring to the complex field.
 *   Note that this could defensibly be a ring monomorphism, but due to floating point errors, we cannot guarantee
 *   injectivity.
 * - [EisensteinInt.toComplex]: convenience method for this monomorphism.
 *
 * Eqs:
 * - [eqEisensteinInt]: equality on Eisenstein numbers.
 *
 * Printables:
 * - [printableEisensteinInt]: a printable Eisenstein number.
 * - [printableEisensteinIntPretty]: a pretty printable Eisenstein number.
 */
object EisensteinIntAlgebras {
    private val sqrt3over2 = sqrt(3.0) / 2.0

    object EisensteinIntCommutativeRing :
        CommutativeRing<EisensteinInt>,
        HasNormSq<EisensteinInt, BigInteger>,
        InvolutiveRing<EisensteinInt> {

        override val zero: EisensteinInt = EisensteinInt.ZERO
        override val one: EisensteinInt = EisensteinInt.ONE

        override val add: AbelianGroup<EisensteinInt> = AbelianGroup.of(
            identity = zero,
            op = BinOp(Symbols.PLUS) { e1, e2 -> EisensteinInt(e1.a + e2.a, e1.b + e2.b) },
            inverse = Endo(Symbols.MINUS) { e -> EisensteinInt(-e.a, -e.b) }
        )

        override val mul: CommutativeMonoid<EisensteinInt> = CommutativeMonoid.of(
            identity = one,
            op = BinOp(Symbols.ASTERISK) { e1, e2 ->
                EisensteinInt(e1.a * e2.a - e1.b * e2.b, e1.a * e2.b + e1.b * e2.a - e1.b * e2.b)
            }
        )

        override val normSq: UnaryOp<EisensteinInt, BigInteger> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) { z ->
                val a = z.a
                val b = z.b
                a * a - a * b + b * b
            }

        override fun fromBigInt(n: BigInteger): EisensteinInt =
            EisensteinInt(n, BigInteger.ZERO)

        // conj(a + bω) = (a - b) + (-b)ω
        override val conj: Endo<EisensteinInt> = Endo(Symbols.CONJ) { ez ->
            EisensteinInt(ez.a - ez.b, -ez.b)
        }
    }

    /**
     * Note that this is actually an integral domain, and in fact a Euclidean domain with
     * norm function:
     * ```text
     * N(a + bω) = a^2 - ab + b^2
     * ```
     * giving gcd and unique factorization.
     *
     * We keep them separate to avoid carrying too much machinery in one class.
     */
    object EisensteinIntEuclideanDomain :
        EuclideanDomain<EisensteinInt, BigInteger> by EuclideanDomain.of(
            add = EisensteinIntCommutativeRing.add,
            mul = EisensteinIntCommutativeRing.mul,
            divRem = BinaryOp(Symbols.DIV_REM) { alpha, beta ->
                require(beta != EisensteinInt.ZERO) { "division by zero" }
                val den = EisensteinIntCommutativeRing.normSq(beta)
                require(den != BigInteger.ZERO) { "normSq(beta) must be nonzero for beta != 0" }

                val conjBeta = EisensteinIntCommutativeRing.conj(beta)
                val num = EisensteinIntCommutativeRing.mul(alpha, conjBeta)
                val m0 = Rational.of(num.a, den).toNearestInt()
                val n0 = Rational.of(num.b, den).toNearestInt()

                fun remainder(q: EisensteinInt): EisensteinInt =
                    EisensteinIntCommutativeRing.add(alpha,
                        EisensteinIntCommutativeRing.add.inverse(EisensteinIntCommutativeRing.mul(beta, q))
                    )

                val (bestQ, bestR) = (-1..1).asSequence()
                    .flatMap { dm -> (-1..1).asSequence().map { dn -> dm to dn } }
                    .map { (dm, dn) -> EisensteinInt(m0 + dm.toBigInteger(), n0 + dn.toBigInteger()) }
                    .map { q -> q to remainder(q) }
                    .minBy { (_, r) -> EisensteinIntCommutativeRing.normSq(r) }

                check(bestR == EisensteinInt.ZERO || EisensteinIntCommutativeRing.normSq(bestR) < den) {
                    "Euclidean condition failed"
                }
                bestQ to bestR
            },
            measureOp = UnaryOp(Symbols.NORM_SQ_SYMBOL) { z ->
                EisensteinIntCommutativeRing.normSq(z)
            }
        )


    object ZModuleEisensteinInt: ZModule<EisensteinInt> {
        override val scalars = IntegerAlgebras.IntegerCommutativeRing
        override val add = EisensteinIntCommutativeRing.add
        override val leftAction: LeftAction<BigInteger, EisensteinInt> =
            LeftAction(Symbols.TRIANGLE_RIGHT) { n, ez ->
                EisensteinInt(n * ez.a, n * ez.b)
            }
    }

    /**
     * The Eisenstein integers `ℤ[ω]` are viewed via three related embeddings:
     *
     * 1) Coordinate embedding: `ℤ² → ℤ[ω]`, `(m, n) ↦ m + nω` (`ℤ²` is the free abelian group on the basis `{1, ω}`)
     * 2) Real (geometric) embedding: `ℤ[ω] → ℝ²`, `a + bω ↦ (a - b/2, (√3/2)b)`
     * 3) Complex embedding: `ℤ[ω] → ℂ`, `a + bω ↦ a + bω`
     *
     * where:
     * ```text
     * ω = e^{2πi/3} = (-1 + √3 i) / 2.
     * ```
     * The bilinear form `dot` is the symmetric bilinear form associated to the quadratic form
     * ```text
     * N(a + bω) = a² - ab + b²
     * ```
     * via polarization:
     * ```text
     * ⟨x, y⟩ = (N(x + y) - N(x) - N(y)) / 2.
     * ```
     *
     * Under the standard real embedding `ℤ[ω] → ℝ²` given by `ω = (-1 + √3 i)/2`,
     * ```text
     * a + bω ↦ (a - b/2, (√3/2)b),
     * ```
     * this `⟨·,·⟩` is exactly the usual Euclidean dot product in ℝ².
     *
     * In particular, for the ℤ-basis vectors `{1, ω}`, we have
     * ```text
     * ⟨1, ω⟩ = -1/2,
     * ```
     * since the corresponding vectors `(1, 0)` and `(-1/2, √3/2)` are not orthogonal.
     *
     * Note that the Eisenstein integers are not obtained by a Cayley-Dickson doubling construction.
     */
    object EisensteinIntLattice: EuclideanLattice<EisensteinInt, Rational> {
        override val rank: Int = 2
        private val ring = EisensteinIntCommutativeRing

        override val dot: (EisensteinInt, EisensteinInt) -> Rational = { e1, e2 ->
            Rational.of(ring.normSq(ring.add(e1, e2)) - ring.normSq(e1) - ring.normSq(e2), BigInteger.TWO)
        }

        /**
         * The Eisenstein integers form a `ℤ`-basis `{1, ω}` over the abelian group `ℤ[ω]`.
         */
        override val basis: List<EisensteinInt> = listOf(
            EisensteinInt.ONE,
            EisensteinInt.OMEGA
        )

        override val addV: AbelianGroup<EisensteinInt> = ring.add
        override val scale: LeftAction<BigInteger, EisensteinInt> = LeftAction(Symbols.TRIANGLE_RIGHT) { s, e ->
            EisensteinInt(s * e.a, s * e.b)
        }

        val embedR2: UnaryOp<EisensteinInt, Vec2<Real>> = UnaryOp { e ->
            val aReal = e.a.toReal()
            val bReal = e.b.toReal()
            Vec2(aReal - bReal / 2.0, bReal * sqrt3over2)
        }

        /**
         * Call validate() last to make sure all necessary conditions are met AFTER initialization happens.
         */
        @Suppress("unused")
        private val _validated = validate()
    }

    val EisensteinIntRank2ZMatrixEmbedding: RingMonomorphism<EisensteinInt, DenseMat<BigInteger>> =
        quadraticRank2MatrixEmbedding(
            domain = EisensteinIntCommutativeRing,
            coefficientRing = IntegerAlgebras.IntegerCommutativeRing,
            s = -BigInteger.ONE,
            t = -BigInteger.ONE,
            coeffs = { it.a to it.b }
        )

    /**
     * Note: due to rounding in Real, for sufficiently large values, this may not precisely meet the exact
     * definition of a ring homomorphism. BigInteger to Real may become lossy quite quickly.
     */
    object EisensteinIntToCHomomorphism: RingHomomorphism<EisensteinInt, Complex> {
        override val domain = EisensteinIntCommutativeRing
        override val codomain = ComplexAlgebras.ComplexField
        override val map = UnaryOp<EisensteinInt, Complex> { e ->
            val aReal = e.a.toReal()
            val bReal = e.b.toReal()
            complex(aReal - bReal / 2.0, bReal * sqrt3over2)
        }
    }

    fun EisensteinInt.toComplex(): Complex =
        EisensteinIntToCHomomorphism(this)

    val eqEisensteinInt: Eq<EisensteinInt> = Eq.default()

    val printableEisensteinInt: Printable<EisensteinInt> =
        ComplexPrintable.complexLikePrintable(
            signed = IntegerAlgebras.SignedInteger,
            zero = BigInteger.ZERO,
            one = BigInteger.ONE,
            re = { it.a },
            im = { it.b },
            basis = Symbols.OMEGA,
            prA = IntegerAlgebras.printableInteger,
            eqA = IntegerAlgebras.eqInteger
        )

    val printableEisensteinIntPretty: Printable<EisensteinInt> =
        printableEisensteinInt
}
