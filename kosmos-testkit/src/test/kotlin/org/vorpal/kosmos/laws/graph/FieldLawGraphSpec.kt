package org.vorpal.kosmos.laws.graph

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawStatus
import org.vorpal.kosmos.laws.NodeReport
import org.vorpal.kosmos.laws.algebra.FieldLaws
import org.vorpal.kosmos.laws.flatten
import org.vorpal.kosmos.laws.leaves
import org.vorpal.kosmos.laws.verify

/**
 * A rational number that remembers whether it came out of the fixture's multiplication.
 * Equality ignores the tag.
 */
private data class Tagged(val q: Rational, val fromMul: Boolean = false)

private val eqTagged = Eq<Tagged> { a, b -> a.q == b.q }
private val prTagged = Printable<Tagged> { it.q.toString() }
private val arbTagged = ArbRational.small.map { Tagged(it) }

/**
 * A controlled "field" on ℚ whose multiplication is ordinary multiplication, except that
 * a left factor that is itself a product adds 1 to the result.
 *
 * Among the field laws, only associativity feeds a product back into the multiplication,
 * so associativity of `*` is the only law this breaks. Every other law sees untagged inputs
 * and ordinary rational arithmetic.
 */
private val nonAssociativeField: Field<Tagged> = Field.of(
    add = AbelianGroup.of(
        identity = Tagged(Rational.ZERO),
        op = BinOp("+") { a, b -> Tagged(a.q + b.q) },
        inverse = Endo("-") { a -> Tagged(-a.q) }
    ),
    mul = CommutativeMonoid.of(
        identity = Tagged(Rational.ONE),
        op = BinOp("*") { a, b ->
            Tagged(a.q * b.q + if (a.fromMul) Rational.ONE else Rational.ZERO, fromMul = true)
        }
    ),
    reciprocal = Endo("⁻¹") { a -> Tagged(a.q.reciprocal()) }
)

/** A real structure that violates several axioms: ℚ with subtraction as its "addition". */
private val subtractionField: Field<Rational> = Field.of(
    add = AbelianGroup.of(
        identity = Rational.ZERO,
        op = BinOp("+") { a, b -> a - b },
        inverse = Endo("-") { a -> -a }
    ),
    mul = RationalAlgebras.RationalField.mul,
    reciprocal = RationalAlgebras.RationalField.reciprocal
)

/** Path labels with the operation symbols stripped from suite names, e.g. "Semigroup (*)" -> "Semigroup". */
private fun NodeReport.shortPath(): List<String> =
    path.labels.dropLast(1).map { it.substringBefore(" (") } + path.labels.last()

class FieldLawGraphSpec : FunSpec({

    val brokenLaws = FieldLaws(nonAssociativeField, arbTagged, eqTagged, prTagged)

    test("the full Field graph reaches multiplicative associativity through Hemiring > Semigroup(*)") {
        val paths = brokenLaws.node(LawMode.FULL).verify().root.leaves().map { it.shortPath() }
        paths.filter { it.last() == "associativity (*)" } shouldContainExactly listOf(
            listOf("Field", "DivisionRing", "Ring", "Rng", "Hemiring", "Semigroup", "associativity (*)")
        )
    }

    test("the compact Field view keeps its recorded coverage, without multiplicative associativity") {
        brokenLaws.laws().map { it.name } shouldNotContain "associativity (*)"
        brokenLaws.node(LawMode.COMPACT).flatten().map { it.name } shouldContainExactly
            brokenLaws.laws().map { it.name }
    }

    test("a non-associative multiplication fails exactly that leaf and its ancestors in full mode") {
        val report = brokenLaws.verify(LawMode.FULL)

        report.status shouldBe LawStatus.FAILED
        report.counts.failed shouldBe 1
        report.counts.errored shouldBe 0
        report.problems.single().shortPath() shouldContainExactly
            listOf("Field", "DivisionRing", "Ring", "Rng", "Hemiring", "Semigroup", "associativity (*)")

        // Every branch on the failing path fails; every other branch passes.
        fun walk(node: NodeReport, onPath: Boolean) {
            if (node is NodeReport.BranchReport) {
                node.status shouldBe if (onPath) LawStatus.FAILED else LawStatus.PASSED
                node.children.forEach { child ->
                    walk(child, child.leaves().any { it.status == LawStatus.FAILED })
                }
            }
        }
        walk(report.root, onPath = true)
    }

    test("the same structure passes the compact view, which does not check multiplicative associativity") {
        brokenLaws.verify(LawMode.COMPACT).status shouldBe LawStatus.PASSED
    }

    test("a structure violating several axioms reports every resulting failure") {
        val report = FieldLaws(
            subtractionField,
            ArbRational.small,
            RationalAlgebras.eqRational,
            RationalAlgebras.printableRationalPretty
        ).verify(LawMode.FULL)

        report.status shouldBe LawStatus.FAILED
        report.problems.map { it.shortPath() } shouldContainExactlyInAnyOrder listOf(
            listOf("Field", "DivisionRing", "Ring", "Rng", "Hemiring", "CommutativeMonoid", "Monoid", "Semigroup", "associativity (+)"),
            listOf("Field", "DivisionRing", "Ring", "Rng", "Hemiring", "CommutativeMonoid", "Monoid", "identity (+)"),
            listOf("Field", "DivisionRing", "Ring", "Rng", "Hemiring", "CommutativeMonoid", "commutativity (+)"),
            listOf("Field", "DivisionRing", "Ring", "Rng", "invertibility (+)")
        )
    }
})
