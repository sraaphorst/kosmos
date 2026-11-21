package org.vorpal.kosmos.laws.inventory

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.BooleanAlgebras
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.setoid.Setoid
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.algebra.AbelianGroupLaws
import org.vorpal.kosmos.laws.algebra.AbelianHeapLaws
import org.vorpal.kosmos.laws.algebra.AbsoluteValueLaws
import org.vorpal.kosmos.laws.algebra.AlgebraLaws
import org.vorpal.kosmos.laws.algebra.BooleanAlgebraLaws
import org.vorpal.kosmos.laws.algebra.BoundedLatticeLaws
import org.vorpal.kosmos.laws.algebra.CommutativeMonoidLaws
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.CommutativeRngLaws
import org.vorpal.kosmos.laws.algebra.CommutativeSemigroupLaws
import org.vorpal.kosmos.laws.algebra.CommutativeSemiringLaws
import org.vorpal.kosmos.laws.algebra.ConjugationLaws
import org.vorpal.kosmos.laws.algebra.DistributiveLatticeLaws
import org.vorpal.kosmos.laws.algebra.DivisionRingLaws
import org.vorpal.kosmos.laws.algebra.FieldLaws
import org.vorpal.kosmos.laws.algebra.GroupLaws
import org.vorpal.kosmos.laws.algebra.HeapLaws
import org.vorpal.kosmos.laws.algebra.HemiringLaws
import org.vorpal.kosmos.laws.algebra.IntegerPowerLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.JoinSemilatticeLaws
import org.vorpal.kosmos.laws.algebra.LatticeLaws
import org.vorpal.kosmos.laws.algebra.LeftRModuleLaws
import org.vorpal.kosmos.laws.algebra.LoopLaws
import org.vorpal.kosmos.laws.algebra.MagmaLaws
import org.vorpal.kosmos.laws.algebra.MeetSemilatticeLaws
import org.vorpal.kosmos.laws.algebra.MonoidLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeAlgebraLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeDivisionAlgebraLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeMonoidLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.NormedDivisionAlgebraLaws
import org.vorpal.kosmos.laws.algebra.OrderedFieldLaws
import org.vorpal.kosmos.laws.algebra.QuasigroupLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.algebra.RingLaws
import org.vorpal.kosmos.laws.algebra.RngLaws
import org.vorpal.kosmos.laws.algebra.SemigroupLaws
import org.vorpal.kosmos.laws.algebra.SemiringLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.flatten
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRngHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.RngHomomorphismLaws
import org.vorpal.kosmos.laws.relation.EquivalenceRelationLaws
import org.vorpal.kosmos.laws.relation.StrictTotalOrderLaws
import org.vorpal.kosmos.laws.relation.TotalOrderLaws
import org.vorpal.kosmos.laws.setoid.SetoidLaws
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.relations.instances.RationalRelations
import java.io.File

/**
 * Golden snapshot of the law inventory of representative suites: the distinct checks each suite's graph
 * executes (in first-occurrence order), and how many occurrences (paths) the graph has.
 *
 * Purpose: pin down *which* checks each suite runs, so a structural change cannot silently drop, add or
 * duplicate coverage. A check is distinct by its binding, exactly as [org.vorpal.kosmos.laws.LawRunner]
 * shares results within a run.
 *
 * Nothing is executed here: only the law lists are built and compared.
 *
 * When a change to the inventory is intended, regenerate the snapshot:
 * ```
 * UPDATE_LAW_INVENTORY=true ./gradlew :kosmos-testkit:test --tests '*LawInventorySpec*'
 * ```
 * and review the diff of `law-inventory.txt` like any other code change.
 */
