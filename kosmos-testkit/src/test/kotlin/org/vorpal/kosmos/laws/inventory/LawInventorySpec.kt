package org.vorpal.kosmos.laws.inventory

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.algebra.AbelianGroupLaws
import org.vorpal.kosmos.laws.algebra.AbelianHeapLaws
import org.vorpal.kosmos.laws.algebra.AlgebraLaws
import org.vorpal.kosmos.laws.algebra.CommutativeMonoidLaws
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.CommutativeRngLaws
import org.vorpal.kosmos.laws.algebra.CommutativeSemigroupLaws
import org.vorpal.kosmos.laws.algebra.CommutativeSemiringLaws
import org.vorpal.kosmos.laws.algebra.ConjugationLaws
import org.vorpal.kosmos.laws.algebra.DivisionRingLaws
import org.vorpal.kosmos.laws.algebra.FieldLaws
import org.vorpal.kosmos.laws.algebra.GroupLaws
import org.vorpal.kosmos.laws.algebra.HeapLaws
import org.vorpal.kosmos.laws.algebra.HemiringLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.LeftRModuleLaws
import org.vorpal.kosmos.laws.algebra.LoopLaws
import org.vorpal.kosmos.laws.algebra.MagmaLaws
import org.vorpal.kosmos.laws.algebra.MonoidLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeAlgebraLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeMonoidLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.NormedDivisionAlgebraLaws
import org.vorpal.kosmos.laws.algebra.QuasigroupLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.algebra.RingLaws
import org.vorpal.kosmos.laws.algebra.RngLaws
import org.vorpal.kosmos.laws.algebra.SemigroupLaws
import org.vorpal.kosmos.laws.algebra.SemiringLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import java.io.File

/**
 * Golden snapshot of the ordered law inventory of representative suites, in both
 * compact (`laws()`) and full (`fullLaws()`) modes.
 *
 * Purpose: the law-testing restructure (#252) changes how suites are assembled.
 * This spec pins down *which* laws each suite runs, in which order and how many
 * times, so a structural refactor cannot silently drop, add or reorder coverage.
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

        fun renderInventory(suites: List<Pair<String, LawSuite>>): String = buildString {
            suites.forEach { (label, suite) ->
                listOf("compact" to suite.laws(), "full" to suite.fullLaws()).forEach { (mode, laws) ->
                    appendLine("## $label [$mode] (${laws.size})")
                    laws.forEach { appendLine(lawLine(it)) }
                    appendLine()
                }
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
            )
        }
    }
}
