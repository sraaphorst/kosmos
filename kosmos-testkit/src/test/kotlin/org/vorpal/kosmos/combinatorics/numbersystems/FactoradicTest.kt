package org.vorpal.kosmos.combinatorics.numbersystems

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import org.vorpal.kosmos.combinatorics.numbersystems.render.FactoradicPrinter
import java.math.BigInteger

class FactoradicTest : FunSpec({

    // Generator for non-negative BigIntegers
    val nonNegativeBigInt = arbitrary { rs ->
        val numBytes = rs.random.nextInt(0, 13) // 0-12 bytes for reasonable sizes
        if (numBytes == 0) {
            BigInteger.ZERO
        } else {
            val bytes = ByteArray(numBytes) { rs.random.nextInt().toByte() }
            BigInteger(1, bytes) // signum = 1 ensures non-negative
        }
    }

    // Generator for valid factoradic representations
    // Ensures: first element is 0 (if non-empty), digit at position i ∈ [0, i], no trailing zeros
    val validFactoradic = arbitrary { rs ->
        val length = rs.random.nextInt(0, 20)
        if (length <= 1) {
            if (rs.random.nextBoolean()) Factoradic.ZERO else Factoradic.ONE
        } else {
            val digits = mutableListOf(0) // First digit must be 0
            for (i in 1 until length) {
                digits.add(rs.random.nextInt(0, i + 1))
            }

            // Ensure no trailing zeros: last digit must be non-zero
            if (digits.last() == 0) {
                digits[digits.lastIndex] = rs.random.nextInt(1, length)
            }

            Factoradic.fromDigitsLs(digits)
        }
    }

    context("zero special case") {
        test("zero encodes to factoradic zero") {
            Factoradic.encode(BigInteger.ZERO) shouldBe Factoradic.ZERO
        }

        test("empty list decodes to zero in LS format") {
            Factoradic.fromDigitsLs(emptyList()) shouldBe Factoradic.ZERO
        }

        test("empty list decodes to zero in MS format") {
            Factoradic.fromDigitsMs(emptyList()) shouldBe Factoradic.ZERO
        }
    }

    context("round trip properties") {
        test("encode → decode is identity") {
            checkAll(nonNegativeBigInt) { n ->
                val encoded = Factoradic.encode(n)
                val decoded = encoded.decode()
                decoded shouldBe n
            }
        }

        test("decode → encode is identity for valid representations") {
            checkAll(validFactoradic) { repr ->
                val decoded = repr.decode()
                val encoded = Factoradic.encode(decoded)
                encoded shouldBe repr
            }
        }
    }

    context("canonical form properties") {
        test("encoded representations have no trailing zeros") {
            //checkAll(1000, nonNegativeBigInt.filter { it > BigInteger.ZERO }) { n ->
            checkAll(1000, nonNegativeBigInt) { n ->
                val encoded = Factoradic.encode(n)
                encoded.digits.isNotEmpty() && encoded.digits.last() != 0
            }
        }

        test("encoded digit at position i satisfies 0 ≤ digit ≤ i") {
            checkAll(1000, nonNegativeBigInt) { n ->
                val encoded = Factoradic.encode(n)
                encoded.digits.withIndex().all { (idx, digit) -> digit in 0..idx }
            }
        }

        test("encoded non-zero numbers start with 0") {
            checkAll(1000, nonNegativeBigInt.filter { it > BigInteger.ZERO }) { n ->
                val encoded = Factoradic.encode(n)
                encoded.digits.first() shouldBe 0
            }
        }
    }

    context("known values") {
        test("small integers") {
            val testCases = mapOf(
                BigInteger.ZERO to Factoradic.fromDigitsLs(emptyList()),
                BigInteger.ONE to Factoradic.fromDigitsLs(listOf(0, 1)),
                2.toBigInteger() to Factoradic.fromDigitsLs(listOf(0, 0, 1)),
                3.toBigInteger() to Factoradic.fromDigitsLs(listOf(0, 1, 1)),
                4.toBigInteger() to Factoradic.fromDigitsLs(listOf(0, 0, 2)),
                5.toBigInteger() to Factoradic.fromDigitsLs(listOf(0, 1, 2)),
                6.toBigInteger() to Factoradic.fromDigitsLs(listOf(0, 0, 0, 1)),
                7.toBigInteger() to  Factoradic.fromDigitsLs(listOf(0, 1, 0, 1)),
                8.toBigInteger() to Factoradic.fromDigitsLs(listOf(0, 0, 1, 1)),
                9.toBigInteger() to Factoradic.fromDigitsLs(listOf(0, 1, 1, 1)),
                10.toBigInteger() to Factoradic.fromDigitsLs(listOf(0, 0, 2, 1)),
                11.toBigInteger() to Factoradic.fromDigitsLs(listOf(0, 1, 2, 1))
            )

            testCases.forEach { (n, repr) ->
                Factoradic.encode(n) shouldBe repr
                repr.decode() shouldBe n
            }
        }

        test("larger known values") {
            // 23 = 0×0! + 1×1! + 2×2! + 3×3!
            //    = 0 + 1 + 4 + 18 = 23
            Factoradic.encode(23.toBigInteger()).digits shouldBe listOf(0, 1, 2, 3)
            Factoradic.fromDigitsLs(listOf(0, 1, 2, 3)).decode() shouldBe 23.toBigInteger()

            // 463 = 0×0! + 1×1! + 0×2! + 1×3! + 4×4! + 3×5!
            //     = 0 + 1 + 0 + 6 + 96 + 360 = 463
            Factoradic.encode(463.toBigInteger()).digits shouldBe listOf(0, 1, 0, 1, 4, 3)
            Factoradic.fromDigitsLs(listOf(0, 1, 0, 1, 4, 3)).decode() shouldBe 463.toBigInteger()

            // 719 = 5! + 4! - 1 = 0×0! + 1×1! + 2×2! + 3×3! + 4×4! + 5×5!
            //     = 0 + 1 + 4 + 18 + 96 + 600 = 719
            Factoradic.encode(719.toBigInteger()).digits shouldBe listOf(0, 1, 2, 3, 4, 5)
            Factoradic.fromDigitsLs(listOf(0, 1, 2, 3, 4, 5)).decode() shouldBe 719.toBigInteger()
        }
    }

    context("encode error handling") {
        test("rejects negative numbers") {
            shouldThrow<IllegalArgumentException> {
                Factoradic.encode((-1).toBigInteger())
            }

            shouldThrow<IllegalArgumentException> {
                Factoradic.encode((-100).toBigInteger())
            }

            checkAll(100, Arb.bigInt(-1000..-1)) { negative ->
                shouldThrow<IllegalArgumentException> {
                    Factoradic.encode(negative)
                }
            }
        }
    }

    context("decode error handling") {
        test("rejects digit out of range for position") {
            // Digit 1 at position 0 is invalid (only 0 allowed)
            shouldThrow<IllegalArgumentException> {
                Factoradic.fromDigitsLs(listOf(1))
            }

            // Digit 2 at position 0 is invalid
            shouldThrow<IllegalArgumentException> {
                Factoradic.fromDigitsLs(listOf(2))
            }

            // Digit 3 at position 1 is invalid (0 or 1 allowed)
            shouldThrow<IllegalArgumentException> {
                Factoradic.fromDigitsLs(listOf(0, 3))
            }

            // Digit 3 at position 2 is invalid (0, 1, or 2 allowed)
            shouldThrow<IllegalArgumentException> {
                Factoradic.fromDigitsLs(listOf(0, 1, 3))
            }

            // Digit 5 at position 3 is invalid (0, 1, 2, or 3 allowed)
            shouldThrow<IllegalArgumentException> {
                Factoradic.fromDigitsLs(listOf(0, 1, 2, 5))
            }
        }

        test("rejects trailing zeros (non-canonical form)") {
            shouldThrow<IllegalArgumentException> {
                Factoradic.fromDigitsLs(listOf(0, 1, 0))
            }

            shouldThrow<IllegalArgumentException> {
                Factoradic.fromDigitsLs(listOf(0, 0, 1, 0))
            }

            shouldThrow<IllegalArgumentException> {
                Factoradic.fromDigitsLs(listOf(0, 1, 2, 1, 0))
            }

            shouldThrow<IllegalArgumentException> {
                Factoradic.fromDigitsLs(listOf(0, 0, 0, 0, 1, 0))
            }
        }

        test("accepts valid edge cases") {
            // Single digit [0, 1] is valid
            Factoradic.fromDigitsLs(listOf(0, 1)) shouldBe Factoradic.ONE

            // Multiple zeros followed by non-zero is valid
            Factoradic.fromDigitsLs(listOf(0, 0, 0, 0, 0, 1)) shouldBe
                Factoradic.encode((0 * 1 + 0 * 1 + 0 * 2 + 0 * 6 + 0 * 24 + 1 * 120).toBigInteger())

            // Maximum digits at each position
            // [0, 1, 2, 3] = 0 + 1 + 4 + 18 = 23
            Factoradic.fromDigitsLs(listOf(0, 1, 2, 3)) shouldBe Factoradic.encode(23.toBigInteger())
        }
    }

    context("mathematical properties") {
        test("n < (k+1)! implies encoded length ≤ k+1") {
            // If n < 5! = 120, then factoradic length should be at most 5
            checkAll(100, Arb.bigInt(0..119)) { n ->
                val encoded = Factoradic.encode(n)
                encoded.size <= 5
            }

            // If n < 6! = 720, then factoradic length should be at most 6
            checkAll(100, Arb.bigInt(0..719)) { n ->
                val encoded = Factoradic.encode(n)
                encoded.size <= 6
            }
        }

        test("maximum value with k digits is (k)! - 1") {
            // Maximum with 4 digits: [0, 1, 2, 3] = 23 = 4! - 1
            Factoradic.encode(23.toBigInteger()).size shouldBe 4
            Factoradic.encode(24.toBigInteger()).size shouldBe 5

            // Maximum with 5 digits: [0, 1, 2, 3, 4] = 119 = 5! - 1
            Factoradic.encode(119.toBigInteger()).size shouldBe 5
            Factoradic.encode(120.toBigInteger()).size shouldBe 6
        }
    }

    test("FactoradicPrintable produces expected results") {
        FactoradicPrinter(Factoradic.fromDigitsLs(listOf(0, 1, 0, 3, 2))) shouldBe "(2 3 0 1 0)_!"
        FactoradicPrinter(Factoradic.ZERO) shouldBe "()_!"
        FactoradicPrinter(Factoradic.ONE) shouldBe "(1 0)_!"
    }
})
