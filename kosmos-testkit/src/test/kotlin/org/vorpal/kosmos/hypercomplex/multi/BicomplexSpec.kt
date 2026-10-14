package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ArbComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex

class BicomplexSpec: StringSpec({
    val ring = BicomplexAlgebras.BicomplexCommutativeRing
    val complexField = ComplexAlgebras.ComplexField
    val eqB = BicomplexAlgebras.eqBicomplex
    val eqC = ComplexAlgebras.eqComplex
    val eqR = RealAlgebras.eqRealApprox
    val zero = ring.zero
    val one = ring.one

    "ofIdempotent(alpha, beta) preserves idempotent coordinates" {
        val alpha = complex(2.0, -3.0)
        val beta = complex(-4.0, 5.0)

        val z = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, alpha, beta)
        val (actualAlpha, actualBeta) = z.idempotent()
        check(eqC(actualAlpha, alpha))
        check(eqC(actualBeta, beta))
    }

    "standard and idempotent coordinates round-trip" {
        checkAll(ArbBicomplex.bicomplex) { z ->
            check(eqB(BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, z.alpha, z.beta), z))
            check(eqB(Bicomplex.ofStandard(z.z1, z.z2), z))
        }
    }

    "idempotent coordinates are multiplicative: (zw)_𝛂 = z_𝛂 w_𝛂 and (zw)_𝜷 = z_𝜷 w_𝜷" {
        checkAll(ArbBicomplex.bicomplex, ArbBicomplex.bicomplex) { z, w ->
            val zw = ring.mul(z, w)
            check(eqC(zw.alpha, complexField.mul(z.alpha, w.alpha)))
            check(eqC(zw.beta, complexField.mul(z.beta, w.beta)))
        }
    }

    "standard multiplication is the Cayley-Dickson product (z1 + z2 j)(w1 + w2 j) = (z1w1 - z2w2) + (z1w2 + z2w1) j" {
        checkAll(ArbBicomplex.bicomplex, ArbBicomplex.bicomplex) { z, w ->
            val zw = ring.mul(z, w)
            val first = complexField.add(
                complexField.mul(z.z1, w.z1),
                complexField.add.inverse(complexField.mul(z.z2, w.z2))
            )
            val second = complexField.add(complexField.mul(z.z1, w.z2), complexField.mul(z.z2, w.z1))
            check(eqB(zw, Bicomplex.ofStandard(first, second)))
        }
    }

    "ofStandard(z1, z2) preserves standard coordinates" {
        val z1 = complex(1.25, -2.5)
        val z2 = complex(3.75, 4.5)

        val z = Bicomplex.ofStandard(z1, z2)
        val (actualZ1, actualZ2) = z.standard()

        check(eqC(actualZ1, z1))
        check(eqC(actualZ2, z2))
    }

    "ofStandard(a, b, c, d) preserves coefficients" {
        val a = 1.5
        val b = -2.25
        val c = 3.125
        val d = -4.75

        val z = Bicomplex.ofStandard(a, b, c, d)
        val coeffs = z.coefficients()

        check(eqR(coeffs[0], a))
        check(eqR(coeffs[1], b))
        check(eqR(coeffs[2], c))
        check(eqR(coeffs[3], d))
    }


    "z1 and z2 accessors agree with standard()" {
        val z = Bicomplex.ofStandard(1.0, 2.0, 3.0, 4.0)
        val (s1, s2) = z.standard()

        check(eqC(z.z1, s1))
        check(eqC(z.z2, s2))
    }

    "a b c d accessors agree with coefficients()" {
        val z = Bicomplex.ofStandard(1.0, -2.0, 3.0, -4.0)
        val coeffs = z.coefficients()
        check(eqR(z.a, coeffs[0]))
        check(eqR(z.b, coeffs[1]))
        check(eqR(z.c, coeffs[2]))
        check(eqR(z.d, coeffs[3]))
    }

    "i squared is minus one" {
        val minusOne = ring.add.inverse(one)
        val i2 = ring.mul(ring.i, ring.i)

        check(eqB(i2, minusOne))
    }

    "j squared is minus one" {
        val minusOne = ring.add.inverse(one)
        val j2 = ring.mul(ring.j, ring.j)

        check(eqB(j2, minusOne))
    }

    "i and j commute" {
        val ij = ring.mul(ring.i, ring.j)
        val ji = ring.mul(ring.j, ring.i)

        check(eqB(ij, ji))
    }

    "i times j is k" {
        val ij = ring.mul(ring.i, ring.j)

        check(eqB(ij, ring.k))
    }

    "k squared is one" {
        val k2 = ring.mul(ring.k, ring.k)

        check(eqB(k2, one))
    }

    "canonical idempotents are zero divisors" {
        val ePlus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, ComplexAlgebras.ComplexField.one, ComplexAlgebras.ComplexField.zero)
        val eMinus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, ComplexAlgebras.ComplexField.zero, ComplexAlgebras.ComplexField.one)

        ePlus.isZeroDivisor() shouldBe true
        eMinus.isZeroDivisor() shouldBe true
    }

    "canonical idempotents multiply to zero" {
        val ePlus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, ComplexAlgebras.ComplexField.one, ComplexAlgebras.ComplexField.zero)
        val eMinus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, ComplexAlgebras.ComplexField.zero, ComplexAlgebras.ComplexField.one)

        val prod = ring.mul(ePlus, eMinus)

        check(eqB(prod, zero))
    }

    "canonical idempotents sum to one" {
        val ePlus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, ComplexAlgebras.ComplexField.one, ComplexAlgebras.ComplexField.zero)
        val eMinus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, ComplexAlgebras.ComplexField.zero, ComplexAlgebras.ComplexField.one)

        val sum = ring.add(ePlus, eMinus)

        check(eqB(sum, one))
    }

    "one is a unit" {
        one.isUnit() shouldBe true
    }

    "zero lies in the null cone but is not a nonzero zero divisor" {
        check(BicomplexAlgebras.BicomplexCommutativeRing.zero.isInNullCone())
        check(!BicomplexAlgebras.BicomplexCommutativeRing.zero.isZeroDivisor())
        check(!BicomplexAlgebras.BicomplexCommutativeRing.zero.isUnit())
    }

    "idempotent axes lie in the null cone" {
        checkAll(ArbComplex.boundedComplex) { alpha ->
            val z = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, alpha, complexField.zero)
            check(z.isInNullCone())
            if (alpha != complexField.zero) {
                check(z.isZeroDivisor())
            }
        }
    }

    "units are exactly elements outside the null cone" {
        checkAll(ArbComplex.boundedComplex, ArbComplex.boundedComplex) { alpha, beta ->
            val z = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, alpha, beta)
            check(z.isUnit() == !z.isInNullCone())
            check(ring.hasReciprocal(z) == z.isUnit())
        }
        // boundedComplex never has a vanishing projection, so exercise the null-cone side too:
        checkAll(ArbComplex.boundedComplex) { alpha ->
            val onAxis = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, alpha, complexField.zero)
            check(!onAxis.isUnit())
            check(!ring.hasReciprocal(onAxis))
        }
    }

    "null cone equals vanishing of normByConj2" {
        checkAll(ArbComplex.boundedComplex, ArbComplex.boundedComplex) { alpha, beta ->
            val z = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, alpha, beta)
            val n2 = ring.normByConj2(z)
            check(z.isInNullCone() == eqC(n2, complexField.zero))
        }
        // explicit null-cone witnesses (a vanishing projection): normByConj2 must vanish.
        checkAll(ArbComplex.boundedComplex) { alpha ->
            val zPlus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, alpha, complexField.zero)
            val zMinus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, complexField.zero, alpha)
            check(zPlus.isInNullCone() && eqC(ring.normByConj2(zPlus), complexField.zero))
            check(zMinus.isInNullCone() && eqC(ring.normByConj2(zMinus), complexField.zero))
        }
    }
})