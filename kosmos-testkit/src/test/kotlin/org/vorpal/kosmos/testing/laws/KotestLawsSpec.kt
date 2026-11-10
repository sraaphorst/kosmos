package org.vorpal.kosmos.testing.laws

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.laws.LawNode
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

    val sharedNode = shared.asNode()
    lawTests(lawGraph("Graph") {
        branch("Left") { node(sharedNode) }
        branch("Right") { node(sharedNode) }
        law(first)
        law(second)
        skipped(Counting("never"), "not applicable")
    })

    context("Nested") {
        lawTests(LawNode.branch("Inner", Counting("inner").asNode()))
    }

    test("the shared leaf ran once for both displayed paths") {
        shared.runs shouldBe 1
    }

    test("separately built leaves with the same label both ran, under distinct names") {
        listOf(first.runs, second.runs) shouldBe listOf(1, 1)
        displayNames(listOf("a", "b", "a", "a")) shouldContainExactly listOf("a", "b", "a [2]", "a [3]")
    }
})
