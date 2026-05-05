package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras

/**
 * Bespoke split-complex tests that go beyond the abstract law suites:
 * - the carrier identity `j² = 1`,
 * - the explicit conjugation and norm formulas,
 * - witnesses that the ring is not an integral domain (zero divisors on the null cone),
 * - the canonical 2×2 matrix images of `1` and `j`.
 */
object SplitComplexBehaviorSpec : StringSpec({
    val ring = SplitComplexAlgebras.RealSplitComplexRing
    val starAlg = SplitComplexAlgebras.RealSplitComplexStarAlgebra
    val eqSC = SplitComplexAlgebras.eqSplitComplex
    val eqR = RealAlgebras.eqRealApprox
    val normSq = SplitComplexAlgebras.splitComplexNormSq

    val j = SplitComplexAlgebras.j

    "j squared equals one" {
        check(eqSC(ring.mul(j, j), ring.one))
    }

    "conjugation formula: conj(a + bj) = a - bj" {
        checkAll(ArbSplitComplex.boundedSplitComplex) { z ->
            val c = starAlg.conj(z)
            check(eqR(c.re, z.re))
            check(eqR(c.hy, -z.hy))
        }
    }

    "conjugation is involutive" {
        checkAll(ArbSplitComplex.boundedSplitComplex) { z ->
            check(eqSC(starAlg.conj(starAlg.conj(z)), z))
        }
    }

    "normSq formula: N(a + bj) = a² − b²" {
        checkAll(ArbSplitComplex.boundedSplitComplex) { z ->
            check(eqR(normSq(z), z.re * z.re - z.hy * z.hy))
        }
    }

    "normSq factors via conjugation: z * conj(z) = N(z) + 0j" {
        checkAll(ArbSplitComplex.boundedSplitComplex) { z ->
            val product = ring.mul(z, starAlg.conj(z))
            check(eqR(product.re, normSq(z)))
            check(eqR(product.hy, 0.0))
        }
    }

    "(1 + j)(1 − j) = 0 — zero divisors on the null cone" {
        val plus = splitComplex(re = 1.0, hy = 1.0)
        val minus = splitComplex(re = 1.0, hy = -1.0)
        check(eqSC(ring.mul(plus, minus), ring.zero))
    }

    "elements on the null cone have N(z) = 0 and are not units" {
        checkAll(ArbReal.fieldReal) { a ->
            val zPlus = splitComplex(re = a, hy = a)
            val zMinus = splitComplex(re = a, hy = -a)
            check(eqR(normSq(zPlus), 0.0))
            check(eqR(normSq(zMinus), 0.0))
        }
    }

    "scalar action is componentwise: r ⊳ (a + bj) = (ra) + (rb)j" {
        val action = starAlg.leftAction
        checkAll(ArbReal.smallReal, ArbSplitComplex.smallSplitComplex) { r, z ->
            val acted = action(r, z)
            check(eqR(acted.re, r * z.re))
            check(eqR(acted.hy, r * z.hy))
        }
    }

    "scalar embedding: r ↦ r + 0j" {
        val embed = SplitComplexAlgebras.scalarEmbedding(RealAlgebras.RealField)
        checkAll(ArbReal.fieldReal) { r ->
            val z = embed(r)
            check(eqR(z.re, r))
            check(eqR(z.hy, 0.0))
        }
    }

    "matrix embedding: 1 ↦ I, j ↦ [[0,1],[1,0]]" {
        val mono = SplitComplexAlgebras.splitComplexToRank2MatrixMonomorphism(
            RealAlgebras.RealField
        )

        val mOne = mono(ring.one)
        val mJ = mono(j)

        check(eqR(mOne[0, 0], 1.0))
        check(eqR(mOne[0, 1], 0.0))
        check(eqR(mOne[1, 0], 0.0))
        check(eqR(mOne[1, 1], 1.0))

        check(eqR(mJ[0, 0], 0.0))
        check(eqR(mJ[0, 1], 1.0))
        check(eqR(mJ[1, 0], 1.0))
        check(eqR(mJ[1, 1], 0.0))
    }
})
