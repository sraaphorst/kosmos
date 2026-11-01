package org.vorpal.kosmos.laws.algebra

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.Meadow
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.AntiHomomorphismLaw
import org.vorpal.kosmos.laws.property.EndomorphismCommutationLaw
import org.vorpal.kosmos.laws.property.InvolutionLaw
import org.vorpal.kosmos.laws.property.RestrictedInverseLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Meadow] laws.
 *
 * The axioms, checked by [laws]:
 * - [CommutativeRingLaws]
 * - [InvolutionLaw] on inv (reflection: `inv(inv(x)) = x`)
 * - [RestrictedInverseLaw] on mul and inv (`x * (x * inv(x)) = x`)
 *
 * [fullLaws] additionally checks identities derivable from the axioms. They should never fail on their own,
 * so a failure there that is not mirrored by an axiom failure points to a bug in the laws or the arbitrary:
 * - `inv(0) = 0` and `inv(1) = 1`
 * - multiplicativity: `inv(x * y) = inv(x) * inv(y)` (via [AntiHomomorphismLaw], as mul is commutative)
 * - `inv(-x) = -inv(x)` (via [EndomorphismCommutationLaw])
 * - `x * inv(x)` is idempotent
 */
class MeadowLaws<A : Any>(
    private val meadow: Meadow<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
): LawSuite {
    override val name = suiteName("Meadow",
        meadow.add.op.symbol, meadow.mul.op.symbol, meadow.inv.symbol
    )

    private val commutativeRingLaws = CommutativeRingLaws(meadow, arb, eq, pr)

    private val structureLaws: List<TestingLaw> = listOf(
        InvolutionLaw(meadow.inv, arb, eq, pr),
        RestrictedInverseLaw(meadow.mul.op, meadow.inv, arb, eq, pr)
    )

    private fun fixedPointLaw(label: String, x: A): TestingLaw =
        TestingLaw.named("inv fixes $label") {
            val invX = meadow.inv(x)
            withClue("Expected inv(${pr(x)}) = ${pr(x)}, but got ${pr(invX)}") {
                check(eq(invX, x))
            }
        }

    private val idempotentLaw: TestingLaw =
        TestingLaw.named("x * inv(x) is idempotent") {
            checkAll(arb) { x ->
                val e = meadow.mul(x, meadow.inv(x))
                val ee = meadow.mul(e, e)
                withClue("For x = ${pr(x)}: e = x * inv(x) = ${pr(e)}, but e * e = ${pr(ee)}") {
                    check(eq(ee, e))
                }
            }
        }

    private val derivedLaws: List<TestingLaw> = listOf(
        fixedPointLaw("zero", meadow.zero),
        fixedPointLaw("one", meadow.one),
        AntiHomomorphismLaw(meadow.inv, meadow.mul.op, meadow.mul.op, arb, eq, pr, pr),
        EndomorphismCommutationLaw(meadow.inv, meadow.add.inverse, arb, eq = eq, pr = pr),
        idempotentLaw
    )

    override fun laws(): List<TestingLaw> =
        commutativeRingLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        commutativeRingLaws.fullLaws() + structureLaws + derivedLaws
}
