package org.vorpal.kosmos.hypercomplex.multi

import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.complexRingOver
import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.quadratic.quadraticRank2MatrixEmbedding
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CarrierTransport
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.contramap
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.basisPrintable
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.complex.RealComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplex
import org.vorpal.kosmos.hypercomplex.splitcomplex.splitComplex
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat
import kotlin.math.sqrt

/**
 * `𝔹 = ℂ[j]/(j² + 1)`: the Cayley-Dickson doubling of `ℂ` (trivial involution, `σ = 1`),
 * transported to the [RealBicomplex] carrier. Its involution is `conj2`.
 *
 * This is file-level rather than a member of [BicomplexAlgebras] so that initializing
 * [BicomplexAlgebras.BicomplexCommutativeRing] never recursively initializes [BicomplexAlgebras].
 */
/**
 * `Complex(R)[j]/(j² + 1)`: the Cayley-Dickson doubling of `Complex(R)` (trivial involution,
 * `σ = 1`), on the [Bicomplex] carrier. Its involution is `conj2`.
 */
internal fun <R : Any> bicomplexRingOver(
    base: CommutativeRing<R>
): CommutativeInvolutiveRing<Bicomplex<R>> {
    val complexRing = complexRingOver(base)
    return CarrierTransport<CD<Complex<R>>, Bicomplex<R>>(
        wrap = UnaryOp { cd -> Bicomplex(cd) },
        unwrap = UnaryOp { z -> z.cd }
    ).commutativeInvolutiveRing(
        CayleyDickson.commutative(complexRing, complexRing.one)
    )
}

private val cayleyDicksonBicomplex: CommutativeInvolutiveRing<RealBicomplex> =
    CarrierTransport<CD<RealComplex>, RealBicomplex>(
        wrap = UnaryOp { cd -> Bicomplex(cd) },
        unwrap = UnaryOp { z -> z.cd }
    ).commutativeInvolutiveRing(
        CayleyDickson.commutative(
            base = ComplexAlgebras.ComplexField,
            sigma = ComplexAlgebras.ComplexField.one
        )
    )

/**
 * Algebraic structures, morphisms, and utilities over the [RealBicomplex] numbers
 * `𝔹 = ℂ(i)[j] ≅ ℂ ⊗ℝ ℂ`.
 *
 * The three imaginary/hyperbolic units satisfy `i² = -1`, `j² = -1` (both imaginary) and
 * `k = ij` with `k² = +1` (hyperbolic). `𝔹` is a commutative ring — not a field — with zero
 * divisors on the null cone.
 *
 * Construction:
 * `𝔹` is the Cayley-Dickson doubling of `ℂ` with the *trivial* involution at `σ = 1`:
 * ```text
 * (z1 + z2 j)(w1 + w2 j) = (z1w1 - z2w2) + (z1w2 + z2w1) j
 * ```
 * obtained from [CayleyDickson.commutative] and transported to the [RealBicomplex] carrier. Nothing
 * in this file re-implements that multiplication; everything else is expressed through it.
 *
 * Ring and its units:
 * - [BicomplexCommutativeRing]: the commutative ring `𝔹`. It also exposes [HasReciprocal]
 *   (the units are exactly the non-zero-divisors) and [HasNormSq] (the Euclidean `normSq`).
 * - [BicomplexCommutativeRing.i], [BicomplexCommutativeRing.j], [BicomplexCommutativeRing.k]:
 *   the imaginary units `i`, `j` and the hyperbolic unit `k = ij`.
 *
 * Conjugations (involutions):
 * - [BicomplexCommutativeRing.conj1]: conjugates `i` only — `a + bi + cj + dk ↦ a - bi + cj - dk`.
 * - [BicomplexCommutativeRing.conj2]: conjugates `j` only — `↦ a + bi - cj - dk`. This is the
 *   Cayley-Dickson conjugation of the doubling.
 * - [BicomplexCommutativeRing.conj3]: the principal involution, conjugating both — `↦ a - bi - cj + dk`.
 *
 * Norm forms `N_t(w) = w · w^(†t)` and the scalar norm:
 * Each conjugation norm lands in the subring fixed by its involution, and is typed by that subring:
 * - [BicomplexCommutativeRing.normByConj1] → [RealBicomplex] confined to the ordinary complex line
 *   `ℂ(j)` (`j² = -1`); not narrowed to [RealComplex], since `ℂ(j) ≠` the canonical `ℂ(i)`.
 * - [BicomplexCommutativeRing.normByConj2] → [RealComplex]: the canonical base field `ℂ(i)`.
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
 *   [RealComplex] via [cjToComplex].
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
 * - [complexToBicomplexMonomorphism]: the Cayley-Dickson embedding `ℂ ↪ 𝔹`, `z ↦ z + 0j`
 *   (`(z, z)` in the idempotent basis).
 * - [BicomplexFirstProjectionHomomorphism], [BicomplexSecondProjectionHomomorphism]: the idempotent
 *   projections `𝔹 → ℂ` (`π_𝛂`, `π_𝜷`), which — unlike standard-basis projections — preserve ring
 *   multiplication.
 * - [BicomplexToComplexMatrixMonomorphism]: the regular representation `𝔹 ↪ M₂(ℂ)` in the
 *   standard basis `{1, j}`.
 * - [BicomplexToDiagonalMatrixMonomorphism]: the embedding `𝔹 ↪ M₂(ℂ)` in the orthogonal-idempotent
 *   (diagonal) basis.
 *
 * Eqs:
 * - [eqBicomplexStrict]: exact componentwise equality.
 * - [eqBicomplex]: approximate componentwise equality.
 *
 * Printables:
 * - [printableBicomplex]: standard rendering in the `1, i, j, k` basis.
 * - [printableBicomplexStrict]: strict rendering (exact reals).
 * - [printableBicomplexPretty]: pretty rendering.
 */
