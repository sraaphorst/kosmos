package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ArbComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws

/**
 * Law-based tests for the structures [BicomplexAlgebras] builds on top of the Cayley-Dickson ring:
 * - the three `*`-algebras, one per involution;
 * - the real and complex vector space structures;
 * - the idempotent projections `π_𝛂, π_𝜷 : 𝔹 → ℂ` as unital ring homomorphisms;
 * - reciprocals of units, and the Cayley-Dickson formula `w⁻¹ = w†2 · N2(w)⁻¹`.
 */
class BicomplexStructuresSpec : StringSpec({
    val ring = BicomplexAlgebras.BicomplexCommutativeRing
    val complexField = ComplexAlgebras.ComplexField
    val complexStar = ComplexAlgebras.ComplexStarAlgebra
    val eqB = BicomplexAlgebras.eqBicomplex
    val prB = BicomplexAlgebras.printableBicomplexPretty
    val eqC = ComplexAlgebras.eqComplex
    val prC = ComplexAlgebras.printableComplexPretty
    val eqR = RealAlgebras.eqRealApprox
    val prR = RealAlgebras.printableRealPretty

    listOf(
        "conj1 (conjugate i)" to BicomplexAlgebras.BicomplexStarAlgebra1,
        "conj2 (conjugate j, Cayley-Dickson)" to BicomplexAlgebras.BicomplexStarAlgebra2,
        "conj3 (principal)" to BicomplexAlgebras.BicomplexStarAlgebraPrincipal
    ).forEach { (label, algebra) ->
        "BicomplexStarAlgebra for $label satisfies StarAlgebraLaws" {
            StarAlgebraLaws(
                algebra = algebra,
                scalarArb = ArbReal.smallReal,
                algebraArb = ArbBicomplex.bicomplex,
                eqR = eqR,
                eqA = eqB,
                prR = prR,
                prA = prB
            ).fullTest().throwIfFailed()
        }
    }

    "conjugations act on idempotent coordinates as (𝛂,𝜷) ↦ (𝜷*,𝛂*), (𝜷,𝛂), (𝛂*,𝜷*)" {
        checkAll(ArbBicomplex.bicomplex) { z ->
            val c1 = ring.conj1(z)
            val c2 = ring.conj2(z)
            val c3 = ring.conj3(z)

            check(eqC(c1.alpha, complexStar.conj(z.beta)) && eqC(c1.beta, complexStar.conj(z.alpha)))
            check(eqC(c2.alpha, z.beta) && eqC(c2.beta, z.alpha))
            check(eqC(c3.alpha, complexStar.conj(z.alpha)) && eqC(c3.beta, complexStar.conj(z.beta)))
        }
    }

    "the three conjugations commute and conj3 = conj1 ∘ conj2" {
        checkAll(ArbBicomplex.bicomplex) { z ->
            check(eqB(ring.conj1(ring.conj2(z)), ring.conj2(ring.conj1(z))))
            check(eqB(ring.conj3(z), ring.conj1(ring.conj2(z))))
        }
    }

    "BicomplexRealVectorSpace satisfies VectorSpaceLaws" {
        VectorSpaceLaws(
            space = BicomplexAlgebras.BicomplexRealVectorSpace,
            scalarArb = ArbReal.smallReal,
            vectorArb = ArbBicomplex.bicomplex,
            eqF = eqR,
            eqV = eqB,
            prF = prR,
            prV = prB
        ).fullTest().throwIfFailed()
    }

    "BicomplexComplexVectorSpace satisfies VectorSpaceLaws" {
        VectorSpaceLaws(
            space = BicomplexAlgebras.BicomplexComplexVectorSpace,
            scalarArb = ArbComplex.boundedComplex,
            vectorArb = ArbBicomplex.bicomplex,
            eqF = eqC,
            eqV = eqB,
            prF = prC,
            prV = prB
        ).fullTest().throwIfFailed()
    }

    "the complex scalar action is multiplication through the embedding ℂ ↪ 𝔹" {
        checkAll(ArbComplex.boundedComplex, ArbBicomplex.bicomplex) { c, z ->
            val acted = BicomplexAlgebras.BicomplexComplexVectorSpace.leftAction(c, z)
            val multiplied = ring.mul(BicomplexAlgebras.complexToBicomplexMonomorphism(c), z)
            check(eqB(acted, multiplied))
        }
    }

    "complexToBicomplexMonomorphism satisfies RingHomomorphismLaws" {
        RingHomomorphismLaws(
            hom = BicomplexAlgebras.complexToBicomplexMonomorphism::invoke,
            domain = complexField,
            codomain = ring,
            arb = ArbComplex.boundedComplex,
            eqB = eqB,
            prA = prC,
            prB = prB
        ).fullTest().throwIfFailed()
    }

    listOf(
        "π_𝛂" to BicomplexAlgebras.BicomplexFirstProjectionHomomorphism,
        "π_𝜷" to BicomplexAlgebras.BicomplexSecondProjectionHomomorphism
    ).forEach { (label, projection) ->
        "the idempotent projection $label satisfies RingHomomorphismLaws" {
            RingHomomorphismLaws(
                hom = projection::invoke,
                domain = ring,
                codomain = complexField,
                arb = ArbBicomplex.bicomplex,
                eqB = eqC,
                prA = prB,
                prB = prC
            ).fullTest().throwIfFailed()
        }
    }

    "units have reciprocals: w · w⁻¹ = 1" {
        checkAll(ArbBicomplex.unitBicomplex) { w ->
            check(ring.hasReciprocal(w))
            check(eqB(ring.mul(w, ring.reciprocal(w)), ring.one))
        }
    }

    "the reciprocal is the Cayley-Dickson formula w⁻¹ = w†2 · N2(w)⁻¹ and is componentwise in idempotent coordinates" {
        checkAll(ArbBicomplex.unitBicomplex) { w ->
            val inv = ring.reciprocal(w)
            val viaNorm = BicomplexAlgebras.BicomplexComplexVectorSpace.leftAction(
                complexField.reciprocal(ring.normByConj2(w)),
                ring.conj2(w)
            )
            check(eqB(inv, viaNorm))
            check(eqC(inv.alpha, complexField.reciprocal(w.alpha)))
            check(eqC(inv.beta, complexField.reciprocal(w.beta)))
        }
    }

    "nonzero zero divisors have no reciprocal and are annihilated by a nonzero element" {
        checkAll(ArbBicomplex.zeroDivisorBicomplex) { z ->
            check(z.isZeroDivisor())
            check(!ring.hasReciprocal(z))

            // If 𝛂 = 0 then e₂-part is the only survivor, so multiplying by e₁ = (1, 0) kills it, and vice versa.
            val annihilator =
                if (eqC(z.alpha, complexField.zero)) Bicomplex.ofIdempotent(complexField.one, complexField.zero)
                else Bicomplex.ofIdempotent(complexField.zero, complexField.one)
            check(eqB(ring.mul(z, annihilator), ring.zero))
        }
    }
})
