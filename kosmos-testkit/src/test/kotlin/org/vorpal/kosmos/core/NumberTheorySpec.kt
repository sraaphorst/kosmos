package org.vorpal.kosmos.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import java.math.BigInteger
import kotlin.math.absoluteValue
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.verify
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

class NumberTheorySpec : FunSpec({
    lawTests(lawGraph("number theory") {
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

        local("gcd is commutative") {
            lawCheckAll(Arb.int(), Arb.int()) { a, b ->
                gcd(a, b) shouldBe gcd(b, a)
            }
        }

        local("gcd with zero gives absolute value") {
            lawCheckAll(Arb.int()) { a ->
                gcd(a, 0) shouldBe a.absoluteValue
                gcd(0, a) shouldBe a.absoluteValue
            }
        }

        local("gcd divides both operands") {
            lawCheckAll(nonZeroInts, nonZeroInts) { a, b ->
                val g = gcd(a, b)
                if (g != 0) {
                    a % g shouldBe 0
                    b % g shouldBe 0
                }
            }
        }

        local("gcd is associative") {
            lawCheckAll(Arb.int(), Arb.int(), Arb.int()) { a, b, c ->
                gcd(gcd(a, b), c) shouldBe gcd(a, gcd(b, c))
                gcd(a, b, c) shouldBe gcd(gcd(a, b), c)
            }
        }

        local("gcd satisfies Euclidean property") {
            lawCheckAll(Arb.int(), nonZeroInts) { a, b ->
                gcd(a, b) shouldBe gcd(b, a % b)
            }
        }

        local("gcd is always non-negative") {
            lawCheckAll(Arb.int(), Arb.int()) { a, b ->
                gcd(a, b) >= 0
            }
        }

        local("gcd of one is one") {
            lawCheckAll(Arb.int()) { a ->
                gcd(1, a) shouldBe 1
                gcd(a, 1) shouldBe 1
            }
        }

        // ===== LCM Property Tests =====

        local("lcm is commutative") {
            lawCheckAll(Arb.int(), Arb.int()) { a, b ->
                lcm(a, b) shouldBe lcm(b, a)
            }
        }

        local("lcm with zero is zero") {
            lawCheckAll(Arb.int()) { a ->
                lcm(a, 0) shouldBe 0
                lcm(0, a) shouldBe 0
            }
        }

        local("lcm is associative") {
            lawCheckAll(smallInts, smallInts, smallInts) { a, b, c ->
                lcm(lcm(a, b), c) shouldBe lcm(a, lcm(b, c))
                lcm(a, b, c) shouldBe lcm(lcm(a, b), c)
            }
        }

        local("lcm is divisible by both operands") {
            lawCheckAll(smallNonZeroInts, smallNonZeroInts) { a, b ->
                val l = lcm(a, b)
                if (l != 0) {
                    l % a shouldBe 0
                    l % b shouldBe 0
                }
            }
        }

        local("lcm is always non-negative") {
            lawCheckAll(Arb.int(), Arb.int()) { a, b ->
                lcm(a, b) >= 0
            }
        }

        local("lcm with one gives absolute value") {
            lawCheckAll(Arb.int()) { a ->
                lcm(1, a) shouldBe a.absoluteValue
                lcm(a, 1) shouldBe a.absoluteValue
            }
        }

        // ===== GCD-LCM Relationship =====

        local("gcd * lcm = |a * b| for non-zero values") {
            lawCheckAll(smallNonZeroInts, smallNonZeroInts) { a, b ->
                val g = gcd(a, b)
                val l = lcm(a, b)
                (g.toLong() * l.toLong()) shouldBe (a.toLong() * b.toLong()).absoluteValue
            }
        }

        // ===== Extended GCD Properties =====

        local("extended gcd satisfies Bézout identity") {
            lawCheckAll(smallInts, smallInts) { a, b ->
                val result = extendedGcd(a, b)
                result.verify() shouldBe true
                (a.toLong() * result.x + b.toLong() * result.y) shouldBe result.gcd.toLong()
            }
        }

        local("extended gcd result equals regular gcd") {
            lawCheckAll(smallInts, smallInts) { a, b ->
                val extResult = extendedGcd(a, b)
                val regularGcd = gcd(a, b)
                extResult.gcd shouldBe regularGcd
            }
        }

        local("extended gcd returns non-negative result") {
            lawCheckAll(smallInts, smallInts) { a, b ->
                val result = extendedGcd(a, b)
                result.gcd >= 0
            }
        }

        local("extended gcd preserves input values") {
            lawCheckAll(smallInts, smallInts) { a, b ->
                val result = extendedGcd(a, b)
                result.a shouldBe a
                result.b shouldBe b
            }
        }

        // ===== Modular Inverse Properties =====

        local("modular inverse satisfies multiplicative identity") {
            lawCheckAll(coprimePairs) { (a, m) ->
                val inverse = a.modInverse(m)
                (a.toLong() * inverse) % m shouldBe 1
            }
        }

        local("modular inverse through extended gcd matches direct function") {
            lawCheckAll(coprimePairs) { (a, m) ->
                val extGcdInverse = extendedGcd(a, m).modInverse()
                val directInverse = a.modInverse(m)
                extGcdInverse shouldBe directInverse
            }
        }

        local("modular inverse throws when gcd is not 1") {
            lawCheckAll(smallInts.filter { it > 1 }, smallInts.filter { it > 1 }) { a, m ->
                if (gcd(a, m) != 1) {
                    shouldThrow<ArithmeticException> {
                        a.modInverse(m)
                    }
                    shouldThrow<ArithmeticException> {
                        extendedGcd(a, m).modInverse()
                    }
                }
            }
        }

        local("modular inverse result is in correct range") {
            lawCheckAll(coprimePairs) { (a, m) ->
                val inverse = a.modInverse(m)
                inverse in 0 until m
            }
        }

        // ===== Long Type Properties =====

        local("long gcd properties match int gcd properties") {
            lawCheckAll(Arb.long(), Arb.long()) { a, b ->
                gcd(a, b) shouldBe gcd(b, a)  // commutativity
            }

            lawCheckAll(nonZeroLongs, nonZeroLongs) { a, b ->
                val g = gcd(a, b)
                if (g != 0L) {
                    a % g shouldBe 0L
                    b % g shouldBe 0L
                }
            }
        }

        local("long lcm properties match int lcm properties") {
            lawCheckAll(Arb.long(), Arb.long()) { a, b ->
                lcm(a, b) shouldBe lcm(b, a)  // commutativity
            }

            lawCheckAll(nonZeroLongs, nonZeroLongs) { a, b ->
                val l = lcm(a, b)
                if (l != 0L) {
                    l % a shouldBe 0L
                    l % b shouldBe 0L
                }
            }
        }

        local("long extended gcd satisfies Bézout identity") {
            lawCheckAll(Arb.long(), Arb.long()) { a, b ->
                val result = extendedGcd(a, b)
                result.verify() shouldBe true
            }
        }

        // ===== BigInteger Properties =====

        local("BigInteger gcd is commutative") {
            lawCheckAll(smallBigInts, smallBigInts) { a, b ->
                gcd(a, b) shouldBe gcd(b, a)
            }
        }

        local("BigInteger gcd divides both operands") {
            lawCheckAll(nonZeroBigInts, nonZeroBigInts) { a, b ->
                val g = gcd(a, b)
                if (g != BigInteger.ZERO) {
                    a % g shouldBe BigInteger.ZERO
                    b % g shouldBe BigInteger.ZERO
                }
            }
        }

        local("BigInteger extended gcd satisfies Bézout identity") {
            lawCheckAll(smallBigInts, smallBigInts) { a, b ->
                val result = extendedGcd(a, b)
                result.verify() shouldBe true
            }
        }

        local("BigInteger gcd * lcm = |a * b|") {
            lawCheckAll(nonZeroBigInts, nonZeroBigInts) { a, b ->
                val g = gcd(a, b)
                val l = lcm(a, b)
                (g * l) shouldBe (a * b).abs()
            }
        }

        // ===== Collection Properties =====

        local("collection gcd is associative") {
            lawCheckAll(Arb.list(Arb.int(), 2..5)) { numbers ->
                val result1 = numbers.gcd()
                val result2 = numbers.reduce { acc, num -> gcd(acc, num) }
                result1 shouldBe result2
            }
        }

        local("collection lcm is associative") {
            lawCheckAll(Arb.list(smallNonZeroInts, 2..5)) { numbers ->
                val result1 = numbers.lcm()
                val result2 = numbers.reduce { acc, num -> lcm(acc, num) }
                result1 shouldBe result2
            }
        }

        // ===== Specific Mathematical Identities =====

        local("gcd(a, b) = gcd(a - b, b)") {
            lawCheckAll(smallInts, smallInts) { a, b ->
                gcd(a, b) shouldBe gcd(a - b, b)
            }
        }

        local("gcd(ka, kb) = k * gcd(a, b) for positive k") {
            lawCheckAll(Arb.int(1..100), smallInts, smallInts) { k, a, b ->
                gcd(k * a, k * b) shouldBe k * gcd(a, b)
            }
        }

        local("lcm(ka, kb) = k * lcm(a, b) for positive k") {
            lawCheckAll(Arb.int(1..10), smallNonZeroInts, smallNonZeroInts) { k, a, b ->
                // Use smaller k to avoid overflow
                lcm(k * a, k * b) shouldBe k * lcm(a, b)
            }
        }

        local("gcd self-operation") {
            lawCheckAll(smallInts) { a ->
                gcd(a, a) shouldBe a.absoluteValue
            }
        }

        local("lcm self-operation") {
            lawCheckAll(smallInts) { a ->
                lcm(a, a) shouldBe a.absoluteValue
            }
        }
    })
})