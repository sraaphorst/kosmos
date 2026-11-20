package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.Sign
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.RealQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * The signed-basis-unit embeddings `ℍ ↪ 𝕆` and the canonical Cayley-Dickson embedding.
 *
 * Counting: 7 · 6 = 42 ordered pairs of distinct imaginary units for the images of `i` and `j`, and
 * 4 independent signs, give 168 embeddings. The 84 *normalized* ones fix the sign of `j` to be positive;
 * the other 84 are obtained from them by precomposing with the automorphism `i ↦ i, j ↦ -j, k ↦ -k` of ℍ.
 *
 * Basis units have coordinates in `{0, ±1}`, so all products involved are exact in floating point
 * and strict octonion equality is used.
 */
class OctonionEmbeddingSpec : FunSpec({
    lawTests(lawGraph("ℍ ↪ 𝕆 embeddings") {
        val quaternions = QuaternionAlgebras.QuaternionDivisionRing
        val octonions = OctonionAlgebras.OctonionDivisionAlgebra
        val eq = OctonionAlgebras.eqOctonionStrict

        local("the Cayley-Dickson basis of 𝕆 = CD⟨ℍ⟩ doubles the quaternion basis") {
            lawCheck(octonions.basis.take(4) == quaternions.basis.map { q -> Octonion(CD(q.cd, quaternions.zero.cd)) })
            lawCheck(octonions.basis.drop(4) == quaternions.basis.map { q -> Octonion(CD(quaternions.zero.cd, q.cd)) })
            lawCheck(listOf(octonions.e1, octonions.e2, octonions.e3, octonions.e4, octonions.e5, octonions.e6, octonions.e7) == octonions.basis.drop(1))
        }

        local("all 168 signed-basis-unit embeddings ℍ ↪ 𝕆 are multiplicative") {
            checkAllQuaternionEmbeddingsMultiplicative(
                embeddings = OctonionAlgebras.allQuaternionEmbeddings(),
                quaternionRing = quaternions,
                quaternionBasis = quaternions.basis,
                octonionRing = octonions,
                eqO = eq
            )
        }

        local("the 84 normalized embeddings are exactly those with j ↦ +e_q, and are multiplicative") {
            val normalized = OctonionAlgebras.normalizedQuaternionEmbeddings()
            normalized.keys.all { spec -> spec.jSign == Sign.PLUS } shouldBe true
            OctonionAlgebras.allQuaternionEmbeddings().keys.containsAll(normalized.keys) shouldBe true

            checkAllQuaternionEmbeddingsMultiplicative(
                embeddings = normalized,
                quaternionRing = quaternions,
                quaternionBasis = quaternions.basis,
                octonionRing = octonions,
                eqO = eq,
                expectedCount = 84
            )
        }

        local("flipping the sign of j is precomposition with the automorphism i ↦ i, j ↦ -j, k ↦ -k of ℍ") {
            val all = OctonionAlgebras.allQuaternionEmbeddings()

            // The automorphism fixes 1 and i and negates j and k.
            fun flip(q: RealQuaternion): RealQuaternion =
                quaternion(q.w, q.x, -q.y, -q.z)

            all.filterKeys { it.jSign == Sign.PLUS }.forEach { (spec, phi) ->
                val partner = all.getValue(
                    spec.copy(jSign = Sign.MINUS, kSign = -spec.kSign)
                )
                quaternions.basis.forEach { q ->
                    lawCheck(eq(partner(q), phi(flip(q)))) { "φ' ≠ φ ∘ flip for $spec on $q" }
                }
            }
        }

        local("each embedding sends k to ±e_r, where r is the third point on the Fano line through p and q") {
            OctonionAlgebras.allQuaternionEmbeddings().forEach { (spec, phi) ->
                val ek = octonions.basisMap.getValue(spec.k)
                val expected = if (spec.kSign > 0) ek else octonions.add.inverse(ek)
                lawCheck(eq(phi(quaternions.basis[3]), expected)) { "k image wrong for $spec" }
            }
        }

        local("the canonical member (i ↦ e₁, j ↦ e₂, both signs positive) agrees with the Cayley-Dickson embedding") {
            val canonical = OctonionAlgebras.allQuaternionEmbeddings().entries.single { (spec, _) ->
                spec.i == 1 && spec.j == 2 && spec.iSign == Sign.PLUS && spec.jSign == Sign.PLUS
            }.value

            quaternions.basis.forEach { q ->
                lawCheck(eq(canonical(q), OctonionAlgebras.QuaternionToOctonionMonomorphism(q)))
            }
        }
    })
})
