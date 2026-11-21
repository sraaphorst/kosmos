package org.vorpal.kosmos.laws

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int

/**
 * Laws that assert with Kotest matchers ([TestingLaw.assertionsAreViolations], as spec-local laws do) report a failed
 * matcher as [LeafOutcome.Failed], inside or outside [lawCheckAll]; an exception from the code under test is still
 * [LeafOutcome.Errored]. Reusable laws (the default) keep treating a matcher failure inside a property as an error.
 *
 * Also covers the four- and five-generator forms of [lawCheckAll].
 */
private val ints = Arb.int(-100..100)

private fun asserting(name: String, test: suspend () -> Unit) =
    TestingLaw.named(name, assertionsAreViolations = true, test = test)

private suspend fun outcomeOf(law: TestingLaw): LeafOutcome =
    LawNode.branch("Root", law.asNode()).verify(LawRunConfig(seed = 7)).root.leaves().single().outcome

private fun divide(a: Int, b: Int): Int = if (b == 0) throw ArithmeticException("division by zero") else a / b

class AssertingLawSpec : FunSpec({

    context("a law that asserts with matchers") {
        test("a false matcher inside lawCheckAll is Failed, with seed and counterexample") {
            val outcome = outcomeOf(asserting("x + 1 = x") {
                lawCheckAll(ints) { x -> (x + 1) shouldBe x }
            })
            val failed = outcome.shouldBeInstanceOf<LeafOutcome.Failed>()
            val d = failed.diagnostics.shouldNotBeNull()
            d.cause.shouldBeInstanceOf<LawViolation>()
            d.cause.cause.shouldBeInstanceOf<AssertionError>()     // the matcher's own error, kept
            d.seed.shouldNotBeNull()
            d.counterexample.shouldNotBeNull()
        }

        test("a false matcher outside any property is Failed, even after a passing lawCheckAll") {
            val outcome = outcomeOf(asserting("then 1 = 2") {
                lawCheckAll(ints) { x -> x shouldBe x }
                1 shouldBe 2
            })
            outcome.shouldBeInstanceOf<LeafOutcome.Failed>()
        }

        test("an exception from the code under test is still Errored, inside and outside a property") {
            val inside = outcomeOf(asserting("x / 0 inside") {
                lawCheckAll(ints) { x -> divide(x, 0) shouldBe 0 }
            })
            inside.shouldBeInstanceOf<LeafOutcome.Errored>().diagnostics.shouldNotBeNull()
                .cause.shouldBeInstanceOf<ArithmeticException>()

            val outside = outcomeOf(asserting("x / 0 outside") { divide(1, 0) shouldBe 0 })
            outside.shouldBeInstanceOf<LeafOutcome.Errored>()
        }

        test("a passing law passes") {
            outcomeOf(asserting("x = x") { lawCheckAll(ints) { x -> x shouldBe x } }) shouldBe LeafOutcome.Passed
        }
    }

    context("a reusable law (the default)") {
        test("a matcher failure inside lawCheckAll is Errored: reusable laws signal falsity with lawCheck") {
            val outcome = outcomeOf(TestingLaw.named("x + 1 = x") {
                lawCheckAll(ints) { x -> (x + 1) shouldBe x }
            })
            outcome.shouldBeInstanceOf<LeafOutcome.Errored>()
        }
    }

    context("four and five generators") {
        test("a false predicate is Failed and the counterexample shows all four values") {
            val outcome = outcomeOf(TestingLaw.named("a + b + c + d = 0") {
                lawCheckAll(ints, ints, ints, ints) { a, b, c, d -> lawCheck(a + b + c + d == 0) { "sum" } }
            })
            val d = outcome.shouldBeInstanceOf<LeafOutcome.Failed>().diagnostics.shouldNotBeNull()
            d.cause.shouldBeInstanceOf<LawViolation>()
            d.counterexample.shouldNotBeNull().split(", ").size shouldBe 4
        }

        test("a false predicate is Failed and the counterexample shows all five values") {
            val outcome = outcomeOf(TestingLaw.named("a + b + c + d + e = 0") {
                lawCheckAll(ints, ints, ints, ints, ints) { a, b, c, d, e ->
                    lawCheck(a + b + c + d + e == 0) { "sum" }
                }
            })
            val d = outcome.shouldBeInstanceOf<LeafOutcome.Failed>().diagnostics.shouldNotBeNull()
            d.counterexample.shouldNotBeNull().split(", ").size shouldBe 5
        }

        test("an exception from the operation is Errored, with its cause") {
            val four = outcomeOf(TestingLaw.named("four") {
                lawCheckAll(ints, ints, ints, ints) { a, _, _, _ -> lawCheck(divide(a, 0) == 0) }
            })
            four.shouldBeInstanceOf<LeafOutcome.Errored>().diagnostics.shouldNotBeNull()
                .cause.shouldBeInstanceOf<ArithmeticException>()

            val five = outcomeOf(TestingLaw.named("five") {
                lawCheckAll(ints, ints, ints, ints, ints) { a, _, _, _, _ -> lawCheck(divide(a, 0) == 0) }
            })
            five.shouldBeInstanceOf<LeafOutcome.Errored>().diagnostics.shouldNotBeNull()
                .cause.message.shouldNotBeNull() shouldContain "division by zero"
        }

        test("a passing four- and five-generator law passes") {
            val sym = TestingLaw.named("symmetric sums") {
                lawCheckAll(ints, ints, ints, ints) { a, b, c, d -> lawCheck(a + b + c + d == d + c + b + a) }
                lawCheckAll(ints, ints, ints, ints, ints) { a, b, c, d, e ->
                    lawCheck(a + b + c + d + e == e + d + c + b + a)
                }
            }
            LawNode.branch("Root", sym.asNode()).verify().root.leaves().map { it.status } shouldContainExactly
                listOf(LawStatus.PASSED)
        }
    }
})
