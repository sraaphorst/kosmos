package org.vorpal.kosmos.hypercomplex.biquaternion

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.morphisms.RingIsomorphism
import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.multi.Bicomplex
import org.vorpal.kosmos.hypercomplex.multi.BicomplexAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.RealQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionTier
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternion
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternionAlgebras
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat

/**
 * `ℍ ⊗ ℂ = CD⟨ℂ[i]/(i² + 1)⟩`: the quaternion tier with complex coefficients.
 *
 * File-level so that initializing [BiquaternionAlgebras.BiquaternionRing] never recursively
 * initializes [BiquaternionAlgebras].
 */
internal val biquaternionTier: QuaternionTier<Complex, Biquaternion> =
    QuaternionTier(
        scalars = ComplexAlgebras.ComplexField,
        wrap = { cd -> Biquaternion(cd) },
        unwrap = { q -> q.cd }
    )

private val cayleyDicksonBiquaternion: InvolutiveRing<Biquaternion> =
    biquaternionTier.ring

/**
 * The coefficientwise complex scalar action `c ⊳ (w + xi + yj + zk) = cw + cx·i + cy·j + cz·k`.
 */
private val complexScaling: LeftAction<Complex, Biquaternion> =
    biquaternionTier.scalarAction

/**
 * Apply [f] to each of the four complex coefficients.
 */
private fun mapCoefficients(q: Biquaternion, f: (Complex) -> Complex): Biquaternion =
    biquaternion(f(q.w), f(q.x), f(q.y), f(q.z))

/**
 * Algebraic structures, morphisms and utilities over the [Biquaternion]s `ℍ ⊗_ℝ ℂ ≅ M₂(ℂ)`.
 *
 * Throughout, `i, j, k` are the quaternion units and `h` is the imaginary unit of the coefficient
 * field ℂ, which is central.
 *
 * Construction:
 * the Hamilton quaternion algebra over ℂ, i.e. [org.vorpal.kosmos.algebra.structures.CayleyDickson.associative]
 * applied to `ℂ[i]/(i² + 1)`, transported to the [Biquaternion] carrier. Nothing in this file
 * re-implements a product.
 *
 * Ring and its units:
 * - [BiquaternionRing]: the associative, noncommutative ring. It exposes [HasNormSq] (the
 *   complex-valued norm) and [HasReciprocal] (the units are the elements of nonzero norm).
 * - [BiquaternionRing.i], [BiquaternionRing.j], [BiquaternionRing.k], [BiquaternionRing.h].
 *
 * The three conjugations:
 * - [BiquaternionRing.conj]: the *quaternion* conjugate `w - xi - yj - zk`. ℂ-linear; an
 *   anti-automorphism. This is the Cayley-Dickson conjugation and the one behind the norm.
 * - [BiquaternionRing.complexConj]: the *complex* conjugate, conjugating each coefficient.
 *   ℂ-antilinear; an automorphism (it does not reverse products).
 * - [BiquaternionRing.hermitianConj]: the *Hermitian* conjugate, the composite of the two.
 *   ℂ-antilinear; an anti-automorphism. Under `ℍ ⊗ ℂ ≅ M₂(ℂ)` it is the conjugate transpose.
 *
 * Vector spaces and star algebras:
 * - [BiquaternionComplexVectorSpace] (dimension 4), [BiquaternionRealVectorSpace] (dimension 8).
 * - [BiquaternionStarAlgebra]: the ℂ-star-algebra for the quaternion conjugate.
 * - [BiquaternionHermitianStarAlgebra]: the ℝ-star-algebra for the Hermitian conjugate.
 *
 * Homomorphisms and embeddings:
 * - [ComplexToBiquaternionMonomorphism]: `ℂ ↪ ℍ ⊗ ℂ` as the center.
 * - [QuaternionToBiquaternionMonomorphism]: `ℍ ↪ ℍ ⊗ ℂ`, the real form with real coefficients.
 * - [SplitQuaternionToBiquaternionMonomorphism]: `ℍ' ↪ ℍ ⊗ ℂ`, the other real form.
 * - [BicomplexToBiquaternionMonomorphism]: the Cayley-Dickson embedding `𝔹 ↪ ℍ ⊗ ℂ`.
 * - [BiquaternionToComplexMatrixIsomorphism]: `ℍ ⊗ ℂ ≅ M₂(ℂ)`.
 */
