package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.Meadow
import org.vorpal.kosmos.algebra.structures.instances.MeadowAlgebras.ZModMeadow
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.MeadowLaws
import java.math.BigInteger

/**
 * The concrete [Meadow]s in [MeadowAlgebras], and the field-to-meadow construction [Meadow.fromField].
 */
class MeadowSpec : StringSpec({

    fun residues(n: Int): Arb<BigInteger> =
        Arb.int(0 until n).map(Int::toBigInteger)

    // ===== ℚ₀ =====

    val rationalMeadow = MeadowAlgebras.RationalMeadow
    val rationalArb: Arb<Rational> = Arb.choice(Arb.constant(Rational.ZERO), ArbRational.small)

    "ℚ₀ satisfies MeadowLaws" {
        MeadowLaws(
            meadow = rationalMeadow,
            arb = rationalArb,
            eq = RationalAlgebras.eqRational,
            pr = RationalAlgebras.printableRationalPretty
        ).fullTest().throwIfFailed()
    }

    "ℚ₀: inv(0) = 0, and inv agrees with the field reciprocal away from 0" {
        rationalMeadow.inv(Rational.ZERO) shouldBe Rational.ZERO
        checkAll(ArbRational.nonZero) { x ->
            rationalMeadow.inv(x) shouldBe RationalAlgebras.RationalField.reciprocal(x)
        }
    }

    "ℚ₀ is a cancellation meadow: x ≠ 0 implies x * inv(x) = 1" {
        checkAll(ArbRational.nonZero) { x ->
            rationalMeadow.mul(x, rationalMeadow.inv(x)) shouldBe Rational.ONE
        }
    }

    // ===== 𝔽₂ on Boolean =====

    "BoolMeadow satisfies MeadowLaws, with inv the identity" {
        val meadow = MeadowAlgebras.BoolMeadow
        MeadowLaws(meadow, Arb.boolean()).fullTest().throwIfFailed()
        listOf(false, true).forEach { b -> meadow.inv(b) shouldBe b }
    }

    // ===== 𝔽ₚ =====

    listOf(2, 3, 7, 101).forEach { p ->
        val meadow = MeadowAlgebras.primeFieldMeadow(p.toBigInteger())

        "𝔽_$p meadow satisfies MeadowLaws" {
            MeadowLaws(meadow, residues(p)).fullTest().throwIfFailed()
        }

        "𝔽_$p meadow: every multiple of $p, not just the literal 0, is sent to 0" {
            listOf(0, p, -p, 5 * p).map(Int::toBigInteger).forEach { a ->
                meadow.inv(a) shouldBe BigInteger.ZERO
            }
        }

        "𝔽_$p meadow is a cancellation meadow" {
            (1 until p).map(Int::toBigInteger).forEach { a ->
                meadow.mul(a, meadow.inv(a)) shouldBe BigInteger.ONE
            }
        }
    }

    // ===== ℤ/nℤ for squarefree n =====

    listOf(1, 2, 6, 15, 30, 101, 2310).forEach { n ->
        val meadow = ZModMeadow(n.toBigInteger())

        "ℤ/${n}ℤ satisfies MeadowLaws" {
            MeadowLaws(meadow, residues(n)).fullTest().throwIfFailed()
        }
    }

    "ℤ/30ℤ: inv(x) is the unique y with x²y = x and y²x = y (exhaustive)" {
        val n = 30
        val meadow = ZModMeadow(n.toBigInteger())
        val elements = (0 until n).map(Int::toBigInteger)
        fun isMeadowInverse(x: BigInteger, y: BigInteger): Boolean =
            meadow.mul(meadow.mul(x, x), y) == x && meadow.mul(meadow.mul(y, y), x) == y

        elements.forEach { x ->
            elements.filter { y -> isMeadowInverse(x, y) } shouldBe listOf(meadow.inv(x))
        }
    }

    "ℤ/pℤ agrees with the 𝔽_p meadow for prime p" {
        listOf(2, 3, 7, 101).forEach { p ->
            val zmod = ZModMeadow(p.toBigInteger())
            val fp = MeadowAlgebras.primeFieldMeadow(p.toBigInteger())
            (0 until p).map(Int::toBigInteger).forEach { a ->
                zmod.inv(a) shouldBe fp.inv(a)
            }
        }
    }

    "ℤ/6ℤ is not a cancellation meadow: 2 * inv(2) = 4 is a nontrivial idempotent" {
        val meadow = ZModMeadow(BigInteger.valueOf(6))
        val two = BigInteger.TWO
        meadow.inv(two) shouldBe two
        val e = meadow.mul(two, meadow.inv(two))
        e shouldBe BigInteger.valueOf(4)
        e shouldNotBe meadow.one
        meadow.mul(e, e) shouldBe e
    }

    "ℤ/nℤ reduces non-canonical representatives before inverting" {
        val meadow = ZModMeadow(BigInteger.valueOf(30))
        checkAll(Arb.int(-1000..1000)) { k ->
            val a = k.toBigInteger()
            meadow.inv(a) shouldBe meadow.inv(a.mod(BigInteger.valueOf(30)))
        }
    }

    "the Carmichael function of a squarefree modulus is lcm(p - 1)" {
        mapOf(1 to 1, 2 to 1, 6 to 2, 15 to 4, 30 to 4, 101 to 100, 2310 to 60).forEach { (n, lambda) ->
            ZModMeadow(n.toBigInteger()).carmichael shouldBe lambda.toBigInteger()
        }
    }

    "ℤ/nℤ is rejected when n is not squarefree or not positive" {
        listOf(0, -6, 4, 12, 18, 49, 2 * 3 * 3 * 5).forEach { n ->
            shouldThrow<IllegalArgumentException> { ZModMeadow(n.toBigInteger()) }
        }
    }

    "isSquarefree agrees with a brute-force check" {
        (1..500).forEach { n ->
            val bruteForce = (2..n).none { d -> n % (d * d) == 0 }
            ZModMeadow.isSquarefree(n.toBigInteger()) shouldBe bruteForce
        }
    }

    // ===== MeadowLaws catch broken inverses =====

    "MeadowLaws rejects ℤ/30ℤ with inv the identity, since 2 * (2 * 2) = 8 ≠ 2" {
        val zmod = ZModMeadow(BigInteger.valueOf(30))
        val broken = Meadow.of(zmod.add, zmod.mul, Endo(Symbols.INVERSE) { it })
        val report = MeadowLaws(broken, residues(30)).test()
        report.failures.map { it.name } shouldContain "restricted inverse law (RIL)"
    }

    "MeadowLaws rejects ℚ with inv(0) = 1 patched in, since the reflection law fails" {
        val broken = Meadow.of(rationalMeadow.add, rationalMeadow.mul, Endo(Symbols.INVERSE) { x ->
            if (x == Rational.ZERO) Rational.ONE else rationalMeadow.inv(x)
        })
        MeadowLaws(broken, rationalArb).test().success shouldBe false
    }

    // ===== Meadow.of =====

    "Meadow.of builds a meadow from its parts" {
        val zmod = ZModMeadow(BigInteger.valueOf(15))
        val meadow = Meadow.of(zmod.add, zmod.mul, zmod.inv)
        MeadowLaws(meadow, residues(15)).fullTest().throwIfFailed()
    }
})
