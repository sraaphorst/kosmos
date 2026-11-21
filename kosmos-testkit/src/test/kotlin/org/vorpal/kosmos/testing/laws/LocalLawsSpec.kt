package org.vorpal.kosmos.testing.laws

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import kotlin.coroutines.cancellation.CancellationException
import org.vorpal.kosmos.laws.LawStatus
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.leaves
import org.vorpal.kosmos.laws.verify

/** The spec-local law helpers: matcher failures fail a local law, and `throwsA` never swallows cancellation. */
class LocalLawsSpec : FunSpec({

    test("a local law asserts with matchers: a false shouldBe is FAILED, a throwing operation ERRORED") {
        val report = lawGraph("local") {
            local("x + 1 = x") { lawCheckAll(Arb.int()) { x -> (x + 1) shouldBe x } }
            local("1 / 0 = 0") { (1 / listOf(0).single()) shouldBe 0 }
            local("x = x") { lawCheckAll(Arb.int()) { x -> x shouldBe x } }
        }.verify()
        report.root.leaves().associate { it.label to it.status } shouldBe mapOf(
            "x + 1 = x" to LawStatus.FAILED,
            "1 / 0 = 0" to LawStatus.ERRORED,
            "x = x" to LawStatus.PASSED
        )
    }

    test("localLaw is flagged as asserting with matchers") {
        localLaw("l") {}.assertionsAreViolations shouldBe true
    }

    context("throwsA") {
        test("true for the expected exception, false for another one or for none") {
            throwsA<ArithmeticException> { throw ArithmeticException() } shouldBe true
            throwsA<ArithmeticException> { throw IllegalStateException() } shouldBe false
            throwsA<ArithmeticException> { } shouldBe false
        }

        test("a cancellation propagates, whatever the expected type") {
            shouldThrow<CancellationException> {
                throwsA<ArithmeticException> { throw CancellationException("stop") }
            }.message shouldBe "stop"
            shouldThrow<CancellationException> {
                throwsA<Throwable> { throw CancellationException("stop") }
            }.shouldBeInstanceOf<CancellationException>()
        }
    }
})
