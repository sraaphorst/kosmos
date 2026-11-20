package org.vorpal.kosmos.laws.graph

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.laws.LawRunConfig
import org.vorpal.kosmos.laws.LawStatus
import org.vorpal.kosmos.laws.NodeReport
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.leaves
import org.vorpal.kosmos.laws.verify

/** Every report in the tree that repeats an earlier result, mapped to the path it repeats (outermost only). */
private fun NodeReport.sharedRoots(): List<Pair<NodeReport, NodeReport>> = buildList {
    val byPath = HashMap<List<Int>, NodeReport>()
    fun index(n: NodeReport) {
        byPath[n.path.indices] = n
        if (n is NodeReport.BranchReport) n.children.forEach(::index)
    }
    fun walk(n: NodeReport) {
        val from = n.sharedFrom
        if (from != null) add(n to byPath.getValue(from.indices))
        else if (n is NodeReport.BranchReport) n.children.forEach(::walk)
    }
    index(this@sharedRoots)
    walk(this@sharedRoots)
}

/**
 * Stage 4 on a real suite: ℚ as a star algebra over itself repeats additive and ring sub-suites across its
 * module, ring and algebra parents. Each is bound to the same objects, so the run executes it once.
 */
class SharedSuiteSpec : FunSpec({

    val q = RationalAlgebras.RationalStarField
    val laws = StarAlgebraLaws(
        q, ArbRational.small, ArbRational.small,
        RationalAlgebras.eqRational, RationalAlgebras.eqRational,
        RationalAlgebras.printableRationalPretty, RationalAlgebras.printableRationalPretty
    )

    test("the StarAlgebra(Q over Q) graph runs each of its 39 distinct checks once") {
        val report = laws.verify(LawRunConfig(seed = 1))

        report.status shouldBe LawStatus.PASSED
        report.counts.total shouldBe 394
        report.executions shouldBe 39

        // No law executes twice: every repeated occurrence is a shared copy.
        val executed = report.root.leaves().filter { it.sharedFrom == null }
        executed.size shouldBe 39
        executed.map { it.node.key }.toSet().size shouldBe 39
    }

    test("every shared node is bound exactly like the node it repeats") {
        val report = laws.verify()
        report.root.sharedRoots().forEach { (copy, original) ->
            copy.node.key shouldBe original.node.key
            copy.status shouldBe original.status
            copy.counts shouldBe original.counts
        }
    }
})
