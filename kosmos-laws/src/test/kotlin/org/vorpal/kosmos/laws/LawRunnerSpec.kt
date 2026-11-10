package org.vorpal.kosmos.laws

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield

private fun passing(name: String, counter: MutableList<String>? = null) =
    TestingLaw.named(name) { counter?.add(name) }

private fun failing(name: String, message: String = "$name is false") =
    TestingLaw.named(name) { throw AssertionError(message) }

private fun erroring(name: String, t: Throwable = ArithmeticException("division by zero")) =
    TestingLaw.named(name) { throw t }

/** A legacy-style suite: flat lists, compact and full differ. */
private class ToySuite(
    override val name: String,
    private val compact: List<TestingLaw>,
    private val extra: List<TestingLaw> = emptyList()
) : LawSuite {
    override fun laws() = compact
    override fun fullLaws() = extra + compact
}

class LawRunnerSpec : FunSpec({

    context("composition") {
        test("an ad hoc graph mixes an adapted suite and standalone laws, in order") {
            val ran = mutableListOf<String>()
            val suite = ToySuite("Toy", listOf(passing("a", ran), passing("b", ran)), listOf(passing("x", ran)))

            val graph = lawGraph("Mixed") {
                suite(suite, LawMode.FULL)
                law(passing("standalone", ran))
            }

            val report = graph.verify()
            report.status shouldBe LawStatus.PASSED
            report.counts shouldBe LawCounts(passed = 4)
            ran shouldContainExactly listOf("x", "a", "b", "standalone")
            report.root.leaves().map { it.path.toString() } shouldContainExactly listOf(
                "Mixed > Toy > x",
                "Mixed > Toy > a",
                "Mixed > Toy > b",
                "Mixed > standalone"
            )
            report.throwIfFailed()
        }

        test("the legacy adapter wraps the selected flat view") {
            val suite = ToySuite("Toy", listOf(passing("a")), listOf(passing("x")))
            suite.node(LawMode.COMPACT).flatten().map { it.name } shouldContainExactly listOf("a")
            suite.node(LawMode.FULL).flatten().map { it.name } shouldContainExactly listOf("x", "a")
        }

        test("flatten is occurrence based: a shared node contributes once per path") {
            val shared = LawNode.Leaf(passing("shared"))
            val graph = LawNode.branch("Root", LawNode.branch("L", shared), LawNode.branch("R", shared))
            graph.flatten().map { it.name } shouldContainExactly listOf("shared", "shared")
        }
    }

    context("outcomes") {
        test("a failure deep in a branch is reported with its full path, and siblings still run") {
            val ran = mutableListOf<String>()
            val graph = lawGraph("Root") {
                branch("Outer") {
                    branch("Inner") {
                        law(passing("before", ran))
                        law(failing("broken"))
                        law(passing("after", ran))
                    }
                }
                law(passing("sibling", ran))
            }

            val report = graph.verify()
            report.status shouldBe LawStatus.FAILED
            report.counts shouldBe LawCounts(passed = 3, failed = 1)
            ran shouldContainExactly listOf("before", "after", "sibling")

            val problem = report.problems.single()
            problem.path.labels shouldContainExactly listOf("Root", "Outer", "Inner", "broken")
            problem.path.indices shouldContainExactly listOf(0, 0, 1)

            val outer = (report.root as NodeReport.BranchReport).children[0]
            outer.status shouldBe LawStatus.FAILED
            (report.root as NodeReport.BranchReport).children[1].status shouldBe LawStatus.PASSED

            val e = shouldThrow<AssertionError> { report.throwIfFailed() }
            e.message shouldContain "Root > Outer > Inner > broken"
            e.message shouldContain "broken is false"
            e.cause!!.message shouldBe "broken is false"
        }

        test("a non-assertion exception is Errored, keeps the original throwable, and outranks Failed") {
            val boom = ArithmeticException("division by zero")
            val report = lawGraph("Root") {
                law(failing("false"))
                law(erroring("throws", boom))
            }.verify()

            report.status shouldBe LawStatus.ERRORED
            report.counts shouldBe LawCounts(failed = 1, errored = 1)
            val outcome = report.problems[1].outcome.shouldBeInstanceOf<LeafOutcome.Errored>()
            outcome.error shouldBeSameInstanceAs boom
        }

        test("fail-fast skips everything after the first problem, with a reason") {
            val ran = mutableListOf<String>()
            val report = lawGraph("Root") {
                law(passing("a", ran))
                law(failing("b"))
                law(passing("c", ran))
            }.verify(LawRunConfig(failFast = true))

            ran shouldContainExactly listOf("a")
            report.counts shouldBe LawCounts(passed = 1, failed = 1, skipped = 1)
            val skipped = report.root.leaves()[2].outcome.shouldBeInstanceOf<LeafOutcome.Skipped>()
            skipped.reason shouldContain "Root > b"
        }

        test("an empty graph is skipped and provides no evidence") {
            val report = LawNode.branch("Empty").verify()
            report.status shouldBe LawStatus.SKIPPED
            report.counts shouldBe LawCounts.ZERO
            report.hasEvidence shouldBe false
            report.success shouldBe true
            shouldThrow<AssertionError> { report.throwIfFailed() }.message shouldContain "no evidence"
            report.throwIfFailed(requireEvidence = false)
        }

        test("status precedence: errored > failed > passed > skipped") {
            LawStatus.worst(listOf(LawStatus.SKIPPED, LawStatus.PASSED)) shouldBe LawStatus.PASSED
            LawStatus.worst(listOf(LawStatus.PASSED, LawStatus.FAILED)) shouldBe LawStatus.FAILED
            LawStatus.worst(listOf(LawStatus.FAILED, LawStatus.ERRORED, LawStatus.PASSED)) shouldBe LawStatus.ERRORED
            LawStatus.worst(listOf(LawStatus.SKIPPED, LawStatus.SKIPPED)) shouldBe LawStatus.SKIPPED
            LawStatus.worst(emptyList()) shouldBe LawStatus.SKIPPED
        }
    }

    context("graph validation") {
        test("a cycle is rejected before any leaf runs") {
            val ran = mutableListOf<String>()
            val children = mutableListOf<LawNode>(LawNode.Leaf(passing("a", ran)))
            val loop = LawNode.Branch("Loop", children)
            children += LawNode.branch("Inner", loop)

            val e = shouldThrow<LawGraphCycleException> { loop.verify() }
            e.cycle shouldContainExactly listOf("Loop", "Inner", "Loop")
            ran shouldBe emptyList()
        }

        test("a diamond (shared node, no cycle) is accepted") {
            val shared = LawNode.Leaf(passing("shared"))
            LawNode.branch("Root", LawNode.branch("L", shared), LawNode.branch("R", shared))
                .verify().counts shouldBe LawCounts(passed = 2)
        }
    }

    context("cancellation") {
        test("cancellation thrown by a law propagates instead of being recorded") {
            val graph = lawGraph("Root") {
                law(TestingLaw.named("cancels") { throw CancellationException("stop") })
                law(passing("never"))
            }
            shouldThrow<CancellationException> { graph.verify() }
        }

        test("cancelling the caller stops the run") {
            val ran = mutableListOf<String>()
            coroutineScope {
                val job = async {
                    lawGraph("Root") {
                        law(TestingLaw.named("slow") { while (true) yield() })
                        law(passing("never", ran))
                    }.verify()
                }
                yield()
                job.cancel()
                shouldThrow<CancellationException> { job.await() }
            }
            ran shouldBe emptyList()
        }

        test("each run has its own state") {
            val graph = lawGraph("Root") {
                law(failing("a"))
                law(passing("b"))
            }
            graph.verify(LawRunConfig(failFast = true)).counts shouldBe LawCounts(failed = 1, skipped = 1)
            graph.verify().counts shouldBe LawCounts(passed = 1, failed = 1)
        }
    }

    context("legacy API compatibility") {
        test("runLawSuite collects every result") {
            val report = runLawSuite("Legacy", listOf(passing("a"), failing("b"), erroring("c")))
            report.totalCount shouldBe 3
            report.failedCount shouldBe 2
            report.results.map { it.name } shouldContainExactly listOf("a", "b", "c")
            shouldThrow<AssertionError> { report.throwIfFailed() }
        }

        test("runLaws is fail-fast") {
            val ran = mutableListOf<String>()
            shouldThrow<AssertionError> { runLaws(passing("a", ran), failing("b"), passing("c", ran)) }
            ran shouldContainExactly listOf("a")
        }

        test("test() and fullTest() use the compact and full views") {
            val suite = ToySuite("Toy", listOf(passing("a")), listOf(failing("x")))
            suite.test().success shouldBe true
            suite.fullTest().failedCount shouldBe 1
            listOf(suite).flatten(full = false).map { it.name } shouldContainExactly listOf("a")
            listOf(suite).flatten(full = true).map { it.name } shouldContainExactly listOf("x", "a")
        }
    }
})