object BicomplexAlgebras {

    // ── Generic, over a commutative ring ─────────────────────────────────────

    /**
     * The bicomplex ring over a commutative base ring: commutative and associative, with
     * `i² = j² = -1` and `k = ij`, `k² = +1`. Its involution is [conj2], the Cayley-Dickson
     * conjugation `z1 + z2·j ↦ z1 - z2·j`.
     */
    fun <R : Any> bicomplexRing(
        base: CommutativeRing<R>
    ): CommutativeInvolutiveRing<Bicomplex<R>> =
        bicomplexRingOver(base)

    /**
     * The standard basis `1, i, j, k` over [base].
     */
    fun <R : Any> basis(
        base: CommutativeRing<R>
    ): List<Bicomplex<R>> {
        val (o, l) = base.zero to base.one
        return listOf(
            Bicomplex.ofStandard(l, o, o, o),
            Bicomplex.ofStandard(o, l, o, o),
            Bicomplex.ofStandard(o, o, l, o),
            Bicomplex.ofStandard(o, o, o, l)
        )
    }

    /**
     * The first conjugation over [base]: conjugates `i` only, `z1 + z2·j ↦ conj(z1) + conj(z2)·j`.
     */
    fun <R : Any> conj1(
        base: CommutativeRing<R>
    ): Endo<Bicomplex<R>> {
        val conj = complexRingOver(base).conj
        return Endo("${Symbols.DAGGER}1") { z -> Bicomplex.ofStandard(conj(z.z1), conj(z.z2)) }
    }

    /**
     * The second conjugation over [base]: conjugates `j` only, `z1 + z2·j ↦ z1 - z2·j`. This is the
     * Cayley-Dickson conjugation of the doubling.
     */
    fun <R : Any> conj2(
        base: CommutativeRing<R>
    ): Endo<Bicomplex<R>> =
        bicomplexRingOver(base).conj

    /**
     * The third (principal) conjugation over [base]: conjugates both `i` and `j`.
     */
    fun <R : Any> conj3(
        base: CommutativeRing<R>
    ): Endo<Bicomplex<R>> {
        val first = conj1(base)
        val second = conj2(base)
        return Endo("${Symbols.DAGGER}3") { z -> first(second(z)) }
    }

    /**
     * The norm for the Cayley-Dickson conjugation, `N2(z) = z·conj2(z) = z1² + z2² ∈ Complex(R)`.
     * It is multiplicative.
     */
    fun <R : Any> normByConj2(
        base: CommutativeRing<R>
    ): UnaryOp<Bicomplex<R>, Complex<R>> {
        val c = complexRingOver(base)
        return UnaryOp("normByConj2") { z -> c.add(c.mul(z.z1, z.z1), c.mul(z.z2, z.z2)) }
    }

