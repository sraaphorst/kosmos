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

/** Equal to every other [Shift] with the same name, whatever its [offset]: `equals` says nothing about behaviour. */
private data class Shift(val name: String) {
    var offset = 0
    fun apply(x: Int): Int = x + offset
}

/** `f(0) = 0`, bound to the function under test. */
private class PreservesZeroLaw(private val f: (Int) -> Int) : TestingLaw {
    override val name = "preserves zero"
    override val binding = LawBinding.of(this, f)
    override suspend fun test() {
        lawCheck(f(0) == 0) { "f(0) = ${f(0)}" }
    }
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
                suite(MonoidLaws(add.monoid, ints, eqInt, prInt))
                suite(MonoidLaws(add.monoid, ints, eqInt, prInt))
            }.verify(LawRunConfig(seed = 5))

            val single = CountingAdd()
            MonoidLaws(single.monoid, ints, eqInt, prInt).verify(LawRunConfig(seed = 5))

            add.calls shouldBe single.calls
            // Monoid = Semigroup (totality, associativity) + NonAssociativeMonoid (totality, left and right
            // identity): 5 occurrences and 4 checks per suite.
            report.counts.total shouldBe 10
            report.executions shouldBe 4
        }

        test("suites whose operations share a symbol but are different objects both run") {
            val one = CountingAdd()
            val other = CountingAdd()
            one.op.symbol shouldBe other.op.symbol
            val report = lawGraph("Root") {
                suite(MonoidLaws(one.monoid, ints, eqInt, prInt))
                suite(MonoidLaws(other.monoid, ints, eqInt, prInt))
            }.verify()
            // Within each suite totality is reached twice and runs once: 4 checks per suite, none shared across.
            report.counts.total shouldBe 10
            report.executions shouldBe 8
            (one.calls > 0 && other.calls > 0) shouldBe true
        }

        test("a different equality is a different check; a different printable is not") {
            val add = CountingAdd()
            val otherEq = Eq<Int> { a, b -> a == b }
            val otherPr = Printable<Int> { "<$it>" }
            val report = lawGraph("Root") {
                suite(MonoidLaws(add.monoid, ints, eqInt, prInt))
                suite(MonoidLaws(add.monoid, ints, otherEq, prInt))
                suite(MonoidLaws(add.monoid, ints, eqInt, otherPr))
            }.verify()
            // 3 x 5 occurrences. The first suite runs its 4 checks. The second shares totality (it does not
            // use the equality) and runs associativity and both identities again. The third differs only in
            // how it prints, so it runs nothing new.
            report.counts.total shouldBe 15
            report.executions shouldBe 7
            report.root.leaves().filter { it.path.indices.first() == 2 }.all { it.sharedFrom != null } shouldBe true
        }

        test("plain values in a binding compare by value, objects by identity") {
            val op = BinOp<Int>("+") { a, b -> a + b }
            val owner = Any()
            LawBinding.of(owner, op, 1e-9, "x", true) shouldBe LawBinding.of(owner, op, 1e-9, "x", true)
            LawBinding.of(owner, op, 1e-9) shouldNotBe LawBinding.of(owner, op, 1e-6)
            LawBinding.of(owner, op) shouldNotBe LawBinding.of(owner, BinOp<Int>("+") { a, b -> a + b })
            LawBinding.of(owner, op) shouldNotBe LawBinding.of("another kind", op)
        }

        test("bound callable references compare by receiver and function") {
            val op = BinOp<Int>("+") { a, b -> a + b }
            val other = BinOp<Int>("+") { a, b -> a + b }
            val owner = Any()
            val first: (Int, Int) -> Int = op::invoke
            val second: (Int, Int) -> Int = op::invoke
            val third: (Int, Int) -> Int = other::invoke
            LawBinding.of(owner, first) shouldBe LawBinding.of(owner, second)
            LawBinding.of(owner, first).hashCode() shouldBe LawBinding.of(owner, second).hashCode()
            LawBinding.of(owner, first) shouldNotBe LawBinding.of(owner, third)
        }

        test("references to the same function on one receiver are the same check") {
            val shift = Shift("s")
            val report = LawNode.branch("Root", PreservesZeroLaw(shift::apply).asNode(), PreservesZeroLaw(shift::apply).asNode())
                .verify()
            report.executions shouldBe 1
        }

        test("references on equal but distinct receivers are different checks") {
            val good = Shift("s")
            val bad = Shift("s").apply { offset = 1 }
            good shouldBe bad   // equal by data class equals, different by behaviour

            LawBinding.of(Any(), good::apply) shouldNotBe LawBinding.of(Any(), bad::apply)
            PreservesZeroLaw(good::apply).binding shouldNotBe PreservesZeroLaw(bad::apply).binding

            PreservesZeroLaw(bad::apply).verify().status shouldBe LawStatus.FAILED

            val root = LawNode.branch("Root", PreservesZeroLaw(good::apply).asNode(), PreservesZeroLaw(bad::apply).asNode())
            val report = root.verify()
            report.executions shouldBe 2
            report.root.find("preserves zero").status shouldBe LawStatus.PASSED
            (report.root as NodeReport.BranchReport).children[1].status shouldBe LawStatus.FAILED

            // The leaf-at-a-time session (the Kotest adapter's path) must not merge them either.
            val session = LawSession(root)
            session.leaf(listOf(0)).status shouldBe LawStatus.PASSED
            session.leaf(listOf(1)).status shouldBe LawStatus.FAILED
            session.executions shouldBe 2
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
