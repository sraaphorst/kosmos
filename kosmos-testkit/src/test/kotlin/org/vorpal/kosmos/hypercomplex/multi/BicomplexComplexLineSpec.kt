package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ArbComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

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
class BicomplexComplexLineSpec : FunSpec({
    lawTests(lawGraph("bicomplex complex lines") {
        val ring = BicomplexAlgebras.BicomplexCommutativeRing
        val complexField = ComplexAlgebras.ComplexField
        val complexStar = ComplexAlgebras.ComplexStarAlgebra
        val embedJ = BicomplexAlgebras.complexToBicomplexAlongJ
        val eqB = BicomplexAlgebras.eqBicomplex
        val eqC = ComplexAlgebras.eqComplex
        val eqR = RealAlgebras.eqRealApprox

        local("complexToBicomplexAlongJ sends 1 ↦ 1 and i ↦ j") {
            lawCheck(eqB(embedJ(complexField.one), ring.one))
            lawCheck(eqB(embedJ(complex(0.0, 1.0)), ring.j))
        }

        local("complexToBicomplexAlongJ lands in the ℂ(j) line: a + bi ↦ a + bj (no i or k part)") {
            lawCheckAll(ArbComplex.boundedComplex) { c ->
                val coeffs = embedJ(c).coefficients()   // basis order [1, i, j, k]
                lawCheck(eqR(coeffs[0], c.re))   // real part on 1
                lawCheck(eqR(coeffs[1], 0.0))   // no i
                lawCheck(eqR(coeffs[2], c.im))   // imaginary part on j
                lawCheck(eqR(coeffs[3], 0.0))   // no k
            }
        }

        local("complexToBicomplexAlongJ is multiplicative (a ring homomorphism)") {
            lawCheckAll(ArbComplex.boundedComplex, ArbComplex.boundedComplex) { c1, c2 ->
                val lhs = embedJ(complexField.mul(c1, c2))
                val rhs = ring.mul(embedJ(c1), embedJ(c2))
                lawCheck(eqB(lhs, rhs))
            }
        }

        local("cjToComplex ∘ complexToBicomplexAlongJ = identity on ℂ") {
            lawCheckAll(ArbComplex.boundedComplex) { c ->
                lawCheck(eqC(BicomplexAlgebras.cjToComplex(embedJ(c)), c))
            }
        }

        local("complexToBicomplexAlongJ ∘ cjToComplex = identity on the ℂ(j) line") {
            lawCheckAll(ArbReal.smallReal, ArbReal.smallReal) { a, b ->
                val zCj = Bicomplex.ofStandard(a, 0.0, b, 0.0)   // a + bj  ∈ ℂ(j)
                lawCheck(eqB(embedJ(BicomplexAlgebras.cjToComplex(zCj)), zCj))
            }
        }

        local("normByConj1ToCanonical = cjToComplex ∘ normByConj1 = 𝛂·conj(𝜷) = (a²+b²-c²-d²) + 2(ac+bd)i") {
            lawCheckAll(ArbReal.smallReal, ArbReal.smallReal, ArbReal.smallReal, ArbReal.smallReal) { a, b, c, d ->
                val w = Bicomplex.ofStandard(a, b, c, d)
                val viaComposite = BicomplexAlgebras.normByConj1ToCanonical(w)
                val viaNorm = BicomplexAlgebras.cjToComplex(ring.normByConj1(w))
                val viaProjections = complexStar.mul(w.alpha, complexStar.conj(w.beta))   // 𝛂·𝜷̄
                val expected = complex(a * a + b * b - c * c - d * d, 2 * (a * c + b * d))
                lawCheck(eqC(viaComposite, viaNorm))
                lawCheck(eqC(viaComposite, viaProjections))
                lawCheck(eqC(viaComposite, expected))
            }
        }
    })
})
