package org.vorpal.kosmos.algebra.structures

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.AssociativityLaw

/**
 * Direct tests of the Cayley-Dickson kernel, independent of the named hypercomplex algebras.
 *
 * Everything here is over ℚ with the trivial involution, so every law is checked exactly. The tower
 * is built generically:
 * ```text
 * A₀ = ℚ,   A₁ = CD⟨A₀⟩,   A₂ = CD⟨A₁⟩,   A₃ = CD⟨A₂⟩,   A₄ = CD⟨A₃⟩
 * ```
 * and each level is checked for exactly the properties the doubling is known to preserve:
 * - every level: the conjugation is an involutive anti-automorphism;
 * - A₁ (commutative doubling): commutative and associative;
 * - A₂ ([CayleyDickson.associative]): associative but not commutative;
 * - A₃ (octonion-like): alternative but not associative;
 * - A₄ (sedenion-like): not alternative, and has zero divisors.
 */
class CayleyDicksonSpec : StringSpec({

    val q = RationalAlgebras.RationalStarField

    /** Rationals `p/q` with `|p| ≤ 12`, `1 ≤ q ≤ 12`: small, so the deep levels stay fast. */
    val smallRational: Arb<Rational> =
        Arb.pair(Arb.int(-12..12), Arb.int(1..12)).map { (p, d) -> Rational.of(p, d) }

    fun <A : Any> doubled(arb: Arb<A>): Arb<CD<A>> =
        Arb.pair(arb, arb).map { (a, b) -> CD(a, b) }

    val eqQ: Eq<Rational> = RationalAlgebras.eqRational
    val prAny: Printable<Any> = Printable.default()

    // ── The commutative quadratic extensions ℚ[x]/(x² + σ) ───────────────────

    listOf(Rational.ONE, -Rational.ONE, Rational.ZERO, Rational.of(2), Rational.of(-3, 2)).forEach { sigma ->
        val ring = CayleyDickson.commutative(RationalAlgebras.RationalField, sigma)
        val arb = doubled(smallRational)
        val eq = CD.eq(eqQ)

        "commutative(ℚ, σ = $sigma) satisfies CommutativeRingLaws and InvolutiveRingLaws" {
            CommutativeRingLaws(ring, arb, eq, prAny).fullTest().throwIfFailed()
            InvolutiveRingLaws(ring, arb, eq, prAny).fullTest().throwIfFailed()
        }

        "commutative(ℚ, σ = $sigma): x = (0, 1) satisfies x² = -σ" {
            val x = CD(Rational.ZERO, Rational.ONE)
            check(eq(ring.mul(x, x), CD(-sigma, Rational.ZERO)))
        }
    }

    // ── The generic tower over ℚ ─────────────────────────────────────────────

    val a1 = CayleyDickson.usual(q)
    val a2 = CayleyDickson.usual(a1)
    val a3 = CayleyDickson.usual(a2)
    val a4 = CayleyDickson.usual(a3)

    val arb1 = doubled(smallRational)
    val arb2 = doubled(arb1)
    val arb3 = doubled(arb2)
    val arb4 = doubled(arb3)

    val eq1 = CD.eq(eqQ)
    val eq2 = CD.eq(eq1)
    val eq3 = CD.eq(eq2)
    val eq4 = CD.eq(eq3)

    "every level of the tower is a NonAssociativeInvolutiveRing (conjugation is an involutive anti-automorphism)" {
        NonAssociativeInvolutiveRingLaws(a1, arb1, eq1, prAny).fullTest().throwIfFailed()
        NonAssociativeInvolutiveRingLaws(a2, arb2, eq2, prAny).fullTest().throwIfFailed()
        NonAssociativeInvolutiveRingLaws(a3, arb3, eq3, prAny).fullTest().throwIfFailed()
        NonAssociativeInvolutiveRingLaws(a4, arb4, eq4, prAny).fullTest().throwIfFailed()
    }

    "CayleyDickson.associative doubles a commutative base to an associative ring (the quaternion step)" {
        val assoc = CayleyDickson.associative(
            CommutativeInvolutiveRing.of(
                ring = CommutativeRing.of(a1.add, CommutativeMonoid.of(a1.mul.identity, a1.mul.op)),
                conj = a1.conj
            )
        )
        InvolutiveRingLaws(assoc, arb2, eq2, prAny).fullTest().throwIfFailed()

        // ... and it is not commutative: ij = -ji.
        val i = CD(CD(Rational.ZERO, Rational.ONE), CD(Rational.ZERO, Rational.ZERO))
        val j = CD(CD(Rational.ZERO, Rational.ZERO), CD(Rational.ONE, Rational.ZERO))
        check(eq2(assoc.mul(i, j), assoc.add.inverse(assoc.mul(j, i))))
        check(!eq2(assoc.mul(i, j), assoc.mul(j, i)))
    }

    "CayleyDickson.associative rejects a noncommutative base" {
        val quaternionLike = CayleyDickson.associative(
            CommutativeInvolutiveRing.of(
                ring = CommutativeRing.of(a1.add, CommutativeMonoid.of(a1.mul.identity, a1.mul.op)),
                conj = a1.conj
            )
        )
        shouldThrow<IllegalArgumentException> { CayleyDickson.associative(quaternionLike) }
    }

    "the third level (octonion-like) is alternative but not associative" {
        AlternativityLaw(a3.mul.op, arb3, eq3, prAny).test()

        val basis = CayleyDickson.doubledBasis(a2.zero, CayleyDickson.doubledBasis(a1.zero, CayleyDickson.doubledBasis(Rational.ZERO, listOf(Rational.ONE))))
        val (e1, e2, e4) = listOf(1, 2, 4).map(basis::get)
        check(!eq3(a3.mul(a3.mul(e1, e2), e4), a3.mul(e1, a3.mul(e2, e4))))
    }

    "the fourth level (sedenion-like) is not alternative and has zero divisors" {
        val basis3 = CayleyDickson.doubledBasis(
            a2.zero,
            CayleyDickson.doubledBasis(a1.zero, CayleyDickson.doubledBasis(Rational.ZERO, listOf(Rational.ONE)))
        )
        val basis4 = CayleyDickson.doubledBasis(a3.zero, basis3)
        basis4.size shouldBe 16

        // Elements e_a ± e_b for a < b among the imaginary units.
        val pairSums = (1 until 16).flatMap { a ->
            (a + 1 until 16).flatMap { b ->
                listOf(
                    a4.add(basis4[a], basis4[b]),
                    a4.add(basis4[a], a4.add.inverse(basis4[b]))
                )
            }
        }

        val zeroDivisorWitness = pairSums.asSequence().flatMap { x ->
            pairSums.asSequence().map { y -> x to y }
        }.firstOrNull { (x, y) -> eq4(a4.mul(x, y), a4.zero) }
        check(zeroDivisorWitness != null) { "Expected a pair of nonzero sedenion-like elements with product 0." }

        val alternativityWitness = pairSums.asSequence().flatMap { x ->
            pairSums.asSequence().map { y -> x to y }
        }.firstOrNull { (x, y) -> !eq4(a4.mul(x, a4.mul(x, y)), a4.mul(a4.mul(x, x), y)) }
        check(alternativityWitness != null) { "Expected a failure of left alternativity at the fourth level." }
    }

    // ── General σ: generalized quaternion algebras (a, b)_ℚ ──────────────────

    "two doublings with σ₁ = -2, σ₂ = -3 give the quaternion algebra (2, 3)_ℚ: i² = 2, j² = 3, ij = -ji" {
        val first = CayleyDickson.withSigma(q, Rational.of(-2))
        val second = CayleyDickson.withSigma(first, CD(Rational.of(-3), Rational.ZERO))
        val eq = CD.eq(CD.eq(eqQ))

        val zero1 = first.zero
        val one1 = first.one
        val i = CD(CD(Rational.ZERO, Rational.ONE), zero1)
        val j = CD(zero1, one1)

        check(eq(second.mul(i, i), second.fromBigInt(2.toBigInteger())))
        check(eq(second.mul(j, j), second.fromBigInt(3.toBigInteger())))
        check(eq(second.mul(i, j), second.add.inverse(second.mul(j, i))))

        AssociativityLaw(second.mul.op, arb2, eq, prAny).test()
        NonAssociativeInvolutiveRingLaws(second, arb2, eq, prAny).fullTest().throwIfFailed()
    }

    // ── Bases and the canonical embedding ────────────────────────────────────

    "doubledBasis doubles the size and places the base basis in the first slot, then the second" {
        val basis1 = CayleyDickson.doubledBasis(Rational.ZERO, listOf(Rational.ONE))
        basis1 shouldBe listOf(CD(Rational.ONE, Rational.ZERO), CD(Rational.ZERO, Rational.ONE))

        val basis2 = CayleyDickson.doubledBasis(a1.zero, basis1)
        basis2.size shouldBe 4
        basis2.take(2) shouldBe basis1.map { e -> CD(e, a1.zero) }
        basis2.drop(2) shouldBe basis1.map { e -> CD(a1.zero, e) }
    }

    "the instance canonicalEmbedding a ↦ (a, 0) is an injective ring homomorphism" {
        NonAssociativeRingHomomorphismLaws(
            hom = a3.canonicalEmbedding()::invoke,
            domain = a2,
            codomain = a3,
            arb = arb2,
            eqB = eq3,
            prA = prAny,
            prB = prAny
        ).fullTest().throwIfFailed()
    }
})
