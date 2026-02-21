package org.vorpal.kosmos.algebra.structures.instances

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
import org.vorpal.kosmos.algebra.structures.instances.embeddings.AxisSignEmbeddings
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger
import kotlin.isFinite

typealias Quaternion = CD<Complex>
val Quaternion.w: Real get() = a.re
val Quaternion.x: Real get() = a.im
val Quaternion.y: Real get() = b.re
val Quaternion.z: Real get() = b.im

/**
 * Convenience constructor for a quaternion:
 *
 *    complex(w + x i_c), complex(y + z i_c)
 */
fun quaternion(w: Real,
               x: Real,
               y: Real,
               z: Real
): Quaternion {
    val a = complex(w, x)
    val b = complex(y, z)
    return Quaternion(a, b)
}

object QuaternionAlgebras {

    object QuaternionDivisionRing:
        DivisionRing<Quaternion>,
        InvolutiveRing<Quaternion>,
        RealNormedDivisionAlgebra<Quaternion> {

        private val base: NonAssociativeInvolutiveRing<Quaternion> =
            CayleyDickson.usual(ComplexAlgebras.ComplexStarAlgebra)

        override val add = base.add

        // base.mul is a NonAssociativeMonoid: for quaternions, it is actually a monoid and
        // Ring expects an associative monoid, so we wrap.
        override val mul: Monoid<Quaternion> = Monoid.of(
            identity = base.mul.identity,
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
                ComplexAlgebras.ComplexRealVectorSpace.leftAction(scale, qc.b))
        }

        override fun fromBigInt(n: BigInteger) =
            base.fromBigInt(n)

        override val conj: Endo<Quaternion> =
            base.conj

        override val normSq: UnaryOp<Quaternion, Real> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL){ q -> mul(q, conj(q)).w }

        // Disambiguate zero and one.
        override val zero = add.identity
        override val one = mul.identity
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

    // --------------------------------------------------------------------------
    // Complex -> Quaternion embeddings (six unital choices) via ComplexEmbedding
    // --------------------------------------------------------------------------
    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical

    /**
     * Return the ring monomorphism embedding ℂ into ℍ determined by [emb].
     *
     * Embed a complex number into ℍ using the subfield `ℝ ⊕ ℝ·i`.
     * Sends:
     * ```
     * a + b i_C  ↦  a·1 + b·u
     * ```
     * where u ∈ {±i, ±j, ±k} based on emb.axis and emb.sign.
     */
    fun complexEmbeddingToQuaternion(
        emb: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<Complex, Quaternion> = RingMonomorphism.of(
        domain = ComplexAlgebras.ComplexField,
        codomain = QuaternionDivisionRing,
        map = UnaryOp { c ->
            val a = c.re
            val b = c.im
            val s = emb.sign.factor.toDouble()

            when (emb.axis) {
                AxisSignEmbeddings.ImagAxis.I -> quaternion(a, s * b, 0.0, 0.0)
                AxisSignEmbeddings.ImagAxis.J -> quaternion(a, 0.0, s * b, 0.0)
                AxisSignEmbeddings.ImagAxis.K -> quaternion(a, 0.0, 0.0, s * b)
            }
        }
    )

    /**
     * Convenience extension.
     *
     * Canonical default is `i_C ↦ i` (i.e. ComplexEmbedding.CANONICAL).
     */
    fun Complex.asQuaternion(
        emb: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): Quaternion = complexEmbeddingToQuaternion(emb)(this)

    /**
     * ℍ as a left ℂ-vector space using the chosen embedding.
     */
    fun quaternionLeftComplexVectorSpace(
        emb: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): FiniteVectorSpace<Complex, Quaternion> {
        val embed = complexEmbeddingToQuaternion(emb)
        return FiniteVectorSpace.of(
            scalars = ComplexAlgebras.ComplexField,
            add = QuaternionDivisionRing.add,
            dimension = 2,
            leftAction = LeftAction { c, q ->
                QuaternionDivisionRing.mul(embed(c), q)
            }
        )
    }

    // Optional convenience vals, replacing the former AlongI/J/K trio.
    val QuaternionLeftComplexVectorSpaceCanonical: FiniteVectorSpace<Complex, Quaternion> =
        quaternionLeftComplexVectorSpace(canonicalEmbedding)

    // A map of all complex embeddings to the vector space that they generate.
    val QuaternionLeftComplexVectorSpacesAll: Map<AxisSignEmbeddings.AxisSignEmbedding, FiniteVectorSpace<Complex, Quaternion>> =
        AxisSignEmbeddings.AxisSignEmbedding.all.associateWith { quaternionLeftComplexVectorSpace(it) }

    val QuaternionStarAlgebra: StarAlgebra<Real, Quaternion> = StarAlgebra.of(
        scalars = RealAlgebras.RealField,
        involutiveRing = QuaternionDivisionRing,
        leftAction = QuaternionVectorSpace.leftAction
    )

    val eqQuaternionStrict: Eq<Quaternion> = CD.eq(ComplexAlgebras.eqComplexStrict)
    val eqQuaternion: Eq<Quaternion> = CD.eq(ComplexAlgebras.eqComplex)
}
