package org.vorpal.kosmos.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.syntax.*
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.std.ArbRational
import java.math.BigInteger

class RationalSpec : StringSpec({

    // ===== Construction and normalization =====

    "rational is always in reduced form" {
        checkAll(ArbRational.wide) { q ->
            q.n.gcd(q.d) shouldBe BigInteger.ONE
        }
    }

    "rational denominator is always positive" {
        checkAll(ArbRational.wide) { q ->
            q.d.signum() shouldBe 1
        }
    }

    "negative denominator is normalized into the numerator" {
        checkAll(ArbRational.wide) { q ->
            q shouldBe Rational.of(-q.n, -q.d)
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
        checkAll(ArbInteger.small) { n ->
            Rational.of(n) shouldBe Rational.of(n, BigInteger.ONE)
            Rational.of(n.toLong()) shouldBe Rational.of(n)
            Rational.of(n.toInt()) shouldBe Rational.of(n)
        }
    }

    // ===== Basic arithmetic =====

    "addition is commutative" {
        checkAll(ArbRational.small, ArbRational.small) { a, b ->
            a + b shouldBe b + a
        }
    }

    "addition is associative" {
        checkAll(ArbRational.small, ArbRational.small, ArbRational.small) { a, b, c ->
            (a + b) + c shouldBe a + (b + c)
        }
    }

    "zero is additive identity" {
        checkAll(ArbRational.wide) { r ->
            r + Rational.ZERO shouldBe r
            Rational.ZERO + r shouldBe r
        }
    }

    "additive inverse works" {
        checkAll(ArbRational.wide) { r ->
            r + (-r) shouldBe Rational.ZERO
            (-r) + r shouldBe Rational.ZERO
        }
    }

    "multiplication is commutative" {
        checkAll(ArbRational.small, ArbRational.small) { a, b ->
            a * b shouldBe b * a
        }
    }

    "multiplication is associative" {
        checkAll(ArbRational.small, ArbRational.small, ArbRational.small) { a, b, c ->
            (a * b) * c shouldBe a * (b * c)
        }
    }

    "one is multiplicative identity" {
        checkAll(ArbRational.wide) { r ->
            r * Rational.ONE shouldBe r
            Rational.ONE * r shouldBe r
        }
    }

    "multiplicative inverse works for nonzero rationals" {
        checkAll(ArbRational.nonZeroWide) { r ->
            r * r.reciprocal() shouldBe Rational.ONE
            r.reciprocal() * r shouldBe Rational.ONE
        }
    }

    "distributivity holds" {
        checkAll(ArbRational.small, ArbRational.small, ArbRational.small) { a, b, c ->
            a * (b + c) shouldBe (a * b) + (a * c)
            (a + b) * c shouldBe (a * c) + (b * c)
        }
    }

    "subtraction is addition of negation" {
        checkAll(ArbRational.small, ArbRational.small) { a, b ->
            a - b shouldBe a + (-b)
        }
    }

    "division is multiplication by reciprocal" {
        checkAll(ArbRational.wide, ArbRational.nonZeroWide) { a, b ->
            a / b shouldBe a * b.reciprocal()
        }
    }

    // ===== Comparison =====

    "comparison is consistent with subtraction" {
        checkAll(ArbRational.small, ArbRational.small) { a, b ->
            val diff = a - b
            when {
                diff.isPositive -> (a > b).shouldBeTrue()
                diff.isNegative -> (a < b).shouldBeTrue()
                else -> a shouldBe b
            }
        }
    }

    "comparison is transitive" {
        checkAll(ArbRational.small, ArbRational.small, ArbRational.small) { a, b, c ->
            if (b in a..c) {
                (a <= c).shouldBeTrue()
            }
        }
    }

    "addition preserves strict order" {
        checkAll(ArbRational.small, ArbRational.small, ArbRational.small) { a, b, c ->
            if (a < b) (a + c < b + c).shouldBeTrue()
        }
    }

    "multiplication by a positive rational preserves strict order" {
        checkAll(ArbRational.small, ArbRational.small, ArbRational.positive) { a, b, c ->
            if (a < b) (a * c < b * c).shouldBeTrue()
        }
    }

    "multiplication by a negative rational reverses strict order" {
        checkAll(ArbRational.small, ArbRational.small, ArbRational.negative) { a, b, c ->
            if (a < b) (a * c > b * c).shouldBeTrue()
        }
    }

    // ===== Powers =====

    "nonnegative integer powers work correctly" {
        checkAll(ArbRational.nonZero, Arb.int(0..10)) { r, exp ->
            val expected =
                generateSequence { r }
                    .take(exp)
                    .fold(Rational.ONE) { acc, x -> acc * x }

            r.pow(exp) shouldBe expected
        }
    }

    "negative integer powers work correctly" {
        checkAll(ArbRational.nonZero, Arb.int(1..10)) { r, exp ->
            r.pow(-exp) shouldBe r.reciprocal().pow(exp)
            r.pow(-exp) shouldBe Rational.ONE / r.pow(exp)
        }
    }

    "zero power is one for nonzero rationals" {
        checkAll(ArbRational.nonZero) { r ->
            r.pow(0) shouldBe Rational.ONE
        }
    }

    "first power is identity" {
        checkAll(ArbRational.nonZero) { r ->
            r.pow(1) shouldBe r
        }
    }

    "power law x^a * x^b = x^(a+b) holds where defined" {
        checkAll(ArbRational.nonZero, Arb.int(-5..5), Arb.int(-5..5)) { r, a, b ->
            r.pow(a) * r.pow(b) shouldBe r.pow(a + b)
        }
    }

    // ===== Absolute value =====

    "absolute value is never negative" {
        checkAll(ArbRational.wide) { r ->
            r.abs().isNegative.shouldBeFalse()
        }
    }

    "absolute value of a positive rational is itself" {
        checkAll(ArbRational.positive) { r ->
            r.abs() shouldBe r
        }
    }

    "absolute value satisfies triangle inequality" {
        checkAll(ArbRational.small, ArbRational.small) { a, b ->
            (a + b).abs() shouldBeLessThanOrEqualTo (a.abs() + b.abs())
        }
    }

    "absolute value is multiplicative" {
        checkAll(ArbRational.small, ArbRational.small) { a, b ->
            (a * b).abs() shouldBe a.abs() * b.abs()
        }
    }

    // ===== Floor, ceil, frac, rem =====

    "floor is always <= original value" {
        checkAll(ArbRational.small) { r ->
            r.floor().toRational() shouldBeLessThanOrEqualTo r
        }
    }

    "ceiling is always >= original value" {
        checkAll(ArbRational.small) { r ->
            r.ceil().toRational() shouldBeGreaterThanOrEqualTo r
        }
    }

    "floor and ceiling agree with integers" {
        checkAll(ArbRational.integer) { r ->
            r.floor() shouldBe r.n
            r.ceil() shouldBe r.n
        }
    }

    "fractional part lies in [0, 1)" {
        checkAll(ArbRational.small) { r ->
            val f = r.frac()
            (f >= Rational.ZERO).shouldBeTrue()
            (f < Rational.ONE).shouldBeTrue()
        }
    }

    "remainder modulo a positive rational lies in [0, modulus)" {
        checkAll(ArbRational.small, ArbRational.positive) { a, m ->
            val r = a % m
            (r >= Rational.ZERO).shouldBeTrue()
            (r < m).shouldBeTrue()
        }
    }

    "a = m * floor(a / m) + (a % m) for positive modulus" {
        checkAll(ArbRational.small, ArbRational.positive) { a, m ->
            val q = (a / m).floor().toRational()
            a shouldBe m * q + (a % m)
        }
    }

    // ===== Type conversions and properties =====

    "integer conversion roundtrip through zero-scale decimal works for small integers" {
        checkAll(ArbInteger.small) { n ->
            n.toRational().toBigDecimal(0).toBigInteger() shouldBe n
        }
    }

    "isInteger property is correct" {
        checkAll(ArbRational.small) { r ->
            r.isInteger shouldBe (r.d == BigInteger.ONE)
        }
    }

    "signum matches numerator signum" {
        checkAll(ArbRational.small) { r ->
            r.signum shouldBe r.n.signum()
        }
    }

    "isZero property is correct" {
        checkAll(ArbRational.small) { r ->
            r.isZero shouldBe (r.n == BigInteger.ZERO)
        }
    }

    // ===== String form =====

    "integer rationals render without denominator" {
        checkAll(ArbInteger.small) { n ->
            Rational.of(n).toString() shouldBe n.toString()
        }
    }

    "non-integer rationals render as n/d" {
        checkAll(ArbInteger.small, ArbInteger.positiveSmall) { n, d ->
            val r = Rational.of(n, d)
            if (!r.isInteger) {
                r.toString() shouldBe "${r.n}/${r.d}"
            }
        }
    }

    // ===== Arithmetic with integers =====

    "rational plus integer agrees with rational plus embedded integer" {
        checkAll(ArbRational.small, ArbInteger.small) { r, n ->
            r + n shouldBe r + n.toRational()
            n + r shouldBe n.toRational() + r
        }
    }

    "rational minus integer agrees with rational minus embedded integer" {
        checkAll(ArbRational.small, ArbInteger.small) { r, n ->
            r - n shouldBe r - n.toRational()
        }
    }

    "rational times integer agrees with rational times embedded integer" {
        checkAll(ArbRational.small, ArbInteger.small) { r, n ->
            r * n shouldBe r * n.toRational()
            n * r shouldBe n.toRational() * r
        }
    }

    "rational divided by integer agrees with rational divided by embedded integer" {
        checkAll(ArbRational.small, ArbInteger.nonZeroSmall) { r, n ->
            r / n shouldBe r / n.toRational()
        }
    }

    // ===== Error handling =====

    "reciprocal of zero throws exception" {
        shouldThrow<IllegalArgumentException> {
            Rational.ZERO.reciprocal()
        }
    }

    "division by zero throws exception" {
        checkAll(ArbRational.small) { r ->
            shouldThrow<IllegalArgumentException> {
                r / Rational.ZERO
            }
        }
    }

    "modulo by zero throws exception" {
        checkAll(ArbRational.small) { r ->
            shouldThrow<IllegalArgumentException> {
                r % Rational.ZERO
            }
        }
    }

    "modulo by a negative rational throws exception" {
        checkAll(ArbRational.small, ArbRational.negative) { r, m ->
            shouldThrow<IllegalArgumentException> {
                r % m
            }
        }
    }

    // ===== Equality and normalization consistency =====

    "equal normalized rationals have equal hash codes" {
        checkAll(ArbInteger.small, ArbInteger.nonZeroSmall) { n, d ->
            val r1 = Rational.of(n, d)
            val r2 = Rational.of(n, d)

            r1 shouldBe r2
            r1.hashCode() shouldBe r2.hashCode()
        }
    }

    "different presentations of the same rational are equal" {
        checkAll(ArbInteger.nonZeroSmall, ArbInteger.nonZeroSmall) { n, k ->
            val r1 = Rational.of(n)
            val r2 = Rational.of(n * k, k)

            r1 shouldBe r2
            r1.hashCode() shouldBe r2.hashCode()
        }
    }

    "double negation is identity" {
        checkAll(ArbRational.small) { r ->
            -(-r) shouldBe r
        }
    }

    "reciprocal of reciprocal is identity on nonzero rationals" {
        checkAll(ArbRational.nonZero) { r ->
            r.reciprocal().reciprocal() shouldBe r
        }
    }
})
