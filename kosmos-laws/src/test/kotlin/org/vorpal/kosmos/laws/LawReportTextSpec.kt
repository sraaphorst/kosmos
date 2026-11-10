package org.vorpal.kosmos.laws

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.property.AssociativityLaw

private fun holds(name: String) = TestingLaw.named(name) { }
private fun fails(name: String, why: String) = TestingLaw.named(name) { lawCheck(false) { why } }

/** A small graph with a pass, a deep failure, an error, a skip and a shared node. */
private fun sample(): LawNode {
    val shared = LawNode.branch("Magma (+)", holds("totality (+)").asNode())
    return lawGraph("Ring (+, *)") {
        node(LawNode.branch("Hemiring (+, *)",
            shared,
            LawNode.branch("Semigroup (*)", fails("associativity (*)", "(a*b)*c ≠ a*(b*c)").asNode())
        ))
        node(LawNode.branch("AbelianGroup (+)", shared, TestingLaw.named("inverse (+)") { error("no inverse") }.asNode()))
        skipped(holds("reciprocal (*)"), "needs a field")
    }
}

class LawReportTextSpec : FunSpec({

    test("the full tree shows every node once, with shared repeats collapsed to one line") {
        val report = sample().verify(LawRunConfig(seed = 1))
        report.renderTree() shouldBe """
            Ring (+, *): ERRORED (passed 2, failed 1, errored 1, skipped 1; 3 executions; seed 1)
            Ring (+, *)  ERRORED [2/5 passed]
            ├─ Hemiring (+, *)  FAILED [1/2 passed]
            │  ├─ Magma (+)  PASSED
            │  │  └─ totality (+)  PASSED
            │  └─ Semigroup (*)  FAILED [0/1 passed]
            │     └─ associativity (*)  FAILED
            ├─ AbelianGroup (+)  ERRORED [1/2 passed]
            │  ├─ Magma (+)  PASSED  (shared: same check as Ring (+, *) > Hemiring (+, *) > Magma (+))
            │  └─ inverse (+)  ERRORED
            └─ reciprocal (*)  SKIPPED (needs a field)
            
        """.trimIndent()
    }

    test("the failure view keeps only failing paths and gives each problem's details") {
        val text = sample().verify(LawRunConfig(seed = 1)).renderFailures()
        text shouldBe """
            Ring (+, *): ERRORED (passed 2, failed 1, errored 1, skipped 1; 3 executions; seed 1)
            Ring (+, *)  ERRORED [2/5 passed]
            ├─ Hemiring (+, *)  FAILED [1/2 passed]
            │  └─ Semigroup (*)  FAILED [0/1 passed]
            │     └─ associativity (*)  FAILED
            └─ AbelianGroup (+)  ERRORED [1/2 passed]
               └─ inverse (+)  ERRORED

            #1 FAILED: Ring (+, *) > Hemiring (+, *) > Semigroup (*) > associativity (*)
              cause: org.vorpal.kosmos.laws.LawViolation: (a*b)*c ≠ a*(b*c)

            #2 ERRORED: Ring (+, *) > AbelianGroup (+) > inverse (+)
              cause: java.lang.IllegalStateException: no inverse
            
        """.trimIndent()
    }

    test("a property failure shows its counterexample, seed and replay call, and the cause chain") {
        val op = BinOp<Int>("+") { a, b -> if (a == b) throw ArithmeticException("equal inputs") else a - b }
        val root = LawNode.branch("Root", AssociativityLaw(op, Arb.int(-5..5), Eq { a, b -> a == b }, Printable { "⟨$it⟩" }).asNode())
        val text = root.verify(LawRunConfig(seed = 3)).renderFailures()
        text shouldContain "counterexample: a = ⟨"
        Regex("""seed: (-?\d+) \(replay: LawRunner\.replay\(root, listOf\(0\), \1L\)\)""").containsMatchIn(text) shouldBe true
        LawReportText.replayCall(listOf(1, 0), -42) shouldBe "LawRunner.replay(root, listOf(1, 0), -42L)"
        LawReportText.replayCall(emptyList(), 7) shouldBe "LawRunner.replay(root, listOf(), 7L)"
        text shouldContain "cause: java.lang.ArithmeticException: equal inputs"
    }

    test("timing is shown only on request") {
        val report = sample().verify()
        report.renderTree() shouldNotContain " ms"
        report.renderTree(timing = true) shouldContain " ms"
    }

    test("throwIfFailed carries the failure view and the first problem's throwable") {
        val error = shouldThrow<AssertionError> { sample().verify(LawRunConfig(seed = 1)).throwIfFailed() }
        error.message!! shouldContain "#1 FAILED: Ring (+, *) > Hemiring (+, *) > Semigroup (*) > associativity (*)"
        error.message!! shouldContain "#2 ERRORED"
        (error.cause is LawViolation) shouldBe true
    }

    test("an empty run says it provides no evidence") {
        val report = LawNode.branch("Nothing").verify()
        report.renderFailures() shouldContain "provides no evidence"
        shouldThrow<AssertionError> { report.throwIfFailed() }
    }
})
