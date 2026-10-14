package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.structures.CD
import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.hypercomplex.complex.ArbComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw

/**
 * The quaternion multiplication table and the six unital embeddings `ℂ ↪ ℍ`.
 *
 * These were previously run as a `main()` in `HypercomplexSanityChecks`; as specs they now run with
 * the rest of the suite.
 */
class QuaternionBasisSpec : StringSpec({
    val ring = QuaternionAlgebras.QuaternionDivisionRing
    val eq = QuaternionAlgebras.eqQuaternionStrict
    val pr = QuaternionAlgebras.printableQuaternionPretty

    "the Cayley-Dickson basis of ℍ = CD⟨ℂ⟩ is 1 = (1, 0), i = (i, 0), j = (0, 1), k = (0, i)" {
        val zero = ComplexAlgebras.ComplexField.zero
        val one = ComplexAlgebras.ComplexField.one
        val i = ComplexAlgebras.i
        check(ring.basis == listOf(CD(one, zero), CD(i, zero), CD(zero, one), CD(zero, i)).map { (a, b) -> Quaternion(CD(a.cd, b.cd)) })
    }

    "Hamilton's relations: i² = j² = k² = -1, ij = k = -ji, jk = i = -kj, ki = j = -ik" {
        val (_, i, j, k) = ring.basis
        val negOne = ring.negOne
        fun neg(q: RealQuaternion): RealQuaternion = ring.add.inverse(q)

        listOf(i, j, k).forEach { u -> check(eq(ring.mul(u, u), negOne)) { "${pr(u)}² != -1" } }
        check(eq(ring.mul(i, j), k) && eq(ring.mul(j, i), neg(k)))
        check(eq(ring.mul(j, k), i) && eq(ring.mul(k, j), neg(i)))
        check(eq(ring.mul(k, i), j) && eq(ring.mul(i, k), neg(j)))
    }

    AxisSignEmbeddings.AxisSignEmbedding.all.forEach { embedding ->
        val phi = ComplexAlgebras.complexToQuaternionEmbedding(embedding)

        "ℂ ↪ ℍ along $embedding sends i to the chosen signed axis" {
            val (_, i, j, k) = ring.basis
            val axis = when (embedding.axis) {
                AxisSignEmbeddings.ImagAxis.I -> i
                AxisSignEmbeddings.ImagAxis.J -> j
                AxisSignEmbeddings.ImagAxis.K -> k
            }
            val expected = when (embedding.sign) {
                AxisSignEmbeddings.Sign.PLUS -> axis
                AxisSignEmbeddings.Sign.MINUS -> ring.add.inverse(axis)
            }
            check(eq(phi(ComplexAlgebras.i), expected))
        }

        "ℂ ↪ ℍ along $embedding is an injective unital ring homomorphism" {
            RingHomomorphismLaws(
                hom = phi::invoke,
                domain = ComplexAlgebras.ComplexField,
                codomain = ring,
                arb = ArbComplex.boundedComplex,
                eqB = QuaternionAlgebras.eqQuaternion,
                prA = ComplexAlgebras.printableComplexPretty,
                prB = pr
            ).fullTest().throwIfFailed()

            injectivityLaw(
                hom = phi::invoke,
                arbA = ArbComplex.boundedComplex,
                eqA = ComplexAlgebras.eqComplex,
                eqB = QuaternionAlgebras.eqQuaternion,
                prA = ComplexAlgebras.printableComplexPretty,
                prB = pr
            ).test()
        }

        "ℍ is a left ℂ-vector space through the embedding along $embedding" {
            VectorSpaceLaws(
                space = QuaternionAlgebras.quaternionLeftComplexVectorSpace(embedding),
                scalarArb = ArbComplex.boundedComplex,
                vectorArb = ArbQuaternion.quaternion,
                eqF = ComplexAlgebras.eqComplex,
                eqV = QuaternionAlgebras.eqQuaternion,
                prF = ComplexAlgebras.printableComplexPretty,
                prV = pr
            ).fullTest().throwIfFailed()
        }
    }
})
