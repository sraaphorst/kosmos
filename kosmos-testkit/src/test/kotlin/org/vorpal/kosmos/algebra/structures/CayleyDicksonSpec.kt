package org.vorpal.kosmos.algebra.structures

import io.kotest.core.spec.style.FunSpec
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
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import org.vorpal.kosmos.testing.laws.throwsA

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
 *
 * The split doublings (`σ = -1`) and the norm formula `N(a, b) = N(a) + σ·N(b)` are checked alongside.
 */
class CayleyDicksonSpec : FunSpec({

    val q = RationalAlgebras.RationalStarField

    /** Rationals `p/q` with `|p| ≤ 12`, `1 ≤ q ≤ 12`: small, so the deep levels stay fast. */
    val smallRational: Arb<Rational> =
        Arb.pair(
            Arb.int(-12..12),
            Arb.int(1..12)
        ).map { (p, d) -> Rational.of(p, d) }

    fun <A : Any> doubled(arb: Arb<A>): Arb<CD<A>> =
        Arb.pair(arb, arb).map { (a, b) -> CD(a, b) }

    val eqQ: Eq<Rational> = RationalAlgebras.eqRational
    val prAny: Printable<Any> = Printable.default()

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

    /** The basis of the third level, e₀ … e₇. */
    val basis3 = CayleyDickson.doubledBasis(
        a2.zero,
        CayleyDickson.doubledBasis(a1.zero, CayleyDickson.doubledBasis(Rational.ZERO, listOf(Rational.ONE)))
    )

    /** The complex-like level `ℚ[i]/(i² + 1)` as a commutative involutive ring. */
    val complexLike = CayleyDickson.commutative(RationalAlgebras.RationalField, Rational.ONE)

    /** `CayleyDickson.associative` of the complex-like level, built from a1's parts (the quaternion step). */
    val quaternionLike = CayleyDickson.associative(
        CommutativeInvolutiveRing.of(
            ring = CommutativeRing.of(a1.add, CommutativeMonoid.of(a1.mul.identity, a1.mul.op)),
            conj = a1.conj
        )
    )

    lawTests(lawGraph("Cayley-Dickson") {

        // ── The commutative quadratic extensions ℚ[x]/(x² + σ) ───────────────────
        listOf(Rational.ONE, -Rational.ONE, Rational.ZERO, Rational.of(2), Rational.of(-3, 2)).forEach { sigma ->
            val ring = CayleyDickson.commutative(RationalAlgebras.RationalField, sigma)
            val eq = CD.eq(eqQ)
            branch("commutative(ℚ, σ = $sigma)") {
                suite(CommutativeRingLaws(ring, arb1, eq, prAny))
                suite(InvolutiveRingLaws(ring, arb1, eq, prAny))
                local("x = (0, 1) satisfies x² = -σ") {
                    val x = CD(Rational.ZERO, Rational.ONE)
                    lawCheck(eq(ring.mul(x, x), CD(-sigma, Rational.ZERO))) { "x² = ${ring.mul(x, x)}" }
                }
            }
        }

        // ── The generic tower over ℚ ─────────────────────────────────────────────
        branch("the usual tower") {
            // Every level: conjugation is an involutive anti-automorphism.
            branch("A₁") { suite(NonAssociativeInvolutiveRingLaws(a1, arb1, eq1, prAny)) }
            branch("A₂") { suite(NonAssociativeInvolutiveRingLaws(a2, arb2, eq2, prAny)) }
            branch("A₃ (octonion-like)") {
                suite(NonAssociativeInvolutiveRingLaws(a3, arb3, eq3, prAny))
                suite(AlternativityLaw(a3.mul.op, arb3, eq3, prAny))
                local("not associative: (e₁e₂)e₄ ≠ e₁(e₂e₄)") {
                    val (e1, e2, e4) = listOf(1, 2, 4).map(basis3::get)
                    lawCheck(!eq3(a3.mul(a3.mul(e1, e2), e4), a3.mul(e1, a3.mul(e2, e4)))) { "(e₁e₂)e₄ = e₁(e₂e₄)" }
                }
            }
            branch("A₄ (sedenion-like)") {
                suite(NonAssociativeInvolutiveRingLaws(a4, arb4, eq4, prAny))
                local("not alternative, and has zero divisors") {
                    val basis4 = CayleyDickson.doubledBasis(a3.zero, basis3)
                    lawCheck(basis4.size == 16) { "basis size ${basis4.size}" }

                    // Elements e_a ± e_b for a < b among the imaginary units.
                    val pairSums = (1 until 16).flatMap { a ->
                        (a + 1 until 16).flatMap { b ->
                            listOf(
                                a4.add(basis4[a], basis4[b]),
                                a4.add(basis4[a], a4.add.inverse(basis4[b]))
                            )
                        }
                    }
                    val pairs = pairSums.asSequence().flatMap { x -> pairSums.asSequence().map { y -> x to y } }

                    lawCheck(pairs.any { (x, y) -> eq4(a4.mul(x, y), a4.zero) }) {
                        "Expected a pair of nonzero sedenion-like elements with product 0."
                    }
                    lawCheck(pairs.any { (x, y) -> !eq4(a4.mul(x, a4.mul(x, y)), a4.mul(a4.mul(x, x), y)) }) {
                        "Expected a failure of left alternativity at the fourth level."
                    }
                }
            }
        }

        branch("CayleyDickson.associative") {
            suite(InvolutiveRingLaws(quaternionLike, arb2, eq2, prAny))
            local("not commutative: ij = -ji ≠ ji") {
                val i = CD(CD(Rational.ZERO, Rational.ONE), CD(Rational.ZERO, Rational.ZERO))
                val j = CD(CD(Rational.ZERO, Rational.ZERO), CD(Rational.ONE, Rational.ZERO))
                lawCheck(eq2(quaternionLike.mul(i, j), quaternionLike.add.inverse(quaternionLike.mul(j, i)))) { "ij ≠ -ji" }
                lawCheck(!eq2(quaternionLike.mul(i, j), quaternionLike.mul(j, i))) { "ij = ji" }
            }
            local("rejects a noncommutative base") {
                lawCheck(throwsA<IllegalArgumentException> { CayleyDickson.associative(quaternionLike) }) { "did not throw" }
            }
        }

        // ── The split doublings (σ = -1) ─────────────────────────────────────────
        branch("split doublings") {
            local("CayleyDickson.split: the new unit ℓ = (0, 1) squares to +1, and (1 + ℓ)(1 - ℓ) = 0") {
                val splitComplexLike = CayleyDickson.split(q)
                val ell1 = CD(Rational.ZERO, Rational.ONE)
                lawCheck(eq1(splitComplexLike.mul(ell1, ell1), splitComplexLike.one)) { "ℓ² ≠ 1" }
                lawCheck(eq1(
                    splitComplexLike.mul(
                        splitComplexLike.add(splitComplexLike.one, ell1),
                        splitComplexLike.add(splitComplexLike.one, splitComplexLike.add.inverse(ell1))
                    ),
                    splitComplexLike.zero
                )) { "(1 + ℓ)(1 - ℓ) ≠ 0" }

                val splitOctonionLike = CayleyDickson.split(a2)
                val ell3 = CD(a2.zero, a2.one)
                lawCheck(eq3(splitOctonionLike.mul(ell3, ell3), splitOctonionLike.one)) { "ℓ² ≠ 1 at the third level" }
            }

            branch("CayleyDickson.associativeSplit") {
                val splitQuaternionLike = CayleyDickson.associativeSplit(complexLike)
                suite(InvolutiveRingLaws(splitQuaternionLike, arb2, eq2, prAny))
                local("i² = -1, j² = k² = +1, ji = -k") {
                    val i = CD(CD(Rational.ZERO, Rational.ONE), CD(Rational.ZERO, Rational.ZERO))
                    val j = CD(CD(Rational.ZERO, Rational.ZERO), CD(Rational.ONE, Rational.ZERO))
                    val k = splitQuaternionLike.mul(i, j)
                    lawCheck(eq2(splitQuaternionLike.mul(i, i), splitQuaternionLike.negOne)) { "i² ≠ -1" }
                    lawCheck(eq2(splitQuaternionLike.mul(j, j), splitQuaternionLike.one)) { "j² ≠ 1" }
                    lawCheck(eq2(splitQuaternionLike.mul(k, k), splitQuaternionLike.one)) { "k² ≠ 1" }
                    lawCheck(eq2(splitQuaternionLike.mul(j, i), splitQuaternionLike.add.inverse(k))) { "ji ≠ -k" }
                }
                local("rejects a noncommutative base") {
                    lawCheck(throwsA<IllegalArgumentException> {
                        CayleyDickson.associativeSplit(CayleyDickson.associative(complexLike))
                    }) { "did not throw" }
                }
            }

            branch("split doubling of the quaternion level") {
                val splitOctonionLike = CayleyDickson.split(CayleyDickson.associative(complexLike))
                suite(NonAssociativeInvolutiveRingLaws(splitOctonionLike, arb3, eq3, prAny))
                suite(AlternativityLaw(splitOctonionLike.mul.op, arb3, eq3, prAny))
                local("not associative, and has zero divisors") {
                    val (e1, e2, e4) = listOf(1, 2, 4).map(basis3::get)
                    lawCheck(!eq3(
                        splitOctonionLike.mul(splitOctonionLike.mul(e1, e2), e4),
                        splitOctonionLike.mul(e1, splitOctonionLike.mul(e2, e4))
                    )) { "(e₁e₂)e₄ = e₁(e₂e₄)" }

                    val plus = splitOctonionLike.add(splitOctonionLike.one, e4)
                    val minus = splitOctonionLike.add(splitOctonionLike.one, splitOctonionLike.add.inverse(e4))
                    lawCheck(eq3(splitOctonionLike.mul(plus, minus), splitOctonionLike.zero)) { "(1 + e₄)(1 - e₄) ≠ 0" }
                }
            }
        }

        local("the norm of a double is N(a, b) = N(a) + σ·N(b)") {
            // Over ℚ with the trivial involution N(a) = a², so N(a, b) = a² + σb².
            listOf(Rational.ONE, -Rational.ONE, Rational.ZERO, Rational.of(5)).forEach { sigma ->
                val ring = CayleyDickson.withSigma(q, sigma)
                lawCheckAll(arb1) { x ->
                    val expected = CD(x.a * x.a + sigma * x.b * x.b, Rational.ZERO)
                    lawCheck(eq1(ring.mul(x, ring.conj(x)), expected)) { "σ = $sigma: x x̄ for $x" }
                    lawCheck(eq1(ring.mul(ring.conj(x), x), expected)) { "σ = $sigma: x̄ x for $x" }
                }
            }
        }

        // ── General σ: generalized quaternion algebras (a, b)_ℚ ──────────────────
        branch("(2, 3)_ℚ from σ₁ = -2, σ₂ = -3") {
            val first = CayleyDickson.withSigma(q, Rational.of(-2))
            val second = CayleyDickson.withSigma(first, CD(Rational.of(-3), Rational.ZERO))
            val eq = CD.eq(CD.eq(eqQ))

            suite(NonAssociativeInvolutiveRingLaws(second, arb2, eq, prAny))
            law(AssociativityLaw(second.mul.op, arb2, eq, prAny))
            local("i² = 2, j² = 3, ij = -ji") {
                val i = CD(CD(Rational.ZERO, Rational.ONE), first.zero)
                val j = CD(first.zero, first.one)
                lawCheck(eq(second.mul(i, i), second.fromBigInt(2.toBigInteger()))) { "i² ≠ 2" }
                lawCheck(eq(second.mul(j, j), second.fromBigInt(3.toBigInteger()))) { "j² ≠ 3" }
                lawCheck(eq(second.mul(i, j), second.add.inverse(second.mul(j, i)))) { "ij ≠ -ji" }
            }
        }

        // ── Bases and the canonical embedding ────────────────────────────────────
        local("doubledBasis doubles the size and places the base basis in the first slot, then the second") {
            val basis1 = CayleyDickson.doubledBasis(Rational.ZERO, listOf(Rational.ONE))
            lawCheck(basis1 == listOf(CD(Rational.ONE, Rational.ZERO), CD(Rational.ZERO, Rational.ONE))) { "$basis1" }

            val basis2 = CayleyDickson.doubledBasis(a1.zero, basis1)
            lawCheck(basis2.size == 4) { "size ${basis2.size}" }
            lawCheck(basis2.take(2) == basis1.map { e -> CD(e, a1.zero) }) { "first half" }
            lawCheck(basis2.drop(2) == basis1.map { e -> CD(a1.zero, e) }) { "second half" }
        }

        branch("canonicalEmbedding a ↦ (a, 0), A₂ → A₃") {
            suite(NonAssociativeRingHomomorphismLaws(
                hom = a3.canonicalEmbedding()::invoke,
                domain = a2,
                codomain = a3,
                arb = arb2,
                eqB = eq3,
                prA = prAny,
                prB = prAny
            ))
        }
    })
})
