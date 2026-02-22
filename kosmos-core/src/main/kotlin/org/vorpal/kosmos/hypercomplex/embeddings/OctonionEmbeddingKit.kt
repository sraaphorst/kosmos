package org.vorpal.kosmos.hypercomplex.embeddings

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingHomomorphism
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.combinatorics.FanoPlane
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.LeftAction

/**
 * The `basisMap: Map<Int, O>` maps index `0..7` to the 8 standard basis elements of the octonion algebra:
 * ```
 * 0 → 1  (the multiplicative identity)
 * 1 → e₁ (first imaginary unit)
 * 2 → e₂
 * ...
 * 7 → e₇
 * ```
 *
 * The `OctonionEmbeddingKit` needs them because it constructs embeddings by *linear combination*.
 *
 * Given a quaternion `q = w + xi + yj + zk`, an embedding `φ` maps it to:
 * ```
 * φ(q) = w · basisMap[0] + x · basisMap[iIndex] + y · basisMap[jIndex] + z · basisMap[kIndex]
 * ```
 *
 * That's the:
 * ```
 * linearCombination(decompose(q).zip(images))
 * ```
 * call. It:
 * - takes the four scalar components of the quaternion (via `decompose`);
 * - pairs each with the corresponding octonion basis element (via `basisMap`); and
 * - scales-then-sums (via `leftAction`).
 *
 * Without the basis map, the kit wouldn't know which octonion elements correspond to `e₁` through `e₇`, so it could not
 * construct the images of `i`, `j`, `k`.
 *
 * How to select one of the 84 embeddings:
 *
 * Two ways:
 * 1. By spec (if you know which one you want):
 * ```kotlin
 * val (spec, hom) = OctonionAlgebras.embeddingKit.createEmbedding(
 *     iIndex = 1,          // quaternion i maps to e₁
 *     jIndex = 2,          // quaternion j maps to e₂
 *     handedness = HyperComplex.Handedness.RIGHT  // i maps to +e₁ (not -e₁)
 * )
 * // Now hom: NonAssociativeRingHomomorphism<Quaternion, Octonion>
 * val octResult = hom(someQuaternion)
 * ```
 *
 * The three parameters fully determine the embedding:
 * - `iIndex`: which `e_n` does quaternion `i` map to?
 * - `jIndex`: which `e_n` does quaternion `j` map to?
 * - `handedness`: does `i` map to `+e_iIndex (RIGHT)` or `-e_iIndex (LEFT)`?
 *
 * The image of `k` is forced: `φ(k) = φ(i) · φ(j)`, and the kit computes it and records the sign in `spec.kSign`.
 *
 * 2. By lookup (enumerate all, pick from the map):
 * ```kotlin
 * val all = OctonionAlgebras.allQuaternionEmbeddings()
 * // all: Map<OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Quaternion, Octonion>>
 *
 * // Find a specific one:
 * val canonical = all.entries.first { (spec, _) ->
 *     spec.i == 1 && spec.j == 2 && spec.handedness == HyperComplex.Handedness.RIGHT
 * }.value
 * ```
 *
 * The canonical embedding (`iIndex=1`, `jIndex=2`, `RIGHT`) corresponds to `Quaternion.asOctonion()`.
 *
 * It maps `i ↦ e₁`, `j ↦ e₂`, `k ↦ e₃` with positive signs.
 */
object OctonionEmbeddingKit {
    data class OctonionEmbeddingSpec(
        val i: Int,
        val j: Int,
        val k: Int,
        val handedness: Handedness,
        val kSign: Int
    )

    /**
     * Generic machinery for constructing the 84 quaternion ↪ octonion embeddings
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
        private val decompose: (Q) -> List<S>  // [w, x, y, z]
    ) {
        private val add = octonionRing.add

        private fun linearCombination(terms: List<Pair<S, O>>): O =
            terms.fold(add.identity) { acc, (s, o) ->
                add(acc, leftAction(s, o))
            }

        /**
         * Construct one of the canonical “basis-unit” embeddings `φ : ℍ ↪ 𝕆` determined by a choice of
         * octonion basis units for the quaternion generators `i` and `j`.
         *
         * We choose two distinct indices [iIndex], [jIndex] in `{1,…,7}` that lie on a common Fano line.
         * Let `e₁,…,e₇` be the chosen imaginary basis units of 𝕆. We define:
         * ```
         * φ(1) = 1
         * φ(i) =  e_{iIndex} (RIGHT)
         * φ(i) = -e_{iIndex} (LEFT: orientation flip, matching quaternion “handedness”)
         * φ(j) =  e_{jIndex}
         * φ(k) = φ(i)·φ(j)   forced by ij = k in ℍ
         * ```
         *
         * The third index `kIndex` is the remaining point on the (unordered) Fano line containing
         * `{iIndex, jIndex}`; the actual image of `k` is `±e_{kIndex}`, where the sign is recorded as `kSign`.
         *
         * Returns:
         * - an [OctonionEmbeddingSpec] describing the embedding (including `kSign`)
         * - the corresponding [NonAssociativeRingHomomorphism] ℍ ↪ 𝕆
         *
         * Notes:
         * - The embedding is constructed to respect multiplication by defining `φ(k)` as `φ(i)·φ(j)`.
         * - A sanity check asserts that `φ(i)·φ(j)` lands on `±e_{kIndex}`; if this fails, the chosen Fano
         *   incidence structure disagrees with the current multiplication table / basis convention.
         */
        fun createEmbedding(
            iIndex: Int,
            jIndex: Int,
            handedness: Handedness
        ): Pair<OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Q, O>> {
            require(iIndex in 1..7) { "iIndex must be in [1,7], got $iIndex" }
            require(jIndex in 1..7) { "jIndex must be in [1,7], got $jIndex" }
            require(iIndex != jIndex) { "iIndex and jIndex must be distinct" }

            // Given that all pairs of points are covered, this always returns a valid kIndex.
            val kIndex = FanoPlane.thirdPoint(iIndex, jIndex)

            val one = basisMap.getValue(0)
            val ei = basisMap.getValue(iIndex)
            val di = when (handedness) {
                Handedness.RIGHT -> ei
                Handedness.LEFT -> add.inverse(ei)
            }
            val dj = basisMap.getValue(jIndex)
            val dk = octonionRing.mul(di, dj)

            // Sanity: dk = ±e_k
            val ek = basisMap.getValue(kIndex)
            val negEk = add.inverse(ek)
            val kSign = when {
                eq(dk, ek) -> +1
                eq(dk, negEk) -> -1
                else -> error("e$iIndex · e$jIndex is not ±e$kIndex — Fano/multiplication mismatch")
            }

            val spec = OctonionEmbeddingSpec(iIndex, jIndex, kIndex, handedness, kSign)
            val images = listOf(one, di, dj, dk)

            val hom = NonAssociativeRingHomomorphism.of(quaternionRing, octonionRing) { q ->
                linearCombination(decompose(q).zip(images))
            }

            return spec to hom
        }

        fun allEmbeddings(): Map<OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Q, O>> =
            buildMap {
                FanoPlane.lines.forEach { line ->
                    line.orderedPairs.forEach { (i, j) ->
                        Handedness.entries.forEach { h ->
                            val (spec, hom) = createEmbedding(i, j, h)
                            require(spec !in this) { "Duplicate spec: $spec" }
                            put(spec, hom)
                        }
                    }
                }
            }
    }
}