object BiquaternionAlgebras {

    object BiquaternionRing :
        InvolutiveRing<Biquaternion> by cayleyDicksonBiquaternion,
        HasNormSq<Biquaternion, Complex>,
        HasReciprocal<Biquaternion> {

        private val complexField = ComplexAlgebras.ComplexField
        private val complexStar = ComplexAlgebras.ComplexStarAlgebra

        override val zero: Biquaternion = cayleyDicksonBiquaternion.zero

        /**
         * The standard ℂ-basis `1, i, j, k`.
         */
        val basis: List<Biquaternion> =
            biquaternionTier.basis

        /** The quaternion unit `i`. */
        val i: Biquaternion = basis[1]

        /** The quaternion unit `j`. */
        val j: Biquaternion = basis[2]

        /** The quaternion unit `k = ij`. */
        val k: Biquaternion = basis[3]

        /** The central imaginary scalar `h` (the `i` of the coefficient field ℂ), with `h² = -1`. */
        val h: Biquaternion =
            biquaternion(ComplexAlgebras.i, complexField.zero, complexField.zero, complexField.zero)

        /**
         * The complex conjugate: conjugates each coefficient, `h ↦ -h`, fixing `i, j, k`.
         *
         * It is a ring *automorphism* (it preserves the order of products) and is ℂ-antilinear. Its
         * fixed ring is the real quaternions ℍ.
         */
        val complexConj: Endo<Biquaternion> = Endo("${Symbols.CONJ}ℂ") { q ->
            mapCoefficients(q, complexStar.conj::invoke)
        }

        /**
         * The Hermitian conjugate `q ↦ complexConj(conj(q))`: an involutive anti-automorphism that is
         * ℂ-antilinear. Under [BiquaternionToComplexMatrixIsomorphism] it is the conjugate transpose.
         */
        val hermitianConj: Endo<Biquaternion> = Endo(Symbols.DAGGER) { q ->
            complexConj(conj(q))
        }

        /**
         * The complex-valued norm
         * ```text
         * N(q) = q·conj(q) = w² + x² + y² + z² ∈ ℂ,
         * ```
         * with no conjugation of the coefficients. It is multiplicative, and under
         * [BiquaternionToComplexMatrixIsomorphism] it is the determinant.
         */
        override val normSq: UnaryOp<Biquaternion, Complex> =
            biquaternionTier.normSq

        /**
         * `q` is invertible iff `N(q)` is invertible in ℂ. Following the floating-point policy of
         * [HasReciprocal], that means `N(q)` is finite and not approximately zero.
         */
        override fun hasReciprocal(a: Biquaternion): Boolean =
            complexField.hasReciprocal(normSq(a))

        /**
         * The reciprocal `q⁻¹ = N(q)⁻¹ ⊳ conj(q)`, exactly as for ℍ except that the norm is complex.
         */
        override val reciprocal: Endo<Biquaternion> = Endo(Symbols.INVERSE) { q ->
            if (!hasReciprocal(q)) throw HasReciprocal.notInvertible(q, "the biquaternions")
            complexScaling(complexField.reciprocal(normSq(q)), conj(q))
        }
    }

    /**
     * The biquaternions as a four-dimensional vector space over ℂ.
     */
    val BiquaternionComplexVectorSpace: FiniteVectorSpace<Complex, Biquaternion> = FiniteVectorSpace.of(
        scalars = ComplexAlgebras.ComplexField,
        add = BiquaternionRing.add,
        dimension = 4,
        leftAction = complexScaling
    )

    /**
     * The biquaternions as an eight-dimensional vector space over ℝ.
     */
    val BiquaternionRealVectorSpace: FiniteVectorSpace<Real, Biquaternion> = FiniteVectorSpace.of(
        scalars = RealAlgebras.RealField,
        add = BiquaternionRing.add,
        dimension = 8,
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { r, q -> complexScaling(complex(r, 0.0), q) }
    )

