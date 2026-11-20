package org.vorpal.kosmos.laws

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.property.AssociativityLaw

/**
 * Stage 3 spike against the pinned Kotest version (6.0.5), using [AssociativityLaw].
 *
 * Verifies, through supported Kotest interfaces only (`PropTestConfig(seed = ...)` and the property body):
 * 1. seed in: the runner injects a per-leaf seed;
 * 2. seed and input out: the report carries that seed and the final (shrunk) counterexample;
 * 3. replay: rerunning one leaf with its seed yields the same counterexample;
 * 4. false vs. threw: a false predicate is Failed, an exception from the operation is Errored,
 *    even though Kotest wraps both in an AssertionError.
 */
private val ints = Arb.int(-100..100)
private val eqInt = Eq<Int> { a, b -> a == b }
private val prInt = Printable<Int> { it.toString() }

private val subtraction = BinOp<Int>("-") { a, b -> a - b }

/** Associative on non-negative inputs (it is addition), throws on any negative input. */
private val partialAddition = BinOp<Int>("+") { a, b ->
    if (a < 0 || b < 0) throw ArithmeticException("negative input: $a, $b") else a + b
}

/** Throws an AssertionError that is not a law violation, e.g. an internal sanity check in the operation. */
private val assertingAddition = BinOp<Int>("+") { a, b ->
    if (a < 0 && b < 0) throw AssertionError("internal invariant broken") else a + b
}

/** The pre-stage-3 shape of a property law: Kotlin `check` inside a plain `checkAll`. */
private class LegacyAssociativityLaw(private val op: BinOp<Int>) : TestingLaw {
    override val name = "legacy associativity (${op.symbol})"
    override suspend fun test() {
        checkAll(TestingLaw.arbTriple(ints)) { (a, b, c) -> check(op(a, op(b, c)) == op(op(a, b), c)) }
    }
}

private fun ints(s: String): List<Int> = Regex("-?\\d+").findAll(s).map { it.value.toInt() }.toList()

