package org.vorpal.kosmos.laws

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.algebra.MonoidLaws

private val ints = Arb.int(-20..20)
private val eqInt = Eq<Int> { a, b -> a == b }

/** Prints integers in a recognisable way, to show that counterexamples use the law's Printable. */
private val prInt = Printable<Int> { "⟨$it⟩" }

/**
 * Stage 3 across a real suite: every property law in `kosmos-laws` now signals falsity explicitly.
 */
class SuiteDiagnosticsSpec : FunSpec({

    test("an operation that throws makes totality Failed and the other laws Errored") {
        // Integer addition, except that it refuses to touch 7.
        val touchy = Monoid.of(0, BinOp<Int>("+") { a, b ->
            if (a == 7 || b == 7) throw ArithmeticException("seven") else a + b
        })

        val report = MonoidLaws(touchy, ints, eqInt, prInt).verify(LawMode.FULL, LawRunConfig(seed = 3))

        val byLaw = report.root.leaves().associate { it.label to it.status }
        byLaw shouldBe mapOf(
            "totality (+)" to LawStatus.FAILED,       // "the operation is total" is exactly what is false
            "associativity (+)" to LawStatus.ERRORED, // the other laws cannot be evaluated
            "identity (+)" to LawStatus.ERRORED
        )

        val totality = report.problems.first { it.label == "totality (+)" }.diagnostics.shouldNotBeNull()
        totality.cause.shouldBeInstanceOf<LawViolation>()
        totality.cause.cause.shouldBeInstanceOf<ArithmeticException>()

        val associativity = report.problems.first { it.label == "associativity (+)" }.diagnostics.shouldNotBeNull()
        associativity.cause.shouldBeInstanceOf<ArithmeticException>()
        associativity.counterexample.shouldNotBeNull() shouldStartWith "a = ⟨"
        associativity.seed.shouldNotBeNull()
    }

    test("a false law in a suite is Failed, with a Printable counterexample, and replays") {
        val subtraction = Monoid.of(0, BinOp<Int>("+") { a, b -> a - b })
        val root = MonoidLaws(subtraction, ints, eqInt, prInt).node(LawMode.FULL)
        val report = root.verify(LawRunConfig(seed = 11))

        report.problems.map { it.label } shouldContainExactly listOf("associativity (+)", "identity (+)")
        report.problems.forEach { it.status shouldBe LawStatus.FAILED }

        val identity = report.problems.first { it.label == "identity (+)" }
        val d = identity.diagnostics.shouldNotBeNull()
        d.counterexample.shouldNotBeNull() shouldStartWith "⟨"

        val replay = LawRunner.replay(root, identity.path.indices, d.seed!!)
        replay.problems.single().diagnostics!!.counterexample shouldBe d.counterexample
    }
})
