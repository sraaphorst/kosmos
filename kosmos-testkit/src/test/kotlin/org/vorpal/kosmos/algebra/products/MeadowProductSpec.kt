package org.vorpal.kosmos.algebra.products

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.Meadow
import org.vorpal.kosmos.algebra.structures.instances.MeadowAlgebras
import org.vorpal.kosmos.algebra.structures.instances.MeadowAlgebras.ZModMeadow
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.algebra.MeadowLaws
import java.math.BigInteger

/**
 * Products of meadows are meadows, though never fields: `(1, 0)` is a zero divisor.
 */
class MeadowProductSpec : StringSpec({

    fun residues(n: Int): Arb<BigInteger> =
        Arb.int(0 until n).map(Int::toBigInteger)

    val rationalArb: Arb<Rational> = Arb.choice(Arb.constant(Rational.ZERO), ArbRational.small)
    val f5 = MeadowAlgebras.primeFieldMeadow(BigInteger.valueOf(5))

    "ℚ₀ × 𝔽_5 satisfies MeadowLaws" {
        val meadow = Meadows.product(MeadowAlgebras.RationalMeadow, f5)
        MeadowLaws(meadow, Arb.pair(rationalArb, residues(5))).fullTest().throwIfFailed()
    }

    "ℚ₀ × 𝔽_5: (1, 0) * inv((1, 0)) = (1, 0), not 1" {
        val meadow = Meadows.product(MeadowAlgebras.RationalMeadow, f5)
        val x = Pair(Rational.ONE, BigInteger.ZERO)
        meadow.mul(x, meadow.inv(x)) shouldBe x
    }

    "doubling, quadrupling, and octupling ℤ/6ℤ give meadows" {
        val z6 = ZModMeadow(BigInteger.valueOf(6))
        val arb1 = residues(6)

        val double = Meadows.double(z6)
        val arb2 = TestingLaw.arbPair(arb1)
        MeadowLaws(double, arb2).fullTest().throwIfFailed()

        val quad = Meadows.double(double)
        val arb4 = TestingLaw.arbPair(arb2)
        MeadowLaws(quad, arb4).fullTest().throwIfFailed()

        val oct = Meadows.double(quad)
        val arb8 = TestingLaw.arbPair(arb4)
        MeadowLaws(oct, arb8).fullTest().throwIfFailed()
    }

    "CRT: ℤ/30ℤ → 𝔽₂ × (𝔽₃ × 𝔽₅) is a meadow isomorphism" {
        val z30 = ZModMeadow(BigInteger.valueOf(30))
        val f2 = MeadowAlgebras.primeFieldMeadow(BigInteger.TWO)
        val f3 = MeadowAlgebras.primeFieldMeadow(BigInteger.valueOf(3))
        val target: Meadow<Pair<BigInteger, Pair<BigInteger, BigInteger>>> =
            Meadows.product(f2, Meadows.product(f3, f5))

        fun crt(x: BigInteger) =
            Pair(x.mod(BigInteger.TWO), Pair(x.mod(BigInteger.valueOf(3)), x.mod(BigInteger.valueOf(5))))

        val elements = (0 until 30).map(Int::toBigInteger)
        elements.map(::crt).toSet().size shouldBe 30

        checkAll(TestingLaw.arbPair(residues(30))) { (x, y) ->
            crt(z30.add(x, y)) shouldBe target.add(crt(x), crt(y))
            crt(z30.mul(x, y)) shouldBe target.mul(crt(x), crt(y))
        }
        elements.forEach { x ->
            crt(z30.inv(x)) shouldBe target.inv(crt(x))
        }
        crt(z30.one) shouldBe target.one
    }
})
