package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingHomomorphism
import org.vorpal.kosmos.algebra.structures.NonAssociativeRing
import org.vorpal.kosmos.core.Eq

/**
 * Check that every one of the signed-basis-unit embeddings `ℍ ↪ 𝕆` in [embeddings] is multiplicative,
 * and that there are exactly [expectedCount] of them (168 for the full family, 84 for the normalized one).
 *
 * Each embedding is linear by construction, so by bilinearity of both products it suffices to check
 * `φ(pq) = φ(p)φ(q)` on the 16 pairs of quaternion basis units `p, q ∈ {1, i, j, k}`. This makes the
 * check exhaustive rather than sampled: all `expectedCount · 16` products are verified.
 */
internal fun <Q : Any, O : Any> checkAllQuaternionEmbeddingsMultiplicative(
    embeddings: Map<OctonionEmbeddingKit.OctonionEmbeddingSpec, NonAssociativeRingHomomorphism<Q, O>>,
    quaternionRing: NonAssociativeRing<Q>,
    quaternionBasis: List<Q>,
    octonionRing: NonAssociativeRing<O>,
    eqO: Eq<O>,
    expectedCount: Int = 168
) {
    check(embeddings.size == expectedCount) { "Expected $expectedCount embeddings, got ${embeddings.size}" }
    check(quaternionBasis.size == 4) { "Expected a quaternion basis of size 4, got ${quaternionBasis.size}" }

    embeddings.forEach { (spec, phi) ->
        check(eqO(phi(quaternionRing.one), octonionRing.one)) { "$spec does not preserve 1" }
        quaternionBasis.forEach { p ->
            quaternionBasis.forEach { q ->
                val lhs = phi(quaternionRing.mul(p, q))
                val rhs = octonionRing.mul(phi(p), phi(q))
                check(eqO(lhs, rhs)) { "$spec is not multiplicative on basis units $p, $q" }
            }
        }
    }
}
