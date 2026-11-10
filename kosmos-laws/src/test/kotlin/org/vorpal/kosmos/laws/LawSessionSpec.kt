package org.vorpal.kosmos.laws

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.property.AssociativityLaw

private class Counting(override val name: String, var holds: Boolean = true) : TestingLaw {
    var runs = 0
    override suspend fun test() { runs++; lawCheck(holds) { "$name is false" } }
}

class LawSessionSpec : FunSpec({

    fun diamond(law: TestingLaw): LawNode {
        val shared = law.asNode()
        return LawNode.branch("Root", LawNode.branch("Left", shared), LawNode.branch("Right", shared))
    }

    test("asking for every leaf runs a shared leaf once") {
        val law = Counting("shared")
        val session = LawSession(diamond(law))
        session.leafIndices shouldContainExactly listOf(listOf(0, 0), listOf(1, 0))
        session.leaf(listOf(0, 0)).sharedFrom.shouldBeNull()
        session.leaf(listOf(1, 0)).sharedFrom.toString() shouldBe "Root > Left > shared"
        law.runs shouldBe 1
        session.executions shouldBe 1
    }

    test("selecting only the second path still executes the check") {
        val law = Counting("shared")
        val session = LawSession(diamond(law))
        val right = session.leaf(listOf(1, 0))
        right.status shouldBe LawStatus.PASSED
        right.sharedFrom.shouldBeNull()      // it executed here
        law.runs shouldBe 1
    }

    test("asking for the same path again (a retry) executes again and never reuses the old result") {
        val law = Counting("flaky", holds = false)
        val session = LawSession(diamond(law))
        session.leaf(listOf(0, 0)).status shouldBe LawStatus.FAILED
        law.holds = true
        session.leaf(listOf(0, 0)).status shouldBe LawStatus.PASSED
        law.runs shouldBe 2
        session.leaf(listOf(1, 0)).status shouldBe LawStatus.PASSED   // the other path sees the latest result
        law.runs shouldBe 2
    }

    test("equal suite keys are shared, as in LawRunner") {
        val a = Counting("a")
        val node1 = LawNode.Branch("S", listOf(a.asNode()), key = LawNodeKey(LawBinding.of(this, a), LawMode.FULL))
        val node2 = LawNode.Branch("S", listOf(a.asNode()), key = LawNodeKey(LawBinding.of(this, a), LawMode.FULL))
        val session = LawSession(LawNode.branch("Root", node1, node2))
        session.leafIndices.forEach { session.leaf(it) }
        a.runs shouldBe 1
    }

    test("leaf seeds match LawRunner's for the same master seed") {
        val op = BinOp<Int>("-") { a, b -> a - b }
        val root = LawNode.branch("Root", AssociativityLaw(op, Arb.int(-9..9), Eq { a, b -> a == b }, Printable { "$it" }).asNode())
        val viaRunner = root.verify(LawRunConfig(seed = 99)).problems.single().diagnostics!!
        val viaSession = LawSession(root, LawRunConfig(seed = 99)).leaf(listOf(0)).diagnostics!!
        viaSession.seed shouldBe viaRunner.seed
        viaSession.counterexample shouldBe viaRunner.counterexample
    }

    test("a cancelled execution stores nothing and propagates") {
        val law = object : TestingLaw {
            override val name = "slow"
            override suspend fun test() { while (true) yield() }
        }
        val session = LawSession(LawNode.branch("Root", law.asNode()))
        coroutineScope {
            val job = async { session.leaf(listOf(0)) }
            yield()
            job.cancel()
            shouldThrow<CancellationException> { job.await() }
        }
        session.executions shouldBe 0
    }

    test("a branch or a missing position is rejected") {
        val session = LawSession(diamond(Counting("x")))
        shouldThrow<IllegalArgumentException> { session.leaf(listOf(0)) }
        shouldThrow<IllegalArgumentException> { session.leaf(listOf(5, 0)) }
    }
})
