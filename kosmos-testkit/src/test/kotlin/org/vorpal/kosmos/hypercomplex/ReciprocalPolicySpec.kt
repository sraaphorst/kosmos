package org.vorpal.kosmos.hypercomplex

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.HasReciprocal
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.dual.DualAlgebras
import org.vorpal.kosmos.hypercomplex.dual.dual
import org.vorpal.kosmos.hypercomplex.multi.Bicomplex
import org.vorpal.kosmos.hypercomplex.multi.BicomplexAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.octonion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplexAlgebras
import org.vorpal.kosmos.hypercomplex.splitcomplex.splitComplex
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.bioctonion.BioctonionAlgebras
import org.vorpal.kosmos.hypercomplex.bioctonion.bioctonion
import org.vorpal.kosmos.hypercomplex.biquaternion.BiquaternionAlgebras
import org.vorpal.kosmos.hypercomplex.biquaternion.biquaternion
import org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonionAlgebras
import org.vorpal.kosmos.hypercomplex.splitoctonion.splitOctonion
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.splitquaternion.splitQuaternion
import org.vorpal.kosmos.numberfields.quadratic.GaussianRat
import org.vorpal.kosmos.numberfields.quadratic.GaussianRatAlgebras

/**
 * The reciprocal contract of [HasReciprocal], checked uniformly across the hypercomplex package.
 *
 * For every structure:
 * - [HasReciprocal.hasReciprocal] and [HasReciprocal.reciprocal] agree: an element is inverted without
 *   throwing iff the predicate accepts it, and a rejected element throws [ArithmeticException];
 * - [HasReciprocal.reciprocalOption] agrees with both.
 *
 * For the floating-point structures, the boundary cases are exercised explicitly: zero, subnormal and
 * tiny values, `NaN`, `±∞`, and overflow-scale values, all of which must be rejected rather than
 * producing a non-finite "inverse". Structures with zero divisors must also reject the null cone.
 *
 * The base fields ℝ, ℚ and ℚ(i) are included: every hypercomplex reciprocal ultimately divides in one
 * of them, so they must honour the same contract (`𝔽_p` is covered by `PrimeFieldSpec`).
 */
