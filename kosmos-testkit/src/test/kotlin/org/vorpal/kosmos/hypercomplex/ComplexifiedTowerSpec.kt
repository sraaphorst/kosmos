package org.vorpal.kosmos.hypercomplex

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.numberfields.quadratic.GaussianRat
import org.vorpal.kosmos.numberfields.quadratic.GaussianRatAlgebras

/**
 * The biquaternion and bioctonion constructions, checked *exactly*.
 *
 * `BiquaternionAlgebras` and `BioctonionAlgebras` build `ℍ ⊗ ℂ` and `𝕆 ⊗ ℂ` over floating-point
 * complex numbers, so their specs can only use approximate equality. The construction itself is
 * generic in the coefficient field, so here the *same* two steps are run over the exact field
 * `ℚ(i)`:
 * ```text
 * K = ℚ(i),   K[i'] = CD_commutative⟨K⟩,   ℍ ⊗ K = CD_associative⟨K[i']⟩,   𝕆 ⊗ K = CD⟨ℍ ⊗ K⟩
 * ```
 * and associativity, alternativity, the Moufang identities and multiplicativity of the `K`-valued
 * norm are verified with strict equality.
 */
class ComplexifiedTowerSpec : StringSpec({
    val k = GaussianRatAlgebras.GaussianRatField

    val complexLike = CayleyDickson.commutative(k, k.one)
    val biquaternions = CayleyDickson.associative(complexLike)
    val bioctonions = CayleyDickson.usual(biquaternions)

    val smallRational: Arb<Rational> =
        Arb.pair(Arb.int(-6..6), Arb.int(1..4)).map { (p, d) -> Rational.of(p, d) }
    val arbK: Arb<GaussianRat> =
        Arb.pair(smallRational, smallRational).map { (re, im) -> GaussianRat(re, im) }

    fun <A : Any> doubled(arb: Arb<A>): Arb<CD<A>> =
        Arb.pair(arb, arb).map { (a, b) -> CD(a, b) }

    val arbQ = doubled(doubled(arbK))
    val arbO = doubled(arbQ)

    fun plus(a: GaussianRat, b: GaussianRat): GaussianRat = k.add(a, b)
    fun square(a: GaussianRat): GaussianRat = k.mul(a, a)

    fun coordinatesQ(q: CD<CD<GaussianRat>>): List<GaussianRat> =
        listOf(q.a.a, q.a.b, q.b.a, q.b.b)

    fun normQ(q: CD<CD<GaussianRat>>): GaussianRat =
        coordinatesQ(q).map(::square).reduce(::plus)

    fun normO(o: CD<CD<CD<GaussianRat>>>): GaussianRat =
        plus(normQ(o.a), normQ(o.b))

    val h = CD(CD(GaussianRat.I, GaussianRat.ZERO), CD(GaussianRat.ZERO, GaussianRat.ZERO))
    val i = CD(CD(GaussianRat.ZERO, GaussianRat.ONE), CD(GaussianRat.ZERO, GaussianRat.ZERO))
    val j = CD(CD(GaussianRat.ZERO, GaussianRat.ZERO), CD(GaussianRat.ONE, GaussianRat.ZERO))

    "ℍ ⊗ ℚ(i) is exactly an associative involutive ring" {
        InvolutiveRingLaws(biquaternions, arbQ).fullTest().throwIfFailed()
    }

    "ℍ ⊗ ℚ(i) is noncommutative with a central scalar h, h² = -1" {
        biquaternions.mul(i, j) shouldBe biquaternions.add.inverse(biquaternions.mul(j, i))
        biquaternions.mul(h, h) shouldBe biquaternions.negOne
        checkAll(arbQ) { q ->
            biquaternions.mul(h, q) shouldBe biquaternions.mul(q, h)
        }
    }

    "the biquaternion norm w² + x² + y² + z² ∈ ℚ(i) is q·conj(q), and is exactly multiplicative" {
        checkAll(arbQ, arbQ) { p, q ->
            biquaternions.mul(p, biquaternions.conj(p)) shouldBe
                CD(CD(normQ(p), GaussianRat.ZERO), CD(GaussianRat.ZERO, GaussianRat.ZERO))
            normQ(biquaternions.mul(p, q)) shouldBe k.mul(normQ(p), normQ(q))
        }
    }

    "ℍ ⊗ ℚ(i) has zero divisors: (1 + h·i)(1 - h·i) = 0" {
        val hi = biquaternions.mul(h, i)
        val plusHi = biquaternions.add(biquaternions.one, hi)
        val minusHi = biquaternions.add(biquaternions.one, biquaternions.add.inverse(hi))
        biquaternions.mul(plusHi, minusHi) shouldBe biquaternions.zero
        normQ(plusHi) shouldBe GaussianRat.ZERO
    }

    "𝕆 ⊗ ℚ(i) is exactly a nonassociative involutive ring" {
        NonAssociativeInvolutiveRingLaws(bioctonions, arbO).fullTest().throwIfFailed()
    }

    "𝕆 ⊗ ℚ(i) is exactly alternative and satisfies the Moufang identities" {
        AlternativityLaw(bioctonions.mul.op, arbO).test()
        MoufangLaw(bioctonions.mul.op, arbO).test()
    }

    "𝕆 ⊗ ℚ(i) is not associative" {
        val basis = CayleyDickson.doubledBasis(
            biquaternions.zero,
            CayleyDickson.doubledBasis(complexLike.zero, CayleyDickson.doubledBasis(k.zero, listOf(k.one)))
        )
        val (e1, e2, e4) = listOf(1, 2, 4).map(basis::get)
        val left = bioctonions.mul(bioctonions.mul(e1, e2), e4)
        val right = bioctonions.mul(e1, bioctonions.mul(e2, e4))
        (left == right) shouldBe false
        left shouldBe bioctonions.add.inverse(right)
    }

    "the bioctonion norm c₀² + … + c₇² ∈ ℚ(i) is exactly multiplicative (a composition algebra)" {
        checkAll(arbO, arbO) { p, q ->
            normO(bioctonions.mul(p, q)) shouldBe k.mul(normO(p), normO(q))
        }
    }
})
