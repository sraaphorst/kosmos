package org.vorpal.kosmos.laws

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import kotlinx.coroutines.CancellationException
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.FieldLaws
import org.vorpal.kosmos.laws.property.InverseExistenceLaw
import org.vorpal.kosmos.laws.property.InvertibilityLaw
import org.vorpal.kosmos.laws.property.LeftInvertibilityLaw
import org.vorpal.kosmos.laws.property.RightInvertibilityLaw

private val ints = Arb.int(-5..5)
private val add = BinOp<Int>("+") { a, b -> a + b }

/** Negation, except that it has no answer at 3. */
private val negationMissingAt3 = UnaryOp<Int, Int?>("-") { a -> if (a == 3) null else -a }

/** Negation, except that it throws at 3. */
private val negationThrowingAt3 = UnaryOp<Int, Int?>("-") { a ->
    if (a == 3) throw ArithmeticException("no inverse of 3") else -a
}

private val q = RationalAlgebras.RationalField
private val rationals: Arb<Rational> = Arb.bind(Arb.int(-20..20), Arb.int(1..12)) { n, d -> Rational.of(n, d) }

/** ℚ whose reciprocal is also (wrongly) undefined at 2. */
private val fieldMissingReciprocalOf2: Field<Rational> = Field.of(
    add = q.add,
    mul = q.mul,
    reciprocal = Endo(q.reciprocal.symbol) { x ->
        if (x == Rational.of(2, 1)) throw ArithmeticException("no reciprocal of 2") else q.reciprocal(x)
    }
)

private fun Throwable.causes(): Sequence<Throwable> = generateSequence(this) { it.cause.takeIf { c -> c !== it } }

private fun LawReport.leafStatus(label: String): LawStatus =
    root.leaves().first { it.label == label }.status

/**
 * Negative tests for required inverses (#61): the inverse laws must reject structures whose inverse is missing,
 * not only accept valid ones.
 */
class RequiredInverseSpec : FunSpec({

    context("a missing inverse") {
        test("an inverse that returns null fails inverse existence") {
            val report = InverseExistenceLaw(add, 0, ints, negationMissingAt3).verify()
            report.status shouldBe LawStatus.FAILED
            report.problems.single().diagnostics.shouldNotBeNull().cause.message.shouldNotBeNull() shouldContain "undefined"
        }

        test("the invertibility suite reports it as a missing inverse, not as a wrong one") {
            val report = InvertibilityLaw(add, 0, ints, negationMissingAt3).verify()
            report.status shouldBe LawStatus.FAILED
            report.leafStatus("inverse exists (-)") shouldBe LawStatus.FAILED
            report.leafStatus("left invertibility (+)") shouldBe LawStatus.PASSED
            report.leafStatus("right invertibility (+)") shouldBe LawStatus.PASSED
        }

        test("an inverse that throws fails (not errors) and keeps the exception as its cause") {
            val report = InverseExistenceLaw(add, 0, ints, negationThrowingAt3).verify()
            report.status shouldBe LawStatus.FAILED
            val problem = report.problems.single()
            problem.status shouldBe LawStatus.FAILED
            val cause = problem.diagnostics.shouldNotBeNull().cause
            cause.message.shouldNotBeNull() shouldContain "ArithmeticException"
            cause.causes().any { it is ArithmeticException && it.message == "no inverse of 3" } shouldBe true
        }

        test("left and right invertibility skip a throwing inverse and leave it to inverse existence") {
            LeftInvertibilityLaw(add, 0, ints, negationThrowingAt3).verify().status shouldBe LawStatus.PASSED
            RightInvertibilityLaw(add, 0, ints, negationThrowingAt3).verify().status shouldBe LawStatus.PASSED
        }
    }

    context("the excluded element") {
        test("zero is excluded for multiplication: the reciprocal may throw there") {
            val mul = q.mul.op
            InvertibilityLaw(mul, Rational.ONE, rationals, q.reciprocal, exclude = Rational.ZERO).verify()
                .status shouldBe LawStatus.PASSED
        }

        test("without the exclusion, zero has no reciprocal and the check fails") {
            InverseExistenceLaw(q.mul.op, Rational.ONE, rationals, q.reciprocal).verify()
                .status shouldBe LawStatus.FAILED
        }

        test("every nonzero element still needs a reciprocal") {
            val law = InverseExistenceLaw(
                q.mul.op, Rational.ONE, rationals, fieldMissingReciprocalOf2.reciprocal, exclude = Rational.ZERO
            )
            law.verify().status shouldBe LawStatus.FAILED
        }

        test("a field missing one nonzero reciprocal fails its field laws at inverse existence") {
            val report = FieldLaws(fieldMissingReciprocalOf2, rationals, RationalAlgebras.eqRational).verify()
            report.status shouldBe LawStatus.FAILED
            report.problems.map { it.label }.toSet() shouldBe setOf("inverse exists (${q.reciprocal.symbol})")
        }
    }

    context("cancellation") {
        val cancelling = UnaryOp<Int, Int?>("-") { throw CancellationException("stop") }

        test("cancellation thrown by the inverse propagates out of inverse existence") {
            shouldThrow<CancellationException> { InverseExistenceLaw(add, 0, ints, cancelling).verify() }
        }

        test("cancellation thrown by the inverse propagates out of left and right invertibility") {
            shouldThrow<CancellationException> { LeftInvertibilityLaw(add, 0, ints, cancelling).verify() }
            shouldThrow<CancellationException> { RightInvertibilityLaw(add, 0, ints, cancelling).verify() }
        }
    }
})