class ReciprocalPolicySpec : StringSpec({

    /** Doubles biased heavily toward the boundary cases. */
    val boundaryReal: Arb<Real> = Arb.choice(
        Arb.element(
            0.0, -0.0, Double.MIN_VALUE, -Double.MIN_VALUE, 1e-300, -1e-300, 1e-160, 1e-5,
            Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
            1e160, 1e300, Double.MAX_VALUE, -Double.MAX_VALUE
        ),
        Arb.double(-10.0, 10.0)
    )

    /**
     * Check the [HasReciprocal] contract on one element: predicate, reciprocal and option agree, and an
     * accepted element inverts to something whose coordinates are finite.
     */
    fun <A : Any> checkContract(
        structure: HasReciprocal<A>,
        a: A,
        isFinite: UnaryOp<A, Boolean>
    ) {
        if (structure.hasReciprocal(a)) {
            val inverse = structure.reciprocal(a)
            check(isFinite(inverse)) { "reciprocal($a) = $inverse is not finite" }
            check(structure.reciprocalOption(a) is Option.Some) { "reciprocalOption($a) should be Some" }
        } else {
            shouldThrow<ArithmeticException> { structure.reciprocal(a) }
            structure.reciprocalOption(a) shouldBe Option.None
        }
    }

    fun allFinite(xs: List<Real>): Boolean = xs.all(Real::isFinite)

    "ℂ: hasReciprocal, reciprocal and reciprocalOption agree, including at 0, tiny, NaN and ∞" {
        checkAll(boundaryReal, boundaryReal) { a, b ->
            checkContract(ComplexAlgebras.ComplexField, complex(a, b), UnaryOp { c -> allFinite(listOf(c.a, c.b)) })
        }
        ComplexAlgebras.ComplexField.hasReciprocal(complex(0.0, 0.0)) shouldBe false
        ComplexAlgebras.ComplexField.hasReciprocal(complex(Double.NaN, 1.0)) shouldBe false
        ComplexAlgebras.ComplexField.hasReciprocal(complex(Double.POSITIVE_INFINITY, 0.0)) shouldBe false
        ComplexAlgebras.ComplexField.hasReciprocal(complex(Double.MIN_VALUE, 0.0)) shouldBe false
        ComplexAlgebras.ComplexStarAlgebra.hasReciprocal(complex(Double.MIN_VALUE, 0.0)) shouldBe false
    }

    "ℍ: hasReciprocal, reciprocal and reciprocalOption agree at the boundary" {
        checkAll(boundaryReal, boundaryReal, boundaryReal, boundaryReal) { w, x, y, z ->
            checkContract(QuaternionAlgebras.QuaternionDivisionRing, quaternion(w, x, y, z), UnaryOp { q ->
                allFinite(listOf(q.a.a, q.a.b, q.b.a, q.b.b))
            })
        }
        QuaternionAlgebras.QuaternionDivisionRing.hasReciprocal(quaternion(1e-300, 0.0, 0.0, 0.0)) shouldBe false
    }

    "𝕆: hasReciprocal, reciprocal and reciprocalOption agree at the boundary" {
        checkAll(boundaryReal, boundaryReal, boundaryReal, boundaryReal) { w, x, u, t ->
            val o = octonion(w, x, 0.0, 0.0, u, 0.0, 0.0, t)
            checkContract(OctonionAlgebras.OctonionDivisionAlgebra, o, UnaryOp { inv ->
                allFinite(listOf(inv.a.a.a, inv.a.a.b, inv.a.b.a, inv.a.b.b, inv.b.a.a, inv.b.a.b, inv.b.b.a, inv.b.b.b))
            })
        }
        OctonionAlgebras.OctonionDivisionAlgebra.hasReciprocal(OctonionAlgebras.OctonionDivisionAlgebra.zero) shouldBe false
    }

    "dual numbers over ℝ: a + bε is invertible iff a is finite and not approximately zero" {
        val ring = DualAlgebras.DualRealRing
        checkAll(boundaryReal, boundaryReal) { a, b ->
            checkContract(ring, dual(a, b), UnaryOp { d -> allFinite(listOf(d.a, d.b)) })
        }
        ring.hasReciprocal(dual(0.0, 1.0)) shouldBe false
        ring.hasReciprocal(dual(Double.NaN, 1.0)) shouldBe false
        ring.hasReciprocal(dual(2.0, 5.0)) shouldBe true
        // The tangent part b/a² of the inverse would overflow, so this is rejected.
        ring.hasReciprocal(dual(1e-4, 1e301)) shouldBe false
    }

    "split-complex numbers over ℝ: the null cone a = ±b is not invertible" {
        val ring = SplitComplexAlgebras.RealSplitComplexRing
        checkAll(boundaryReal, boundaryReal) { a, b ->
            checkContract(ring, splitComplex(a, b), UnaryOp { z -> allFinite(listOf(z.re, z.hy)) })
        }
        ring.hasReciprocal(splitComplex(1.0, 1.0)) shouldBe false
        ring.hasReciprocal(splitComplex(3.0, -3.0)) shouldBe false
        ring.hasReciprocal(splitComplex(2.0, 1.0)) shouldBe true
        check(SplitComplexAlgebras.eqSplitComplex(
            ring.mul(splitComplex(2.0, 1.0), ring.reciprocal(splitComplex(2.0, 1.0))),
            ring.one
        ))
    }

    "split-complex numbers over ℚ: exact reciprocal (2 + j)⁻¹ = (2 - j)/3, and 1 + j is a zero divisor" {
        val ring = SplitComplexAlgebras.splitComplexRingWithReciprocal(RationalAlgebras.RationalField)
        val z = splitComplex(Rational.of(2), Rational.ONE)
        ring.reciprocal(z) shouldBe splitComplex(Rational.of(2, 3), Rational.of(-1, 3))
        ring.mul(z, ring.reciprocal(z)) shouldBe ring.one

        val nullCone = splitComplex(Rational.ONE, Rational.ONE)
        ring.hasReciprocal(nullCone) shouldBe false
        shouldThrow<ArithmeticException> { ring.reciprocal(nullCone) }
        ring.mul(nullCone, splitComplex(Rational.ONE, -Rational.ONE)) shouldBe ring.zero
    }

    "bicomplex numbers: hasReciprocal agrees with reciprocal at the boundary and on the null cone" {
        val ring = BicomplexAlgebras.BicomplexCommutativeRing
        checkAll(boundaryReal, boundaryReal, boundaryReal, boundaryReal) { a, b, c, d ->
            checkContract(ring, Bicomplex.ofStandard(a, b, c, d), UnaryOp { z -> allFinite(z.coefficients()) })
        }
        // 1 + k lies on the null cone (its 𝛂-coordinate vanishes).
        ring.hasReciprocal(Bicomplex.ofStandard(1.0, 0.0, 0.0, 1.0)) shouldBe false
        ring.hasReciprocal(Bicomplex.ofStandard(1.0, 0.0, 0.0, 0.0)) shouldBe true
    }

    "ℝ: hasReciprocal, reciprocal and reciprocalOption agree, including at 0, tiny, NaN and ∞" {
        val field = RealAlgebras.RealField
        checkAll(boundaryReal) { a ->
            checkContract(field, a, UnaryOp { r -> r.isFinite() })
        }
        listOf(0.0, -0.0, Double.MIN_VALUE, 1e-300, Double.NaN, Double.POSITIVE_INFINITY).forEach { a ->
            field.hasReciprocal(a) shouldBe false
            shouldThrow<ArithmeticException> { field.reciprocal(a) }
        }
        field.reciprocal(4.0) shouldBe 0.25
        RealAlgebras.RealStarField.hasReciprocal(Double.NaN) shouldBe false
        shouldThrow<ArithmeticException> { RealAlgebras.RealStarField.reciprocal(0.0) }
    }

    "ℚ and ℚ(i): exactly the nonzero elements are invertible, and 0 throws ArithmeticException" {
        val q = RationalAlgebras.RationalField
        q.hasReciprocal(Rational.ZERO) shouldBe false
        shouldThrow<ArithmeticException> { q.reciprocal(Rational.ZERO) }
        q.reciprocalOption(Rational.ZERO) shouldBe Option.None
        q.reciprocal(Rational.of(2, 3)) shouldBe Rational.of(3, 2)

        listOf(GaussianRatAlgebras.GaussianRatField, GaussianRatAlgebras.GaussianRatStarAlgebra).forEach { k ->
            k.hasReciprocal(GaussianRat.ZERO) shouldBe false
            shouldThrow<ArithmeticException> { k.reciprocal(GaussianRat.ZERO) }
            k.reciprocalOption(GaussianRat.ZERO) shouldBe Option.None
            k.reciprocal(GaussianRat.I) shouldBe GaussianRat(Rational.ZERO, -Rational.ONE)
        }
    }

    "split-quaternions over ℝ: the null cone w² + x² = y² + z² is not invertible" {
        val ring = SplitQuaternionAlgebras.RealSplitQuaternionRing
        checkAll(boundaryReal, boundaryReal, boundaryReal, boundaryReal) { w, x, y, z ->
            checkContract(ring, splitQuaternion(w, x, y, z), UnaryOp { q -> allFinite(q.coefficients()) })
        }
        ring.hasReciprocal(splitQuaternion(1.0, 0.0, 1.0, 0.0)) shouldBe false
        ring.hasReciprocal(splitQuaternion(3.0, 4.0, 5.0, 0.0)) shouldBe false
        ring.hasReciprocal(ring.zero) shouldBe false
        ring.hasReciprocal(splitQuaternion(2.0, 0.0, 1.0, 0.0)) shouldBe true
    }

    "split-octonions over ℝ: the null cone is not invertible" {
        val ring = SplitOctonionAlgebras.RealSplitOctonionAlgebra
        checkAll(boundaryReal, boundaryReal, boundaryReal, boundaryReal) { w, x, u, t ->
            checkContract(ring, splitOctonion(w, x, 0.0, 0.0, u, 0.0, 0.0, t), UnaryOp { o -> allFinite(o.coefficients()) })
        }
        ring.hasReciprocal(splitOctonion(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0)) shouldBe false
        ring.hasReciprocal(ring.zero) shouldBe false
        ring.hasReciprocal(ring.one) shouldBe true
    }

    "biquaternions: hasReciprocal agrees with reciprocal at the boundary and on the null cone" {
        val ring = BiquaternionAlgebras.BiquaternionRing
        checkAll(boundaryReal, boundaryReal, boundaryReal, boundaryReal) { a, b, c, d ->
            val q = biquaternion(complex(a, b), complex(c, 0.0), complex(0.0, d), complex(1.0, 0.0))
            checkContract(ring, q, UnaryOp { inv -> allFinite(inv.coefficients().flatMap { z -> listOf(z.a, z.b) }) })
        }
        // 1 + h·i has norm 1 + h² = 0.
        ring.hasReciprocal(biquaternion(complex(1.0, 0.0), complex(0.0, 1.0), complex(0.0, 0.0), complex(0.0, 0.0))) shouldBe false
        ring.hasReciprocal(ring.zero) shouldBe false
        ring.hasReciprocal(ring.one) shouldBe true
    }

    "bioctonions: hasReciprocal agrees with reciprocal at the boundary and on the null cone" {
        val ring = BioctonionAlgebras.BioctonionAlgebra
        val zero = complex(0.0, 0.0)
        checkAll(boundaryReal, boundaryReal, boundaryReal, boundaryReal) { a, b, c, d ->
            val o = bioctonion(listOf(complex(a, b), zero, zero, complex(c, 0.0), zero, complex(0.0, d), zero, complex(1.0, 0.0)))
            checkContract(ring, o, UnaryOp { inv -> allFinite(inv.coefficients().flatMap { z -> listOf(z.a, z.b) }) })
        }
        // 1 + h·e₄ has norm 1 + h² = 0.
        ring.hasReciprocal(bioctonion(listOf(complex(1.0, 0.0), zero, zero, zero, complex(0.0, 1.0), zero, zero, zero))) shouldBe false
        ring.hasReciprocal(ring.zero) shouldBe false
        ring.hasReciprocal(ring.one) shouldBe true
    }

    "ℍ_ℚ: exactly the nonzero elements are invertible, and 0 throws ArithmeticException" {
        val ring = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        ring.hasReciprocal(ring.zero) shouldBe false
        shouldThrow<ArithmeticException> { ring.reciprocal(ring.zero) }
        ring.hasReciprocal(ring.one) shouldBe true
    }
})
