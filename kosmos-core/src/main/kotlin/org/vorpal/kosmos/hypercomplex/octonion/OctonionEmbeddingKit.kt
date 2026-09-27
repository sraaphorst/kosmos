package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingHomomorphism
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.combinatorics.FanoPlane
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.Sign

/**
 * The signed-basis-unit embeddings of a quaternion tier into the matching octonion tier.
 *
 * An embedding `φ : ℍ ↪ 𝕆` of this kind sends the quaternion generators to signed imaginary basis units:
 * ```text
 * φ(i) = σᵢ e_p,   φ(j) = σⱼ e_q,   φ(k) = φ(i)·φ(j) = ±e_r
 * ```
 * where `p ≠ q` are points of `{1, …, 7}` and `σᵢ, σⱼ ∈ {+1, -1}`. Every pair of distinct points
 * lies on exactly one line `{p, q, r}` of the Fano plane, `e_p` and `e_q` anticommute and square to
 * `-1`, and so `(φ(i), φ(j))` generates a copy of ℍ. The image of `k` is forced; its sign is recorded
 * as [OctonionEmbeddingSpec.kSign].
 *
 * Counting: 7 · 6 = 42 ordered pairs `(p, q)` times 4 sign choices `(σᵢ, σⱼ)` gives
 * ```text
 * 42 · 4 = 168
 * ```
 * distinct embeddings. The normalized half-family with `σⱼ = +1` (84 embeddings) is available as
 * [OctonionEmbeddingKit.normalizedEmbeddings].
 *
 * The `basisMap: Map<Int, O>` maps index `0..7` to the 8 standard basis elements of the octonion algebra:
 * ```
 * 0 → 1  (the multiplicative identity)
 * 1 → e₁ (first imaginary unit)
 * ...
 * 7 → e₇
 * ```
 * The kit constructs each embedding by *linear combination*: for `q = w + xi + yj + zk`,
 * ```
 * φ(q) = w · 1 + x · φ(i) + y · φ(j) + z · φ(k).
 * ```
 *
 * Selecting an embedding:
 * 1. By spec:
 * ```kotlin
 * val (spec, hom) = OctonionAlgebras.embeddingKit.createEmbedding(
 *     iIndex = 1, jIndex = 2, iSign = Sign.PLUS, jSign = Sign.PLUS
 * )
 * ```
 * 2. By lookup:
 * ```kotlin
 * val all = OctonionAlgebras.allQuaternionEmbeddings()   // 168 entries
 * ```
 *
 * The canonical embedding (`iIndex = 1`, `jIndex = 2`, both signs `PLUS`) is the Cayley-Dickson
 * embedding `q ↦ (q, 0)`: `i ↦ e₁`, `j ↦ e₂`, `k ↦ e₃`.
 */
object OctonionEmbeddingKit {
    data class OctonionEmbeddingSpec(
        val i: Int,
        val j: Int,
        val k: Int,
        val iSign: Sign,
        val jSign: Sign,
        val kSign: Int
    )

    /**
     * Generic machinery for constructing the 168 quaternion ↪ octonion embeddings
     * over any scalar type.
     *
     * @param S scalar type (Real, Rational, BigInteger)
     * @param Q quaternion-like type
     * @param O octonion-like type
     */
    class OctonionEmbeddingKit<S : Any, Q : Any, O : Any>(
        private val quaternionRing: NonAssociativeInvolutiveRing<Q>,
        private val octonionRing: NonAssociativeInvolutiveRing<O>,
        private val basisMap: Map<Int, O>,
        private val leftAction: LeftAction<S, O>,
        private val eq: Eq<O>,
        private val decompose: UnaryOp<Q, List<S>>  // [w, x, y, z]
    ) {
        private val add = octonionRing.add

        private fun linearCombination(terms: List<Pair<S, O>>): O =
            terms.fold(add.identity) { acc, (s, o) ->
                add(acc, leftAction(s, o))
            }

        private fun signed(sign: Sign, o: O): O =
            when (sign) {
                Sign.PLUS -> o
                Sign.MINUS -> add.inverse(o)
            }

        /**
         * Construct the embedding `φ : ℍ ↪ 𝕆` with `φ(i) = iSign · e_{iIndex}` and
         * `φ(j) = jSign · e_{jIndex}`.
         *
         * The third index `kIndex` is the remaining point on the Fano line through `iIndex` and
         * `jIndex`; the image of `k` is `φ(i)·φ(j) = ±e_{kIndex}`, and the sign is recorded as `kSign`.
         *
         * A sanity check asserts that `φ(i)·φ(j)` lands on `±e_{kIndex}`; if this fails, the Fano
         * incidence structure disagrees with the multiplication table / basis convention.
         */
        fun createEmbedding(
            iIndex: Int,
            jIndex: Int,
            iSign: Sign = Sign.PLUS,
            jSign: Sign = Sign.PLUS
        ): Pair<OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Q, O>> {
            require(iIndex in 1..7) { "iIndex must be in [1,7], got $iIndex" }
            require(jIndex in 1..7) { "jIndex must be in [1,7], got $jIndex" }
            require(iIndex != jIndex) { "iIndex and jIndex must be distinct" }

            // Every pair of distinct points lies on a unique line, so this is always defined.
            val kIndex = FanoPlane.thirdPoint(iIndex, jIndex)

            val one = basisMap.getValue(0)
            val di = signed(iSign, basisMap.getValue(iIndex))
            val dj = signed(jSign, basisMap.getValue(jIndex))
            val dk = octonionRing.mul(di, dj)

            val ek = basisMap.getValue(kIndex)
            val kSign = when {
                eq(dk, ek) -> +1
                eq(dk, add.inverse(ek)) -> -1
                else -> error("e$iIndex · e$jIndex is not ±e$kIndex: Fano/multiplication mismatch")
            }

            val spec = OctonionEmbeddingSpec(iIndex, jIndex, kIndex, iSign, jSign, kSign)
            val images = listOf(one, di, dj, dk)

            val hom = NonAssociativeRingHomomorphism.of(quaternionRing, octonionRing) { q ->
                linearCombination(decompose(q).zip(images))
            }

            return spec to hom
        }

        /**
         * All 168 signed-basis-unit embeddings, keyed by their spec.
         */
        fun allEmbeddings(): Map<OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Q, O>> =
            embeddings(Sign.entries)

        /**
         * The 84 embeddings with the normalization `j ↦ +e_q`. Every embedding in [allEmbeddings]
         * is one of these up to the sign of `j`.
         */
        fun normalizedEmbeddings(): Map<OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Q, O>> =
            embeddings(listOf(Sign.PLUS))

        private fun embeddings(
            jSigns: List<Sign>
        ): Map<OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Q, O>> =
            FanoPlane.lines
                .flatMap { line -> line.orderedPairs }
                .flatMap { (i, j) ->
                    Sign.entries.flatMap { iSign ->
                        jSigns.map { jSign -> createEmbedding(i, j, iSign, jSign) }
                    }
                }
                .also { pairs ->
                    val specs = pairs.map { it.first }
                    require(specs.toSet().size == specs.size) { "Duplicate embedding spec generated." }
                }
                .toMap()
    }
}
