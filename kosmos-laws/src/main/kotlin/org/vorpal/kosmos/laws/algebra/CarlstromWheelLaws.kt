package org.vorpal.kosmos.laws.algebra

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.CarlstromWheel
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.AnnihilationLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [CarlstromWheel] law suite (this instance has bottom `⊥` and infinity `∞`).
 * [WheelLaws] with the additional laws below.
 *
 * Bottom / nullity behavior:
 * - `inv(⊥) = ⊥`
 * - `⊥` is absorbing for both operations (tested via [AnnihilationLaw]):
 *   - `⊥ + x = ⊥` and `x + ⊥ = ⊥`
 *   - `⊥ * x = ⊥` and `x * ⊥ = ⊥`
 *
 * Infinity behavior (single-infinity Carlström convention: ∞ corresponds to 1/0, ⊥ to 0/0):
 * - `inv(0) = ∞`
 * - `inv(∞) = 0`
 * - `∞ + ∞ = ⊥`
 * - `∞ * inv(∞) = ⊥`
 * - `0 * ∞ = ⊥`
 * - `∞ * ∞ = ∞`
 *
 * - The [Arb] used should be biased towards the special values with heavy weights given to:
 *   `⊥`, `∞`, `0`, and `1`. The rest of the time, the [Arb] should produce random finite values.
 *
 * Notes:
 * - Wheels are ring-like but not rings: identities such as `0 * x = 0` do not hold globally once
 *   infinity / nullity are present.
 *   [oai_citation:2‡Cambridge University Press & Assessment](https://www.cambridge.org/core/journals/mathematical-structures-in-computer-science/article/wheels-on-division-by-zero/183248B486FBFAF27E8AE3EE1EEA4717?utm_source=chatgpt.com)
 * - The “annihilation” laws here are really “absorbing element” laws; wheels use a designated
 *   nullity element (often `0/0`) that propagates through operations.
 *   [oai_citation:3‡Mathematics Stack Exchange](https://math.stackexchange.com/questions/3003703/what-are-the-mathematical-properties-of-%E2%8A%A5-in-wheel-theory?utm_source=chatgpt.com)
 */
class CarlstromWheelLaws<A : Any>(
    val wheel: CarlstromWheel<A>,
    val arb: Arb<A>,
    val eq: Eq<A> = Eq.default(),
    val pr: Printable<A> = Printable.default()
): LawSuite {

    override val name = suiteName(
        "CarlstromWheel", wheel.add.op.symbol, wheel.mul.op.symbol, wheel.inv.symbol
    )

    private val bottom = pr(wheel.bottom).ifEmpty { Symbols.BOTTOM }
    private val infinity = pr(wheel.inf).ifEmpty { Symbols.INFINITY }
    private val zero = pr(wheel.zero).ifEmpty { Symbols.ZERO }
    private val one = pr(wheel.one).ifEmpty { Symbols.ONE }

    private val wheelLaws = WheelLaws(wheel, arb, eq, pr)
    private val bottomDominatesAdd = AnnihilationLaw(wheel.add.op, wheel.bottom, arb, eq, pr)
    private val bottomDominatesMul = AnnihilationLaw(wheel.mul.op, wheel.bottom, arb, eq, pr)

    /**
     * Test the special values of the wheel with regard to the inv operation.
     */
    private val specialInvLaws: TestingLaw = object : TestingLaw {
        override val name = "inv special values (${wheel.inv.symbol})"

        override suspend fun test() {
            val result1 = wheel.inv(wheel.bottom)
            withClue("inv($bottom) should be $bottom, got: ${pr(result1)}") {
                check(eq(result1, wheel.bottom))
            }

            val result2 = wheel.inv(wheel.zero)
            withClue("inv($zero) should be $infinity, got: ${pr(result2)}") {
                check(eq(result2, wheel.inf))
            }

            val result3 = wheel.inv(wheel.inf)
            withClue("inv($infinity) should be $zero, got: ${pr(result3)}") {
                check(eq(result3, wheel.zero))
            }

            val result4 = wheel.inv(wheel.one)
            withClue("inv($one) should be $one, got: ${pr(result4)}") {
                check(eq(result4, wheel.one))
            }
        }
    }

    private val specialInfLaws: TestingLaw = object : TestingLaw {
        private val addSym = wheel.add.op.symbol
        private val mulSym = wheel.mul.op.symbol
        override val name = "$infinity special cases ($addSym, $mulSym)"

        override suspend fun test() {
            val result1 = wheel.add(wheel.inf, wheel.inf)
            withClue("$infinity $addSym $infinity should be $bottom, got: ${pr(result1)}") {
                check(eq(result1, wheel.bottom))
            }

            val result2 = wheel.mul(wheel.inf, wheel.inv(wheel.inf))
            withClue("$infinity $mulSym inv($infinity) should be $bottom, got: ${pr(result2)}") {
                check(eq(result2, wheel.bottom))
            }

            val result3 = wheel.mul(wheel.zero, wheel.inf)
            withClue("$zero $mulSym $infinity should be $bottom, got: ${pr(result3)}") {
                check(eq(result3, wheel.bottom))
            }

            val result4 = wheel.mul(wheel.inf, wheel.inf)
            withClue("$infinity $mulSym $infinity should be $infinity, got: ${pr(result4)}") {
                check(eq(result4, wheel.inf))
            }
        }
    }

    private val structureLaws: List<TestingLaw> = listOf(
        bottomDominatesAdd,
        bottomDominatesMul,
        specialInvLaws,
        specialInfLaws
    )

    override fun laws(): List<TestingLaw> =
        wheelLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        wheelLaws.fullLaws() + structureLaws
}
