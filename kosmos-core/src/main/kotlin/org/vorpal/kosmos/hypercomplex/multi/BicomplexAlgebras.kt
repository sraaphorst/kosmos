package org.vorpal.kosmos.hypercomplex.multi

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.basisPrintable
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.complex.re
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger

/**
 * [BicomplexAlgebras] contains the algebraic structures over the [Bicomplex] type.
 *
 * These include:
 * - [BicomplexCommutativeRing]: the bicomplex commutative ring (has zero divisors).
 * - [bicomplexRealVectorSpace]: the 4D vector space over the reals.
 * - [bicomplexComplexVectorSpace]: the 2D vector space over the complex numbers.
 */
object BicomplexAlgebras {

    object BicomplexCommutativeRing :
        CommutativeRing<Bicomplex>,
        HasReciprocal<Bicomplex>,
        HasNormSq<Bicomplex, Real> {

        private val complexRing = ComplexAlgebras.ComplexField

        override val zero = Bicomplex.ofIdempotent(complexRing.zero, complexRing.zero)
        override val one = Bicomplex.ofIdempotent(complexRing.one, complexRing.one)

        // Imaginary unit from the first complex plane
        val i = Bicomplex.ofStandard(0.0, 1.0, 0.0, 0.0)

        // The bicomplex unit
        val j = Bicomplex.ofStandard(0.0, 0.0, 1.0, 0.0)

        // The product of the two units (k = ij)
        val k = Bicomplex.ofStandard(0.0, 0.0, 0.0, 1.0)

        override val add = AbelianGroup.of(
            identity = zero,
            op = BinOp(Symbols.PLUS) { x, y ->
                Bicomplex.ofIdempotent(
                    complexRing.add(x.alpha, y.alpha),
                    complexRing.add(x.beta, y.beta)
                )
            },
            inverse = Endo(Symbols.MINUS) { x ->
                Bicomplex.ofIdempotent(
                    complexRing.add.inverse(x.alpha),
                    complexRing.add.inverse(x.beta)
                )
            }
        )

        override val mul = CommutativeMonoid.of(
            identity = one,
            op = BinOp(Symbols.ASTERISK) { x, y ->
                Bicomplex.ofIdempotent(
                    complexRing.mul(x.alpha, y.alpha),
                    complexRing.mul(x.beta, y.beta)
                )
            }
        )

        /**
         * Determine if [a] has a reciprocal, i.e. is not a zero divisor.
         */
        override fun hasReciprocal(a: Bicomplex): Boolean =
            a.isUnit()

        override val reciprocal: Endo<Bicomplex> = Endo(Symbols.INVERSE) { z ->
            if (hasReciprocal(z)) Bicomplex.ofIdempotent(
                complexRing.reciprocal(z.alpha),
                complexRing.reciprocal(z.beta)
            )
            else throw ArithmeticException("Cannot take reciprocal of a non-invertible bicomplex number.")
        }

        override fun fromBigInt(n: BigInteger): Bicomplex {
            val c = complexRing.fromBigInt(n)
            return Bicomplex.ofIdempotent(c, c)
        }

        /**
         * First conjugation on bicomplex numbers.
         *
         * Conjugates only the complex unit `i`, leaving `j` unchanged.
         *
         * In the standard basis:
         * ```kotlin
         * a + bi + cj + d(ij) ↦ a - bi + cj - d(ij)
         * ```
         * In the idempotent basis:
         * ```kotlin
         * (𝛂, 𝜷) ↦ (𝜷*, 𝛂*)
         * ```
         */
        val conj1: Endo<Bicomplex> = Endo("${Symbols.DAGGER}1") { z ->
            Bicomplex.ofIdempotent(
                ComplexAlgebras.ComplexField.conj(z.beta),
                ComplexAlgebras.ComplexField.conj(z.alpha)
            )
        }

        /**
         * Norm with respect to the first conjugation.
         *
         * Annihilates the `i` and `k` terms, mapping the result perfectly into the
         * split-complex plane `ℂ(j)` spanned by `{1, j}`.
         *
         * ```kotlin
         * * N1(w) = w * w†1 = (a² + b² - c² - d²) + 2(ac + bd)j
         * ```
         * Idempotent logic:
         * ```kotlin
         * (𝛂, 𝜷)(𝜷*, 𝛂*) = (𝛂𝜷*, 𝜷𝛂*)
         * ```
         *
         * TODO: Consider extracting to a SplitComplex type when available, but
         *       returning [Bicomplex] is structurally sound as `ℂ(j)` is a valid subring.
         */
        val norm1: UnaryOp<Bicomplex, Bicomplex> = UnaryOp("norm1") { z ->
            val complexField = ComplexAlgebras.ComplexField
            Bicomplex.ofIdempotent(
                complexField.mul(z.alpha, complexField.conj(z.beta)),
                complexField.mul(z.beta, complexField.conj(z.alpha))
            )
        }

        /**
         * Second conjugation on bicomplex numbers.
         *
         * Conjugates only the bicomplex unit `j`, leaving `i` unchanged.
         *
         * In the standard basis:
         * ```kotlin
         * a + bi + cj + d(ij) ↦ a + bi - cj - d(ij)
         * ```
         * In the idempotent basis:
         * ```kotlin
         * (𝛂, 𝜷) ↦ (𝜷, 𝛂)
         * ```
         */
        val conj2: Endo<Bicomplex> = Endo("${Symbols.DAGGER}2") { z ->
            Bicomplex.ofIdempotent(z.beta, z.alpha)
        }

        /**
         * Norm with respect to the second conjugation.
         *
         * Annihilates the `j` and `k` terms, mapping the result perfectly into the
         * canonical complex plane `ℂ(i)` spanned by `{1, i}`.
         *
         * ```kotlin
         * N2(w) = w * w†2 = (a² - b² + c² - d²) + 2(ab + cd)i
         * ```
         * Idempotent logic:
         * ```kotlin
         * (𝛂, 𝜷)(𝜷, 𝛂) = (𝛂𝜷, 𝛂𝜷)
         * ```
         * * Note: Because both idempotents are identical, this is strictly a standard [Complex] number.
         */
        val norm2: UnaryOp<Bicomplex, Bicomplex> = UnaryOp("norm2") { z ->
            val prod = ComplexAlgebras.ComplexField.mul(z.alpha, z.beta)
            Bicomplex.ofIdempotent(prod, prod)
        }

        /**
         * Third conjugation on bicomplex numbers: the principal involution.
         *
         * Conjugates both `i` and `j`.
         *
         * In the standard basis:
         * ```kotlin
         * a + bi + cj + d(ij) ↦ a - bi - cj + d(ij)
         * ```
         * In the idempotent basis:
         * ```kotlin
         * (𝛂, 𝜷) ↦ (𝛂*, 𝜷*)
         * ```
         */
        val conj3: Endo<Bicomplex> = Endo("${Symbols.DAGGER}3") { z ->
            Bicomplex.ofIdempotent(
                ComplexAlgebras.ComplexField.conj(z.alpha),
                ComplexAlgebras.ComplexField.conj(z.beta)
            )
        }

        /**
         * Norm with respect to the principal involution.
         *
         * Annihilates the `i` and `j` terms, mapping the result into the
         * split-complex plane `ℂ(k)` spanned by `{1, k}`, where `k = ij`.
         * ```kotlin
         * N3(w) = w * w†3 = (a² + b² + c² + d²) + 2(ad - bc)k
         * ```
         * Idempotent logic:
         * ```kotlin
         * (𝛂, 𝜷)(𝛂*, 𝜷*) = (|𝛂|², |𝜷|²)
         * ```
         * The real part of this result yields the squared Euclidean norm.
         */
        val norm3: UnaryOp<Bicomplex, Bicomplex> = UnaryOp("norm3") { z ->
            val complexField = ComplexAlgebras.ComplexField
            Bicomplex.ofIdempotent(
                complexField.mul(z.alpha, complexField.conj(z.alpha)),
                complexField.mul(z.beta, complexField.conj(z.beta))
            )
        }

        /**
         * Calculates the squared 4D Euclidean norm of the bicomplex number.
         * * ||w||² = a² + b² + c² + d²
         */
        override val normSq: UnaryOp<Bicomplex, Real> = UnaryOp(Symbols.NORM_SQ_SYMBOL) {
            val n3 = norm3(it)
            n3.standard().first.re
        }

        /**
         * Calculates the 4D Euclidean norm of the bicomplex number.
         */
        val norm: UnaryOp<Bicomplex, Real> = UnaryOp("norm") {
            kotlin.math.sqrt(normSq(it))
        }
    }

