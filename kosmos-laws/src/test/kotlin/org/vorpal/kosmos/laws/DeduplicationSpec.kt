package org.vorpal.kosmos.laws

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.AbelianGroupLaws
import org.vorpal.kosmos.laws.algebra.FieldLaws
import org.vorpal.kosmos.laws.algebra.MonoidLaws
import org.vorpal.kosmos.laws.algebra.RingLaws
import org.vorpal.kosmos.laws.property.IdentityLaw

private val q = RationalAlgebras.RationalField
private val rationals: Arb<Rational> = Arb.bind(Arb.int(-20..20), Arb.int(1..12)) { n, d -> Rational.of(n, d) }
private val ints = Arb.int(-50..50)

/** Integer addition that counts its calls, to see how often a law really ran. */
private class TallyAdd {
    var calls = 0
    val op = BinOp<Int>("+") { a, b -> calls++; a + b }
    val monoid = Monoid.of(0, op)
}

/** The leaves that actually executed (not shown as a copy of an earlier one). */
private fun LawReport.executed(): List<NodeReport.LeafReport> =
    root.leaves().filter { it.sharedFrom == null }

/** Executions of the leaves with this label. */
private fun LawReport.executionsOf(label: String): Int =
    executed().count { it.label == label }

/** Occurrences (paths) of the leaves with this label. */
private fun LawReport.occurrencesOf(label: String): Int =
    root.leaves().count { it.label == label }

/**
 * Acceptance tests for law deduplication (#61): every equivalent bound check executes exactly once per run, and
 * checks that differ in anything that changes them stay separate. All suites are built the normal way, with
 * default arguments and the generators the suites derive themselves.
 */
class DeduplicationSpec : FunSpec({

    context("the deduplication table") {
        test("the same bound law reached through two inheritance paths executes once") {
            // A field reaches associativity of * through its hemiring (Semigroup on *) and its semiring
            // (Monoid on *), among others.
            val report = FieldLaws(q, rationals, RationalAlgebras.eqRational).verify()
            report.occurrencesOf("associativity (*)") shouldBeGreaterThan 1
            report.executionsOf("associativity (*)") shouldBe 1
        }

        test("the same operations with a different equality or generator execute separately") {
            val add = TallyAdd()
            val otherEq = Eq<Int> { a, b -> a == b }
            val report = lawGraph("Root") {
                suite(MonoidLaws(add.monoid, ints))
                suite(MonoidLaws(add.monoid, ints, otherEq))
                suite(MonoidLaws(add.monoid, Arb.int(0..9)))
            }.verify()
            report.executionsOf("associativity (+)") shouldBe 3
        }

        test("the same law class with a different identity parameter executes separately") {
            val add = TallyAdd()
            val report = lawGraph("Root") {
                suite(IdentityLaw(add.op, 0, ints))
                suite(IdentityLaw(add.op, 1, ints))
                suite(IdentityLaw(add.op, 0, ints))
            }.verify()
            report.executionsOf("left identity (+)") shouldBe 2
            report.executionsOf("right identity (+)") shouldBe 2
        }

        test("running the same graph again executes each distinct check once more") {
            val add = TallyAdd()
            val graph = lawGraph("Root") {
                suite(MonoidLaws(add.monoid, ints))
                suite(MonoidLaws(add.monoid, ints))
            }
            val first = graph.verify(LawRunConfig(seed = 7))
            val callsAfterFirst = add.calls
            val second = graph.verify(LawRunConfig(seed = 7))

            first.executions shouldBe second.executions
            add.calls shouldBe 2 * callsAfterFirst
        }
    }

    context("overlapping declarations") {
        test("a field, a ring and a group declared together run each equivalent check once") {
            val report = lawGraph("ℚ") {
                suite(FieldLaws(q, rationals, RationalAlgebras.eqRational))
                suite(RingLaws(q, rationals, RationalAlgebras.eqRational))
                suite(AbelianGroupLaws(q.add, rationals, RationalAlgebras.eqRational))
            }.verify()

            report.status shouldBe LawStatus.PASSED

            // Every law is bound, and no two executed laws are bound the same way.
            val executedKeys = report.executed().map { it.node.key.shouldNotBeNull() }
            executedKeys.toSet() shouldHaveSize executedKeys.size
            report.executions shouldBe executedKeys.size

            // The ring and the additive group add nothing the field has not already run.
            val fieldOnly = FieldLaws(q, rationals, RationalAlgebras.eqRational).verify()
            report.executions shouldBe fieldOnly.executions
        }

        test("a field's graph contains every field axiom") {
            val labels = FieldLaws(q, rationals, RationalAlgebras.eqRational).verify().root.leaves().map { it.label }.toSet()
            labels shouldContainAll listOf(
                "totality (+)", "associativity (+)", "commutativity (+)", "left identity (+)", "right identity (+)",
                "inverse exists (-)", "left invertibility (+)", "right invertibility (+)",
                "totality (*)", "associativity (*)", "commutativity (*)", "left identity (*)", "right identity (*)",
                "left invertibility (*)", "right invertibility (*)",
                "distributivity (left: * over +)", "distributivity (right: * over +)"
            )
            labels.any { it.startsWith("inverse exists") && it != "inverse exists (-)" } shouldBe true
            labels shouldContainAll listOf("distinct identities / nontriviality (1 ≠ 0)")
        }
    }
})
