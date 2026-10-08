package org.vorpal.kosmos.hypercomplex

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.hypercomplex.complex.ArbComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras.asQuaternion
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras.powInt
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras.asOctonion
import org.vorpal.kosmos.hypercomplex.quaternion.ArbQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.AxisSignEmbeddings.AxisSignEmbedding
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplexAlgebras
import org.vorpal.kosmos.hypercomplex.splitcomplex.splitComplex
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational

/**
 * The small conveniences on the main tower `ℝ ⊂ ℂ ⊂ ℍ ⊂ 𝕆`: integer powers of complex numbers and
 * the `asQuaternion` / `asOctonion` extension functions.
 */
class TowerConveniencesSpec : StringSpec({
    val complexField = ComplexAlgebras.ComplexField
    val eqC = ComplexAlgebras.eqComplex

    "Complex.powInt agrees with repeated multiplication" {
        checkAll(ArbComplex.unitCircleComplex, Arb.int(0..12)) { c, n ->
            val expected = (1..n).fold(complexField.one) { acc, _ -> complexField.mul(acc, c) }
            check(eqC(c.powInt(n), expected))
        }
    }

    "Complex.powInt: c⁰ = 1, c¹ = c, i² = -1, i⁴ = 1" {
        checkAll(ArbComplex.boundedComplex) { c ->
            c.powInt(0) shouldBe complexField.one
            check(eqC(c.powInt(1), c))
        }
        check(eqC(ComplexAlgebras.i.powInt(2), complex(-1.0, 0.0)))
        check(eqC(ComplexAlgebras.i.powInt(4), complexField.one))
    }

    "Complex.powInt rejects negative exponents" {
        shouldThrow<IllegalArgumentException> { complex(1.0, 1.0).powInt(-1) }
    }

    "Complex.asQuaternion is the chosen embedding, canonical by default" {
        checkAll(ArbComplex.boundedComplex) { c ->
            c.asQuaternion() shouldBe CD(c, complexField.zero)
            AxisSignEmbedding.all.forEach { emb ->
                c.asQuaternion(emb) shouldBe ComplexAlgebras.complexToQuaternionEmbedding(emb)(c)
            }
        }
    }

    "Quaternion.asOctonion is the canonical Cayley-Dickson embedding q ↦ (q, 0)" {
        checkAll(ArbQuaternion.quaternion) { q ->
            q.asOctonion() shouldBe CD(q, QuaternionAlgebras.QuaternionDivisionRing.zero)
            q.asOctonion() shouldBe OctonionAlgebras.QuaternionToOctonionMonomorphism(q)
        }
    }

    "ℂ ↪ ℍ ↪ 𝕆 composes: (a + bi) ↦ a + b·e₁" {
        val e1 = OctonionAlgebras.OctonionDivisionAlgebra.e1
        val octonions = OctonionAlgebras.OctonionDivisionAlgebra
        check(OctonionAlgebras.eqOctonion(ComplexAlgebras.i.asQuaternion().asOctonion(), e1))
        check(OctonionAlgebras.eqOctonion(octonions.mul(e1, e1), octonions.negOne))
    }

    "hyperbolicUnit is the element j of the split-complex ring over any base, with j² = 1" {
        val q = RationalAlgebras.RationalField
        val j = SplitComplexAlgebras.hyperbolicUnit(q)
        val ring = SplitComplexAlgebras.splitComplexRing(q)
        j shouldBe splitComplex(Rational.ZERO, Rational.ONE)
        ring.mul(j, j) shouldBe ring.one
        SplitComplexAlgebras.j shouldBe splitComplex(0.0, 1.0)
    }
})
