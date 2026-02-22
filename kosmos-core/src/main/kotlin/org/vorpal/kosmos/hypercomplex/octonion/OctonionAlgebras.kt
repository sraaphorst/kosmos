package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras.QuaternionDivisionRing
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NonAssociativeDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.algebra.structures.NonAssociativeStarAlgebra
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
import org.vorpal.kosmos.hypercomplex.quaternion.Quaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras.QuaternionVectorSpace
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras.eqQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras.eqQuaternionStrict
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.hypercomplex.embeddings.OctonionEmbeddingKit
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion
import org.vorpal.kosmos.hypercomplex.quaternion.w
import org.vorpal.kosmos.hypercomplex.quaternion.x
import org.vorpal.kosmos.hypercomplex.quaternion.y
import org.vorpal.kosmos.hypercomplex.quaternion.z
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

/**
 * [OctonionAlgebras] contains the algebraic structures over the [Octonion] type, as well as the
 * homomorphisms and [Eq] instances.
 *
 * These include:
 * - [OctonionDivisionAlgebraReal]: the real octonion non-associative division algebra.
 * - [OctonionVectorSpace]: the eight-dimensional vector space of rational octonions over the reals.
 * - [OctonionStarAlgebra]: the non-associative star algebra of octonions over the reals.
 *
 * We have the following homomorphisms:
 * - [QuaternionToOctonionMonomorphism]: canonical CD embedding of quaternions into octonions.
 * - [Quaternion.asOctonion]: convenience extension for the canonical embedding.
 *
 * We also have the following [Eq]s:
 * - [eqOctonionStrict]: strict equality on octonions.
 * - [eqOctonion]: approximate equality on octonions.
 */
object OctonionAlgebras {

    /**
     * In this case, the most we can say about the Octonions are that they are an [NonAssociativeDivisionAlgebra].
     *
     * We get everything but the reciprocal from the [NonAssociativeInvolutiveRing] returned by the [CayleyDickson] construction.
     */
    object OctonionDivisionAlgebraReal : RealNormedDivisionAlgebra<Octonion> {

        private val base: NonAssociativeInvolutiveRing<Octonion> =
            CayleyDickson.usual(QuaternionDivisionRing)

        override val add: AbelianGroup<Octonion> = base.add
        override val mul: NonAssociativeMonoid<Octonion> = base.mul

        // The only thing that makes octonions invertible is that their norm is a composition norm.
        override val reciprocal: Endo<Octonion> = Endo(Symbols.SLASH) { o ->
            val n2: Real = normSq(o)
            require(RealAlgebras.eqRealApprox.neqv(n2, 0.0) && n2.isFinite()) { "$n2 has no multiplicative inverse in ${Symbols.BB_O}."}

            val oc = conj(o)
            val scale: Real = 1.0 / n2

            // Use the QuaternionModule's action to scale.
            // We could use OctonionModule, but we fall back to QuaternionModule to avoid circular dependencies.
            Octonion(
                QuaternionVectorSpace.leftAction(scale, oc.a),
                QuaternionVectorSpace.leftAction(scale, oc.b)
            )
        }

        override fun fromBigInt(n: BigInteger) =
            base.fromBigInt(n)

        override val conj: Endo<Octonion> = base.conj

        override val zero = add.identity
        override val one = mul.identity                                                             // quaternion 1 in "a"
        val e1 = octonion(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // quaternion i in "a"
        val e2 = octonion(0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0) // quaternion j in "a"
        val e3 = octonion(0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0) // quaternion k in "a"
        val e4 = octonion(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0) // quaternion 1 in "b"
        val e5 = octonion(0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0) // quaternion i in "b"
        val e6 = octonion(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0) // quaternion j in "b"
        val e7 = octonion(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0) // quaternion k in "b"
        val basisMap = mapOf(0 to one, 1 to e1, 2 to e2, 3 to e3, 4 to e4, 5 to e5, 6 to e6, 7 to e7)

        override val normSq: UnaryOp<Octonion, Real> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) {o -> mul(o, conj(o)).a.w }
    }

    /**
     * This is the only module from the Cayley-Dickson tower we can define for the octonions, since
     * the reals lie in the center, so scalar multiplication is safe.
     */
    val OctonionVectorSpace: FiniteVectorSpace<Real, Octonion> = FiniteVectorSpace.of(
        scalars = RealField,
        add = OctonionDivisionAlgebraReal.add,
        dimension = 8,
        leftAction = LeftAction { r, o -> octonion(
            r * o.w, r * o.x, r * o.y, r * o.z,
            r * o.u, r * o.v, r * o.s, r * o.t)
        }
    )

    val OctonionStarAlgebra: NonAssociativeStarAlgebra<Real, Octonion> = NonAssociativeStarAlgebra.of(
        scalars = RealField,
        involutiveRing = OctonionDivisionAlgebraReal,
        leftAction = OctonionVectorSpace.leftAction,
    )

    val QuaternionToOctonionMonomorphism: NonAssociativeRingMonomorphism<Quaternion, Octonion> =
        CayleyDickson.canonicalEmbedding(
            base = QuaternionDivisionRing,
            doubled = OctonionDivisionAlgebraReal
        )

    /**
     * Embed a quaternion number into an octonion.
     * Note that this corresponds to the first homomorphism from Quaternion to Octonion in the list generated below.
     */
    fun Quaternion.asOctonion(): Octonion =
        QuaternionToOctonionMonomorphism(this)

    val eqOctonionStrict: Eq<Octonion> = CD.eq(eqQuaternionStrict)
    val eqOctonion: Eq<Octonion> = CD.eq(eqQuaternion)

    val embeddingKit = OctonionEmbeddingKit.OctonionEmbeddingKit(
        quaternionRing = QuaternionDivisionRing,
        octonionRing = OctonionDivisionAlgebraReal,
        basisMap = OctonionDivisionAlgebraReal.basisMap,
        leftAction = OctonionVectorSpace.leftAction,
        eq = eqOctonionStrict,
        decompose = { q -> listOf(q.w, q.x, q.y, q.z) }
    )

    /**
     * Enumerate the full family of 84 “basis-unit” quaternion embeddings `ℍ ↪ 𝕆` arising from:
     *
     * - 7 unordered Fano lines in the standard Fano plane on `{1,…,7}`
     * - 6 ordered pairs `(i,j)` per line (choice of images for quaternion basis vectors `i` and `j`)
     * - 2 handedness choices (RIGHT: `i ↦ e_i`, LEFT: `i ↦ -e_i`), which flips the induced k-image sign
     *
     *
     *     Total: 7 * 6 * 2 = 84 embeddings.
     *
     * The result is keyed by [Embedding] so callers can deterministically select an embedding and also
     * inspect the induced [kSign]. A duplicate-key check is included as a guard against bugs in
     * enumeration or spec construction.
     */
    fun allQuaternionEmbeddings() = embeddingKit.allEmbeddings()
}