    /**
     * The first idempotent projection `z1 + z2·j ↦ 𝛂 = z1 + i·z2`, a ring homomorphism onto
     * `Complex(R)` over any base.
     */
    fun <R : Any> firstProjection(
        base: CommutativeRing<R>
    ): RingHomomorphism<Bicomplex<R>, Complex<R>> {
        val c = complexRingOver(base)
        val i = complex(base.zero, base.one)
        return RingHomomorphism.of(
            domain = bicomplexRingOver(base),
            codomain = c,
            map = UnaryOp("${Symbols.PROJECTION}_𝛂") { z -> c.add(z.z1, c.mul(i, z.z2)) }
        )
    }

    /**
     * The second idempotent projection `z1 + z2·j ↦ 𝜷 = z1 - i·z2`, a ring homomorphism onto
     * `Complex(R)` over any base.
     */
    fun <R : Any> secondProjection(
        base: CommutativeRing<R>
    ): RingHomomorphism<Bicomplex<R>, Complex<R>> {
        val c = complexRingOver(base)
        val i = complex(base.zero, base.one)
        return RingHomomorphism.of(
            domain = bicomplexRingOver(base),
            codomain = c,
            map = UnaryOp("${Symbols.PROJECTION}_𝜷") { z -> c.add(z.z1, c.add.inverse(c.mul(i, z.z2))) }
        )
    }

    /**
     * The Cayley-Dickson embedding `Complex(R) ↪ Bicomplex(R)`, `z ↦ z + 0j`.
     */
    fun <R : Any> complexToBicomplexMonomorphism(
        base: CommutativeRing<R>
    ): RingMonomorphism<Complex<R>, Bicomplex<R>> {
        val c = complexRingOver(base)
        return RingMonomorphism.of(
            domain = c,
            codomain = bicomplexRingOver(base),
            map = UnaryOp { z -> Bicomplex.ofStandard(z, c.zero) }
        )
    }

    /**
     * The action of `Complex(R)` through that embedding: `c ⊳ (z1 + z2·j) = c·z1 + (c·z2)·j`.
     */
    fun <R : Any> complexLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<Complex<R>, Bicomplex<R>> {
        val c = complexRingOver(base)
        return LeftAction(Symbols.TRIANGLE_RIGHT) { s, z -> Bicomplex.ofStandard(c.mul(s, z.z1), c.mul(s, z.z2)) }
    }

    /**
     * The coordinatewise action of the base ring.
     */
    fun <R : Any> baseLeftAction(
        base: CommutativeRing<R>
    ): LeftAction<R, Bicomplex<R>> =
        LeftAction(Symbols.TRIANGLE_RIGHT) { r, z ->
            Bicomplex.ofStandard(base.mul(r, z.a), base.mul(r, z.b), base.mul(r, z.c), base.mul(r, z.d))
        }

    /**
     * `Bicomplex<R>` as a free `R`-module of rank 4.
     */
    fun <R : Any> bicomplexModule(
        base: CommutativeRing<R>
    ): RModule<R, Bicomplex<R>> =
        RModule.of(
            scalars = base,
            add = bicomplexRingOver(base).add,
            leftAction = baseLeftAction(base)
        )

    /**
     * `Bicomplex<R>` as an `R`-star-algebra under the Cayley-Dickson conjugation [conj2].
     */
    fun <R : Any> bicomplexStarAlgebra(
        base: CommutativeRing<R>
    ): StarAlgebra<R, Bicomplex<R>> =
        StarAlgebra.of(
            scalars = base,
            involutiveRing = bicomplexRingOver(base),
            leftAction = baseLeftAction(base)
        )

    /**
     * Coordinatewise equality on `Bicomplex<R>`.
     */
    fun <R : Any> eqBicomplex(eqR: Eq<R>): Eq<Bicomplex<R>> =
        CD.eq(ComplexAlgebras.eqComplex(eqR)).contramap { z: Bicomplex<R> -> z.cd }

    // ── The real bicomplex numbers 𝔹 ─────────────────────────────────────────

