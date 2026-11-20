package org.vorpal.kosmos.laws

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import kotlinx.coroutines.CancellationException

/**
 * Regressions for review findings on the stage 3–5 runner:
 * cancellation during shrinking, one seed per property check, and replay keeping its path and skip policy.
 */
class ReviewFixesSpec : FunSpec({

    test("a cancellation thrown while Kotest is shrinking propagates instead of becoming a failure") {
        var evaluations = 0
        val law = TestingLaw.named("cancelled while shrinking") {
            lawCheckAll(Arb.int(10..1000), render = { "$it" }) { _ ->
                evaluations++
                if (evaluations == 1) lawCheck(false) { "false on the first sample" }
                throw CancellationException("cancelled during shrinking")
            }
        }
        shouldThrow<CancellationException> { LawNode.branch("Root", law.asNode()).verify() }
        (evaluations > 1) shouldBe true   // the cancellation really happened after the first failure
    }

    test("each property check in one leaf gets its own seed, reproducibly") {
        val firsts = mutableListOf<Long>()
        val law = TestingLaw.named("two checks") {
            repeat(2) {
                var first = true
                lawCheckAll(Arb.long(), render = { "$it" }) { x -> if (first) { firsts += x; first = false } }
            }
        }
        val root = LawNode.branch("Root", law.asNode())

        root.verify(LawRunConfig(seed = 4))
        val run1 = firsts.toList()
        firsts.clear()
        root.verify(LawRunConfig(seed = 4))

        run1[0] shouldNotBe run1[1]   // independent samples, not the same stream twice
        firsts shouldBe run1          // same master seed, same samples
    }

    context("replay") {
        val subtraction = TestingLaw.named("false") { lawCheck(false) { "no" } }
        val never = TestingLaw.named("skipped") { error("must not run") }
        val root = lawGraph("Root") {
            branch("Inner") {
                law(subtraction)
                skipped(never, "not applicable")
            }
        }

        test("keeps the leaf's full path from the root") {
            val leaf = LawRunner.replay(root, listOf(0, 0), 1L).root
            leaf.path.toString() shouldBe "Root > Inner > false"
            leaf.path.indices shouldBe listOf(0, 0)
        }

        test("honours an explicit skip instead of running the leaf") {
            val leaf = LawRunner.replay(root, listOf(0, 1), 1L).root as NodeReport.LeafReport
            leaf.outcome shouldBe LeafOutcome.Skipped("not applicable")
        }
    }
})