class LawInventorySpec : StringSpec({

    "law inventory matches the recorded snapshot" {
        val actual = renderInventory(representativeSuites())
        val golden = File(GOLDEN_PATH)

        if (System.getenv("UPDATE_LAW_INVENTORY") == "true") {
            golden.parentFile.mkdirs()
            golden.writeText(actual)
        }
        check(golden.exists()) { "Missing $GOLDEN_PATH; regenerate with UPDATE_LAW_INVENTORY=true." }

        actual shouldBe golden.readText()
    }
}) {
    companion object {
        /** Relative to the module directory, which is Gradle's test working directory. */
        const val GOLDEN_PATH = "src/test/resources/law-inventory.txt"

        private fun lawLine(law: TestingLaw): String =
            "${law::class.simpleName ?: "<anonymous>"} | ${law.name}"

        /** The distinct checks of [root] in first-occurrence order, skipping repeats the way a run shares them. */
        private fun distinctLaws(root: LawNode): List<TestingLaw> {
            val seen = HashSet<Any>()
            val out = mutableListOf<TestingLaw>()
            fun visit(node: LawNode) {
                if (!seen.add(node.key ?: node)) return
                when (node) {
                    is LawNode.Leaf -> out += node.law
                    is LawNode.Branch -> node.children.forEach(::visit)
                }
            }
            visit(root)
            return out
        }

        fun renderInventory(suites: List<Pair<String, LawSuite>>): String = buildString {
            suites.forEach { (label, suite) ->
                val root = suite.node()
                val laws = distinctLaws(root)
                appendLine("## $label (${laws.size} checks, ${root.flatten().size} occurrences)")
                laws.forEach { appendLine(lawLine(it)) }
                appendLine()
            }
        }

        fun representativeSuites(): List<Pair<String, LawSuite>> {
            val z = IntegerAlgebras.IntegerCommutativeRing
            val zArb = ArbInteger.small
            val zEq = IntegerAlgebras.eqInteger
            val zPr = IntegerAlgebras.printableInteger

            val q = RationalAlgebras.RationalStarField
            val qArb = ArbRational.small
            val qEq = RationalAlgebras.eqRational
            val qPr = RationalAlgebras.printableRationalPretty

            val bool = BooleanAlgebras.BoolAlgebra
            val boolArb = Arb.boolean()

            return listOf(
                // Single-operation chain on (Z, +) and (Z, *).
                "Magma(Z,+)" to MagmaLaws(z.add, zArb, zPr),
                "Semigroup(Z,+)" to SemigroupLaws(z.add, zArb, zEq, zPr),
                "CommutativeSemigroup(Z,+)" to CommutativeSemigroupLaws(z.add, zArb, zEq, zPr),
                "Monoid(Z,*)" to MonoidLaws(z.mul, zArb, zEq, zPr),
                "CommutativeMonoid(Z,*)" to CommutativeMonoidLaws(z.mul, zArb, zEq, zPr),
                "NonAssociativeMonoid(Z,*)" to NonAssociativeMonoidLaws(z.mul, zArb, zEq, zPr),
                "Quasigroup(Z,+)" to QuasigroupLaws(z.add, zArb, zEq, zPr),
                "Loop(Z,+)" to LoopLaws(z.add, zArb, zEq, zPr),
                "Group(Z,+)" to GroupLaws(z.add, zArb, zEq, zPr),
                "AbelianGroup(Z,+)" to AbelianGroupLaws(z.add, zArb, zEq, zPr),
                "Heap(Z)" to HeapLaws(IntegerAlgebras.IntegerAbelianHeap, zArb, zEq, zPr),
                "AbelianHeap(Z)" to AbelianHeapLaws(IntegerAlgebras.IntegerAbelianHeap, zArb, zEq, zPr),

                // Two-operation chain on Z.
                "Hemiring(Z)" to HemiringLaws(z, zArb, zEq, zPr),
                "Semiring(Z)" to SemiringLaws(z, zArb, zEq, zPr),
                "CommutativeSemiring(Z)" to CommutativeSemiringLaws(z, zArb, zEq, zPr),
                "NonAssociativeRing(Z)" to NonAssociativeRingLaws(z, zArb, zEq, zPr),
                "Rng(Z)" to RngLaws(z, zArb, zEq, zPr),
                "CommutativeRng(Z)" to CommutativeRngLaws(z, zArb, zEq, zPr),
                "Ring(Z)" to RingLaws(z, zArb, zEq, zPr),
                "CommutativeRing(Z)" to CommutativeRingLaws(z, zArb, zEq, zPr),
                "NonAssociativeInvolutiveRing(Z)" to NonAssociativeInvolutiveRingLaws(z, zArb, zEq, zPr),
                "InvolutiveRing(Z)" to InvolutiveRingLaws(z, zArb, zEq, zPr),
                "Conjugation(Z)" to ConjugationLaws(z.conj, z.add, z.mul, zArb, zEq, zPr),

                // Division ring / field on Q.
                "DivisionRing(Q)" to DivisionRingLaws(q, qArb, qEq, qPr),
                "Field(Q)" to FieldLaws(q, qArb, qEq, qPr),
                "OrderedField(Q)" to OrderedFieldLaws(q, RationalRelations.RationalTotalOrder, qArb, qEq, qPr),
                "AbsoluteValue(Q)" to AbsoluteValueLaws(
                    q, RationalRelations.RationalTotalOrder, Endo("|·|") { it.abs() }, qArb, qEq, qPr
                ),
                "IntegerPowers(Q)" to IntegerPowerLaws(q.mul, q.reciprocal, Rational::pow, qArb, Rational.ZERO, 10, qEq, qPr),

                // Modules and algebras over Q (and Z).
                "LeftRModule(Q over Q)" to LeftRModuleLaws(q, qArb, qArb, qEq, qEq, qPr, qPr),
                "RModule(Q over Z)" to RModuleLaws(
                    RationalAlgebras.ZModuleRational, zArb, qArb, zEq, qEq, zPr, qPr
                ),
                "NonAssociativeAlgebra(Q over Q)" to NonAssociativeAlgebraLaws(q, qArb, qArb, qEq, qEq, qPr, qPr),
                "Algebra(Q over Q)" to AlgebraLaws(q, qArb, qArb, qEq, qEq, qPr, qPr),
                "NonAssociativeStarAlgebra(Q over Q)" to NonAssociativeStarAlgebraLaws(q, qArb, qArb, qEq, qEq, qPr, qPr),
                "StarAlgebra(Q over Q)" to StarAlgebraLaws(q, qArb, qArb, qEq, qEq, qPr, qPr),
                "NormedDivisionAlgebra(Q)" to NormedDivisionAlgebraLaws(
                    q, qArb, q.mul.op, qEq, qEq, qPr, qPr
                ),
                "NonAssociativeDivisionAlgebra(Q)" to NonAssociativeDivisionAlgebraLaws(q, qArb, qEq, qPr),

                // Homomorphisms: the identity on Z.
                "NonAssociativeRngHomomorphism(Z -> Z)" to
                    NonAssociativeRngHomomorphismLaws({ it }, z, z, zArb, zEq, zPr, zPr),
                "NonAssociativeRingHomomorphism(Z -> Z)" to
                    NonAssociativeRingHomomorphismLaws({ it }, z, z, zArb, zEq, zPr, zPr),
                "RngHomomorphism(Z -> Z)" to RngHomomorphismLaws({ it }, z, z, zArb, zEq, zPr, zPr),
                "RingHomomorphism(Z -> Z)" to RingHomomorphismLaws({ it }, z, z, zArb, zEq, zPr, zPr),

                // Lattices on Boolean.
                "JoinSemilattice(Bool)" to JoinSemilatticeLaws(bool, boolArb),
                "MeetSemilattice(Bool)" to MeetSemilatticeLaws(bool, boolArb),
                "Lattice(Bool)" to LatticeLaws(bool, boolArb),
                "BoundedLattice(Bool)" to BoundedLatticeLaws(bool, boolArb),
                "DistributiveLattice(Bool)" to DistributiveLatticeLaws(bool, boolArb),
                "BooleanAlgebra(Bool)" to BooleanAlgebraLaws(bool, boolArb),

                // Relations.
                "EquivalenceRelation(Z, =)" to EquivalenceRelationLaws(Setoid.of(zEq).relation, zArb, zPr),
                "Setoid(Z)" to SetoidLaws(Setoid.of(zEq), zArb, zPr),
                "TotalOrder(Q, ≤)" to TotalOrderLaws(RationalRelations.RationalTotalOrder.relation, qArb, qEq, qPr),
                "StrictTotalOrder(Q, <)" to
                    StrictTotalOrderLaws(RationalRelations.RationalTotalStrictOrder.strictRelation, qArb, qEq, qPr),
            )
        }
    }
}
