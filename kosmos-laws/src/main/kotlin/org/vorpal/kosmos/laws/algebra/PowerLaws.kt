package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.property.FirstPowerLaw
import org.vorpal.kosmos.laws.property.InverseExistenceLaw
import org.vorpal.kosmos.laws.property.NegativePowerLaw
import org.vorpal.kosmos.laws.property.PowerAdditionLaw
import org.vorpal.kosmos.laws.property.PowerSuccessorLaw
import org.vorpal.kosmos.laws.property.ZerothPowerLaw

/**
 * Natural powers `pow(x, n) = xⁿ` (`n ≥ 0`) in a monoid: `n ↦ xⁿ` is a monoid homomorphism from (ℕ, +).
 *
 * - [ZerothPowerLaw]: `x⁰ = 1`
 * - [FirstPowerLaw]: `x¹ = x`
 * - [PowerSuccessorLaw]: `xⁿ⁺¹ = xⁿ ⋆ x`
 * - [PowerAdditionLaw]: `xᵐ⁺ⁿ = xᵐ ⋆ xⁿ` for `0 ≤ m, n ≤ maxExponent`
 *
 * The monoid laws themselves are not included: register the structure's own suite as well.
 */
class NaturalPowerLaws<A : Any>(
    private val monoid: Monoid<A>,
    private val pow: (A, Int) -> A,
    private val arb: Arb<A>,
    private val maxExponent: Int = 10,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = "NaturalPowers (${monoid.op.symbol})"

    override val binding = LawBinding.of(this, monoid, pow, arb, maxExponent, eq, pr)

    override fun children(): List<LawNode> = listOf(
        ZerothPowerLaw(pow, monoid.identity, arb, eq, pr),
        FirstPowerLaw(pow, arb, eq, pr),
        PowerSuccessorLaw(pow, monoid.op, arb, maxExponent, eq, pr),
        PowerAdditionLaw(pow, monoid.op, arb, 0, maxExponent, eq, pr)
    ).asNodes()
}

/**
 * Integer powers `pow(x, n) = xⁿ` (`n ∈ ℤ`) in a monoid whose elements other than [exclude] are invertible (e.g. the
 * multiplicative monoid of a field, with [exclude] = 0): on them, `n ↦ xⁿ` is a group homomorphism from (ℤ, +).
 *
 * - [NaturalPowerLaws] (on every element, [exclude] included)
 * - [InverseExistenceLaw]: every element other than [exclude] has an inverse (the negative-power laws skip
 *   elements without one, so this is what makes a missing inverse fail)
 * - [NegativePowerLaw]: `x⁻ⁿ = (x⁻¹)ⁿ`
 * - [PowerAdditionLaw]: `xᵐ⁺ⁿ = xᵐ ⋆ xⁿ` for `-maxExponent ≤ m, n ≤ maxExponent`
 */
class IntegerPowerLaws<A : Any>(
    private val monoid: Monoid<A>,
    private val inverse: UnaryOp<A, out A?>,
    private val pow: (A, Int) -> A,
    private val arb: Arb<A>,
    private val exclude: A? = null,
    private val maxExponent: Int = 10,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : LawSuite {
    override val name = "IntegerPowers (${monoid.op.symbol}, ${inverse.symbol})"

    override val binding = LawBinding.of(this, monoid, inverse, pow, arb, exclude, maxExponent, eq, pr)

    override fun children(): List<LawNode> = listOf(
        NaturalPowerLaws(monoid, pow, arb, maxExponent, eq, pr),
        InverseExistenceLaw(monoid.op, monoid.identity, arb, inverse, eq, pr, exclude),
        NegativePowerLaw(pow, inverse, arb, maxExponent, eq, pr, exclude),
        PowerAdditionLaw(pow, monoid.op, arb, -maxExponent, maxExponent, eq, pr, exclude)
    ).asNodes()
}