class PropertyDiagnosticsSpec : FunSpec({

    fun graph(op: BinOp<Int>) = LawNode.branch("Root", AssociativityLaw(op, ints, eqInt, prInt).asNode())

    context("seed in, seed and counterexample out") {
        test("a false predicate is Failed with its seed and final counterexample") {
            val report = graph(subtraction).verify(LawRunConfig(seed = 42))
            val leaf = report.problems.single()

            val outcome = leaf.outcome.shouldBeInstanceOf<LeafOutcome.Failed>()
            outcome.error.shouldBeInstanceOf<AssertionError>()      // Kotest's wrapper
            val d = outcome.diagnostics.shouldNotBeNull()
            d.cause.shouldBeInstanceOf<LawViolation>()
            d.seed.shouldNotBeNull()
            d.counterexample.shouldNotBeNull() shouldContain "a = "
            d.unavailable shouldBe emptyList()

            // The recorded counterexample is the one Kotest reports after shrinking.
            val kotestMessage = outcome.error.message!!
            val reportedArg = Regex("Arg 0: \\(([^)]*)\\)").find(kotestMessage)!!.groupValues[1]
            ints(d.counterexample!!) shouldContainExactly ints(reportedArg)
            kotestMessage shouldContain "Repeat this test by using seed ${d.seed}"
        }

        test("the same master seed reproduces the same leaf seed and counterexample") {
            val first = graph(subtraction).verify(LawRunConfig(seed = 7)).problems.single().diagnostics!!
            val second = graph(subtraction).verify(LawRunConfig(seed = 7)).problems.single().diagnostics!!
            second.seed shouldBe first.seed
            second.counterexample shouldBe first.counterexample
        }

        test("different master seeds give different leaf seeds") {
            val a = graph(subtraction).verify(LawRunConfig(seed = 1)).problems.single().diagnostics!!.seed
            val b = graph(subtraction).verify(LawRunConfig(seed = 2)).problems.single().diagnostics!!.seed
            a shouldNotBe b
        }
    }

    context("replay") {
        test("replaying one leaf with its recorded seed yields the same counterexample") {
            val root = graph(subtraction)
            val leaf = root.verify().problems.single()
            val d = leaf.diagnostics!!

            val replay = LawRunner.replay(root, leaf.path.indices, d.seed!!)
            replay.status shouldBe LawStatus.FAILED
            replay.problems.single().diagnostics!!.counterexample shouldBe d.counterexample
        }

        test("replay selects a leaf by indices and rejects a branch") {
            val root = graph(subtraction)
            shouldThrow<IllegalArgumentException> { LawRunner.replay(root, emptyList(), 1L) }
            shouldThrow<IllegalArgumentException> { LawRunner.replay(root, listOf(5), 1L) }
        }
    }

    context("false versus threw") {
        test("an exception from the operation inside the property is Errored, not Failed") {
            val outcome = graph(partialAddition).verify().problems.single().outcome
                .shouldBeInstanceOf<LeafOutcome.Errored>()
            outcome.error.shouldBeInstanceOf<AssertionError>()      // Kotest wraps it...
            val d = outcome.diagnostics.shouldNotBeNull()
            d.cause.shouldBeInstanceOf<ArithmeticException>()       // ...but the cause is the arithmetic error
            d.counterexample.shouldNotBeNull()
        }

        test("an AssertionError thrown by the operation is Errored: only LawViolation means false") {
            val d = graph(assertingAddition).verify().problems.single()
                .also { it.status shouldBe LawStatus.ERRORED }
                .diagnostics.shouldNotBeNull()
            d.cause.shouldBeInstanceOf<AssertionError>()
            d.cause.message shouldBe "internal invariant broken"
        }

        test("a passing law passes") {
            graph(BinOp("+") { a, b -> a + b }).verify().status shouldBe LawStatus.PASSED
        }
    }

    context("deterministic, generator and legacy failures") {
        test("a deterministic violation is Failed, with no seed or counterexample, and says why") {
            val law = TestingLaw.named("1 = 0") { lawCheck(false) { "1 ≠ 0" } }
            val d = LawNode.branch("Root", law.asNode()).verify().problems.single()
                .also { it.status shouldBe LawStatus.FAILED }
                .diagnostics.shouldNotBeNull()
            d.seed.shouldBeNull()
            d.counterexample.shouldBeNull()
            d.unavailable.size shouldBe 2
        }

        test("a generator that throws before any evaluation is Errored, with a seed but no counterexample") {
            // Note: Kotest 6.0.5's Arb.filter retries forever when nothing passes (it does not give up after
            // maxFilterAttempts), so an exhausted filter hangs rather than erroring. A throwing generator is
            // the closest failure-before-sampling we can provoke.
            val broken = ints.map<Int, Int> { throw IllegalStateException("generator broke") }
            val report = LawNode.branch("Root", AssociativityLaw(subtraction, broken, eqInt, prInt).asNode()).verify()
            val d = report.problems.single().also { it.status shouldBe LawStatus.ERRORED }.diagnostics!!
            d.counterexample.shouldBeNull()
            d.seed.shouldNotBeNull()
            d.cause.message shouldBe "generator broke"
        }

        test("legacy laws keep the compatibility classification and have no seed") {
            val failed = LawNode.branch("Root", LegacyAssociativityLaw(subtraction).asNode()).verify()
            failed.status shouldBe LawStatus.FAILED
            failed.problems.single().diagnostics!!.seed.shouldBeNull()

            // Documented limitation: Kotest wraps the operation's exception, and a legacy law has no explicit
            // signal, so a throwing operation still reads as Failed.
            LawNode.branch("Root", LegacyAssociativityLaw(partialAddition).asNode()).verify()
                .status shouldBe LawStatus.FAILED
        }
    }

    context("skip and cancellation") {
        test("an explicitly skipped leaf is not executed and carries its reason") {
            var ran = false
            val report = lawGraph("Root") {
                skipped(TestingLaw.named("never") { ran = true }, "needs an invertible element")
                law(AssociativityLaw(BinOp("+") { a, b -> a + b }, ints, eqInt, prInt))
            }.verify()
            ran shouldBe false
            report.counts shouldBe LawCounts(passed = 1, skipped = 1)
            report.root.leaves()[0].outcome shouldBe LeafOutcome.Skipped("needs an invertible element")
        }

        test("cancelling the caller during a property check propagates, even though Kotest catches it") {
            val slow = BinOp<Int>("+") { a, b -> a + b }
            val law = object : TestingLaw {
                override val name = "slow"
                override suspend fun test() {
                    lawCheckAll(ints, render = { it.toString() }) { _ ->
                        yield()
                        lawCheck(slow(1, 2) == 3) { "unreachable" }
                        while (true) yield()
                    }
                }
            }
            coroutineScope {
                val job = async { LawNode.branch("Root", law.asNode()).verify() }
                yield()
                job.cancel()
                shouldThrow<CancellationException> { job.await() }
            }
        }
    }

    context("legacy entry points are unchanged") {
        test("a migrated law still fails as an AssertionError through runLaws and test()") {
            shouldThrow<AssertionError> { runLaws(AssociativityLaw(subtraction, ints, eqInt, prInt)) }
            runLawSuite("Legacy", listOf(AssociativityLaw(subtraction, ints, eqInt, prInt))).failedCount shouldBe 1
        }
    }
})
