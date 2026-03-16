package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex

class BicomplexSpec: StringSpec({
    val ring = BicomplexAlgebras.BicomplexCommutativeRing
    val eqB = BicomplexAlgebras.eqBicomplex
    val eqC = ComplexAlgebras.eqComplex
    val eqR = RealAlgebras.eqRealApprox
    val zero = ring.zero
    val one = ring.one

    "ofIdempotent(alpha, beta) preserves idempotent coordinates" {
        val alpha = complex(2.0, -3.0)
        val beta = complex(-4.0, 5.0)

        val z = Bicomplex.ofIdempotent(alpha, beta)
        val (actualAlpha, actualBeta) = z
        check(eqC(actualAlpha, alpha))
        check(eqC(actualBeta, beta))
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
        val ePlus = Bicomplex.ofIdempotent(ComplexAlgebras.ComplexField.one, ComplexAlgebras.ComplexField.zero)
        val eMinus = Bicomplex.ofIdempotent(ComplexAlgebras.ComplexField.zero, ComplexAlgebras.ComplexField.one)

        ePlus.isZeroDivisor() shouldBe true
        eMinus.isZeroDivisor() shouldBe true
    }

    "canonical idempotents multiply to zero" {
        val ePlus = Bicomplex.ofIdempotent(ComplexAlgebras.ComplexField.one, ComplexAlgebras.ComplexField.zero)
        val eMinus = Bicomplex.ofIdempotent(ComplexAlgebras.ComplexField.zero, ComplexAlgebras.ComplexField.one)

        val prod = ring.mul(ePlus, eMinus)

        check(eqB(prod, zero))
    }

    "canonical idempotents sum to one" {
        val ePlus = Bicomplex.ofIdempotent(ComplexAlgebras.ComplexField.one, ComplexAlgebras.ComplexField.zero)
        val eMinus = Bicomplex.ofIdempotent(ComplexAlgebras.ComplexField.zero, ComplexAlgebras.ComplexField.one)

        val sum = ring.add(ePlus, eMinus)

        check(eqB(sum, one))
    }

    "zero is a zero divisor under the chosen convention" {
        zero.isZeroDivisor() shouldBe true
    }

    "one is a unit" {
        one.isUnit() shouldBe true
    }
})