    /**
     * Standard embedding of ℂ into the Bicomplex ring.
     *
     * In the idempotent representation, maps:
     * ```kotlin
     * z -> (z, z)
     * ```
     */
    val complexToBicomplexMonomorphism: RingMonomorphism<Complex, Bicomplex> = RingMonomorphism.of(
        domain = ComplexAlgebras.ComplexField,
        codomain = BicomplexCommutativeRing,
        map = UnaryOp { z -> Bicomplex.ofIdempotent(z, z) }
    )

    /**
     * Homomorphic projection of a bicomplex number onto the complex plane via the first idempotent element (𝛂).
     * Unlike standard basis projections, idempotent projections preserve ring multiplication.
     */
    object BicomplexFirstProjectionHomomorphism: RingHomomorphism<Bicomplex, Complex> {
        override val domain = BicomplexCommutativeRing
        override val codomain = ComplexAlgebras.ComplexField
        override val map = UnaryOp<Bicomplex, Complex>("${Symbols.PROJECTION}_𝛂") { it.idempotent().first }
    }

    /**
     * Homomorphic projection of a bicomplex number onto the complex plane via the second idempotent element (𝜷).
     * Unlike standard basis projections, idempotent projections preserve ring multiplication.
     */
    object BicomplexSecondProjectionHomomorphism: RingHomomorphism<Bicomplex, Complex> {
        override val domain = BicomplexCommutativeRing
        override val codomain = ComplexAlgebras.ComplexField
        override val map = UnaryOp<Bicomplex, Complex>("${Symbols.PROJECTION}_𝜷") { it.idempotent().second }
    }

