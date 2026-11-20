package org.vorpal.kosmos.laws

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.relations.Relation
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.core.relations.instances.RationalRelations
import org.vorpal.kosmos.laws.algebra.AbsoluteValueLaws
import org.vorpal.kosmos.laws.algebra.FieldLaws
import org.vorpal.kosmos.laws.algebra.IntegerPowerLaws
import org.vorpal.kosmos.laws.algebra.NaturalPowerLaws
import org.vorpal.kosmos.laws.algebra.OrderedFieldLaws
import org.vorpal.kosmos.laws.relation.StrictTotalOrderLaws
import org.vorpal.kosmos.laws.relation.TotalOrderLaws

private val q = RationalAlgebras.RationalField
private val rationals: Arb<Rational> = Arb.bind(Arb.int(-20..20), Arb.int(1..12)) { n, d -> Rational.of(n, d) }
private val le = RationalRelations.RationalTotalOrder
private val abs = Endo<Rational>("|·|") { it.abs() }

/** ℚ with its order reversed: still a total order, and still compatible with addition, but not with products. */
private val reversed: TotalOrder<Rational> = TotalOrder.of(Relation("≥") { a, b -> a >= b })

/** Total order by absolute value, ties broken by value: total, but not compatible with addition. */
private val byAbs: TotalOrder<Rational> =
    TotalOrder.of(Relation("≤|·|") { a, b -> a.abs() < b.abs() || (a.abs() == b.abs() && a <= b) })

private fun LawReport.failedLabels(): Set<String> = problems.map { it.label }.toSet()

/**
 * The reusable order, absolute value and power laws accept ℚ and reject broken variants of it.
 */
class OrderedLawsSpec : FunSpec({

    context("orders") {
        test("≤ on ℚ is a total order and < a strict total order") {
            TotalOrderLaws(le.relation, rationals).verify().status shouldBe LawStatus.PASSED
            StrictTotalOrderLaws(RationalRelations.RationalTotalStrictOrder.strictRelation, rationals).verify()
                .status shouldBe LawStatus.PASSED
        }

        test("< is not a total order, and ≤ is not a strict one") {
            TotalOrderLaws(RationalRelations.RationalLT, rationals).verify().failedLabels() shouldContain "reflexivity (<)"
            StrictTotalOrderLaws(le.relation, rationals).verify().failedLabels() shouldContain
                "irreflexivity (${le.relation.symbol})"
        }
    }

    context("ordered fields") {
        test("ℚ with its usual order is an ordered field") {
            OrderedFieldLaws(q, le, rationals).verify().status shouldBe LawStatus.PASSED
        }

        test("the reversed order fails nonnegative products, not additive monotonicity") {
            val failed = OrderedFieldLaws(q, reversed, rationals).verify().failedLabels()
            failed shouldContain "nonnegative products (≥, *)"
            failed shouldNotContain "monotonicity (≥, +)"
        }

        test("ordering by absolute value fails additive monotonicity") {
            OrderedFieldLaws(q, byAbs, rationals).verify().failedLabels() shouldContain "monotonicity (≤|·|, +)"
        }

        test("an ordered field shares its field laws with a separately registered field") {
            val both = lawGraph("ℚ") {
                suite(OrderedFieldLaws(q, le, rationals))
                suite(FieldLaws(q, rationals))
            }.verify()
            both.executions shouldBe OrderedFieldLaws(q, le, rationals).verify().executions
        }
    }

    context("absolute value") {
        test("|·| on ℚ satisfies the absolute value laws") {
            AbsoluteValueLaws(q, le, abs, rationals).verify().status shouldBe LawStatus.PASSED
        }

        test("the identity is not an absolute value") {
            val failed = AbsoluteValueLaws(q, le, Endo("id") { it }, rationals).verify().failedLabels()
            failed shouldContainAll listOf("nonnegativity (id)", "agrees with the order (id, ${le.relation.symbol})")
        }

        test("squaring fails the triangle inequality") {
            AbsoluteValueLaws(q, le, Endo("sq") { it * it }, rationals).verify().failedLabels() shouldContain
                "triangle inequality (sq, +)"
        }
    }

    context("powers") {
        test("Rational.pow satisfies the integer power laws") {
            IntegerPowerLaws(q.mul, q.reciprocal, Rational::pow, rationals, Rational.ZERO).verify()
                .status shouldBe LawStatus.PASSED
        }

        test("a power that ignores the sign of negative exponents fails only the negative-exponent laws") {
            val badPow: (Rational, Int) -> Rational = { x, n -> x.pow(kotlin.math.abs(n)) }
            NaturalPowerLaws(q.mul, badPow, rationals).verify().status shouldBe LawStatus.PASSED
            IntegerPowerLaws(q.mul, q.reciprocal, badPow, rationals, Rational.ZERO).verify().failedLabels() shouldBe
                setOf("negative power (${q.reciprocal.symbol})", "power addition (*, exponents -10..10)")
        }

        test("a missing inverse fails the integer power laws, even with correct powers") {
            val noInverse = UnaryOp<Rational, Rational?>(q.reciprocal.symbol) { null }
            IntegerPowerLaws(q.mul, noInverse, Rational::pow, rationals, Rational.ZERO).verify().failedLabels() shouldBe
                setOf("inverse exists (${q.reciprocal.symbol})")
        }

        test("a power that is off by one fails the natural power laws") {
            val badPow: (Rational, Int) -> Rational = { x, n -> x.pow(n + 1) }
            NaturalPowerLaws(q.mul, badPow, rationals).verify().failedLabels() shouldContainAll
                listOf("zeroth power", "first power", "power addition (*, exponents 0..10)")
        }
    }
})
