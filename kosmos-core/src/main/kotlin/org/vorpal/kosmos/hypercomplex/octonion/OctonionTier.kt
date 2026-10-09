package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CarrierTransport
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.contramap
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionTier

/**
 * The octonion-level construction: one more Cayley-Dickson doubling of a [QuaternionTier],
 * delivered on a public carrier `O`.
 * ```text
 * Octonion<R>        usual doubling of Quaternion<R>     𝕆 (R = ℝ), 𝕆_ℚ (R = ℚ), Gravesian (R = ℤ)
 * SplitOctonion<R>   split doubling of Quaternion<R>
 * Bioctonion         usual doubling of Biquaternion
 * ```
 * An [OctonionTier] holds everything that depends only on that choice: the (nonassociative) ring,
 * the basis `1, e₁, …, e₇`, coordinates `[w, x, y, z, u, v, s, t]`, the coordinatewise scalar action,
 * the norm, the canonical embedding of the quaternion tier, the quaternion-embedding kit, an [Eq]
 * and a printable, all on the public carriers.
 *
 * @param quaternions the quaternion tier being doubled.
 * @param wrap names a raw Cayley-Dickson pair as an `O`. Must be inverse to [unwrap].
 * @param unwrap the raw Cayley-Dickson pair of an `O`.
 * @param split `false` for the usual doubling (`e₄, …, e₇` square to `-1`), `true` for the split
 *   doubling (they square to `+1`).
 */
internal class OctonionTier<S : Any, Q : Any, O : Any>(
    val quaternions: QuaternionTier<S, Q>,
    val wrap: (CD<CD<CD<S>>>) -> O,
    val unwrap: (O) -> CD<CD<CD<S>>>,
    val split: Boolean = false
) {
    private val scalars = quaternions.scalars

    /**
     * The Cayley-Dickson double on the raw carrier: usual (`σ = 1`) or split (`σ = -1`).
     */
    val rawRing: CayleyDickson<CD<CD<S>>> =
        if (split) CayleyDickson.split(quaternions.rawRing)
        else CayleyDickson.usual(quaternions.rawRing)

    /**
     * The ring on the public carrier.
     */
    val ring: NonAssociativeInvolutiveRing<O> =
        CarrierTransport<CD<CD<CD<S>>>, O>(
            wrap = UnaryOp { cd -> wrap(cd) },
            unwrap = UnaryOp { o -> unwrap(o) }
        ).nonAssociativeInvolutiveRing(rawRing)

    private fun rawCoordinates(q: CD<CD<S>>): List<S> =
        listOf(q.a.a, q.a.b, q.b.a, q.b.b)

    private fun rawQuaternion(cs: List<S>): CD<CD<S>> =
        CD(CD(cs[0], cs[1]), CD(cs[2], cs[3]))

    /**
     * The coordinates `[w, x, y, z, u, v, s, t]` of `(w + xi + yj + zk, u + vi + sj + tk)`.
     */
    val coordinates: UnaryOp<O, List<S>> =
        UnaryOp { o ->
            val cd = unwrap(o)
            rawCoordinates(cd.a) + rawCoordinates(cd.b)
        }

    /**
     * The octonion from its coordinates `[w, x, y, z, u, v, s, t]`.
     */
    val fromCoordinates: UnaryOp<List<S>, O> =
        UnaryOp { cs ->
            require(cs.size == 8) { "An octonion has 8 coordinates: got ${cs.size}." }
            wrap(CD(rawQuaternion(cs.subList(0, 4)), rawQuaternion(cs.subList(4, 8))))
        }

    /**
     * The standard basis `1, e₁, …, e₇`, obtained by doubling the basis `1, i, j, k` of the
     * quaternion tier: `e₁, e₂, e₃` are `i, j, k` in the first slot and `e₄, …, e₇` are `1, i, j, k`
     * in the second.
     */
    val basis: List<O> =
        CayleyDickson.doubledBasis(
            zero = quaternions.rawRing.zero,
            baseBasis = quaternions.basis.map(quaternions.unwrap)
        ).map(wrap)

    val basisMap: Map<Int, O> =
        basis.withIndex().associate { (idx, e) -> idx to e }

    /**
     * The coordinatewise scalar action. The scalars lie in the center (indeed the nucleus) of the
     * algebra, so this is also multiplication by the embedded scalar.
     */
    val scalarAction: LeftAction<S, O> =
        LeftAction(Symbols.TRIANGLE_RIGHT) { s, o ->
            fromCoordinates(coordinates(o).map { c -> scalars.mul(s, c) })
        }

    /**
     * The norm `N(o) = o·conj(o)`, read off the quaternion norms: for a doubling with sign `σ`,
     * ```text
     * N(a, b) = N(a) + σ·N(b),
     * ```
     * the sum of the eight squared coordinates for the usual doubling, and `N(a) - N(b)` (signature
     * `(4, 4)` over a usual quaternion tier) for the split one.
     */
    val normSq: UnaryOp<O, S> =
        UnaryOp(Symbols.NORM_SQ_SYMBOL) { o ->
            val cd = unwrap(o)
            val na = quaternions.normSq(quaternions.wrap(cd.a))
            val nb = quaternions.normSq(quaternions.wrap(cd.b))
            scalars.add(na, if (split) scalars.add.inverse(nb) else nb)
        }

    /**
     * The embedding of the scalars, `s ↦ s·1`.
     */
    val scalarEmbeddingMap: UnaryOp<S, O> =
        UnaryOp { s -> fromCoordinates(listOf(s) + List(7) { scalars.zero }) }

    /**
     * The canonical Cayley-Dickson embedding of the quaternion tier, `q ↦ (q, 0)`: the universal
     * property of the doubling, on the public carriers.
     */
    val canonicalEmbeddingMap: UnaryOp<Q, O> =
        UnaryOp { q -> wrap(CD(quaternions.unwrap(q), quaternions.rawRing.zero)) }

    /**
     * The machinery for the 168 signed-basis-unit embeddings of the quaternion tier into this tier.
     *
     * Only meaningful for the usual doubling over a usual quaternion tier, where every imaginary
     * basis unit squares to `-1`.
     */
    fun embeddingKit(eq: Eq<O>): OctonionEmbeddingKit.OctonionEmbeddingKit<S, Q, O> =
        OctonionEmbeddingKit.OctonionEmbeddingKit(
            quaternionRing = quaternions.ring,
            octonionRing = ring,
            basisMap = basisMap,
            leftAction = scalarAction,
            eq = eq,
            decompose = quaternions.coordinates
        )

    /**
     * Coordinatewise equality on the public carrier.
     */
    fun eq(eqS: Eq<S>): Eq<O> =
        CD.eq(CD.eq(CD.eq(eqS))).contramap { o: O -> unwrap(o) }

    fun printable(
        signed: SignedOps<S>,
        prS: Printable<S>,
        eqS: Eq<S>
    ): Printable<O> =
        OctonionPrintable.octonionPrintable(
            signed = signed,
            zero = scalars.zero,
            one = scalars.one,
            prA = prS,
            eqA = eqS,
            decompose = coordinates
        )
}