    /**
     * A monomorphism from 𝔹 to M_2(ℂ) using the canonical basis.
     * Maps z_1 + z_2 j ↦ [[z_1, z_2], [-z_2, z_1]]
     */
    object BicomplexToComplexMatrixMonomorphism : RingMonomorphism<Bicomplex, DenseMat<Complex>> {
        private val complexField = ComplexAlgebras.ComplexField
        override val domain = BicomplexCommutativeRing
        override val codomain = DenseMatAlgebras.DenseMatRing(complexField, 2)
        override val map: UnaryOp<Bicomplex, DenseMat<Complex>> = UnaryOp("M_2(ℂ)_canonical") { z ->
            val (z1, z2) = z.standard()
            DenseMat.ofRows(
                listOf(
                    listOf(z1, z2),
                    listOf(complexField.add.inverse(z2), z1)
                )
            )
        }
    }

    /**
     * A monomorphism from 𝔹 to M_2(ℂ) using the orthogonal idempotent basis.
     * Maps (𝛂, 𝜷) ↦ [[𝛂, 0], [0, 𝜷]]
     */
    object BicomplexToDiagonalMatrixMonomorphism : RingMonomorphism<Bicomplex, DenseMat<Complex>> {
        private val complexField = ComplexAlgebras.ComplexField
        override val domain = BicomplexCommutativeRing
        override val codomain = DenseMatAlgebras.DenseMatRing(complexField, 2)
        override val map: UnaryOp<Bicomplex, DenseMat<Complex>> = UnaryOp("M_2(ℂ)_diagonal") { b ->
            DenseMat.ofRows(
                listOf(
                    listOf(b.alpha, complexField.zero),
                    listOf(complexField.zero, b.beta)
                )
            )
        }
    }

    /**
     * The bicomplex numbers are a 4-dimensional vector space over the reals.
     */
    val bicomplexRealVectorSpace: FiniteVectorSpace<Real, Bicomplex> = FiniteVectorSpace.of(
        scalars = RealAlgebras.RealField,
        add = BicomplexCommutativeRing.add,
        dimension = 4,
        leftAction = LeftAction(Symbols.ARROW_RIGHT) { r, b ->
            Bicomplex.ofIdempotent(
                ComplexAlgebras.ComplexRealVectorSpace.leftAction(r, b.alpha),
                ComplexAlgebras.ComplexRealVectorSpace.leftAction(r, b.beta)
            )
        }
    )

