package org.vorpal.kosmos.laws

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.algebra.MonoidLaws

private val ints = Arb.int(-20..20)
private val eqInt = Eq<Int> { a, b -> a == b }
private val prInt = Printable<Int> { it.toString() }

/** A deterministic law that counts how often it runs. */
private class CountingLaw(override val name: String, private val holds: Boolean = true) : TestingLaw {
    var runs = 0
    override suspend fun test() {
        runs++
        lawCheck(holds) { "$name does not hold" }
    }
}

/** Integer addition (or subtraction, if [broken]) that counts its evaluations. */
private class CountingAdd(broken: Boolean = false) {
    var calls = 0
    val op = BinOp<Int>("+") { a, b -> calls++; if (broken) a - b else a + b }
    val monoid = Monoid.of(0, op)
}

private fun NodeReport.find(vararg labels: String): NodeReport =
    labels.fold(this) { node, l -> (node as NodeReport.BranchReport).children.first { it.label == l } }

/**
 * Stage 4: within one run, a node reached twice (the same instance, or a suite bound to the same
 * operations, generator, equality and printable) executes once and is shown under every parent.
 */
class SharedNodeSpec : FunSpec({

    context("diamonds") {
        test("the same leaf under two parents runs once and shows its result under both") {
            val law = CountingLaw("shared")
            val shared = law.asNode()
            val report = LawNode.branch(
                "Root",
                LawNode.branch("Left", shared),
                LawNode.branch("Right", shared)
            ).verify()

            law.runs shouldBe 1
            report.counts shouldBe LawCounts(passed = 2)   // occurrences, one per path
            report.executions shouldBe 1                   // executions

            val left = report.root.find("Left", "shared")
            val right = report.root.find("Right", "shared")
            left.sharedFrom.shouldBeNull()
            right.sharedFrom shouldBe left.path
            right.path.toString() shouldBe "Root > Right > shared"
            right.status shouldBe LawStatus.PASSED
        }

        test("a shared failing leaf is reported under both paths") {
            val law = CountingLaw("false", holds = false)
            val shared = law.asNode()
            val report = LawNode.branch(
                "Root",
                LawNode.branch("Left", shared),
                LawNode.branch("Right", shared)
            ).verify()

            law.runs shouldBe 1
            report.problems.map { it.path.toString() } shouldContainExactly
                listOf("Root > Left > false", "Root > Right > false")
            report.root.find("Left").status shouldBe LawStatus.FAILED
            report.root.find("Right").status shouldBe LawStatus.FAILED

            val message = shouldThrow<AssertionError> { report.throwIfFailed() }.message!!
            message shouldContain "#2 FAILED: Root > Right > false"
            message shouldContain "same check as Root > Left > false"
        }

        test("a shared branch is repeated with every descendant re-pathed") {
            val a = CountingLaw("a")
            val b = CountingLaw("b")
            val inner = LawNode.branch("Inner", a.asNode(), b.asNode())
            val report = LawNode.branch("Root", inner, LawNode.branch("Other", inner)).verify()

            listOf(a.runs, b.runs) shouldBe listOf(1, 1)
            report.executions shouldBe 2
            report.root.leaves().map { it.path.toString() } shouldContainExactly listOf(
                "Root > Inner > a", "Root > Inner > b", "Root > Other > Inner > a", "Root > Other > Inner > b"
            )
            report.root.find("Other", "Inner", "b").sharedFrom.toString() shouldBe "Root > Inner > b"
        }
    }

    context("what counts as the same check") {
        test("separately built leaves with identical labels both run") {
            val first = CountingLaw("associativity (+)")
            val second = CountingLaw("associativity (+)")
            val report = LawNode.branch("Root", first.asNode(), second.asNode()).verify()
            listOf(first.runs, second.runs) shouldBe listOf(1, 1)
            report.executions shouldBe 2
        }

        test("two suites bound to the same arguments run once") {
            val add = CountingAdd()
            val report = lawGraph("Root") {
                suite(MonoidLaws(add.monoid, ints, eqInt, prInt), LawMode.FULL)
                suite(MonoidLaws(add.monoid, ints, eqInt, prInt), LawMode.FULL)
            }.verify(LawRunConfig(seed = 5))

            val single = CountingAdd()
            MonoidLaws(single.monoid, ints, eqInt, prInt).verify(LawMode.FULL, LawRunConfig(seed = 5))

            add.calls shouldBe single.calls
            report.counts.total shouldBe 6
            report.executions shouldBe 3
        }

        test("suites whose operations share a symbol but are different objects both run") {
            val one = CountingAdd()
            val other = CountingAdd()
            one.op.symbol shouldBe other.op.symbol
            val report = lawGraph("Root") {
                suite(MonoidLaws(one.monoid, ints, eqInt, prInt))
                suite(MonoidLaws(other.monoid, ints, eqInt, prInt))
            }.verify()
            report.executions shouldBe report.counts.total
            (one.calls > 0 && other.calls > 0) shouldBe true
        }

        test("the same suite bound to a different equality or printable is a different check") {
            val add = CountingAdd()
            val otherEq = Eq<Int> { a, b -> a == b }
            val otherPr = Printable<Int> { "<$it>" }
            val report = lawGraph("Root") {
                suite(MonoidLaws(add.monoid, ints, eqInt, prInt))
                suite(MonoidLaws(add.monoid, ints, otherEq, prInt))
                suite(MonoidLaws(add.monoid, ints, eqInt, otherPr))
            }.verify()
            // 3 x (totality, associativity, identity). Totality (MagmaLaws) does not use the equality,
            // so the first two suites share it; everything else runs once per suite.
            report.counts.total shouldBe 9
            report.executions shouldBe 8
            val shared = report.root.leaves().single { it.sharedFrom != null }
            shared.label shouldBe "totality (+)"
            shared.path.indices.first() shouldBe 1
            shared.sharedFrom!!.indices.first() shouldBe 0
        }

        test("compact and full views of the same suite are different nodes") {
            val suite = MonoidLaws(CountingAdd().monoid, ints, eqInt, prInt)
            suite.node(LawMode.COMPACT).key shouldNotBe suite.node(LawMode.FULL).key
            suite.node(LawMode.FULL).key shouldBe suite.node(LawMode.FULL).key
        }

        test("plain values in a binding compare by value, objects by identity") {
            val op = BinOp<Int>("+") { a, b -> a + b }
            val owner = Any()
            LawBinding.of(owner, op, 1e-9, "x", true) shouldBe LawBinding.of(owner, op, 1e-9, "x", true)
            LawBinding.of(owner, op, 1e-9) shouldNotBe LawBinding.of(owner, op, 1e-6)
            LawBinding.of(owner, op) shouldNotBe LawBinding.of(owner, BinOp<Int>("+") { a, b -> a + b })
            LawBinding.of(owner, op) shouldNotBe LawBinding.of("another kind", op)
        }
    }

    context("runs") {
        test("each run executes again: nothing is cached across runs") {
            val law = CountingLaw("shared")
            val node = law.asNode()
            val root = LawNode.branch("Root", node, node)
            root.verify()
            root.verify()
            law.runs shouldBe 2
        }

        test("legacy flat views keep counting occurrences") {
            val law = CountingLaw("shared")
            val root = LawNode.branch("Root", law.asNode().let { n -> LawNode.branch("A", n) }, LawNode.branch("B", law.asNode()))
            root.flatten().size shouldBe 2
        }

        test("failFast stops after a failure even when later occurrences would be shared") {
            val bad = CountingLaw("bad", holds = false)
            val later = CountingLaw("later")
            val badNode = bad.asNode()
            val report = LawNode.branch("Root", badNode, later.asNode(), badNode)
                .verify(LawRunConfig(failFast = true))
            later.runs shouldBe 0
            report.root.leaves().map { it.status } shouldContainExactly
                listOf(LawStatus.FAILED, LawStatus.SKIPPED, LawStatus.FAILED)
        }
    }
})
