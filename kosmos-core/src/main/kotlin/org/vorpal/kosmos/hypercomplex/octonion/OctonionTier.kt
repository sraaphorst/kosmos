package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.LinearCombinationPrintable.SignedOps
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionTier

/**
 * The octonion construction `CD⟨Q⟩`, parameterized by its quaternion tier.
 *
 * 𝕆, 𝕆_ℚ and the Gravesian octonions are one construction over three quaternion tiers:
 * ```text
 * 𝕆         = CD⟨ℍ⟩     coefficients S = Real
 * 𝕆_ℚ       = CD⟨ℍ_ℚ⟩   coefficients S = Rational
 * Gravesian = CD⟨Lip⟩   coefficients S = BigInteger
 * ```
 * An [OctonionTier] holds everything that depends only on that choice: the (nonassociative)
 * Cayley-Dickson ring, the basis `1, e₁, …, e₇`, coordinates `[w, x, y, z, u, v, s, t]`, the
 * coordinatewise scalar action, the norm, the quaternion-embedding kit, and the printable.
 */
internal class OctonionTier<S : Any, C : Any>(
    val quaternions: QuaternionTier<S, C>
) {
    /**
     * The Cayley-Dickson double `CD⟨CD⟨C⟩⟩`.
     */
    val ring: CayleyDickson<CD<C>> =
        CayleyDickson.usual(quaternions.ring)

    /**
     * The coordinates `[w, x, y, z, u, v, s, t]` of `(w + xi + yj + zk, u + vi + sj + tk)`.
     */
    val coordinates: UnaryOp<CD<CD<C>>, List<S>> =
        UnaryOp { o -> quaternions.coordinates(o.a) + quaternions.coordinates(o.b) }

    /**
     * The octonion from its coordinates `[w, x, y, z, u, v, s, t]`.
     */
    val fromCoordinates: UnaryOp<List<S>, CD<CD<C>>> =
        UnaryOp { cs ->
            require(cs.size == 8) { "An octonion has 8 coordinates: got ${cs.size}." }
            CD(quaternions.fromCoordinates(cs.subList(0, 4)), quaternions.fromCoordinates(cs.subList(4, 8)))
        }

    /**
     * The standard basis `1, e₁, …, e₇`, obtained by doubling the basis `1, i, j, k` of the
     * quaternion tier: `e₁, e₂, e₃` are `i, j, k` in the first slot and `e₄, …, e₇` are `1, i, j, k`
     * in the second.
     */
    val basis: List<CD<CD<C>>> =
        CayleyDickson.doubledBasis(
            zero = quaternions.ring.zero,
            baseBasis = quaternions.basis
        )

    val basisMap: Map<Int, CD<CD<C>>> =
        basis.withIndex().associate { (idx, e) -> idx to e }

    /**
     * The coordinatewise scalar action. The scalars lie in the center (indeed the nucleus) of the
     * octonions, so this is also multiplication by the embedded scalar.
     */
    val scalarAction: LeftAction<S, CD<CD<C>>> =
        LeftAction(Symbols.TRIANGLE_RIGHT) { s, o ->
            fromCoordinates(coordinates(o).map { c -> quaternions.scalars.mul(s, c) })
        }

    /**
     * The norm `N(o) = o·conj(o)`, the sum of the squares of the eight coordinates.
     *
     * For the usual (σ = 1) doubling, `N(a, b) = N(a) + N(b)`, so it is read off the quaternion norms.
     */
    val normSq: UnaryOp<CD<CD<C>>, S> =
        UnaryOp(Symbols.NORM_SQ_SYMBOL) { o ->
            quaternions.scalars.add(quaternions.normSq(o.a), quaternions.normSq(o.b))
        }

    /**
     * The machinery for the 168 signed-basis-unit embeddings of the quaternion tier into this tier.
     */
    fun embeddingKit(eq: Eq<CD<CD<C>>>): OctonionEmbeddingKit.OctonionEmbeddingKit<S, CD<C>, CD<CD<C>>> =
        OctonionEmbeddingKit.OctonionEmbeddingKit(
            quaternionRing = quaternions.ring,
            octonionRing = ring,
            basisMap = basisMap,
            leftAction = scalarAction,
            eq = eq,
            decompose = quaternions.coordinates
        )

    fun printable(
        signed: SignedOps<S>,
        prS: Printable<S>,
        eqS: Eq<S>
    ): Printable<CD<CD<C>>> =
        OctonionPrintable.octonionPrintable(
            signed = signed,
            zero = quaternions.scalars.zero,
            one = quaternions.scalars.one,
            prA = prS,
            eqA = eqS,
            decompose = coordinates
        )
}