    /**
     * The bicomplex numbers are a 2-dimensional vector space over the complex numbers.
     */
    val bicomplexComplexVectorSpace: FiniteVectorSpace<Complex, Bicomplex> = FiniteVectorSpace.of(
        scalars = ComplexAlgebras.ComplexField,
        add = BicomplexCommutativeRing.add,
        dimension = 2,
        leftAction = LeftAction(Symbols.ARROW_RIGHT) { c, b ->
            Bicomplex.ofIdempotent(
                ComplexAlgebras.ComplexField.mul(c, b.alpha),
                ComplexAlgebras.ComplexField.mul(c, b.beta)
            )
        }
    )

    /**
     * The star algebra associated with the first conjugation (conjugating only the 'i' unit).
     */
    val bicomplexStarAlgebra1: StarAlgebra<Real, Bicomplex> = StarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = object : InvolutiveRing<Bicomplex>, CommutativeRing<Bicomplex> by BicomplexCommutativeRing {
            override val zero = BicomplexCommutativeRing.zero
            override val conj = BicomplexCommutativeRing.conj1
        },
        leftAction = bicomplexRealVectorSpace.leftAction
    )

    /**
     * The star algebra associated with the second conjugation (conjugating only the 'j' unit).
     */
    val bicomplexStarAlgebra2: StarAlgebra<Real, Bicomplex> = StarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = object : InvolutiveRing<Bicomplex>, CommutativeRing<Bicomplex> by BicomplexCommutativeRing {
            override val zero = BicomplexCommutativeRing.zero
            override val conj = BicomplexCommutativeRing.conj2
        },
        leftAction = bicomplexRealVectorSpace.leftAction
    )

    /**
     * The principal star algebra associated with the third conjugation (conjugating both 'i' and 'j').
     * This is the standard involution used when treating Bicomplex as a RealNormedAlgebra.
     */
    val bicomplexStarAlgebraPrincipal: StarAlgebra<Real, Bicomplex> = StarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = object : InvolutiveRing<Bicomplex>, CommutativeRing<Bicomplex> by BicomplexCommutativeRing {
            override val zero = BicomplexCommutativeRing.zero
            override val conj = BicomplexCommutativeRing.conj3
        },
        leftAction = bicomplexRealVectorSpace.leftAction
    )

    val eqBicomplexStrict: Eq<Bicomplex> = Eq { x, y ->
        ComplexAlgebras.eqComplexStrict(x.alpha, y.alpha) &&
            ComplexAlgebras.eqComplexStrict(x.beta, y.beta)
    }

    val eqBicomplex: Eq<Bicomplex> = Eq { x, y ->
        ComplexAlgebras.eqComplex(x.alpha, y.alpha) && ComplexAlgebras.eqComplex(x.beta, y.beta)
    }

    private fun printableBicomplexGenerator(
        prReal: Printable<Real>,
        eqReal: Eq<Real>
    ): Printable<Bicomplex> =
        basisPrintable(
            labels = listOf("", Symbols.IMAGINARY_I, Symbols.IMAGINARY_J, Symbols.IMAGINARY_K),
            decompose = { it.coefficients() },
            signed = RealAlgebras.SignedReal,
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            prA = prReal,
            eqA = eqReal
        )

    val printableBicomplex: Printable<Bicomplex> = printableBicomplexGenerator(
        prReal = RealAlgebras.printableReal,
        eqReal = RealAlgebras.eqRealApprox
    )

    val printableBicomplexStrict: Printable<Bicomplex> = printableBicomplexGenerator(
        prReal = RealAlgebras.printableRealStrict,
        eqReal = RealAlgebras.eqRealStrict
    )

    val printableBicomplexPretty: Printable<Bicomplex> = printableBicomplexGenerator(
        prReal = RealAlgebras.printableRealPretty,
        eqReal = RealAlgebras.eqRealApprox
    )
}