package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplexAlgebras
import org.vorpal.kosmos.hypercomplex.splitcomplex.splitComplex
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Tests for the three conjugation norms of the bicomplex numbers and the Euclidean [normSq].
 *
 * The bicomplex units here are `i² = -1`, `j² = -1` (both imaginary) and `k = ij` with `k² = +1`
 * (hyperbolic). Each norm `N_t(w) = w · w^{†t}` lands in a distinct 2D plane:
 *
 *  - `normByConj1` (conjugate `i`)  → `ℂ(j)`, an *ordinary* complex line (`j² = -1`),
 *  - `normByConj2` (conjugate `j`)  → `ℂ(i)`, an *ordinary* complex line (`i² = -1`),
 *  - `normByConj3` (principal)      → `ℂ(k)`, a *split-complex* line (`k² = +1`),
 *
 * so `normByConj3` is the only one whose image is genuinely split-complex; it returns
 * `SplitComplex<Real>` directly.
 */
class BicomplexNormSpec : FunSpec({
    lawTests(lawGraph("bicomplex norm") {
        val ring = BicomplexAlgebras.BicomplexCommutativeRing
        val eqB = BicomplexAlgebras.eqBicomplex
        val eqC = ComplexAlgebras.eqComplex
        val eqR = RealAlgebras.eqRealApprox
        val eqSC = SplitComplexAlgebras.eqSplitComplex
        val one = ring.one

        local("unit squares: i² = -1, j² = -1, k² = +1, and ij = k") {
            val negOne = ring.add.inverse(one)
            lawCheck(eqB(ring.mul(ring.i, ring.i), negOne))
            lawCheck(eqB(ring.mul(ring.j, ring.j), negOne))
            lawCheck(eqB(ring.mul(ring.k, ring.k), one))
            lawCheck(eqB(ring.mul(ring.i, ring.j), ring.k))
        }

        local("normByConj1 lands in ℂ(j): N1(w) = (a²+b²-c²-d²) + 2(ac+bd)j, annihilating i and k") {
            lawCheckAll(ArbReal.smallReal, ArbReal.smallReal, ArbReal.smallReal, ArbReal.smallReal) { a, b, c, d ->
                val w = Bicomplex.ofStandard(a, b, c, d)
                val expected = Bicomplex.ofStandard(a * a + b * b - c * c - d * d, 0.0, 2 * (a * c + b * d), 0.0)
                lawCheck(eqB(ring.normByConj1(w), expected))
            }
        }

        local("normByConj2 returns a canonical Complex in ℂ(i): N2(w) = (a²-b²+c²-d²) + 2(ab+cd)i = 𝛂𝜷") {
            lawCheckAll(ArbReal.smallReal, ArbReal.smallReal, ArbReal.smallReal, ArbReal.smallReal) { a, b, c, d ->
                val w = Bicomplex.ofStandard(a, b, c, d)
                val expected = complex(a * a - b * b + c * c - d * d, 2 * (a * b + c * d))
                lawCheck(eqC(ring.normByConj2(w), expected))
                // normByConj2 is exactly the product of the two idempotent projections, αβ.
                lawCheck(eqC(ring.normByConj2(w), ComplexAlgebras.ComplexField.mul(w.alpha, w.beta)))
            }
        }

        local("normByConj3 lands in the split-complex plane ℂ(k): N3(w) = (a²+b²+c²+d²) + 2(ad-bc)·j_split") {
            lawCheckAll(ArbReal.smallReal, ArbReal.smallReal, ArbReal.smallReal, ArbReal.smallReal) { a, b, c, d ->
                val w = Bicomplex.ofStandard(a, b, c, d)
                val expected = splitComplex(
                    re = a * a + b * b + c * c + d * d,
                    hy = 2 * (a * d - b * c)
                )
                lawCheck(eqSC(ring.normByConj3(w), expected))
            }
        }

        local("normSq is the Euclidean norm and equals the real part of normByConj3") {
            lawCheckAll(ArbReal.smallReal, ArbReal.smallReal, ArbReal.smallReal, ArbReal.smallReal) { a, b, c, d ->
                val w = Bicomplex.ofStandard(a, b, c, d)
                lawCheck(eqR(ring.normSq(w), a * a + b * b + c * c + d * d))
            }
        }
    })
})
