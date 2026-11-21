package org.vorpal.kosmos.testing.laws

import io.kotest.core.names.DuplicateTestNameMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSession
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNode
import org.vorpal.kosmos.laws.lawGraph

/** A law that counts how often it runs. */
private class Counting(override val name: String) : TestingLaw {
    var runs = 0
    override suspend fun test() { runs++ }
}

private val shared = Counting("shared")
private val first = Counting("same label")
private val second = Counting("same label")

/**
 * The nested adapter, exercised through Kotest itself: the graph below is registered as containers and tests,
 * and the last tests check what ran.
 */
class KotestLawsSpec : FunSpec({
    // Strict: a duplicate sibling name fails the spec instead of being renamed by Kotest.
    duplicateTestNameMode = DuplicateTestNameMode.Error

    val sharedNode = shared.asNode()
    val graph = lawGraph("Graph") {
        branch("Left") { node(sharedNode) }
        branch("Right") { node(sharedNode) }
        law(first)
        law(second)
        law("same label [2]", Counting("third"))
        // A skipped leaf whose annotated name equals an active sibling's label.
        skipped(Counting("a"), "x")
        law("a (skipped: x)", Counting("active"))
        skipped(Counting("never"), "not applicable")
    }
    lawTests(graph)

    context("Nested") {
        lawTests(LawNode.branch("Inner", Counting("inner").asNode()))
    }

    test("the shared leaf ran once") {
        shared.runs shouldBe 1
    }

    test("a check reached along two paths is registered once, at its first path; its other paths are kept") {
        val session = LawSession(graph)
        // Left and Left > shared are registered; Right holds only the repeat, so neither it nor the leaf is.
        val registered = registeredPositions(session)
        registered shouldContainAll listOf(emptyList(), listOf(0), listOf(0, 0))
        registered shouldNotContain listOf(1)
        registered shouldNotContain listOf(1, 0)
        // Every other leaf is distinct, so it is registered (skipped ones as disabled tests).
        (2..7).forEach { registered shouldContain listOf(it) }
        session.uniqueLeafIndices.size shouldBe session.leafIndices.size - 1

        session.sharedPaths(listOf(0, 0)).map { it.toString() } shouldContainExactly listOf("Graph > Right > shared")
        session.sharedPaths(listOf(2)) shouldBe emptyList()
    }

    test("a failing check's message lists the other paths that reach it") {
        // One node reached along twelve paths (an unbound law is shared only as the same node).
        val failing = TestingLaw.named("always false") { lawCheck(false) { "nope" } }.asNode()
        val root = lawGraph("Root") {
            (1..12).forEach { i -> branch("B$i") { node(failing) } }
        }
        val session = LawSession(root)
        val message = failureMessage(session.leaf(listOf(0, 0)), session.sharedPaths(listOf(0, 0)))
        message shouldContain "FAILED: Root > B1 > always false"
        message shouldContain "also reached via 11 other path(s)"
        message shouldContain "Root > B2 > always false"
        message shouldContain "... and 1 more"
        registeredPositions(session) shouldBe setOf(emptyList(), listOf(0), listOf(0, 0))
    }

    test("separately built leaves with the same label both ran, under distinct names") {
        listOf(first.runs, second.runs) shouldBe listOf(1, 1)
        displayNames(listOf("a", "b", "a", "a")) shouldContainExactly listOf("a", "b", "a [2]", "a [3]")
        // A suffix never takes a name another sibling already has as its label.
        displayNames(listOf("a", "a", "a [2]")) shouldContainExactly listOf("a", "a [3]", "a [2]")
        // Skip annotations are part of the name before it is made unique.
        val skippedA = LawNode.Leaf(Counting("a"), skip = "x")
        val activeA = LawNode.Leaf(Counting("active"), label = "a (skipped: x)")
        displayNames(listOf(skippedA, activeA).map(::baseName)) shouldContainExactly
            listOf("a (skipped: x)", "a (skipped: x) [2]")
    }
})
