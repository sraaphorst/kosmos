package org.vorpal.kosmos.hypercomplex.sextonion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.FiniteFieldAlgebras
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.splitoctonion.ArbSplitOctonion
import org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonionAlgebras
import org.vorpal.kosmos.hypercomplex.splitoctonion.splitOctonion
import org.vorpal.kosmos.hypercomplex.splitquaternion.ArbSplitQuaternion
import org.vorpal.kosmos.hypercomplex.zorn.ArbZorn
import org.vorpal.kosmos.hypercomplex.zorn.Zorn
import org.vorpal.kosmos.hypercomplex.zorn.ZornAlgebras
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.Vec2
import org.vorpal.kosmos.linear.values.Vec3
import java.math.BigInteger

/**
 * The sextonions inside the Zorn vector-matrices and the split-octonions.
 *
 * `φ : Sextonion<R> → Zorn<R>` is an injective homomorphism over any commutative ring, compatible
 * with conjugation and the norm. The coordinate projection back is R-linear and undoes `φ`, but
 * it is not multiplicative (its typed version waits on R-module homomorphisms, issue #270).
 *
 * Into the Cayley-Dickson split-octonions the embedding needs 2 to be invertible, and that is
 * forced: the idempotent `(E₁₁, 0)` has nowhere to go in `SplitOctonion<ℤ>` or over 𝔽₂.
 */
