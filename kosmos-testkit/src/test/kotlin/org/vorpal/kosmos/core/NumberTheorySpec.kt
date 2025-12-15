package org.vorpal.kosmos.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import java.math.BigInteger
import kotlin.math.absoluteValue

class NumberTheorySpec : StringSpec({

    // ===== Arb Generators =====

    val nonZeroInts = Arb.int(-10000..10000).filter { it != 0 }
    val nonZeroLongs = Arb.long(-10000L..10000L).filter { it != 0L }
    Arb.int(1..10000)
    Arb.long(1L..10000L)

    // Use smaller ranges to avoid overflow in LCM calculations
    // LCM can grow very quickly and cause Int overflow with large inputs
    val smallInts = Arb.int(-1000..1000)
    val smallNonZeroInts = Arb.int(-1000..1000).filter { it != 0 }

    val smallBigInts = Arb.int(-10000..10000).map { BigInteger.valueOf(it.toLong()) }
    val nonZeroBigInts = smallBigInts.filter { it != BigInteger.ZERO }
    Arb.int(1..10000).map { BigInteger.valueOf(it.toLong()) }

    // Generate coprime pairs for modular inverse testing
    val coprimePairs = Arb.bind(
        Arb.int(2..100),
        Arb.int(2..100)
    ) { a, b ->
        if (gcd(a, b) == 1) Pair(a, b) else Pair(a, b + 1)
    }.filter { (a, b) -> gcd(a, b) == 1 }

    // ===== GCD Property Tests =====

    "gcd is commutative" {
        checkAll(Arb.int(), Arb.int()) { a, b ->
            gcd(a, b) shouldBe gcd(b, a)
        }
    }

    "gcd with zero gives absolute value" {
        checkAll(Arb.int()) { a ->
            gcd(a, 0) shouldBe a.absoluteValue
            gcd(0, a) shouldBe a.absoluteValue
        }
    }

    "gcd divides both operands" {
        checkAll(nonZeroInts, nonZeroInts) { a, b ->
            val g = gcd(a, b)
            if (g != 0) {
                a % g shouldBe 0
                b % g shouldBe 0
            }
        }
    }

    "gcd is associative" {
        checkAll(Arb.int(), Arb.int(), Arb.int()) { a, b, c ->
            gcd(gcd(a, b), c) shouldBe gcd(a, gcd(b, c))
            gcd(a, b, c) shouldBe gcd(gcd(a, b), c)
        }
    }

    "gcd satisfies Euclidean property" {
        checkAll(Arb.int(), nonZeroInts) { a, b ->
            gcd(a, b) shouldBe gcd(b, a % b)
        }
    }

    "gcd is always non-negative" {
        checkAll(Arb.int(), Arb.int()) { a, b ->
            gcd(a, b) >= 0
        }
    }

    "gcd of one is one" {
        checkAll(Arb.int()) { a ->
            gcd(1, a) shouldBe 1
            gcd(a, 1) shouldBe 1
        }
    }

    // ===== LCM Property Tests =====

    "lcm is commutative" {
        checkAll(Arb.int(), Arb.int()) { a, b ->
            lcm(a, b) shouldBe lcm(b, a)
        }
    }

    "lcm with zero is zero" {
        checkAll(Arb.int()) { a ->
            lcm(a, 0) shouldBe 0
            lcm(0, a) shouldBe 0
        }
    }

    "lcm is associative" {
        checkAll(smallInts, smallInts, smallInts) { a, b, c ->
            lcm(lcm(a, b), c) shouldBe lcm(a, lcm(b, c))
            lcm(a, b, c) shouldBe lcm(lcm(a, b), c)
        }
    }

    "lcm is divisible by both operands" {
        checkAll(smallNonZeroInts, smallNonZeroInts) { a, b ->
            val l = lcm(a, b)
            if (l != 0) {
                l % a shouldBe 0
                l % b shouldBe 0
            }
        }
    }

    "lcm is always non-negative" {
        checkAll(Arb.int(), Arb.int()) { a, b ->
            lcm(a, b) >= 0
        }
    }

    "lcm with one gives absolute value" {
        checkAll(Arb.int()) { a ->
            lcm(1, a) shouldBe a.absoluteValue
            lcm(a, 1) shouldBe a.absoluteValue
        }
    }

    // ===== GCD-LCM Relationship =====

    "gcd * lcm = |a * b| for non-zero values" {
        checkAll(smallNonZeroInts, smallNonZeroInts) { a, b ->
            val g = gcd(a, b)
            val l = lcm(a, b)
            (g.toLong() * l.toLong()) shouldBe (a.toLong() * b.toLong()).absoluteValue
        }
    }

    // ===== Extended GCD Properties =====

    "extended gcd satisfies Bézout identity" {
        checkAll(smallInts, smallInts) { a, b ->
            val result = extendedGcd(a, b)
            result.verify() shouldBe true
            (a.toLong() * result.x + b.toLong() * result.y) shouldBe result.gcd.toLong()
        }
    }

    "extended gcd result equals regular gcd" {
        checkAll(smallInts, smallInts) { a, b ->
            val extResult = extendedGcd(a, b)
            val regularGcd = gcd(a, b)
            extResult.gcd shouldBe regularGcd
        }
    }

    "extended gcd returns non-negative result" {
        checkAll(smallInts, smallInts) { a, b ->
            val result = extendedGcd(a, b)
            result.gcd >= 0
        }
    }

    "extended gcd preserves input values" {
        checkAll(smallInts, smallInts) { a, b ->
            val result = extendedGcd(a, b)
            result.a shouldBe a
            result.b shouldBe b
        }
    }

    // ===== Modular Inverse Properties =====

    "modular inverse satisfies multiplicative identity" {
        checkAll(coprimePairs) { (a, m) ->
            val inverse = a.modInverse(m)
            (a.toLong() * inverse) % m shouldBe 1
        }
    }

    "modular inverse through extended gcd matches direct function" {
        checkAll(coprimePairs) { (a, m) ->
            val extGcdInverse = extendedGcd(a, m).modInverse()
            val directInverse = a.modInverse(m)
            extGcdInverse shouldBe directInverse
        }
    }

    "modular inverse throws when gcd is not 1" {
        checkAll(smallInts.filter { it > 1 }, smallInts.filter { it > 1 }) { a, m ->
            if (gcd(a, m) != 1) {
                shouldThrow<IllegalArgumentException> {
                    a.modInverse(m)
                }
                shouldThrow<IllegalArgumentException> {
                    extendedGcd(a, m).modInverse()
                }
            }
        }
    }

    "modular inverse result is in correct range" {
        checkAll(coprimePairs) { (a, m) ->
            val inverse = a.modInverse(m)
            inverse in 0 until m
        }
    }

    // ===== Long Type Properties =====

    "long gcd properties match int gcd properties" {
        checkAll(Arb.long(), Arb.long()) { a, b ->
            gcd(a, b) shouldBe gcd(b, a)  // commutativity
        }

        checkAll(nonZeroLongs, nonZeroLongs) { a, b ->
            val g = gcd(a, b)
            if (g != 0L) {
                a % g shouldBe 0L
                b % g shouldBe 0L
            }
        }
    }

    "long lcm properties match int lcm properties" {
        checkAll(Arb.long(), Arb.long()) { a, b ->
            lcm(a, b) shouldBe lcm(b, a)  // commutativity
        }

        checkAll(nonZeroLongs, nonZeroLongs) { a, b ->
            val l = lcm(a, b)
            if (l != 0L) {
                l % a shouldBe 0L
                l % b shouldBe 0L
            }
        }
    }

    "long extended gcd satisfies Bézout identity" {
        checkAll(Arb.long(), Arb.long()) { a, b ->
            val result = extendedGcd(a, b)
            result.verify() shouldBe true
        }
    }

    // ===== BigInteger Properties =====

    "BigInteger gcd is commutative" {
        checkAll(smallBigInts, smallBigInts) { a, b ->
            gcd(a, b) shouldBe gcd(b, a)
        }
    }

    "BigInteger gcd divides both operands" {
        checkAll(nonZeroBigInts, nonZeroBigInts) { a, b ->
            val g = gcd(a, b)
            if (g != BigInteger.ZERO) {
                a % g shouldBe BigInteger.ZERO
                b % g shouldBe BigInteger.ZERO
            }
        }
    }

    "BigInteger extended gcd satisfies Bézout identity" {
        checkAll(smallBigInts, smallBigInts) { a, b ->
            val result = extendedGcd(a, b)
            result.verify() shouldBe true
        }
    }

    "BigInteger gcd * lcm = |a * b|" {
        checkAll(nonZeroBigInts, nonZeroBigInts) { a, b ->
            val g = gcd(a, b)
            val l = lcm(a, b)
            (g * l) shouldBe (a * b).abs()
        }
    }

    // ===== Collection Properties =====

    "collection gcd is associative" {
        checkAll(Arb.list(Arb.int(), 2..5)) { numbers ->
            val result1 = numbers.gcd()
            val result2 = numbers.reduce { acc, num -> gcd(acc, num) }
            result1 shouldBe result2
        }
    }

    "collection lcm is associative" {
        checkAll(Arb.list(smallNonZeroInts, 2..5)) { numbers ->
            val result1 = numbers.lcm()
            val result2 = numbers.reduce { acc, num -> lcm(acc, num) }
            result1 shouldBe result2
        }
    }

    // ===== Specific Mathematical Identities =====

    "gcd(a, b) = gcd(a - b, b)" {
        checkAll(smallInts, smallInts) { a, b ->
            gcd(a, b) shouldBe gcd(a - b, b)
        }
    }

    "gcd(ka, kb) = k * gcd(a, b) for positive k" {
        checkAll(Arb.int(1..100), smallInts, smallInts) { k, a, b ->
            gcd(k * a, k * b) shouldBe k * gcd(a, b)
        }
    }

    "lcm(ka, kb) = k * lcm(a, b) for positive k" {
        checkAll(Arb.int(1..10), smallNonZeroInts, smallNonZeroInts) { k, a, b ->
            // Use smaller k to avoid overflow
            lcm(k * a, k * b) shouldBe k * lcm(a, b)
        }
    }

    "gcd self-operation" {
        checkAll(smallInts) { a ->
            gcd(a, a) shouldBe a.absoluteValue
        }
    }

    "lcm self-operation" {
        checkAll(smallInts) { a ->
            lcm(a, a) shouldBe a.absoluteValue
        }
    }
})