package org.vorpal.kosmos.hypercomplex

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.numberfields.quadratic.ArbGaussianInt
import org.vorpal.kosmos.numberfields.quadratic.ArbGaussianRat
import org.vorpal.kosmos.numberfields.quadratic.GaussianInt
import org.vorpal.kosmos.numberfields.quadratic.GaussianIntAlgebras
import org.vorpal.kosmos.numberfields.quadratic.GaussianRat
import org.vorpal.kosmos.numberfields.quadratic.GaussianRatAlgebras
import java.math.BigInteger

/**
 * `GaussianInt` and `GaussianRat` are value classes over the Cayley-Dickson pair, and their ring
 * arithmetic is the Cayley-Dickson doubling of ℤ and ℚ. These tests pin the carrier down and check
 * the transported operations against the textbook formulas.
 */
class GaussianCarrierSpec : StringSpec({
    val z = GaussianIntAlgebras.GaussianIntEuclideanDomain
    val k = GaussianRatAlgebras.GaussianRatField

    "GaussianInt wraps CD<BigInteger>: re, im, components and the constants" {
        val g = GaussianInt(BigInteger.valueOf(3), BigInteger.valueOf(-4))
        g.cd shouldBe CD(BigInteger.valueOf(3), BigInteger.valueOf(-4))
        GaussianInt(g.cd) shouldBe g
        val (re, im) = g
        (re to im) shouldBe (g.re to g.im)
        g.toString() shouldBe "GaussianInt(re=3, im=-4)"

        GaussianInt.ZERO shouldBe z.zero
        GaussianInt.ONE shouldBe z.one
        z.mul(GaussianInt.I, GaussianInt.I) shouldBe z.negOne
    }

    "GaussianRat wraps CD<Rational>: re, im, components and the constants" {
        val g = GaussianRat(Rational.of(1, 2), Rational.of(-3))
        g.cd shouldBe CD(Rational.of(1, 2), Rational.of(-3))
        GaussianRat(g.cd) shouldBe g
        val (re, im) = g
        (re to im) shouldBe (g.re to g.im)

        GaussianRat.ZERO shouldBe k.zero
        GaussianRat.ONE shouldBe k.one
        k.mul(GaussianRat.I, GaussianRat.I) shouldBe k.negOne
    }

    "ℤ[i] arithmetic is the textbook formula" {
        checkAll(ArbGaussianInt.small, ArbGaussianInt.small) { a, b ->
            z.add(a, b) shouldBe GaussianInt(a.re + b.re, a.im + b.im)
            z.add.inverse(a) shouldBe GaussianInt(-a.re, -a.im)
            z.mul(a, b) shouldBe GaussianInt(a.re * b.re - a.im * b.im, a.re * b.im + a.im * b.re)
            z.conj(a) shouldBe GaussianInt(a.re, -a.im)
            z.normSq(a) shouldBe z.mul(a, z.conj(a)).re
        }
    }

    "ℚ(i) arithmetic is the textbook formula" {
        val conj = GaussianRatAlgebras.GaussianRatStarAlgebra.conj
        checkAll(ArbGaussianRat.small, ArbGaussianRat.small) { a, b ->
            k.add(a, b) shouldBe GaussianRat(a.re + b.re, a.im + b.im)
            k.add.inverse(a) shouldBe GaussianRat(-a.re, -a.im)
            k.mul(a, b) shouldBe GaussianRat(a.re * b.re - a.im * b.im, a.re * b.im + a.im * b.re)
            conj(a) shouldBe GaussianRat(a.re, -a.im)
        }
    }

    "ℤ[i] and ℚ(i) are the Cayley-Dickson doublings of ℤ and ℚ at σ = 1" {
        val cdZ = CayleyDickson.commutative(IntegerAlgebras.IntegerCommutativeRing, BigInteger.ONE)
        checkAll(ArbGaussianInt.small, ArbGaussianInt.small) { a, b ->
            z.mul(a, b).cd shouldBe cdZ.mul(a.cd, b.cd)
            z.add(a, b).cd shouldBe cdZ.add(a.cd, b.cd)
            z.conj(a).cd shouldBe cdZ.conj(a.cd)
        }

        val cdQ = CayleyDickson.commutative(RationalAlgebras.RationalField, Rational.ONE)
        checkAll(ArbGaussianRat.small, ArbGaussianRat.small) { a, b ->
            k.mul(a, b).cd shouldBe cdQ.mul(a.cd, b.cd)
            k.add(a, b).cd shouldBe cdQ.add(a.cd, b.cd)
        }
    }

    "Euclidean division in ℤ[i] still satisfies a = qb + r with N(r) < N(b)" {
        checkAll(ArbGaussianInt.small, ArbGaussianInt.nonzero) { a, b ->
            val (quotient, remainder) = z.divRem(a, b)
            z.add(z.mul(quotient, b), remainder) shouldBe a
            (z.normSq(remainder) < z.normSq(b)) shouldBe true
        }
    }
})
