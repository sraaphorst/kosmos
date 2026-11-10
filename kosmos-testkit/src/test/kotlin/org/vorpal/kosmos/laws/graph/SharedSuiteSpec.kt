package org.vorpal.kosmos.laws.graph

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawRunConfig
import org.vorpal.kosmos.laws.LawStatus
import org.vorpal.kosmos.laws.NodeReport
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
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

private fun NodeReport.shortPath(): String =
    path.labels.drop(1).joinToString(" > ") { it.substringBefore(" (") }

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

    test("the full StarAlgebra(Q over Q) graph runs 32 of its 52 law occurrences") {
        val report = laws.verify(LawMode.FULL, LawRunConfig(seed = 1))

        report.status shouldBe LawStatus.PASSED
        report.counts.total shouldBe 52            // same as fullLaws(): coverage is unchanged
        laws.fullLaws().size shouldBe 52
        report.executions shouldBe 32

        val nas = "NonAssociativeStarAlgebra > NonAssociativeAlgebra"
        report.root.sharedRoots().map { (copy, original) -> copy.shortPath() to original.shortPath() } shouldContainExactlyInAnyOrder listOf(
            "$nas > NonAssociativeRing > AbelianGroup > Group > Monoid" to
                "$nas > CommutativeRing > CommutativeRng > Rng > Hemiring > CommutativeMonoid > Monoid",
            "$nas > NonAssociativeRing > NonAssociativeMonoid > Magma" to
                "$nas > CommutativeRing > CommutativeRng > Rng > Hemiring > Semigroup > Magma",
            "$nas > RModule > CommutativeRing" to "$nas > CommutativeRing",
            "$nas > RModule > AbelianGroup" to "$nas > NonAssociativeRing > AbelianGroup"
        )
    }

    test("every shared node is bound exactly like the node it repeats") {
        val report = laws.verify(LawMode.FULL)
        report.root.sharedRoots().forEach { (copy, original) ->
            copy.node.key shouldBe original.node.key
            copy.status shouldBe original.status
            copy.counts shouldBe original.counts
        }
    }

    test("the compact view has nothing to share") {
        val report = laws.verify(LawMode.COMPACT)
        report.executions shouldBe report.counts.total
        report.counts.total shouldBe laws.laws().size
    }
})
