package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll

/*
 * Laws of integer powers `pow(x, n) = xⁿ` with respect to a multiplication `⋆`: together they say that
 * `n ↦ xⁿ` is a monoid homomorphism from (ℕ, +) (or a group homomorphism from (ℤ, +) on invertible x).
 *
 * Exponents are drawn inside each law from a range given by plain numbers, so the range is part of the binding.
 */

private fun <A : Any> Arb<A>.without(exclude: A?, eq: Eq<A>): Arb<A> =
    exclude?.let { z -> filterNot { eq(it, z) } } ?: this

/** `x⁰ = 1`. */
class ZerothPowerLaw<A : Any>(
    private val pow: (A, Int) -> A,
    private val one: A,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    override val binding = LawBinding.of(this, pow, one, arb, eq)

    override val name = "zeroth power"

    override suspend fun test() {
        lawCheckAll(arb, render = { "x = ${pr(it)}" }) { x ->
            val p = pow(x, 0)
            lawCheck(eq(p, one)) { "${pr(x)}^0 = ${pr(p)}, expected ${pr(one)}" }
        }
    }
}

/** `x¹ = x`. */
class FirstPowerLaw<A : Any>(
    private val pow: (A, Int) -> A,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    override val binding = LawBinding.of(this, pow, arb, eq)

    override val name = "first power"

    override suspend fun test() {
        lawCheckAll(arb, render = { "x = ${pr(it)}" }) { x ->
            val p = pow(x, 1)
            lawCheck(eq(p, x)) { "${pr(x)}^1 = ${pr(p)}, expected ${pr(x)}" }
        }
    }
}

/** `xⁿ⁺¹ = xⁿ ⋆ x` for `0 ≤ n < maxExponent`: powers are repeated products. */
class PowerSuccessorLaw<A : Any>(
    private val pow: (A, Int) -> A,
    private val mul: BinOp<A>,
    private val arb: Arb<A>,
    private val maxExponent: Int,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {
    init { require(maxExponent >= 1) { "maxExponent must be at least 1, was $maxExponent." } }

    override val binding = LawBinding.of(this, pow, mul, arb, maxExponent, eq)

    override val name = "power successor (${mul.symbol})"

    override suspend fun test() {
        lawCheckAll(arb, Arb.int(0 until maxExponent), render = { x, n -> "x = ${pr(x)}, n = $n" }) { x, n ->
            val left = pow(x, n + 1)
            val right = mul(pow(x, n), x)
            lawCheck(eq(left, right)) {
                "${pr(x)}^${n + 1} = ${pr(left)}, but ${pr(x)}^$n ${mul.symbol} ${pr(x)} = ${pr(right)}"
            }
        }
    }
}

/**
 * `xᵐ⁺ⁿ = xᵐ ⋆ xⁿ` for `m, n` in `minExponent..maxExponent`, over the generated `x` other than [exclude].
 *
 * With `minExponent = 0` this is the law for a monoid; with negative exponents it needs `x` invertible, which is
 * what [exclude] (e.g. zero in a field) is for.
 */
class PowerAdditionLaw<A : Any>(
    private val pow: (A, Int) -> A,
    private val mul: BinOp<A>,
    private val arb: Arb<A>,
    private val minExponent: Int,
    private val maxExponent: Int,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
    private val exclude: A? = null
) : TestingLaw {
    init { require(minExponent <= maxExponent) { "Empty exponent range $minExponent..$maxExponent." } }

    override val binding = LawBinding.of(this, pow, mul, arb, minExponent, maxExponent, eq, exclude)

    override val name = "power addition (${mul.symbol}, exponents $minExponent..$maxExponent)"

    override suspend fun test() {
        val exponents = Arb.int(minExponent..maxExponent)
        lawCheckAll(
            arb.without(exclude, eq), exponents, exponents,
            render = { x, m, n -> "x = ${pr(x)}, m = $m, n = $n" }
        ) { x, m, n ->
            val left = pow(x, m + n)
            val right = mul(pow(x, m), pow(x, n))
            lawCheck(eq(left, right)) {
                "${pr(x)}^${m + n} = ${pr(left)}, but ${pr(x)}^$m ${mul.symbol} ${pr(x)}^$n = ${pr(right)}"
            }
        }
    }
}

/**
 * `x⁻ⁿ = (x⁻¹)ⁿ` for `1 ≤ n ≤ maxExponent`, over the generated `x` other than [exclude].
 *
 * Elements whose inverse is missing are left to [InverseExistenceLaw].
 */
class NegativePowerLaw<A : Any>(
    private val pow: (A, Int) -> A,
    private val inverse: UnaryOp<A, out A?>,
    private val arb: Arb<A>,
    private val maxExponent: Int,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
    private val exclude: A? = null
) : TestingLaw {
    init { require(maxExponent >= 1) { "maxExponent must be at least 1, was $maxExponent." } }

    override val binding = LawBinding.of(this, pow, inverse, arb, maxExponent, eq, exclude)

    override val name = "negative power (${inverse.symbol})"

    override suspend fun test() {
        lawCheckAll(arb.without(exclude, eq), Arb.int(1..maxExponent), render = { x, n -> "x = ${pr(x)}, n = $n" }) { x, n ->
            val inv = inverse(x) ?: return@lawCheckAll
            val left = pow(x, -n)
            val right = pow(inv, n)
            lawCheck(eq(left, right)) {
                "${pr(x)}^-$n = ${pr(left)}, but (${inverse.symbol}(${pr(x)}))^$n = ${pr(right)}"
            }
        }
    }
}