    /**
     * The ℂ-star-algebra of biquaternions under the quaternion conjugate, which is ℂ-linear.
     */
    val BiquaternionStarAlgebra: StarAlgebra<Complex, Biquaternion> = StarAlgebra.of(
        scalars = ComplexAlgebras.ComplexField,
        involutiveRing = BiquaternionRing,
        leftAction = complexScaling
    )

    /**
     * The ℝ-star-algebra of biquaternions under the Hermitian conjugate. The Hermitian conjugate is
     * only ℝ-linear, so this is a star algebra over ℝ, not over ℂ.
     */
    val BiquaternionHermitianStarAlgebra: StarAlgebra<Real, Biquaternion> = StarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = InvolutiveRing.of(BiquaternionRing, BiquaternionRing.hermitianConj),
        leftAction = BiquaternionRealVectorSpace.leftAction
    )

    /**
     * The embedding of ℂ as the scalars `c ↦ c·1`, which is the center of the biquaternions.
     */
    val ComplexToBiquaternionMonomorphism: RingMonomorphism<Complex, Biquaternion> = RingMonomorphism.of(
        domain = ComplexAlgebras.ComplexField,
        codomain = BiquaternionRing,
        map = biquaternionTier.scalarEmbeddingMap
    )

    /**
     * The embedding `ℍ ↪ ℍ ⊗ ℂ` of the real quaternions as the biquaternions with real
     * coefficients: the fixed ring of [BiquaternionRing.complexConj].
     */
    val QuaternionToBiquaternionMonomorphism: RingMonomorphism<RealQuaternion, Biquaternion> = RingMonomorphism.of(
        domain = QuaternionAlgebras.QuaternionDivisionRing,
        codomain = BiquaternionRing,
        map = UnaryOp { q ->
            biquaternionTier.fromCoordinates(q.coefficients().map { r -> complex(r, 0.0) })
        }
    )

    /**
     * The embedding `ℍ' ↪ ℍ ⊗ ℂ` of the real split-quaternions:
     * ```text
     * i ↦ i,   j ↦ h·j,   k ↦ h·k,
     * ```
     * which works because `(hj)² = h²j² = +1`. The quaternions and the split-quaternions are the two
     * real forms of the biquaternions: `ℍ ⊗ ℂ ≅ ℍ' ⊗ ℂ ≅ M₂(ℂ)`.
     */
    val SplitQuaternionToBiquaternionMonomorphism: RingMonomorphism<SplitQuaternion<Real>, Biquaternion> =
        RingMonomorphism.of(
            domain = SplitQuaternionAlgebras.RealSplitQuaternionRing,
            codomain = BiquaternionRing,
            map = UnaryOp { (w, x, y, z) ->
                biquaternion(complex(w, 0.0), complex(x, 0.0), complex(0.0, y), complex(0.0, z))
            }
        )

    /**
     * The Cayley-Dickson embedding `𝔹 ↪ ℍ ⊗ ℂ`, `z1 + z2·j_𝔹 ↦ z1 + z2·i`.
     *
     * The biquaternions are the doubling of `ℂ[i]/(i² + 1)`, which is exactly the bicomplex ring, so
     * this is the first-slot injection: the bicomplex unit `j_𝔹` becomes the quaternion unit `i`, and
     * the bicomplex unit `i_𝔹` becomes the central scalar `h`.
     */
    val BicomplexToBiquaternionMonomorphism: RingMonomorphism<Bicomplex, Biquaternion> = RingMonomorphism.of(
        domain = BicomplexAlgebras.BicomplexCommutativeRing,
        codomain = BiquaternionRing,
        map = UnaryOp { b -> Biquaternion(CD(b.cd, biquaternionTier.complexRing.zero)) }
    )

    /**
     * The isomorphism `ℍ ⊗ ℂ ≅ M₂(ℂ)`: the ℂ-linear extension of the representation `ℍ ↪ M₂(ℂ)`,
     * ```text
     * i ↦ [[h, 0], [0, -h]],   j ↦ [[0, 1], [-1, 0]],   k ↦ [[0, h], [h, 0]],
     * ```
     * i.e.
     * ```text
     * w + xi + yj + zk ↦ [[w + hx, y + hz], [-y + hz, w - hx]].
     * ```
     * The norm becomes the determinant and the Hermitian conjugate becomes the conjugate transpose.
     * The inverse is
     * ```text
     * [[p, q], [r, s]] ↦ (p + s)/2 + (p - s)/(2h)·i + (q - r)/2·j + (q + r)/(2h)·k.
     * ```
     */
    val BiquaternionToComplexMatrixIsomorphism: RingIsomorphism<Biquaternion, DenseMat<Complex>> = run {
        val field = ComplexAlgebras.ComplexField
        val matrices = DenseMatAlgebras.DenseMatRing(field, 2)
        val h = ComplexAlgebras.i
        val half = complex(0.5, 0.0)
        // 1 / (2h) = -h / 2
        val halfOverH = complex(0.0, -0.5)
        fun minus(a: Complex, b: Complex): Complex = field.add(a, field.add.inverse(b))

        RingIsomorphism.of(
            forward = RingHomomorphism.of(
                domain = BiquaternionRing,
                codomain = matrices,
                map = UnaryOp { (w, x, y, z) ->
                    val hx = field.mul(h, x)
                    val hz = field.mul(h, z)
                    DenseMat.ofRows(
                        listOf(
                            listOf(field.add(w, hx), field.add(y, hz)),
                            listOf(minus(hz, y), minus(w, hx))
                        )
                    )
                }
            ),
            backward = RingHomomorphism.of(
                domain = matrices,
                codomain = BiquaternionRing,
                map = UnaryOp { m ->
                    val (p, q) = m[0, 0] to m[0, 1]
                    val (r, s) = m[1, 0] to m[1, 1]
                    biquaternion(
                        w = field.mul(half, field.add(p, s)),
                        x = field.mul(halfOverH, minus(p, s)),
                        y = field.mul(half, minus(q, r)),
                        z = field.mul(halfOverH, field.add(q, r))
                    )
                }
            )
        )
    }

    val eqBiquaternionStrict: Eq<Biquaternion> =
        biquaternionTier.eq(ComplexAlgebras.eqComplexStrict)

    val eqBiquaternion: Eq<Biquaternion> =
        biquaternionTier.eq(ComplexAlgebras.eqComplex)

    private fun printableBiquaternionGenerator(
        prComplex: Printable<Complex>,
        eqComplex: Eq<Complex>
    ): Printable<Biquaternion> =
        complexCoefficientPrintable(
            labels = listOf("", Symbols.IMAGINARY_I, Symbols.IMAGINARY_J, Symbols.IMAGINARY_K),
            prComplex = prComplex,
            eqComplex = eqComplex
        ) { q: Biquaternion -> q.coefficients() }

    val printableBiquaternion: Printable<Biquaternion> =
        printableBiquaternionGenerator(ComplexAlgebras.printableComplex, ComplexAlgebras.eqComplex)

    val printableBiquaternionStrict: Printable<Biquaternion> =
        printableBiquaternionGenerator(ComplexAlgebras.printableComplexStrict, ComplexAlgebras.eqComplexStrict)

    val printableBiquaternionPretty: Printable<Biquaternion> =
        printableBiquaternionGenerator(ComplexAlgebras.printableComplexPretty, ComplexAlgebras.eqComplex)
}

/**
 * A [Printable] for an element with *complex* coefficients over a labelled basis, e.g.
 * `(1 + 2I) + (3 - I)J`: each nonzero coefficient is printed in parentheses before its basis label,
 * and the zero element prints as `0`.
 *
 * Shared by the biquaternions and the bioctonions, whose coefficients are not ordered and so cannot
 * use the signed linear-combination printer of the real, rational and integer tiers.
 */
internal fun <Z : Any> complexCoefficientPrintable(
    labels: List<String>,
    prComplex: Printable<Complex>,
    eqComplex: Eq<Complex>,
    decompose: (Z) -> List<Complex>
): Printable<Z> = Printable { z ->
    val zero = ComplexAlgebras.ComplexField.zero
    decompose(z)
        .zip(labels)
        .filterNot { (c, _) -> eqComplex(c, zero) }
        .joinToString(" + ") { (c, label) -> "(${prComplex(c)})$label" }
        .ifEmpty { "0" }
}
