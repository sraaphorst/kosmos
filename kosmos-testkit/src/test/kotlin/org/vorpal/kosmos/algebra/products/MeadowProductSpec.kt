package org.vorpal.kosmos.algebra.products

import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.Meadow
import org.vorpal.kosmos.algebra.structures.instances.MeadowAlgebras
import org.vorpal.kosmos.algebra.structures.instances.MeadowAlgebras.ZModMeadow
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.algebra.MeadowLaws
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import java.math.BigInteger

private fun residues(n: Int): Arb<BigInteger> =
    Arb.int(0 until n).map(Int::toBigInteger)

/**
 * Products of meadows are meadows, though never fields: `(1, 0)` is a zero divisor.
 */
class MeadowProductSpec : FunSpec({
    val rationalArb: Arb<Rational> = Arb.choice(Arb.constant(Rational.ZERO), ArbRational.small)
    val f5 = MeadowAlgebras.primeFieldMeadow(BigInteger.valueOf(5))
    val qTimesF5 = Meadows.product(MeadowAlgebras.RationalMeadow, f5)

    val z6 = ZModMeadow(BigInteger.valueOf(6))
    val arb1 = residues(6)
    val double = Meadows.double(z6)
    val arb2 = TestingLaw.arbPair(arb1)
    val quad = Meadows.double(double)
    val arb4 = TestingLaw.arbPair(arb2)
    val oct = Meadows.double(quad)
    val arb8 = TestingLaw.arbPair(arb4)

    lawTests(lawGraph("meadow products") {
        branch("ℚ₀ × 𝔽_5") {
            suite(MeadowLaws(qTimesF5, Arb.pair(rationalArb, residues(5))))
            local("(1, 0) * inv((1, 0)) = (1, 0), not 1") {
                val x = Pair(Rational.ONE, BigInteger.ZERO)
                lawCheck(qTimesF5.mul(x, qTimesF5.inv(x)) == x) { "(1, 0) * inv((1, 0)) = ${qTimesF5.mul(x, qTimesF5.inv(x))}" }
            }
        }

        branch("(ℤ/6ℤ)²") { suite(MeadowLaws(double, arb2)) }
        branch("(ℤ/6ℤ)⁴") { suite(MeadowLaws(quad, arb4)) }
        branch("(ℤ/6ℤ)⁸") { suite(MeadowLaws(oct, arb8)) }

        local("CRT: ℤ/30ℤ → 𝔽₂ × (𝔽₃ × 𝔽₅) is a meadow isomorphism") {
            val z30 = ZModMeadow(BigInteger.valueOf(30))
            val f2 = MeadowAlgebras.primeFieldMeadow(BigInteger.TWO)
            val f3 = MeadowAlgebras.primeFieldMeadow(BigInteger.valueOf(3))
            val target: Meadow<Pair<BigInteger, Pair<BigInteger, BigInteger>>> =
                Meadows.product(f2, Meadows.product(f3, f5))

            fun crt(x: BigInteger) =
                Pair(x.mod(BigInteger.TWO), Pair(x.mod(BigInteger.valueOf(3)), x.mod(BigInteger.valueOf(5))))

            val elements = (0 until 30).map(Int::toBigInteger)
            lawCheck(elements.map(::crt).toSet().size == 30) { "the CRT map is not injective on ℤ/30ℤ" }

            lawCheckAll(TestingLaw.arbPair(residues(30))) { (x, y) ->
                lawCheck(crt(z30.add(x, y)) == target.add(crt(x), crt(y))) { "crt($x + $y)" }
                lawCheck(crt(z30.mul(x, y)) == target.mul(crt(x), crt(y))) { "crt($x * $y)" }
            }
            elements.forEach { x ->
                lawCheck(crt(z30.inv(x)) == target.inv(crt(x))) { "crt(inv($x))" }
            }
            lawCheck(crt(z30.one) == target.one) { "crt(1) ≠ 1" }
        }
    })
})
