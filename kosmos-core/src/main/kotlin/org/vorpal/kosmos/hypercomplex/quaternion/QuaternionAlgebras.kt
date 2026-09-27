package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.BinaryOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.complex.im
import org.vorpal.kosmos.hypercomplex.complex.re
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat

/**
 * `ℍ = CD⟨ℂ⟩`: the real quaternion tier.
 *
 * File-level so that initializing [QuaternionAlgebras.QuaternionDivisionRing] never recursively
 * initializes [QuaternionAlgebras].
 */
internal val realQuaternionTier: QuaternionTier<Real, Complex> =
    QuaternionTier(
        complexRing = ComplexAlgebras.ComplexStarAlgebra,
        scalars = RealAlgebras.RealField,
        re = UnaryOp { c -> c.re },
        im = UnaryOp { c -> c.im },
        mkComplex = BinaryOp { a, b -> complex(a, b) }
    )

/**
 * Main structures:
 * - [QuaternionDivisionRing]: the quaternion division ring.
 *
 * Vector spaces and modules:
 * - [QuaternionVectorSpace]: the four-dimensional vector space of quaternions over the real numbers.
 * - [quaternionLeftComplexVectorSpace]: a two-dimensional left ℂ-vector space using the chosen embedding.
 * - [QuaternionLeftComplexVectorSpacesAll]: a map of all complex embeddings to the vector space that they generate.
 * - [QuaternionLeftComplexVectorSpaceCanonical]: the canonical vector space of quaternions over the complex numbers.
 * - [QuaternionStarAlgebra]: the quaternion star algebra.
 *
 * We have the following homomorphisms:
 * - [complexEmbedding] (also [ComplexAlgebras.complexToQuaternionEmbedding]): the six unital embeddings ℂ ↪ ℍ.
 * - [QuaternionToComplexMatrixMonomorphism]: ℍ ↪ M₂(ℂ).
 *
 * We also have the following [Eq]s:
 * - [eqQuaternionStrict]: strict equality on quaternions.
 * - [eqQuaternion]: approximate equality on quaternions.
 */
object QuaternionAlgebras {

    /**
     * The real quaternions: an associative, noncommutative real normed division algebra.
     *
     * The ring structure is [realQuaternionTier]'s Cayley-Dickson double of ℂ. Reciprocals follow
     * the floating-point policy of [HasReciprocal]: `q` is invertible iff `N(q)` is finite and not
     * approximately zero, and then `q⁻¹ = conj(q) / N(q)`.
     */
    object QuaternionDivisionRing:
        DivisionRing<Quaternion>,
        InvolutiveRing<Quaternion> by realQuaternionTier.ring,
        RealNormedDivisionAlgebra<Quaternion> {

        override val zero: Quaternion = realQuaternionTier.ring.zero
        override val one: Quaternion = realQuaternionTier.ring.one

        override val normSq: UnaryOp<Quaternion, Real> =
            realQuaternionTier.normSq

        override fun hasReciprocal(a: Quaternion): Boolean {
            val n2 = normSq(a)
            return n2.isFinite() && RealAlgebras.eqRealApprox.neqv(n2, 0.0)
        }

        override val reciprocal: Endo<Quaternion> = Endo(Symbols.INVERSE) { q ->
            if (!hasReciprocal(q)) throw HasReciprocal.notInvertible(q, Symbols.BB_H)
            realQuaternionTier.scalarAction(1.0 / normSq(q), conj(q))
        }

        /**
         * The standard basis `1, i, j, k`, obtained by doubling the basis `1, i` of ℂ.
         */
        val basis: List<Quaternion> =
            realQuaternionTier.basis
    }

    /**
     * ℍ as a four-dimensional real vector space; the scalars act coordinatewise.
     */
    val QuaternionVectorSpace: FiniteVectorSpace<Real, Quaternion> = FiniteVectorSpace.of(
        scalars = RealAlgebras.RealField,
        add = QuaternionDivisionRing.add,
        dimension = 4,
        leftAction = realQuaternionTier.scalarAction
    )

    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical

    /**
     * The unital embedding ℂ ↪ ℍ sending `i_ℂ` to the signed axis chosen by [emb]:
     * ```text
     * a + b i_ℂ ↦ a + b u,   u ∈ {±i, ±j, ±k}
     * ```
     */
    fun complexEmbedding(
        emb: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<Complex, Quaternion> =
        RingMonomorphism.of(
            domain = ComplexAlgebras.ComplexField,
            codomain = QuaternionDivisionRing,
            map = realQuaternionTier.complexEmbeddingMap(emb)
        )

    /**
     * ℍ as a left ℂ-vector space using the chosen embedding.
     */
    fun quaternionLeftComplexVectorSpace(
        emb: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): FiniteVectorSpace<Complex, Quaternion> {
        val embed = complexEmbedding(emb)
        return FiniteVectorSpace.of(
            scalars = ComplexAlgebras.ComplexField,
            add = QuaternionDivisionRing.add,
            dimension = 2,
            leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { c, q ->
                QuaternionDivisionRing.mul(embed(c), q)
            }
        )
    }

    // A map of all complex embeddings to the vector space that they generate.
    val QuaternionLeftComplexVectorSpacesAll: Map<AxisSignEmbeddings.AxisSignEmbedding, FiniteVectorSpace<Complex, Quaternion>> =
        AxisSignEmbeddings.AxisSignEmbedding.all.associateWith(::quaternionLeftComplexVectorSpace)

    val QuaternionLeftComplexVectorSpaceCanonical: FiniteVectorSpace<Complex, Quaternion> =
        quaternionLeftComplexVectorSpace(canonicalEmbedding)

    val QuaternionStarAlgebra: StarAlgebra<Real, Quaternion> = StarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = QuaternionDivisionRing,
        leftAction = QuaternionVectorSpace.leftAction
    )

    /**
     * A monomorphism from H to M_2(C).
     */
    object QuaternionToComplexMatrixMonomorphism: RingMonomorphism<Quaternion, DenseMat<Complex>> {
        private val complexStarAlgebra = ComplexAlgebras.ComplexStarAlgebra
        override val domain = QuaternionDivisionRing
        override val codomain = DenseMatAlgebras.DenseMatRing(complexStarAlgebra, 2)
        override val map: UnaryOp<Quaternion, DenseMat<Complex>> = UnaryOp { (a, b) ->
            DenseMat.ofRows(
                listOf(
                    listOf(a, b),
                    listOf(complexStarAlgebra.add.inverse(complexStarAlgebra.conj(b)), complexStarAlgebra.conj(a))
                )
            )
        }
    }

    val eqQuaternionStrict: Eq<Quaternion> = CD.eq(ComplexAlgebras.eqComplexStrict)
    val eqQuaternion: Eq<Quaternion> = CD.eq(ComplexAlgebras.eqComplex)

    val printableQuaternion: Printable<Quaternion> =
        realQuaternionTier.printable(RealAlgebras.SignedReal, RealAlgebras.printableReal, RealAlgebras.eqRealApprox)

    val printableQuaternionStrict: Printable<Quaternion> =
        realQuaternionTier.printable(RealAlgebras.SignedReal, RealAlgebras.printableRealStrict, RealAlgebras.eqRealStrict)

    val printableQuaternionPretty: Printable<Quaternion> =
        realQuaternionTier.printable(RealAlgebras.SignedReal, RealAlgebras.printableRealPretty, RealAlgebras.eqRealApprox)
}
