package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
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
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger
import kotlin.isFinite

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
 * - [complexEmbeddingToQuaternion]: the unital embeddings from the complex numbers to the quaternions.
 *
 * We also have the following [Eq]s:
 * - [eqQuaternionStrict]: strict equality on quaternions.
 * - [eqQuaternion]: approximate equality on quaternions.
 */
object QuaternionAlgebras {

    object QuaternionDivisionRing:
        DivisionRing<Quaternion>,
        InvolutiveRing<Quaternion>,
        RealNormedDivisionAlgebra<Quaternion> {

        private val base: NonAssociativeInvolutiveRing<Quaternion> =
            CayleyDickson.usual(ComplexAlgebras.ComplexStarAlgebra)

        override val zero = base.add.identity
        override val one = base.mul.identity

        override val add = base.add

        override val mul: Monoid<Quaternion> = Monoid.of(
            identity = one,
            op = base.mul.op
        )

        override val reciprocal: Endo<Quaternion> = Endo(Symbols.SLASH) { q ->
            val n2 = normSq(q)
            require(RealAlgebras.eqRealApprox.neqv(n2, 0.0) && n2.isFinite()) {
                "Zero has no multiplicative inverse in ${Symbols.BB_H}."
            }

            val qc = conj(q)
            val scale = 1.0 / n2

            Quaternion(
                ComplexAlgebras.ComplexRealVectorSpace.leftAction(scale, qc.a),
                ComplexAlgebras.ComplexRealVectorSpace.leftAction(scale, qc.b)
            )
        }

        override fun fromBigInt(n: BigInteger) =
            base.fromBigInt(n)

        override val conj: Endo<Quaternion> =
            base.conj

        override val normSq: UnaryOp<Quaternion, Real> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL){ q -> mul(q, conj(q)).w }
    }

    // Scalars: Real, act componentwise on (a, b)
    val QuaternionVectorSpace: FiniteVectorSpace<Real, Quaternion> = FiniteVectorSpace.of(
        scalars = RealAlgebras.RealField,
        add = QuaternionDivisionRing.add,
        dimension = 4,
        leftAction = LeftAction { r, q ->
            Quaternion(
                ComplexAlgebras.ComplexRealVectorSpace.leftAction(r, q.a),
                ComplexAlgebras.ComplexRealVectorSpace.leftAction(r, q.b)
            )
        }
    )

    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical

    /**
     * ℍ as a left ℂ-vector space using the chosen embedding.
     */
    fun quaternionLeftComplexVectorSpace(
        emb: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): FiniteVectorSpace<Complex, Quaternion> {
        val embed = ComplexAlgebras.complexToQuaternionEmbedding(emb)
        return FiniteVectorSpace.of(
            scalars = ComplexAlgebras.ComplexField,
            add = QuaternionDivisionRing.add,
            dimension = 2,
            leftAction = LeftAction { c, q ->
                QuaternionDivisionRing.mul(embed(c), q)
            }
        )
    }

    // A map of all complex embeddings to the vector space that they generate.
    val QuaternionLeftComplexVectorSpacesAll: Map<AxisSignEmbeddings.AxisSignEmbedding, FiniteVectorSpace<Complex, Quaternion>> =
        AxisSignEmbeddings.AxisSignEmbedding.all.associateWith(::quaternionLeftComplexVectorSpace)

    // Optional convenience vals, replacing the former AlongI/J/K trio.
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

    private fun printableQuaternionGenerator(
        prReal: Printable<Real>,
        eqReal: Eq<Real>
    ): Printable<Quaternion> =
        QuaternionPrintable.quaternionPrintable(
            signed = RealAlgebras.SignedReal,
            zero = RealAlgebras.RealField.zero,
            one = RealAlgebras.RealField.one,
            prA = prReal,
            eqA = eqReal,
            decompose = { q -> listOf(q.w, q.x, q.y, q.z) }
        )

    val printableQuaternion: Printable<Quaternion> = printableQuaternionGenerator(
        prReal = RealAlgebras.printableReal,
        eqReal = RealAlgebras.eqRealApprox
    )

    val printableQuaternionStrict: Printable<Quaternion> = printableQuaternionGenerator(
        prReal = RealAlgebras.printableRealStrict,
        eqReal = RealAlgebras.eqRealStrict
    )

    val printableQuaternionPretty: Printable<Quaternion> = printableQuaternionGenerator(
        prReal = RealAlgebras.printableRealPretty,
        eqReal = RealAlgebras.eqRealApprox
    )
}
