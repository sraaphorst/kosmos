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
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.basisPrintable
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplex
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplexAlgebras
import org.vorpal.kosmos.hypercomplex.splitcomplex.re
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger
import kotlin.math.sqrt

/**
 * Algebraic structures, morphisms, and utilities over the [Bicomplex] numbers
 * `𝔹 = ℂ(i)[j] ≅ ℂ ⊗ℝ ℂ`.
 *
 * The three imaginary/hyperbolic units satisfy `i² = -1`, `j² = -1` (both imaginary) and
 * `k = ij` with `k² = +1` (hyperbolic). `𝔹` is a commutative ring — not a field — with zero
 * divisors on the null cone.
 *
 * Ring and its units:
 * - [BicomplexCommutativeRing]: the commutative ring `𝔹`. It also exposes [HasReciprocal]
 *   (the units are exactly the non-zero-divisors) and [HasNormSq] (the Euclidean `normSq`).
 * - [BicomplexCommutativeRing.i], [BicomplexCommutativeRing.j], [BicomplexCommutativeRing.k]:
 *   the imaginary units `i`, `j` and the hyperbolic unit `k = ij`.
 *
 * Conjugations (involutions):
 * - [BicomplexCommutativeRing.conj1]: conjugates `i` only — `a + bi + cj + dk ↦ a - bi + cj - dk`.
 * - [BicomplexCommutativeRing.conj2]: conjugates `j` only — `↦ a + bi - cj - dk`.
 * - [BicomplexCommutativeRing.conj3]: the principal involution, conjugating both — `↦ a - bi - cj + dk`.
 *
 * Norm forms `N_t(w) = w · w^(†t)` and the scalar norm:
 * Each conjugation norm lands in the subring fixed by its involution, and is typed by that subring:
 * - [BicomplexCommutativeRing.normByConj1] → [Bicomplex] confined to the ordinary complex line
 *   `ℂ(j)` (`j² = -1`); not narrowed to [Complex], since `ℂ(j) ≠` the canonical `ℂ(i)`.
 * - [BicomplexCommutativeRing.normByConj2] → [Complex]: the canonical base field `ℂ(i)`.
 * - [BicomplexCommutativeRing.normByConj3] → [SplitComplex]<[Real]>: the split-complex line
 *   `ℂ(k)` (`k² = +1`).
 * - [BicomplexCommutativeRing.normSq], [BicomplexCommutativeRing.norm]: the scalar 4D Euclidean
 *   squared norm `a² + b² + c² + d²` (the real part of `normByConj3`) and its square root.
 *
 * ℂ(j) ↔ ℂ identification (the non-canonical `j ↦ i`):
 * - [complexToBicomplexAlongJ]: embeds `ℂ ↪ ℂ(j) ⊂ 𝔹`, `a + bi ↦ a + bj` — the `ℂ(j)` counterpart
 *   of [complexToBicomplexMonomorphism].
 * - [cjToComplex]: the left-inverse retraction `ℂ(j) → ℂ`, `a + bj ↦ a + bi`.
 * - [normByConj1ToCanonical]: [BicomplexCommutativeRing.normByConj1] delivered as a canonical
 *   [Complex] via [cjToComplex].
 *
 * Vector spaces:
 * - [BicomplexRealVectorSpace]: `𝔹` as a 4-dimensional vector space over the reals.
 * - [BicomplexComplexVectorSpace]: `𝔹` as a 2-dimensional vector space over the complex numbers.
 *
 * Star algebras (one per involution):
 * - [BicomplexStarAlgebra1]: the `*`-algebra for [BicomplexCommutativeRing.conj1].
 * - [BicomplexStarAlgebra2]: the `*`-algebra for [BicomplexCommutativeRing.conj2].
 * - [BicomplexStarAlgebraPrincipal]: the `*`-algebra for the principal involution [BicomplexCommutativeRing.conj3].
 *
 * Homomorphisms and embeddings:
 * - [complexToBicomplexMonomorphism]: the embedding `ℂ ↪ 𝔹`, `z ↦ (z, z)` in the idempotent basis.
 * - [BicomplexFirstProjectionHomomorphism], [BicomplexSecondProjectionHomomorphism]: the idempotent
 *   projections `𝔹 → ℂ` (`π_𝛂`, `π_𝜷`), which — unlike standard-basis projections — preserve ring
 *   multiplication.
 * - [BicomplexToComplexMatrixMonomorphism]: the embedding `𝔹 ↪ M₂(ℂ)` in the canonical basis.
 * - [BicomplexToDiagonalMatrixMonomorphism]: the embedding `𝔹 ↪ M₂(ℂ)` in the orthogonal-idempotent
 *   (diagonal) basis.
 *
 * Eqs:
 * - [eqBicomplexStrict]: exact componentwise equality.
 * - [eqBicomplex]: approximate equality in the idempotent representation.
 *
 * Printables:
 * - [printableBicomplex]: standard rendering in the `1, i, j, k` basis.
 * - [printableBicomplexStrict]: strict rendering (exact reals).
 * - [printableBicomplexPretty]: pretty rendering.
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
         * ```text
         * a + bi + cj + d(ij) ↦ a - bi + cj - d(ij)
         * ```
         * In the idempotent basis:
         * ```text
         * (𝛂, 𝜷) ↦ (𝜷*, 𝛂*)
         * ```
         */
        val conj1: Endo<Bicomplex> = Endo("${Symbols.DAGGER}1") { z ->
            Bicomplex.ofIdempotent(
                ComplexAlgebras.ComplexStarAlgebra.conj(z.beta),
                ComplexAlgebras.ComplexStarAlgebra.conj(z.alpha)
            )
        }

        /**
         * Norm with respect to the first conjugation.
         *
         * Annihilates the `i` and `k` terms, mapping the result into the ordinary complex
         * plane `ℂ(j)` spanned by `{1, j}`. Note `j² = -1` here: in this construction both `i`
         * and `j` are imaginary units and only `k = ij` is hyperbolic, so `{1, j}` is an
         * ordinary complex line, *not* a split-complex one — contrast [normByConj3], whose image
         * lies in the split-complex plane `ℂ(k)` with `k² = +1`.
         *
         * ```text
         * N1(w) = w * w†1 = (a² + b² - c² - d²) + 2(ac + bd)j
         * ```
         * Idempotent logic:
         * ```text
         * (𝛂, 𝜷)(𝜷*, 𝛂*) = (𝛂𝜷*, 𝜷𝛂*)
         * ```
         *
         * The result is returned as a [Bicomplex]: it lives inside the embedded subring
         * `ℂ(j) ⊂ 𝔹`, *not* inside your canonical [Complex] (which is `ℂ(i)`). Unlike [normByConj2],
         * whose two idempotent projections coincide and so collapse to an unambiguous [Complex],
         * the projections of `N1` are complex conjugates of one another (`𝛂𝜷*` and `𝜷𝛂*`), so
         * narrowing to [Complex] would require arbitrarily choosing the identification `j ↦ i`
         * or `j ↦ -i`.
         */
        val normByConj1: UnaryOp<Bicomplex, Bicomplex> = UnaryOp("normByConj1") { z ->
            val complex = ComplexAlgebras.ComplexStarAlgebra
            Bicomplex.ofIdempotent(
                complex.mul(z.alpha, complex.conj(z.beta)),
                complex.mul(z.beta, complex.conj(z.alpha))
            )
        }

        /**
         * Second conjugation on bicomplex numbers.
         *
         * Conjugates only the bicomplex unit `j`, leaving `i` unchanged.
         *
         * In the standard basis:
         * ```text
         * a + bi + cj + d(ij) ↦ a + bi - cj - d(ij)
         * ```
         * In the idempotent basis:
         * ```text
         * (𝛂, 𝜷) ↦ (𝜷, 𝛂)
         * ```
         */
        val conj2: Endo<Bicomplex> = Endo("${Symbols.DAGGER}2") { z ->
            Bicomplex.ofIdempotent(z.beta, z.alpha)
        }

        /**
         * Norm with respect to the second conjugation.
         *
         * Annihilates the `j` and `k` terms, mapping the result into the canonical complex plane
         * `ℂ(i)` spanned by `{1, i}` — the base field that `𝔹 = ℂ(i)[j]` is built over.
         *
         * ```text
         * N2(w) = w * w†2 = (a² - b² + c² - d²) + 2(ab + cd)i
         * ```
         * Idempotent logic:
         * ```text
         * (𝛂, 𝜷)(𝜷, 𝛂) = (𝛂𝜷, 𝛂𝜷)
         * ```
         * Because both idempotent projections of the result coincide (both equal `𝛂𝜷`), the value
         * is unambiguously a canonical [Complex] number — there is no choice of imaginary unit to
         * make — so we return [Complex] directly rather than a [Bicomplex] confined to `ℂ(i)`.
         */
        val normByConj2: UnaryOp<Bicomplex, Complex> = UnaryOp("normByConj2") { z ->
            ComplexAlgebras.ComplexField.mul(z.alpha, z.beta)   // 𝛂𝜷 ∈ ℂ(i)
        }

        /**
         * Third conjugation on bicomplex numbers: the principal involution.
         *
         * Conjugates both `i` and `j`.
         *
         * In the standard basis:
         * ```text
         * a + bi + cj + d(ij) ↦ a - bi - cj + d(ij)
         * ```
         * In the idempotent basis:
         * ```text
         * (𝛂, 𝜷) ↦ (𝛂*, 𝜷*)
         * ```
         */
        val conj3: Endo<Bicomplex> = Endo("${Symbols.DAGGER}3") { z ->
            val complex = ComplexAlgebras.ComplexStarAlgebra
            Bicomplex.ofIdempotent(
                complex.conj(z.alpha),
                complex.conj(z.beta)
            )
        }

        /**
         * Norm with respect to the principal involution.
         *
         * Annihilates the `i` and `j` terms, mapping the result into the split-complex plane
         * `ℂ(k)` spanned by `{1, k}`, where `k = ij` and `k² = +1`. Because this is genuinely a
         * split-complex line, the result is returned as a [SplitComplex]<[Real]>:
         * ```text
         * N3(w) = w * w†3 = (a² + b² + c² + d²) + 2(ad - bc)k   ↦   re + hy·j
         * ```
         * with `re = a² + b² + c² + d²` and `hy = 2(ad - bc)` under the identification `k ↦ j`.
         *
         * In the idempotent basis the principal norm is already diagonal:
         * ```text
         * (𝛂, 𝜷)(𝛂*, 𝜷*) = (|𝛂|², |𝜷|²)
         * ```
         * and `(|𝛂|², |𝜷|²)` are exactly the split-complex diagonal coordinates. The `𝜷`-idempotent
         * `(1 + k)/2` maps to `e₊` and the `𝛂`-idempotent `(1 - k)/2` to `e₋`, so we pass
         * `ePlus = |𝜷|²`, `eMinus = |𝛂|²` to [SplitComplexAlgebras.fromDiagonal].
         *
         * The real part `re` is the squared Euclidean norm; see [normSq].
         */
        val normByConj3: UnaryOp<Bicomplex, SplitComplex<Real>> = UnaryOp("normByConj3") { z ->
            val complex = ComplexAlgebras.ComplexStarAlgebra
            SplitComplexAlgebras.fromDiagonal(
                ePlus = complex.normSq(z.beta),
                eMinus = complex.normSq(z.alpha)
            )
        }

        /**
         * Calculates the squared 4D Euclidean norm of the bicomplex number.
         * * ||w||² = a² + b² + c² + d²
         */
        override val normSq: UnaryOp<Bicomplex, Real> = UnaryOp(Symbols.NORM_SQ_SYMBOL) {
            normByConj3(it).re
        }

        /**
         * Calculates the 4D Euclidean norm of the bicomplex number.
         */
        val norm: UnaryOp<Bicomplex, Real> = UnaryOp("norm") {
            sqrt(normSq(it))
        }
    }

    /**
     * Standard embedding of ℂ into the Bicomplex ring.
     *
     * In the idempotent representation, maps:
     * ```text
     * z -> (z, z)
     * ```
     */
    val complexToBicomplexMonomorphism: RingMonomorphism<Complex, Bicomplex> = RingMonomorphism.of(
        domain = ComplexAlgebras.ComplexField,
        codomain = BicomplexCommutativeRing,
        map = UnaryOp { z -> Bicomplex.ofIdempotent(z, z) }
    )

    /**
     * Embedding of ℂ into the `ℂ(j)` line of `𝔹`: the ring monomorphism `a + bi ↦ a + bj`.
     *
     * This is the `ℂ(j)` counterpart of [complexToBicomplexMonomorphism] (which embeds ℂ into the
     * canonical `ℂ(i)`). It is a genuine ring homomorphism — `j² = -1` exactly like `i²` — whose
     * image is the subring `ℂ(j) ⊂ 𝔹`, and [cjToComplex] is its left inverse
     * (`cjToComplex ∘ complexToBicomplexAlongJ = id_ℂ`).
     */
    val complexToBicomplexAlongJ: RingMonomorphism<Complex, Bicomplex> = RingMonomorphism.of(
        domain = ComplexAlgebras.ComplexField,
        codomain = BicomplexCommutativeRing,
        map = UnaryOp { c -> Bicomplex.ofStandard(c.a, 0.0, c.b, 0.0) }   // (re + im·i) ↦ (re + im·j)
    )

    /**
     * Retraction of the `ℂ(j)` line onto the canonical [Complex] field via the ring isomorphism
     * `ℂ(j) ≅ ℂ(i)`, `j ↦ i`:  `a + bj ↦ a + bi`.
     *
     * Equivalently, the first idempotent projection `π_𝛂` restricted to `ℂ(j)`. It is an
     * isomorphism ONLY on that line: off it (nonzero `i` or `k` parts) those parts are dropped, so
     * pass only `ℂ(j)`-valued elements — e.g. the result of [BicomplexCommutativeRing.normByConj1].
     * It is the left inverse of [complexToBicomplexAlongJ].
     */
    fun cjToComplex(z: Bicomplex): Complex {
        val coeffs = z.coefficients()
        return complex(coeffs[0], coeffs[2])   // (1-coefficient, j-coefficient) ↦ (re, im)
    }

    /**
     * [BicomplexCommutativeRing.normByConj1] delivered as a canonical [Complex] in `ℂ(i)`, i.e.
     * `cjToComplex ∘ normByConj1`.
     *
     * Returns `𝛂·conj(𝜷) = (a² + b² - c² - d²) + 2(ac + bd)i`. Use this when you want the
     * first-conjugation norm as an ordinary complex number and are willing to commit to the
     * (non-canonical) `j ↦ i` identification; otherwise prefer
     * [BicomplexCommutativeRing.normByConj1], which keeps the value honestly inside `ℂ(j) ⊂ 𝔹`.
     */
    val normByConj1ToCanonical: UnaryOp<Bicomplex, Complex> =
        UnaryOp("normByConj1ToCanonical") { w ->
            cjToComplex(BicomplexCommutativeRing.normByConj1(w))
        }

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
    val BicomplexRealVectorSpace: FiniteVectorSpace<Real, Bicomplex> = FiniteVectorSpace.of(
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
    val BicomplexComplexVectorSpace: FiniteVectorSpace<Complex, Bicomplex> = FiniteVectorSpace.of(
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
    val BicomplexStarAlgebra1: StarAlgebra<Real, Bicomplex> = StarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = object : InvolutiveRing<Bicomplex>, CommutativeRing<Bicomplex> by BicomplexCommutativeRing {
            override val zero = BicomplexCommutativeRing.zero
            override val conj = BicomplexCommutativeRing.conj1
        },
        leftAction = BicomplexRealVectorSpace.leftAction
    )

    /**
     * The star algebra associated with the second conjugation (conjugating only the 'j' unit).
     */
    val BicomplexStarAlgebra2: StarAlgebra<Real, Bicomplex> = StarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = object : InvolutiveRing<Bicomplex>, CommutativeRing<Bicomplex> by BicomplexCommutativeRing {
            override val zero = BicomplexCommutativeRing.zero
            override val conj = BicomplexCommutativeRing.conj2
        },
        leftAction = BicomplexRealVectorSpace.leftAction
    )

    /**
     * The principal star algebra associated with the third conjugation (conjugating both 'i' and 'j').
     * This is the standard involution used when treating Bicomplex as a RealNormedAlgebra.
     */
    val BicomplexStarAlgebraPrincipal: StarAlgebra<Real, Bicomplex> = StarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = object : InvolutiveRing<Bicomplex>, CommutativeRing<Bicomplex> by BicomplexCommutativeRing {
            override val zero = BicomplexCommutativeRing.zero
            override val conj = BicomplexCommutativeRing.conj3
        },
        leftAction = BicomplexRealVectorSpace.leftAction
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
