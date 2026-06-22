package org.vorpal.kosmos.core.ops

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

/**
 * Tests for [ExactDivOps], which supplies *exact* division operations `q = a / b`
 * (so that `a = b · q`) for use in fraction-free algorithms such as Bareiss elimination.
 *
 * Unlike field division, these are expected to fail loudly when the division is not exact.
 */
class ExactDivOpsSpec : FunSpec({

    context("ExactDivOps.bigInteger") {
        val div = ExactDivOps.bigInteger

        test("divides exactly when the remainder is zero") {
            div(12.toBigInteger(), 3.toBigInteger()) shouldBe 4.toBigInteger()
            div((-12).toBigInteger(), 4.toBigInteger()) shouldBe (-3).toBigInteger()
        }

        test("round-trips exact products: (a·b) / b = a") {
            checkAll(ArbInteger.small, ArbInteger.nonZeroSmall) { a, b ->
                div(a.multiply(b), b) shouldBe a
            }
        }

        test("throws when the division is not exact") {
            shouldThrow<IllegalArgumentException> {
                div(7.toBigInteger(), 2.toBigInteger())
            }
        }

        test("throws on division by zero") {
            shouldThrow<IllegalArgumentException> {
                div(BigInteger.ONE, BigInteger.ZERO)
            }
        }
    }

    context("ExactDivOps.fromField over the rationals") {
        val q = RationalAlgebras.RationalField
        val eqQ = RationalAlgebras.eqRational
        val div = ExactDivOps.fromField(q)

        test("round-trips: (a / b) · b = a for nonzero b") {
            checkAll(ArbRational.small, ArbRational.nonZero) { a, b ->
                eqQ(q.mul(div(a, b), b), a) shouldBe true
            }
        }

        test("agrees with field reciprocal multiplication") {
            checkAll(ArbRational.small, ArbRational.nonZero) { a, b ->
                eqQ(div(a, b), q.mul(a, q.reciprocal(b))) shouldBe true
            }
        }

        test("throws on division by zero") {
            shouldThrow<IllegalArgumentException> {
                div(Rational.ONE, Rational.ZERO)
            }
        }
    }
})
