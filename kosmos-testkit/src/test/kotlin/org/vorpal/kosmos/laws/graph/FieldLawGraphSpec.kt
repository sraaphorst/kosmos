package org.vorpal.kosmos.laws.graph

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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
import org.vorpal.kosmos.laws.LawStatus
import org.vorpal.kosmos.laws.NodeReport
import org.vorpal.kosmos.laws.algebra.FieldLaws
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

    test("every path to multiplicative associativity goes through a semigroup on *, and it runs once") {
        val report = brokenLaws.verify()
        val paths = report.root.leaves().filter { it.label == "associativity (*)" }
        paths.size shouldBe 12
        paths.forEach { it.shortPath().dropLast(1).last() shouldBe "Semigroup" }
        paths.count { it.sharedFrom == null } shouldBe 1
        paths.first().shortPath() shouldContainExactly
            listOf("Field", "DivisionRing", "Ring", "Rng", "Hemiring", "Semigroup", "associativity (*)")
    }

    test("a non-associative multiplication fails exactly that check, and only branches above it fail") {
        val report = brokenLaws.verify()

        report.status shouldBe LawStatus.FAILED
        report.counts.errored shouldBe 0
        report.problems.map { it.label }.toSet() shouldBe setOf("associativity (*)")
        report.problems.count { it.sharedFrom == null } shouldBe 1

        // Every branch on a failing path fails; every other branch passes.
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

    test("a structure violating several axioms reports every resulting failure, each checked once") {
        val report = FieldLaws(
            subtractionField,
            ArbRational.small,
            RationalAlgebras.eqRational,
            RationalAlgebras.printableRationalPretty
        ).verify()

        report.status shouldBe LawStatus.FAILED
        // a - b: not associative or commutative; 0 is only a right identity (0 - a = -a); -a is not an inverse
        // ((-a) - a = -2a); and left division a \ b = (-a) - b does not solve a - x = b.
        report.problems.filter { it.sharedFrom == null }.map { it.shortPath() } shouldContainExactlyInAnyOrder listOf(
            listOf("Field", "DivisionRing", "Ring", "Rng", "Hemiring", "CommutativeMonoid", "Monoid", "Semigroup", "associativity (+)"),
            listOf("Field", "DivisionRing", "Ring", "Rng", "Hemiring", "CommutativeMonoid", "Monoid", "NonAssociativeMonoid", "identity", "left identity (+)"),
            listOf("Field", "DivisionRing", "Ring", "Rng", "Hemiring", "CommutativeMonoid", "CommutativeSemigroup", "commutativity (+)"),
            listOf("Field", "DivisionRing", "Ring", "Rng", "AbelianGroup", "Group", "Loop", "Quasigroup", "left-division (+, \\)"),
            listOf("Field", "DivisionRing", "Ring", "Rng", "AbelianGroup", "Group", "invertibility", "left invertibility (+)"),
            listOf("Field", "DivisionRing", "Ring", "Rng", "AbelianGroup", "Group", "invertibility", "right invertibility (+)")
        )
    }
})
