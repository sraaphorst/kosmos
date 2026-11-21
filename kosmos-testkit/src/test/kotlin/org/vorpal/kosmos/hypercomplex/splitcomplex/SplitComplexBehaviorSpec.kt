package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Bespoke split-complex tests that go beyond the abstract law suites:
 * - the carrier identity `j² = 1`,
 * - the explicit conjugation and norm formulas,
 * - witnesses that the ring is not an integral domain (zero divisors on the null cone),
 * - the canonical 2×2 matrix images of `1` and `j`.
 */
class SplitComplexBehaviorSpec : FunSpec({
    lawTests(lawGraph("split-complex behaviour") {
        val ring = SplitComplexAlgebras.RealSplitComplexRing
        val starAlg = SplitComplexAlgebras.RealSplitComplexStarAlgebra
        val eqSC = SplitComplexAlgebras.eqSplitComplex
        val eqR = RealAlgebras.eqRealApprox
        val normSq = SplitComplexAlgebras.splitComplexNormSq

        val j = SplitComplexAlgebras.j

        local("j squared equals one") {
            lawCheck(eqSC(ring.mul(j, j), ring.one))
        }

        local("conjugation formula: conj(a + bj) = a - bj") {
            lawCheckAll(ArbSplitComplex.boundedSplitComplex) { z ->
                val c = starAlg.conj(z)
                lawCheck(eqR(c.re, z.re))
                lawCheck(eqR(c.hy, -z.hy))
            }
        }

        local("conjugation is involutive") {
            lawCheckAll(ArbSplitComplex.boundedSplitComplex) { z ->
                lawCheck(eqSC(starAlg.conj(starAlg.conj(z)), z))
            }
        }

        local("normSq formula: N(a + bj) = a² − b²") {
            lawCheckAll(ArbSplitComplex.boundedSplitComplex) { z ->
                lawCheck(eqR(normSq(z), z.re * z.re - z.hy * z.hy))
            }
        }

        local("normSq factors via conjugation: z * conj(z) = N(z) + 0j") {
            lawCheckAll(ArbSplitComplex.boundedSplitComplex) { z ->
                val product = ring.mul(z, starAlg.conj(z))
                lawCheck(eqR(product.re, normSq(z)))
                lawCheck(eqR(product.hy, 0.0))
            }
        }

        local("(1 + j)(1 − j) = 0 — zero divisors on the null cone") {
            val plus = splitComplex(re = 1.0, hy = 1.0)
            val minus = splitComplex(re = 1.0, hy = -1.0)
            lawCheck(eqSC(ring.mul(plus, minus), ring.zero))
        }

        local("elements on the null cone have N(z) = 0 and are not units") {
            lawCheckAll(ArbReal.fieldReal) { a ->
                val zPlus = splitComplex(re = a, hy = a)
                val zMinus = splitComplex(re = a, hy = -a)
                lawCheck(eqR(normSq(zPlus), 0.0))
                lawCheck(eqR(normSq(zMinus), 0.0))
            }
        }

        local("scalar action is componentwise: r ⊳ (a + bj) = (ra) + (rb)j") {
            val action = starAlg.leftAction
            lawCheckAll(ArbReal.smallReal, ArbSplitComplex.smallSplitComplex) { r, z ->
                val acted = action(r, z)
                lawCheck(eqR(acted.re, r * z.re))
                lawCheck(eqR(acted.hy, r * z.hy))
            }
        }

        local("scalar embedding: r ↦ r + 0j") {
            val embed = SplitComplexAlgebras.scalarEmbedding(RealAlgebras.RealField)
            lawCheckAll(ArbReal.fieldReal) { r ->
                val z = embed(r)
                lawCheck(eqR(z.re, r))
                lawCheck(eqR(z.hy, 0.0))
            }
        }

        local("matrix embedding: 1 ↦ I, j ↦ [[0,1],[1,0]]") {
            val mono = SplitComplexAlgebras.splitComplexToRank2MatrixMonomorphism(
                RealAlgebras.RealField
            )

            val mOne = mono(ring.one)
            val mJ = mono(j)

            lawCheck(eqR(mOne[0, 0], 1.0))
            lawCheck(eqR(mOne[0, 1], 0.0))
            lawCheck(eqR(mOne[1, 0], 0.0))
            lawCheck(eqR(mOne[1, 1], 1.0))

            lawCheck(eqR(mJ[0, 0], 0.0))
            lawCheck(eqR(mJ[0, 1], 1.0))
            lawCheck(eqR(mJ[1, 0], 1.0))
            lawCheck(eqR(mJ[1, 1], 0.0))
        }
    })
})