class SextonionZornSpec : StringSpec({
    val q = RationalAlgebras.RationalField
    val z = IntegerAlgebras.IntegerCommutativeRing
    val sextonions = SextonionAlgebras.sextonionRing(q)
    val zorns = ZornAlgebras.zornRing(q)
    val phi = ZornAlgebras.sextonionToZornMonomorphism(q)
    val arb = ArbSextonion.rationalSextonion

    val (zero, one) = Rational.ZERO to Rational.ONE
    val zeroVec3 = Vec3(zero, zero, zero)

    /** The coordinate projection Zorn<R> → Sextonion<R>, dropping v₃ and w₂. */
    fun <R : Any> project(base: CommutativeRing<R>, x: Zorn<R>): Sextonion<R> {
        val (a, v, w, d) = x
        return Sextonion(Mat2(a, v.x, w.x, d), Vec2(v.y, base.add.inverse(w.z)))
    }

    // ---------- Sextonion → Zorn ----------

    "φ : Sextonion<ℚ> → Zorn<ℚ> is a unital ring homomorphism, and injective" {
        NonAssociativeRingHomomorphismLaws(hom = phi::invoke, domain = sextonions, codomain = zorns, arb = arb)
            .fullTest().throwIfFailed()
        injectivityLaw(hom = phi::invoke, arbA = arb).test()
    }

    "φ is defined over ℤ and over 𝔽₂ too: an injective homomorphism over any commutative ring" {
        val phiZ = ZornAlgebras.sextonionToZornMonomorphism(z)
        NonAssociativeRingHomomorphismLaws(
            hom = phiZ::invoke,
            domain = SextonionAlgebras.sextonionRing(z),
            codomain = ZornAlgebras.zornRing(z),
            arb = ArbSextonion.integerSextonion
        ).fullTest().throwIfFailed()
        injectivityLaw(hom = phiZ::invoke, arbA = ArbSextonion.integerSextonion).test()

        val f2 = FiniteFieldAlgebras.F2
        val phi2 = ZornAlgebras.sextonionToZornMonomorphism(f2)
        NonAssociativeRingHomomorphismLaws(
            hom = phi2::invoke,
            domain = SextonionAlgebras.sextonionRing(f2),
            codomain = ZornAlgebras.zornRing(f2),
            arb = ArbSextonion.sextonionOf(Arb.int(0..1).map(Int::toBigInteger))
        ).fullTest().throwIfFailed()
    }

    "φ([[a, b], [c, d]], (e, f)) = [[a, (b, e, 0)], [(c, 0, −f), d]]" {
        checkAll(arb) { x ->
            val (m, nu) = x
            phi(x) shouldBe Zorn(m.a, Vec3(m.b, nu.x, zero), Vec3(m.c, zero, -nu.y), m.d)
        }
    }

    "φ commutes with conjugation and carries det A to the Zorn norm ad − v·w" {
        val sNorm = SextonionAlgebras.normSq(q)
        val zNorm = ZornAlgebras.normSq(q)
        checkAll(arb) { x ->
            phi(sextonions.conj(x)) shouldBe zorns.conj(phi(x))
            zNorm(phi(x)) shouldBe sNorm(x)
        }
    }

    // ---------- The projection back (hand-written until #270) ----------

    "the projection π : Zorn → Sextonion undoes φ, and is additive and ℚ-linear" {
        checkAll(arb) { x -> project(q, phi(x)) shouldBe x }
        val action = ZornAlgebras.zornLeftAction(q)
        val sAction = SextonionAlgebras.sextonionLeftAction(q)
        checkAll(ArbZorn.rationalZorn, ArbZorn.rationalZorn, ArbSplitQuaternion.smallRational) { x, y, r ->
            project(q, zorns.add(x, y)) shouldBe sextonions.add(project(q, x), project(q, y))
            project(q, action(r, x)) shouldBe sAction(r, project(q, x))
        }
        project(q, zorns.zero) shouldBe sextonions.zero
    }

    "π is not multiplicative: π(U₃) = 0, so π(U₃)π(W₃) = 0, but π(U₃W₃) = (E₁₁, 0)" {
        val e3 = Vec3(zero, zero, one)
        val u3 = Zorn(zero, e3, zeroVec3, zero)
        val w3 = Zorn(zero, zeroVec3, e3, zero)
        project(q, u3) shouldBe sextonions.zero
        sextonions.mul(project(q, u3), project(q, w3)) shouldBe sextonions.zero
        project(q, zorns.mul(u3, w3)) shouldBe SextonionAlgebras.basis(q)[0]
    }

    // ---------- Sextonion → SplitOctonion ----------

    "Sextonion<ℚ> ↪ SplitOctonion<ℚ> is an injective unital homomorphism compatible with conj and norm" {
        val mono = ZornAlgebras.sextonionToSplitOctonionMonomorphism(q)
        val splitOctonions = SplitOctonionAlgebras.splitOctonionRing(q)
        NonAssociativeRingHomomorphismLaws(
            hom = mono::invoke,
            domain = sextonions,
            codomain = splitOctonions,
            arb = arb
        ).fullTest().throwIfFailed()
        injectivityLaw(hom = mono::invoke, arbA = arb).test()
        val sNorm = SextonionAlgebras.normSq(q)
        val oNorm = SplitOctonionAlgebras.normSq(q)
        checkAll(arb) { x ->
            mono(sextonions.conj(x)) shouldBe splitOctonions.conj(mono(x))
            oNorm(mono(x)) shouldBe sNorm(x)
        }
    }

    "the idempotent (E₁₁, 0) lands on (1 + e₄)/2: the half is unavoidable" {
        val mono = ZornAlgebras.sextonionToSplitOctonionMonomorphism(q)
        val h = Rational.of(1, 2)
        mono(SextonionAlgebras.basis(q)[0]) shouldBe splitOctonion(h, zero, zero, zero, h, zero, zero, zero)
    }

    "every split-octonion satisfies x² = 2w·x − N(x), so over ℤ an idempotent has (2w − 1)·v = 0" {
        val ring = SplitOctonionAlgebras.splitOctonionRing(z)
        val normSq = SplitOctonionAlgebras.normSq(z)
        val two = BigInteger.TWO
        checkAll(ArbSplitOctonion.splitOctonionOf(Arb.int(-20..20).map(Int::toBigInteger))) { x ->
            val (scale, scalar) = two * x.w to splitOctonion(
                normSq(x), BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO
            )
            val scaled = SplitOctonionAlgebras.splitOctonionLeftAction(z)(scale, x)
            ring.mul(x, x) shouldBe ring.add(scaled, ring.add.inverse(scalar))
        }
    }

    "over 𝔽₂ the only idempotents of the split-octonions are 0 and 1, so no embedding exists" {
        val f2 = FiniteFieldAlgebras.F2
        val ring = SplitOctonionAlgebras.splitOctonionRing(f2)
        val bits = listOf(BigInteger.ZERO, BigInteger.ONE)
        val all = (0 until 256).map { n ->
            val c = (0 until 8).map { i -> bits[(n shr i) and 1] }
            splitOctonion(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7])
        }
        all.filter { x -> ring.mul(x, x) == x }.toSet() shouldBe setOf(ring.zero, ring.one)

        val sextonionsF2 = SextonionAlgebras.sextonionRing(f2)
        val e11 = SextonionAlgebras.basis(f2)[0]
        sextonionsF2.mul(e11, e11) shouldBe e11
        e11 shouldNotBe sextonionsF2.zero
        e11 shouldNotBe sextonionsF2.one

        shouldThrow<IllegalArgumentException> { ZornAlgebras.sextonionToSplitOctonionMonomorphism(f2) }
    }

    "over 𝔽₃ the embedding into the split-octonions exists and is a homomorphism" {
        val f3 = PrimeField(BigInteger.valueOf(3))
        val mono = ZornAlgebras.sextonionToSplitOctonionMonomorphism(f3)
        NonAssociativeRingHomomorphismLaws(
            hom = mono::invoke,
            domain = SextonionAlgebras.sextonionRing(f3),
            codomain = SplitOctonionAlgebras.splitOctonionRing(f3),
            arb = ArbSextonion.sextonionOf(Arb.int(0..2).map(Int::toBigInteger))
        ).fullTest().throwIfFailed()
    }
})
