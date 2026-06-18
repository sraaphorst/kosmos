package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ArbComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex

/**
 * Tests for the canonical identification of the `ℂ(j)` line of `𝔹` with the base field `ℂ(i)`
 * via `j ↦ i`:
 *  - [BicomplexAlgebras.complexToBicomplexAlongJ]  (`ℂ ↪ ℂ(j)`, `a + bi ↦ a + bj`),
 *  - [BicomplexAlgebras.cjToComplex]               (`ℂ(j) → ℂ`, `a + bj ↦ a + bi`),
 *  - [BicomplexAlgebras.normByConj1ToCanonical]    (`normByConj1` then `cjToComplex`).
 *
 * `complexToBicomplexAlongJ` is a ring monomorphism (`j² = -1`, just like `i²`); `cjToComplex` is
 * its left inverse and an isomorphism on the `ℂ(j)` line.
 */
class BicomplexComplexLineSpec : StringSpec({
    val ring = BicomplexAlgebras.BicomplexCommutativeRing
    val complexField = ComplexAlgebras.ComplexField
    val complexStar = ComplexAlgebras.ComplexStarAlgebra
    val embedJ = BicomplexAlgebras.complexToBicomplexAlongJ
    val eqB = BicomplexAlgebras.eqBicomplex
    val eqC = ComplexAlgebras.eqComplex
    val eqR = RealAlgebras.eqRealApprox

    "complexToBicomplexAlongJ sends 1 ↦ 1 and i ↦ j" {
        check(eqB(embedJ(complexField.one), ring.one))
        check(eqB(embedJ(complex(0.0, 1.0)), ring.j))
    }

    "complexToBicomplexAlongJ lands in the ℂ(j) line: a + bi ↦ a + bj (no i or k part)" {
        checkAll(ArbComplex.boundedComplex) { c ->
            val coeffs = embedJ(c).coefficients()   // basis order [1, i, j, k]
            check(eqR(coeffs[0], c.a))   // real part on 1
            check(eqR(coeffs[1], 0.0))   // no i
            check(eqR(coeffs[2], c.b))   // imaginary part on j
            check(eqR(coeffs[3], 0.0))   // no k
        }
    }

    "complexToBicomplexAlongJ is multiplicative (a ring homomorphism)" {
        checkAll(ArbComplex.boundedComplex, ArbComplex.boundedComplex) { c1, c2 ->
            val lhs = embedJ(complexField.mul(c1, c2))
            val rhs = ring.mul(embedJ(c1), embedJ(c2))
            check(eqB(lhs, rhs))
        }
    }

    "cjToComplex ∘ complexToBicomplexAlongJ = identity on ℂ" {
        checkAll(ArbComplex.boundedComplex) { c ->
            check(eqC(BicomplexAlgebras.cjToComplex(embedJ(c)), c))
        }
    }

    "complexToBicomplexAlongJ ∘ cjToComplex = identity on the ℂ(j) line" {
        checkAll(ArbReal.smallReal, ArbReal.smallReal) { a, b ->
            val zCj = Bicomplex.ofStandard(a, 0.0, b, 0.0)   // a + bj  ∈ ℂ(j)
            check(eqB(embedJ(BicomplexAlgebras.cjToComplex(zCj)), zCj))
        }
    }

    "normByConj1ToCanonical = cjToComplex ∘ normByConj1 = 𝛂·conj(𝜷) = (a²+b²-c²-d²) + 2(ac+bd)i" {
        checkAll(ArbReal.smallReal, ArbReal.smallReal, ArbReal.smallReal, ArbReal.smallReal) { a, b, c, d ->
            val w = Bicomplex.ofStandard(a, b, c, d)
            val viaComposite = BicomplexAlgebras.normByConj1ToCanonical(w)
            val viaNorm = BicomplexAlgebras.cjToComplex(ring.normByConj1(w))
            val viaProjections = complexStar.mul(w.alpha, complexStar.conj(w.beta))   // 𝛂·𝜷̄
            val expected = complex(a * a + b * b - c * c - d * d, 2 * (a * c + b * d))
            check(eqC(viaComposite, viaNorm))
            check(eqC(viaComposite, viaProjections))
            check(eqC(viaComposite, expected))
        }
    }
})
