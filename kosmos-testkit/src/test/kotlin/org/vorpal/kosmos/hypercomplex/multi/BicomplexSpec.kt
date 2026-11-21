package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ArbComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

class BicomplexSpec : FunSpec({
    lawTests(lawGraph("bicomplex numbers") {
        val ring = BicomplexAlgebras.BicomplexCommutativeRing
        val complexField = ComplexAlgebras.ComplexField
        val eqB = BicomplexAlgebras.eqBicomplex
        val eqC = ComplexAlgebras.eqComplex
        val eqR = RealAlgebras.eqRealApprox
        val zero = ring.zero
        val one = ring.one

        local("ofIdempotent(alpha, beta) preserves idempotent coordinates") {
            val alpha = complex(2.0, -3.0)
            val beta = complex(-4.0, 5.0)

            val z = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, alpha, beta)
            val (actualAlpha, actualBeta) = z.idempotent()
            lawCheck(eqC(actualAlpha, alpha))
            lawCheck(eqC(actualBeta, beta))
        }

        local("standard and idempotent coordinates round-trip") {
            lawCheckAll(ArbBicomplex.bicomplex) { z ->
                lawCheck(eqB(BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, z.alpha, z.beta), z))
                lawCheck(eqB(Bicomplex.ofStandard(z.z1, z.z2), z))
            }
        }

        local("idempotent coordinates are multiplicative: (zw)_𝛂 = z_𝛂 w_𝛂 and (zw)_𝜷 = z_𝜷 w_𝜷") {
            lawCheckAll(ArbBicomplex.bicomplex, ArbBicomplex.bicomplex) { z, w ->
                val zw = ring.mul(z, w)
                lawCheck(eqC(zw.alpha, complexField.mul(z.alpha, w.alpha)))
                lawCheck(eqC(zw.beta, complexField.mul(z.beta, w.beta)))
            }
        }

        local("standard multiplication is the Cayley-Dickson product (z1 + z2 j)(w1 + w2 j) = (z1w1 - z2w2) + (z1w2 + z2w1) j") {
            lawCheckAll(ArbBicomplex.bicomplex, ArbBicomplex.bicomplex) { z, w ->
                val zw = ring.mul(z, w)
                val first = complexField.add(
                    complexField.mul(z.z1, w.z1),
                    complexField.add.inverse(complexField.mul(z.z2, w.z2))
                )
                val second = complexField.add(complexField.mul(z.z1, w.z2), complexField.mul(z.z2, w.z1))
                lawCheck(eqB(zw, Bicomplex.ofStandard(first, second)))
            }
        }

        local("ofStandard(z1, z2) preserves standard coordinates") {
            val z1 = complex(1.25, -2.5)
            val z2 = complex(3.75, 4.5)

            val z = Bicomplex.ofStandard(z1, z2)
            val (actualZ1, actualZ2) = z.standard()

            lawCheck(eqC(actualZ1, z1))
            lawCheck(eqC(actualZ2, z2))
        }

        local("ofStandard(a, b, c, d) preserves coefficients") {
            val a = 1.5
            val b = -2.25
            val c = 3.125
            val d = -4.75

            val z = Bicomplex.ofStandard(a, b, c, d)
            val coeffs = z.coefficients()

            lawCheck(eqR(coeffs[0], a))
            lawCheck(eqR(coeffs[1], b))
            lawCheck(eqR(coeffs[2], c))
            lawCheck(eqR(coeffs[3], d))
        }


        local("z1 and z2 accessors agree with standard()") {
            val z = Bicomplex.ofStandard(1.0, 2.0, 3.0, 4.0)
            val (s1, s2) = z.standard()

            lawCheck(eqC(z.z1, s1))
            lawCheck(eqC(z.z2, s2))
        }

        local("a b c d accessors agree with coefficients()") {
            val z = Bicomplex.ofStandard(1.0, -2.0, 3.0, -4.0)
            val coeffs = z.coefficients()
            lawCheck(eqR(z.a, coeffs[0]))
            lawCheck(eqR(z.b, coeffs[1]))
            lawCheck(eqR(z.c, coeffs[2]))
            lawCheck(eqR(z.d, coeffs[3]))
        }

        local("i squared is minus one") {
            val minusOne = ring.add.inverse(one)
            val i2 = ring.mul(ring.i, ring.i)

            lawCheck(eqB(i2, minusOne))
        }

        local("j squared is minus one") {
            val minusOne = ring.add.inverse(one)
            val j2 = ring.mul(ring.j, ring.j)

            lawCheck(eqB(j2, minusOne))
        }

        local("i and j commute") {
            val ij = ring.mul(ring.i, ring.j)
            val ji = ring.mul(ring.j, ring.i)

            lawCheck(eqB(ij, ji))
        }

        local("i times j is k") {
            val ij = ring.mul(ring.i, ring.j)

            lawCheck(eqB(ij, ring.k))
        }

        local("k squared is one") {
            val k2 = ring.mul(ring.k, ring.k)

            lawCheck(eqB(k2, one))
        }

        local("canonical idempotents are zero divisors") {
            val ePlus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, ComplexAlgebras.ComplexField.one, ComplexAlgebras.ComplexField.zero)
            val eMinus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, ComplexAlgebras.ComplexField.zero, ComplexAlgebras.ComplexField.one)

            ePlus.isZeroDivisor() shouldBe true
            eMinus.isZeroDivisor() shouldBe true
        }

        local("canonical idempotents multiply to zero") {
            val ePlus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, ComplexAlgebras.ComplexField.one, ComplexAlgebras.ComplexField.zero)
            val eMinus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, ComplexAlgebras.ComplexField.zero, ComplexAlgebras.ComplexField.one)

            val prod = ring.mul(ePlus, eMinus)

            lawCheck(eqB(prod, zero))
        }

        local("canonical idempotents sum to one") {
            val ePlus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, ComplexAlgebras.ComplexField.one, ComplexAlgebras.ComplexField.zero)
            val eMinus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, ComplexAlgebras.ComplexField.zero, ComplexAlgebras.ComplexField.one)

            val sum = ring.add(ePlus, eMinus)

            lawCheck(eqB(sum, one))
        }

        local("one is a unit") {
            one.isUnit() shouldBe true
        }

        local("zero lies in the null cone but is not a nonzero zero divisor") {
            lawCheck(BicomplexAlgebras.BicomplexCommutativeRing.zero.isInNullCone())
            lawCheck(!BicomplexAlgebras.BicomplexCommutativeRing.zero.isZeroDivisor())
            lawCheck(!BicomplexAlgebras.BicomplexCommutativeRing.zero.isUnit())
        }

        local("idempotent axes lie in the null cone") {
            lawCheckAll(ArbComplex.boundedComplex) { alpha ->
                val z = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, alpha, complexField.zero)
                lawCheck(z.isInNullCone())
                if (alpha != complexField.zero) {
                    lawCheck(z.isZeroDivisor())
                }
            }
        }

        local("units are exactly elements outside the null cone") {
            lawCheckAll(ArbComplex.boundedComplex, ArbComplex.boundedComplex) { alpha, beta ->
                val z = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, alpha, beta)
                lawCheck(z.isUnit() == !z.isInNullCone())
                lawCheck(ring.hasReciprocal(z) == z.isUnit())
            }
            // boundedComplex never has a vanishing projection, so exercise the null-cone side too:
            lawCheckAll(ArbComplex.boundedComplex) { alpha ->
                val onAxis = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, alpha, complexField.zero)
                lawCheck(!onAxis.isUnit())
                lawCheck(!ring.hasReciprocal(onAxis))
            }
        }

        local("null cone equals vanishing of normByConj2") {
            lawCheckAll(ArbComplex.boundedComplex, ArbComplex.boundedComplex) { alpha, beta ->
                val z = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, alpha, beta)
                val n2 = ring.normByConj2(z)
                lawCheck(z.isInNullCone() == eqC(n2, complexField.zero))
            }
            // explicit null-cone witnesses (a vanishing projection): normByConj2 must vanish.
            lawCheckAll(ArbComplex.boundedComplex) { alpha ->
                val zPlus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, alpha, complexField.zero)
                val zMinus = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField, complexField.zero, alpha)
                lawCheck(zPlus.isInNullCone() && eqC(ring.normByConj2(zPlus), complexField.zero))
                lawCheck(zMinus.isInNullCone() && eqC(ring.normByConj2(zMinus), complexField.zero))
            }
        }
    })
})