    object BicomplexCommutativeRing :
        CommutativeRing<RealBicomplex> by cayleyDicksonBicomplex,
        HasReciprocal<RealBicomplex>,
        HasNormSq<RealBicomplex, Real> {

        private val complexField = ComplexAlgebras.ComplexField
        private val complexStar = ComplexAlgebras.ComplexStarAlgebra

        override val zero: RealBicomplex = Bicomplex.ZERO

        // Imaginary unit from the first complex plane
        val i: RealBicomplex = Bicomplex.ofStandard(0.0, 1.0, 0.0, 0.0)

        // The bicomplex unit
        val j: RealBicomplex = Bicomplex.ofStandard(0.0, 0.0, 1.0, 0.0)

        // The product of the two units (k = ij)
        val k: RealBicomplex = Bicomplex.ofStandard(0.0, 0.0, 0.0, 1.0)

        /**
         * First conjugation on bicomplex numbers.
         *
         * Conjugates only the complex unit `i`, leaving `j` unchanged.
         *
         * In the standard basis:
         * ```text
         * z1 + z2 j ↦ z1* + z2* j,   i.e.   a + bi + cj + d(ij) ↦ a - bi + cj - d(ij)
         * ```
         * In the idempotent basis:
         * ```text
         * (𝛂, 𝜷) ↦ (𝜷*, 𝛂*)
         * ```
         */
        val conj1: Endo<RealBicomplex> = Endo("${Symbols.DAGGER}1") { z ->
            Bicomplex.ofStandard(complexStar.conj(z.z1), complexStar.conj(z.z2))
        }

        /**
         * Second conjugation on bicomplex numbers.
         *
         * Conjugates only the bicomplex unit `j`, leaving `i` unchanged. This is precisely the
         * Cayley-Dickson conjugation `(z1, z2) ↦ (z1*, -z2)` of the doubling, since the base
         * involution on `ℂ` is trivial.
         *
         * In the standard basis:
         * ```text
         * z1 + z2 j ↦ z1 - z2 j,   i.e.   a + bi + cj + d(ij) ↦ a + bi - cj - d(ij)
         * ```
         * In the idempotent basis:
         * ```text
         * (𝛂, 𝜷) ↦ (𝜷, 𝛂)
         * ```
         */
        val conj2: Endo<RealBicomplex> = Endo("${Symbols.DAGGER}2") { z ->
            cayleyDicksonBicomplex.conj(z)
        }

        /**
         * Third conjugation on bicomplex numbers: the principal involution `conj3 = conj1 ∘ conj2`.
         *
         * Conjugates both `i` and `j`.
         *
         * In the standard basis:
         * ```text
         * z1 + z2 j ↦ z1* - z2* j,   i.e.   a + bi + cj + d(ij) ↦ a - bi - cj + d(ij)
         * ```
         * In the idempotent basis:
         * ```text
         * (𝛂, 𝜷) ↦ (𝛂*, 𝜷*)
         * ```
         */
        val conj3: Endo<RealBicomplex> = Endo("${Symbols.DAGGER}3") { z ->
            conj1(conj2(z))
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
         * The result is returned as a [RealBicomplex]: it lives inside the embedded subring
         * `ℂ(j) ⊂ 𝔹`, *not* inside your canonical [RealComplex] (which is `ℂ(i)`). Unlike [normByConj2],
         * whose two idempotent projections coincide and so collapse to an unambiguous [RealComplex],
         * the projections of `N1` are complex conjugates of one another (`𝛂𝜷*` and `𝜷𝛂*`), so
         * narrowing to [RealComplex] would require arbitrarily choosing the identification `j ↦ i`
         * or `j ↦ -i`.
         */
        val normByConj1: UnaryOp<RealBicomplex, RealBicomplex> = UnaryOp("normByConj1") { z ->
            mul(z, conj1(z))
        }

        /**
         * Norm with respect to the second (Cayley-Dickson) conjugation.
         *
         * Annihilates the `j` and `k` terms, mapping the result into the canonical complex plane
         * `ℂ(i)` spanned by `{1, i}` — the base field that `𝔹 = ℂ(i)[j]` is built over.
         *
         * ```text
         * N2(w) = w * w†2 = z1² + z2² = (a² - b² + c² - d²) + 2(ab + cd)i
         * ```
         * Idempotent logic:
         * ```text
         * (𝛂, 𝜷)(𝜷, 𝛂) = (𝛂𝜷, 𝛂𝜷)
         * ```
         * Because the `j`-component of the product vanishes, the value is unambiguously a canonical
         * [RealComplex] number — there is no choice of imaginary unit to make — so we return [RealComplex]
         * directly rather than a [RealBicomplex] confined to `ℂ(i)`.
         */
        val normByConj2: UnaryOp<RealBicomplex, RealComplex> = UnaryOp("normByConj2") { z ->
            mul(z, conj2(z)).z1
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
         *
         * The real part `re` is the squared Euclidean norm; see [normSq].
         */
        val normByConj3: UnaryOp<RealBicomplex, SplitComplex<Real>> = UnaryOp("normByConj3") { z ->
            val product = mul(z, conj3(z))
            // product = re + hy·k, and k = (0 + 0i) + (0 + 1i)j, so hy is the imaginary part of z2.
            splitComplex(
                re = product.a,
                hy = product.d
            )
        }

        /**
         * Calculates the squared 4D Euclidean norm of the bicomplex number.
         * * ||w||² = a² + b² + c² + d²
         */
        override val normSq: UnaryOp<RealBicomplex, Real> = UnaryOp(Symbols.NORM_SQ_SYMBOL) {
            normByConj3(it).re
        }

        /**
         * Calculates the 4D Euclidean norm of the bicomplex number.
         */
        val norm: UnaryOp<RealBicomplex, Real> = UnaryOp("norm") {
            sqrt(normSq(it))
        }

        /**
         * Determine if [a] has a *numerically safe* reciprocal.
         *
         * Algebraically, `w` is invertible iff it is off the null cone ([Bicomplex.isUnit]), i.e. iff
         * `N2(w) = z1² + z2² = 𝛂𝜷 ≠ 0`. Following the floating-point policy of [HasReciprocal], the
         * predicate used for inversion is that `N2(w)` is invertible in [ComplexAlgebras.ComplexField]
         * (finite and not approximately zero), which is exactly what [reciprocal] needs. It can differ
         * from [Bicomplex.isUnit] only on elements within rounding distance of the null cone.
         */
        override fun hasReciprocal(a: RealBicomplex): Boolean =
            complexField.hasReciprocal(normByConj2(a))

        /**
         * The reciprocal via the Cayley-Dickson conjugation, exactly as for `ℂ`, `ℍ` and `𝕆`,
         * except that the "norm" `N2(w) = w · w†2` is complex rather than real:
         * ```text
         * w⁻¹ = w†2 · N2(w)⁻¹
         * ```
         * `N2(w) = 𝛂𝜷` is invertible iff `w` is off the null cone.
         */
        override val reciprocal: Endo<RealBicomplex> = Endo(Symbols.INVERSE) { z ->
            if (!hasReciprocal(z)) throw HasReciprocal.notInvertible(z, "the bicomplex numbers")
            val scale = complexField.reciprocal(normByConj2(z))
            BicomplexComplexVectorSpace.leftAction(scale, conj2(z))
        }
    }

    /**
     * The Cayley-Dickson embedding of ℂ into the RealBicomplex ring, `z ↦ z + 0j`.
     *
     * In the idempotent representation, maps:
     * ```text
     * z -> (z, z)
     * ```
     */
    val complexToBicomplexMonomorphism: RingMonomorphism<RealComplex, RealBicomplex> = RingMonomorphism.of(
        domain = ComplexAlgebras.ComplexField,
        codomain = BicomplexCommutativeRing,
        map = UnaryOp { z -> Bicomplex.ofStandard(z, ComplexAlgebras.ComplexField.zero) }
    )

    /**
     * Embedding of ℂ into the `ℂ(j)` line of `𝔹`: the ring monomorphism `a + bi ↦ a + bj`.
     *
     * This is the `ℂ(j)` counterpart of [complexToBicomplexMonomorphism] (which embeds ℂ into the
     * canonical `ℂ(i)`). It is a genuine ring homomorphism — `j² = -1` exactly like `i²` — whose
     * image is the subring `ℂ(j) ⊂ 𝔹`, and [cjToComplex] is its left inverse
     * (`cjToComplex ∘ complexToBicomplexAlongJ = id_ℂ`).
     */
    val complexToBicomplexAlongJ: RingMonomorphism<RealComplex, RealBicomplex> = RingMonomorphism.of(
        domain = ComplexAlgebras.ComplexField,
        codomain = BicomplexCommutativeRing,
        map = UnaryOp { c -> Bicomplex.ofStandard(c.re, 0.0, c.im, 0.0) }   // (re + im·i) ↦ (re + im·j)
    )

    /**
     * Retraction of the `ℂ(j)` line onto the canonical [RealComplex] field via the ring isomorphism
     * `ℂ(j) ≅ ℂ(i)`, `j ↦ i`:  `a + bj ↦ a + bi`.
     *
     * Equivalently, the first idempotent projection `π_𝛂` restricted to `ℂ(j)`. It is an
     * isomorphism ONLY on that line: off it (nonzero `i` or `k` parts) those parts are dropped, so
     * pass only `ℂ(j)`-valued elements — e.g. the result of [BicomplexCommutativeRing.normByConj1].
     * It is the left inverse of [complexToBicomplexAlongJ].
     */
    fun cjToComplex(z: RealBicomplex): RealComplex =
        complex(z.a, z.c)   // (1-coefficient, j-coefficient) ↦ (re, im)

    /**
     * [BicomplexCommutativeRing.normByConj1] delivered as a canonical [RealComplex] in `ℂ(i)`, i.e.
     * `cjToComplex ∘ normByConj1`.
     *
     * Returns `𝛂·conj(𝜷) = (a² + b² - c² - d²) + 2(ac + bd)i`. Use this when you want the
     * first-conjugation norm as an ordinary complex number and are willing to commit to the
     * (non-canonical) `j ↦ i` identification; otherwise prefer
     * [BicomplexCommutativeRing.normByConj1], which keeps the value honestly inside `ℂ(j) ⊂ 𝔹`.
     */
    val normByConj1ToCanonical: UnaryOp<RealBicomplex, RealComplex> =
        UnaryOp("normByConj1ToCanonical") { w ->
            cjToComplex(BicomplexCommutativeRing.normByConj1(w))
        }

    /**
     * Homomorphic projection of a bicomplex number onto the complex plane via the first idempotent element (𝛂).
     * Unlike standard basis projections, idempotent projections preserve ring multiplication.
     */
    object BicomplexFirstProjectionHomomorphism: RingHomomorphism<RealBicomplex, RealComplex> {
        override val domain = BicomplexCommutativeRing
        override val codomain = ComplexAlgebras.ComplexField
        override val map = UnaryOp<RealBicomplex, RealComplex>("${Symbols.PROJECTION}_𝛂") { it.alpha }
    }

    /**
     * Homomorphic projection of a bicomplex number onto the complex plane via the second idempotent element (𝜷).
     * Unlike standard basis projections, idempotent projections preserve ring multiplication.
     */
    object BicomplexSecondProjectionHomomorphism: RingHomomorphism<RealBicomplex, RealComplex> {
        override val domain = BicomplexCommutativeRing
        override val codomain = ComplexAlgebras.ComplexField
        override val map = UnaryOp<RealBicomplex, RealComplex>("${Symbols.PROJECTION}_𝜷") { it.beta }
    }

    /**
     * The regular representation of `𝔹` over `ℂ` in the standard basis `{1, j}` with `j² = -1`:
     * ```text
     * z1 + z2 j ↦ [[z1, -z2], [z2, z1]]
     * ```
     * This is the same [quadraticRank2MatrixEmbedding] that gives `ℂ ↪ M₂(ℝ)`, one level up.
     */
    val BicomplexToComplexMatrixMonomorphism: RingMonomorphism<RealBicomplex, DenseMat<RealComplex>> =
        quadraticRank2MatrixEmbedding(
            domain = BicomplexCommutativeRing,
            coefficientRing = ComplexAlgebras.ComplexField,
            s = ComplexAlgebras.ComplexField.add.inverse(ComplexAlgebras.ComplexField.one),
            t = ComplexAlgebras.ComplexField.zero,
            coeffs = UnaryOp { z -> z.standard() }
        )

    /**
     * A monomorphism from 𝔹 to M_2(ℂ) using the orthogonal idempotent basis.
     * Maps (𝛂, 𝜷) ↦ [[𝛂, 0], [0, 𝜷]]
     */
    object BicomplexToDiagonalMatrixMonomorphism : RingMonomorphism<RealBicomplex, DenseMat<RealComplex>> {
        private val complexField = ComplexAlgebras.ComplexField
        override val domain = BicomplexCommutativeRing
        override val codomain = DenseMatAlgebras.DenseMatRing(complexField, 2)
        override val map: UnaryOp<RealBicomplex, DenseMat<RealComplex>> = UnaryOp("M_2(ℂ)_diagonal") { b ->
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
    val BicomplexRealVectorSpace: FiniteVectorSpace<Real, RealBicomplex> = FiniteVectorSpace.of(
        scalars = RealAlgebras.RealField,
        add = BicomplexCommutativeRing.add,
        dimension = 4,
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { r, z ->
            val scale = ComplexAlgebras.ComplexRealVectorSpace.leftAction
            Bicomplex.ofStandard(scale(r, z.z1), scale(r, z.z2))
        }
    )

    /**
     * The bicomplex numbers are a 2-dimensional vector space over the complex numbers `ℂ(i)`,
     * acting through the Cayley-Dickson embedding: `c ⊳ (z1 + z2 j) = cz1 + cz2 j`.
     */
    val BicomplexComplexVectorSpace: FiniteVectorSpace<RealComplex, RealBicomplex> = FiniteVectorSpace.of(
        scalars = ComplexAlgebras.ComplexField,
        add = BicomplexCommutativeRing.add,
        dimension = 2,
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { c, z ->
            val field = ComplexAlgebras.ComplexField
            Bicomplex.ofStandard(field.mul(c, z.z1), field.mul(c, z.z2))
        }
    )

    /**
     * The real star algebra structure on `𝔹` for a chosen involution.
     */
    private fun bicomplexStarAlgebra(
        conj: Endo<RealBicomplex>
    ): StarAlgebra<Real, RealBicomplex> =
        StarAlgebra.of(
            scalars = RealAlgebras.RealField,
            involutiveRing = InvolutiveRing.of(BicomplexCommutativeRing, conj),
            leftAction = BicomplexRealVectorSpace.leftAction
        )

    /**
     * The star algebra associated with the first conjugation (conjugating only the 'i' unit).
     */
    val BicomplexStarAlgebra1: StarAlgebra<Real, RealBicomplex> =
        bicomplexStarAlgebra(BicomplexCommutativeRing.conj1)

    /**
     * The star algebra associated with the second conjugation (conjugating only the 'j' unit).
     * This is the involution supplied by the Cayley-Dickson construction.
     */
    val BicomplexStarAlgebra2: StarAlgebra<Real, RealBicomplex> =
        bicomplexStarAlgebra(BicomplexCommutativeRing.conj2)

    /**
     * The principal star algebra associated with the third conjugation (conjugating both 'i' and 'j').
     * This is the standard involution used when treating RealBicomplex as a RealNormedAlgebra.
     */
    val BicomplexStarAlgebraPrincipal: StarAlgebra<Real, RealBicomplex> =
        bicomplexStarAlgebra(BicomplexCommutativeRing.conj3)

    val eqBicomplexStrict: Eq<RealBicomplex> =
        eqBicomplex(RealAlgebras.eqRealStrict)

    val eqBicomplex: Eq<RealBicomplex> =
        eqBicomplex(RealAlgebras.eqRealApprox)

    private fun printableBicomplexGenerator(
        prReal: Printable<Real>,
        eqReal: Eq<Real>
    ): Printable<RealBicomplex> =
        basisPrintable(
            labels = listOf("", Symbols.IMAGINARY_I, Symbols.IMAGINARY_J, Symbols.IMAGINARY_K),
            decompose = { it.coefficients() },
            signed = RealAlgebras.SignedReal,
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            prA = prReal,
            eqA = eqReal
        )

    val printableBicomplex: Printable<RealBicomplex> = printableBicomplexGenerator(
        prReal = RealAlgebras.printableReal,
        eqReal = RealAlgebras.eqRealApprox
    )

    val printableBicomplexStrict: Printable<RealBicomplex> = printableBicomplexGenerator(
        prReal = RealAlgebras.printableRealStrict,
        eqReal = RealAlgebras.eqRealStrict
    )

    val printableBicomplexPretty: Printable<RealBicomplex> = printableBicomplexGenerator(
        prReal = RealAlgebras.printableRealPretty,
        eqReal = RealAlgebras.eqRealApprox
    )
}
