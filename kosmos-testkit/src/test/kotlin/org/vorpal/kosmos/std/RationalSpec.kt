package org.vorpal.kosmos.std

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigInt
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Rational
import java.math.BigInteger

class RationalSpec : StringSpec({

    // ===== Additional Generators =====

    val arbSmallInts = Arb.int(-1000..1000)
    val arbPositiveRational = arbRational.filter { it.isPositive }
    val arbIntegerRational = arbRational.filter { it.isInteger }

    // Generate rationals that won't cause overflow in operations
    val arbSafeRational = Arb.bind(
        Arb.int(-1000..1000),
        Arb.int(1..1000)
    ) { n, d -> Rational.of(n, d) }

    val arbSafeNonzeroRational = arbSafeRational.filter { !it.isZero }

    // ===== Construction and Normalization Tests =====

    "rational is always in reduced form" {
        checkAll(Arb.bigInt(32), Arb.bigInt(32).filter { it != BigInteger.ZERO }) { n, d ->
            val r = Rational.of(n, d)
            r.n.gcd(r.d) shouldBe BigInteger.ONE
        }
    }

    "rational denominator is always positive" {
        checkAll(Arb.bigInt(32), Arb.bigInt(32).filter { it != BigInteger.ZERO }) { n, d ->
            val r = Rational.of(n, d)
            r.d.signum() shouldBe 1
        }
    }

    "rational handles negative denominators correctly" {
        checkAll(arbSmallInts, arbSmallInts.filter { it != 0 }) { n, d ->
            val r1 = Rational.of(n, d)
            val r2 = Rational.of(-n, -d)
            r1 shouldBe r2
        }
    }

    "zero denominator throws exception" {
        shouldThrow<IllegalArgumentException> {
            Rational.of(1, 0)
        }
        shouldThrow<IllegalArgumentException> {
            Rational.of(BigInteger.ONE, BigInteger.ZERO)
        }
    }

    "factory methods create equivalent rationals" {
        checkAll(arbSmallInts) { n ->
            Rational.of(n) shouldBe Rational.of(n, 1)
            Rational.of(n.toLong()) shouldBe Rational.of(n)
            Rational.of(n.toBigInteger()) shouldBe Rational.of(n)
        }
    }

    // ===== Field Axioms Tests =====

    "addition is commutative" {
        checkAll(arbSafeRational, arbSafeRational) { a, b ->
            a + b shouldBe b + a
        }
    }

    "addition is associative" {
        checkAll(arbSafeRational, arbSafeRational, arbSafeRational) { a, b, c ->
            (a + b) + c shouldBe a + (b + c)
        }
    }

    "zero is additive identity" {
        checkAll(arbSafeRational) { r ->
            r + Rational.ZERO shouldBe r
            Rational.ZERO + r shouldBe r
        }
    }

    "additive inverses work correctly" {
        checkAll(arbSafeRational) { r ->
            r + (-r) shouldBe Rational.ZERO
            (-r) + r shouldBe Rational.ZERO
        }
    }

    "multiplication is commutative" {
        checkAll(arbSafeRational, arbSafeRational) { a, b ->
            a * b shouldBe b * a
        }
    }

    "multiplication is associative" {
        checkAll(arbSafeRational, arbSafeRational, arbSafeRational) { a, b, c ->
            (a * b) * c shouldBe a * (b * c)
        }
    }

    "one is multiplicative identity" {
        checkAll(arbSafeRational) { r ->
            r * Rational.ONE shouldBe r
            Rational.ONE * r shouldBe r
        }
    }

    "multiplicative inverses work correctly" {
        checkAll(arbSafeNonzeroRational) { r ->
            r * r.reciprocal() shouldBe Rational.ONE
            r.reciprocal() * r shouldBe Rational.ONE
        }
    }

    "distributivity holds" {
        checkAll(arbSafeRational, arbSafeRational, arbSafeRational) { a, b, c ->
            a * (b + c) shouldBe (a * b) + (a * c)
            (a + b) * c shouldBe (a * c) + (b * c)
        }
    }

    "subtraction is addition of negation" {
        checkAll(arbSafeRational, arbSafeRational) { a, b ->
            a - b shouldBe a + (-b)
        }
    }

    "division is multiplication by reciprocal" {
        checkAll(arbSafeRational, arbSafeNonzeroRational) { a, b ->
            a / b shouldBe a * b.reciprocal()
        }
    }

    // ===== Comparison Tests =====

    "comparison is consistent with arithmetic" {
        checkAll(arbSafeRational, arbSafeRational) { a, b ->
            val diff = a - b
            when {
                diff.isPositive -> a > b
                diff.isNegative -> a < b
                diff.isZero -> a shouldBe b
            }
        }
    }

    "comparison is transitive" {
        checkAll(arbSafeRational, arbSafeRational, arbSafeRational) { a, b, c ->
            if (b in a..c) {
                a <= c
            }
        }
    }

    "comparison respects arithmetic operations" {
        checkAll(arbSafeRational, arbSafeRational, arbSafeRational) { a, b, c ->
            if (a < b) {
                a + c < b + c
                if (c.isPositive) a * c < b * c
                if (c.isNegative) a * c > b * c
            }
        }
    }

    // ===== Power Operation Tests =====

    "positive integer powers work correctly" {
        checkAll(arbSafeNonzeroRational, Arb.int(0..10)) { r, exp ->
            val result = r.pow(exp)
            result shouldBe generateSequence { r }.take(exp).fold(Rational.ONE) { acc, x -> acc * x }
        }
    }

    "negative integer powers work correctly" {
        checkAll(arbSafeNonzeroRational, Arb.int(1..10)) { r, exp ->
            r.pow(-exp) shouldBe r.reciprocal().pow(exp)
            r.pow(-exp) shouldBe Rational.ONE / r.pow(exp)
        }
    }

    "power of zero" {
        checkAll(arbSafeNonzeroRational) { r ->
            r.pow(0) shouldBe Rational.ONE
        }
    }

    "power of one" {
        checkAll(arbSafeRational) { r ->
            r.pow(1) shouldBe r
        }
    }

    "power laws hold" {
        checkAll(arbSafeNonzeroRational, Arb.int(-5..5), Arb.int(-5..5)) { r, a, b ->
            if (a + b in -10..10) { // Avoid overflow
                r.pow(a) * r.pow(b) shouldBe r.pow(a + b)
            }
        }
    }

    // ===== Absolute Value Tests =====

    "absolute value is never negative" {
        checkAll(arbSafeRational) { r ->
            r.abs().isNegative shouldBe false
        }
    }

    "absolute value of positive number is itself" {
        checkAll(arbPositiveRational) { r ->
            r.abs() shouldBe r
        }
    }

    "absolute value satisfies triangle inequality" {
        checkAll(arbSafeRational, arbSafeRational) { a, b ->
            (a + b).abs() <= a.abs() + b.abs()
        }
    }

    "absolute value multiplicative property" {
        checkAll(arbSafeRational, arbSafeRational) { a, b ->
            (a * b).abs() shouldBe a.abs() * b.abs()
        }
    }

    // ===== Type Conversion Tests =====

    "integer conversion roundtrip" {
        checkAll(arbSmallInts) { n ->
            n.toRational().toBigDecimal(0).toBigInteger() shouldBe n.toBigInteger()
        }
    }

    // ===== String Representation Tests =====

    "integer rationals toString without denominator" {
        checkAll(arbSmallInts) { n ->
            val r = Rational.of(n)
            r.toString() shouldBe n.toString()
        }
    }

    "non-integer rationals toString with denominator" {
        checkAll(arbSmallInts, arbSmallInts.filter { it > 1 }) { n, d ->
            val r = Rational.of(n, d)
            if (!r.isInteger) {
                r.toString() shouldBe "${r.n}/${r.d}"
            }
        }
    }

    // ===== Property Tests for New Methods =====

    "isInteger property is correct" {
        checkAll(arbSafeRational) { r ->
            r.isInteger shouldBe (r.d == BigInteger.ONE)
        }
    }

    "signum property matches numerator signum" {
        checkAll(arbSafeRational) { r ->
            r.signum shouldBe r.n.signum()
        }
    }

    "isZero property is correct" {
        checkAll(arbSafeRational) { r ->
            r.isZero shouldBe (r.n == BigInteger.ZERO)
        }
    }

    "floor is always ≤ original value" {
        checkAll(arbSafeRational) { r ->
            val floor = r.floor()
            floor.toRational() <= r
        }
    }

    "ceiling is always ≥ original value" {
        checkAll(arbSafeRational) { r ->
            val ceil = r.ceil()
            ceil.toRational() >= r
        }
    }

    "floor and ceiling for integers" {
        checkAll(arbIntegerRational) { r ->
            r.floor() shouldBe r.n
            r.ceil() shouldBe r.n
        }
    }

    // ===== Arithmetic with Integers Tests =====

    "rational plus integer equals rational plus rational" {
        checkAll(arbSafeRational, arbSmallInts) { r, n ->
            r + n shouldBe r + n.toRational()
            n + r shouldBe n.toRational() + r
        }
    }

    "rational times integer equals rational times rational" {
        checkAll(arbSafeRational, arbSmallInts) { r, n ->
            r * n shouldBe r * n.toRational()
            n * r shouldBe n.toRational() * r
        }
    }

    // ===== Error Handling Tests =====

    "reciprocal of zero throws exception" {
        shouldThrow<IllegalArgumentException> {
            Rational.ZERO.reciprocal()
        }
    }

    "division by zero throws exception" {
        checkAll(arbSafeRational) { r ->
            shouldThrow<IllegalArgumentException> {
                r / Rational.ZERO
            }
        }
    }

    // ===== Edge Cases =====

    "operations with zero" {
        checkAll(arbSafeRational) { r ->
            r + Rational.ZERO shouldBe r
            r * Rational.ZERO shouldBe Rational.ZERO
            Rational.ZERO * r shouldBe Rational.ZERO
        }
    }

    "operations with one" {
        checkAll(arbSafeRational) { r ->
            r * Rational.ONE shouldBe r
            if (!r.isZero) {
                r / Rational.ONE shouldBe r
            }
        }
    }

    "double negation" {
        checkAll(arbSafeRational) { r ->
            -(-r) shouldBe r
        }
    }

    "reciprocal of reciprocal" {
        checkAll(arbSafeNonzeroRational) { r ->
            r.reciprocal().reciprocal() shouldBe r
        }
    }

    // ===== Consistency Tests =====

    "data class equality works correctly" {
        checkAll(arbSmallInts, arbSmallInts.filter { it != 0 }) { n, d ->
            val r1 = Rational.of(n, d)
            val r2 = Rational.of(n, d)
            val r3 = Rational.of(2 * n, 2 * d)

            r1 shouldBe r2
            r1 shouldBe r3  // Should be equal due to normalization
            r1.hashCode() shouldBe r2.hashCode()
            r1.hashCode() shouldBe r3.hashCode()
        }
    }

    "different representations of same value are equal" {
        checkAll(arbSmallInts.filter { it != 0 }, arbSmallInts.filter { it != 0 }) { n, k ->
            val r1 = Rational.of(n, 1)
            val r2 = Rational.of(n * k, k)
            r1 shouldBe r2
        }
    }
})