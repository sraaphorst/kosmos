package org.vorpal.kosmos.hypercomplex.contracts

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.dual.DualAlgebras
import org.vorpal.kosmos.hypercomplex.dual.dual
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion
import org.vorpal.kosmos.hypercomplex.sextonion.Sextonion
import org.vorpal.kosmos.hypercomplex.sextonion.SextonionAlgebras
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplexAlgebras
import org.vorpal.kosmos.hypercomplex.splitcomplex.splitComplex
import org.vorpal.kosmos.hypercomplex.zorn.Zorn
import org.vorpal.kosmos.hypercomplex.zorn.ZornAlgebras
import org.vorpal.kosmos.linear.instances.FixedMatAlgebras
import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.Vec2
import org.vorpal.kosmos.linear.values.Vec3
import java.math.BigInteger

/**
 * Regression tests for the `HasReciprocal` contract: when `hasReciprocal(x)` is true, `reciprocal(x)`
 * must not throw and (over `Real`) must be finite, and `reciprocalOption` is total.
 *
 * Also the dual-number product, which used to compute `0·(d·b)` and so turned a large but finite
 * tangent product into `0·∞ = NaN` in the primal part.
 */
class ReciprocalContractSpec : StringSpec({
    val real = RealAlgebras.RealField
    val f5 = PrimeField(BigInteger.valueOf(5))

    // ---------- Dual product ----------

    "dual multiplication with large finite tangents keeps a finite primal part" {
        val x = dual(1.0, 1e200)
        DualAlgebras.RealDualRing.mul(x, x) shouldBe dual(1.0, 2e200)
        DualAlgebras.dualRing(real).mul(x, x) shouldBe dual(1.0, 2e200)
    }

    // ---------- Generic predicates consult the field ----------

    "complex over RealField with the default zero test: a tiny norm is rejected, not accepted and then thrown on" {
        val c = ComplexAlgebras.complexRingWithReciprocal(real)
        val x = complex(1e-6, 0.0)
        c.hasReciprocal(x) shouldBe false
        c.reciprocalOption(x) shouldBe Option.None
    }

    "quaternions, split-complex numbers, Zorn matrices, sextonions and Mat2 over RealField agree with RealField's predicate" {
        val tinyQ = quaternion(1e-6, 0.0, 0.0, 0.0)
        QuaternionAlgebras.quaternionRingWithReciprocal(real).hasReciprocal(tinyQ) shouldBe false

        val tinyS = splitComplex(1e-6, 0.0)
        SplitComplexAlgebras.splitComplexRingWithReciprocal(real).hasReciprocal(tinyS) shouldBe false

        val zero3 = Vec3(0.0, 0.0, 0.0)
        val tinyZ = Zorn(1e-6, zero3, zero3, 1e-6)
        ZornAlgebras.zornRingWithReciprocal(real).hasReciprocal(tinyZ) shouldBe false

        val tinyM = Mat2(1e-6, 0.0, 0.0, 1e-6)
        FixedMatAlgebras.mat2RingWithReciprocal(real).hasReciprocal(tinyM) shouldBe false
        SextonionAlgebras.sextonionRingWithReciprocal(real)
            .hasReciprocal(Sextonion(tinyM, Vec2(0.0, 0.0))) shouldBe false
    }

    "dual numbers over RealField with the default zero test: a tiny primal part is rejected" {
        val d = DualAlgebras.dualRingWithReciprocal(real)
        d.hasReciprocal(dual(1e-200, 0.0)) shouldBe false
        d.reciprocalOption(dual(1e-200, 0.0)) shouldBe Option.None
        d.reciprocal(dual(2.0, 4.0)) shouldBe dual(0.5, -1.0)
    }

    "dual numbers over 𝔽₅: a primal part of 5 is not invertible, even though it is not the literal zero" {
        val d = DualAlgebras.dualRingWithReciprocal(f5)
        val five = BigInteger.valueOf(5)
        d.hasReciprocal(dual(five, BigInteger.ONE)) shouldBe false
        d.reciprocalOption(dual(five, BigInteger.ONE)) shouldBe Option.None

        val two = BigInteger.TWO
        val inv = d.reciprocal(dual(two, BigInteger.ONE))
        d.mul(inv, dual(two, BigInteger.ONE)) shouldBe d.one
    }

    // ---------- Real indefinite forms: finite norm does not mean finite inverse ----------

    "RealZornAlgebra rejects an element whose norm is fine but whose inverse overflows" {
        val x = Zorn(0.1, Vec3(1e308, 0.0, 0.0), Vec3(0.0, 0.0, 0.0), 1.0)
        ZornAlgebras.RealZornAlgebra.normSq(x) shouldBe 0.1
        ZornAlgebras.RealZornAlgebra.hasReciprocal(x) shouldBe false
        ZornAlgebras.RealZornAlgebra.reciprocalOption(x) shouldBe Option.None
    }

    "RealZornAlgebra still inverts ordinary elements" {
        val zero3 = Vec3(0.0, 0.0, 0.0)
        val x = Zorn(2.0, zero3, zero3, 4.0)
        val inv = ZornAlgebras.RealZornAlgebra.reciprocal(x)
        inv.a shouldBe 0.5
        inv.d shouldBe 0.25
        // The vector parts are −0.0, which equals 0.0 as a number but not as a boxed Double.
        listOf(inv.v, inv.w).all { u -> u.x == 0.0 && u.y == 0.0 && u.z == 0.0 } shouldBe true
    }
})
