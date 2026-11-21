package org.vorpal.kosmos.core.ops

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigInteger
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Tests for [ExactDivOps], which supplies *exact* division operations `q = a / b`
 * (so that `a = b · q`) for use in fraction-free algorithms such as Bareiss elimination.
 *
 * Unlike field division, these are expected to fail loudly when the division is not exact.
 */
class ExactDivOpsSpec : FunSpec({
    lawTests(lawGraph("exact division") {
        branch("ExactDivOps.bigInteger") {
            val div = ExactDivOps.bigInteger

            local("divides exactly when the remainder is zero") {
                div(12.toBigInteger(), 3.toBigInteger()) shouldBe 4.toBigInteger()
                div((-12).toBigInteger(), 4.toBigInteger()) shouldBe (-3).toBigInteger()
            }

            local("round-trips exact products: (a·b) / b = a") {
                lawCheckAll(ArbInteger.small, ArbInteger.nonZeroSmall) { a, b ->
                    div(a.multiply(b), b) shouldBe a
                }
            }

            local("throws when the division is not exact") {
                shouldThrow<ArithmeticException> {
                    div(7.toBigInteger(), 2.toBigInteger())
                }
            }

            local("throws on division by zero") {
                shouldThrow<ArithmeticException> {
                    div(BigInteger.ONE, BigInteger.ZERO)
                }
            }
        }

        branch("ExactDivOps.fromField over the rationals") {
            val q = RationalAlgebras.RationalField
            val eqQ = RationalAlgebras.eqRational
            val div = ExactDivOps.fromField(q)

            local("round-trips: (a / b) · b = a for nonzero b") {
                lawCheckAll(ArbRational.small, ArbRational.nonZero) { a, b ->
                    eqQ(q.mul(div(a, b), b), a) shouldBe true
                }
            }

            local("agrees with field reciprocal multiplication") {
                lawCheckAll(ArbRational.small, ArbRational.nonZero) { a, b ->
                    eqQ(div(a, b), q.mul(a, q.reciprocal(b))) shouldBe true
                }
            }

            local("throws on division by zero") {
                shouldThrow<ArithmeticException> {
                    div(Rational.ONE, Rational.ZERO)
                }
            }
        }
    })
})
