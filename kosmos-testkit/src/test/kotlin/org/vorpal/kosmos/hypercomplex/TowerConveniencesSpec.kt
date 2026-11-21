package org.vorpal.kosmos.hypercomplex

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.complex.ArbComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras.asQuaternion
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras.powInt
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.octonion.Octonion
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras.asOctonion
import org.vorpal.kosmos.hypercomplex.quaternion.ArbQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.AxisSignEmbedding
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplexAlgebras
import org.vorpal.kosmos.hypercomplex.splitcomplex.splitComplex
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * The small conveniences on the main tower `ℝ ⊂ ℂ ⊂ ℍ ⊂ 𝕆`: integer powers of complex numbers and
 * the `asQuaternion` / `asOctonion` extension functions.
 */
class TowerConveniencesSpec : FunSpec({
    lawTests(lawGraph("tower conveniences") {
        val complexField = ComplexAlgebras.ComplexField
        val eqC = ComplexAlgebras.eqComplex

        local("Complex.powInt agrees with repeated multiplication") {
            lawCheckAll(ArbComplex.unitCircleComplex, Arb.int(0..12)) { c, n ->
                val expected = (1..n).fold(complexField.one) { acc, _ -> complexField.mul(acc, c) }
                lawCheck(eqC(c.powInt(n), expected))
            }
        }

        local("Complex.powInt: c⁰ = 1, c¹ = c, i² = -1, i⁴ = 1") {
            lawCheckAll(ArbComplex.boundedComplex) { c ->
                c.powInt(0) shouldBe complexField.one
                lawCheck(eqC(c.powInt(1), c))
            }
            lawCheck(eqC(ComplexAlgebras.i.powInt(2), complex(-1.0, 0.0)))
            lawCheck(eqC(ComplexAlgebras.i.powInt(4), complexField.one))
        }

        local("Complex.powInt rejects negative exponents") {
            shouldThrow<IllegalArgumentException> { complex(1.0, 1.0).powInt(-1) }
        }

        local("Complex.asQuaternion is the chosen embedding, canonical by default") {
            lawCheckAll(ArbComplex.boundedComplex) { c ->
                c.asQuaternion() shouldBe quaternion(c.re, c.im, 0.0, 0.0)
                AxisSignEmbedding.all.forEach { emb ->
                    c.asQuaternion(emb) shouldBe ComplexAlgebras.complexToQuaternionEmbedding(emb)(c)
                }
            }
        }

        local("RealQuaternion.asOctonion is the canonical Cayley-Dickson embedding q ↦ (q, 0)") {
            lawCheckAll(ArbQuaternion.quaternion) { q ->
                q.asOctonion() shouldBe Octonion(CD(q.cd, QuaternionAlgebras.QuaternionDivisionRing.zero.cd))
                q.asOctonion() shouldBe OctonionAlgebras.QuaternionToOctonionMonomorphism(q)
            }
        }

        local("ℂ ↪ ℍ ↪ 𝕆 composes: (a + bi) ↦ a + b·e₁") {
            val e1 = OctonionAlgebras.OctonionDivisionAlgebra.e1
            val octonions = OctonionAlgebras.OctonionDivisionAlgebra
            lawCheck(OctonionAlgebras.eqOctonion(ComplexAlgebras.i.asQuaternion().asOctonion(), e1))
            lawCheck(OctonionAlgebras.eqOctonion(octonions.mul(e1, e1), octonions.negOne))
        }

        local("hyperbolicUnit is the element j of the split-complex ring over any base, with j² = 1") {
            val q = RationalAlgebras.RationalField
            val j = SplitComplexAlgebras.hyperbolicUnit(q)
            val ring = SplitComplexAlgebras.splitComplexRing(q)
            j shouldBe splitComplex(Rational.ZERO, Rational.ONE)
            ring.mul(j, j) shouldBe ring.one
            SplitComplexAlgebras.j shouldBe splitComplex(0.0, 1.0)
        }
    })
})
