package org.vorpal.kosmos.hypercomplex.sextonion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.FiniteFieldAlgebras
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.splitquaternion.ArbSplitQuaternion
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.splitquaternion.splitQuaternion
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.linear.instances.FixedMatAlgebras
import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.Vec2
import org.vorpal.kosmos.linear.values.arbMat2
import org.vorpal.kosmos.linear.values.arbVec2
import java.math.BigInteger

/**
 * The sextonions as a split extension `S(R) = M₂(R) ⋉ J` of the 2 × 2 matrices by the
 * square-zero ideal `J = { (0, ν) } ≅ R²`.
 *
 * - `ι : M₂(R) → S`, `A ↦ (A, 0)`, is an injective unital ring homomorphism over any commutative ring.
 * - `ρ : S → M₂(R)`, `(A, ν) ↦ A`, is a surjective unital ring homomorphism with kernel `J` and
 *   `ρ ∘ ι = id` (a retraction, checked by hand until the retraction law of issue #270 exists).
 * - `M₂(R)` acts on `J` by `(A, 0)(0, ν) = (0, νA)` and `(0, ν)(A, 0) = (0, ν·adj A)`. These are not
 *   associative actions: `J` is the Cayley bimodule, with `x·n = n·x̄`.
 * - Through the split-quaternions: `H′(R) → S` over any commutative ring, injective exactly when
 *   2 is not a zero divisor (so over ℤ and ℚ, but not over 𝔽₂), and `S → H′(F)` when 2 is
 *   invertible in `F`.
 */
class SextonionMat2Spec : StringSpec({
    val q = RationalAlgebras.RationalField
    val z = IntegerAlgebras.IntegerCommutativeRing
    val f2 = FiniteFieldAlgebras.F2
    val f3 = PrimeField(BigInteger.valueOf(3))

    val sextonions = SextonionAlgebras.sextonionRing(q)
    val matrices = FixedMatAlgebras.mat2Ring(q)
    val iota = SextonionAlgebras.mat2ToSextonionMonomorphism(q)
    val rho = SextonionAlgebras.sextonionMat2Retraction(q)

    val arbS = ArbSextonion.rationalSextonion
    val arbM = arbMat2(ArbSplitQuaternion.smallRational)
    val arbV = arbVec2(ArbSplitQuaternion.smallRational)
    val arbF2 = Arb.int(0..1).map(Int::toBigInteger)
    val arbF3 = Arb.int(0..2).map(Int::toBigInteger)

    val (zero, one) = Rational.ZERO to Rational.ONE
    val zeroVec = Vec2(zero, zero)

    /** The element `(0, ν)` of the ideal `J`. */
    fun j(nu: Vec2<Rational>) = Sextonion(matrices.zero, nu)

    // ---------- ι : M₂(R) → S ----------

    "ι : M₂(ℚ) → S(ℚ) is an injective unital ring homomorphism" {
        NonAssociativeRingHomomorphismLaws(hom = iota::invoke, domain = matrices, codomain = sextonions, arb = arbM)
            .fullTest().throwIfFailed()
        injectivityLaw(hom = iota::invoke, arbA = arbM).test()
    }

    "ι is a homomorphism over ℤ and over 𝔽₂ as well" {
        NonAssociativeRingHomomorphismLaws(
            hom = SextonionAlgebras.mat2ToSextonionMonomorphism(z)::invoke,
            domain = FixedMatAlgebras.mat2Ring(z),
            codomain = SextonionAlgebras.sextonionRing(z),
            arb = arbMat2(Arb.int(-20..20).map(Int::toBigInteger))
        ).fullTest().throwIfFailed()

        NonAssociativeRingHomomorphismLaws(
            hom = SextonionAlgebras.mat2ToSextonionMonomorphism(f2)::invoke,
            domain = FixedMatAlgebras.mat2Ring(f2),
            codomain = SextonionAlgebras.sextonionRing(f2),
            arb = arbMat2(arbF2)
        ).fullTest().throwIfFailed()
    }

    "ι(A) = (A, 0)" {
        checkAll(arbM) { a ->
            iota(a) shouldBe Sextonion(a, zeroVec)
        }
    }

    // ---------- ρ : S → M₂(R) ----------

    "ρ : S(ℚ) → M₂(ℚ) is a unital ring homomorphism: the matrix part of a product is the product of the matrix parts" {
        NonAssociativeRingHomomorphismLaws(hom = rho::invoke, domain = sextonions, codomain = matrices, arb = arbS)
            .fullTest().throwIfFailed()
    }

    "ρ is a homomorphism over ℤ and over 𝔽₂ as well" {
        NonAssociativeRingHomomorphismLaws(
            hom = SextonionAlgebras.sextonionMat2Retraction(z)::invoke,
            domain = SextonionAlgebras.sextonionRing(z),
            codomain = FixedMatAlgebras.mat2Ring(z),
            arb = ArbSextonion.integerSextonion
        ).fullTest().throwIfFailed()

        NonAssociativeRingHomomorphismLaws(
            hom = SextonionAlgebras.sextonionMat2Retraction(f2)::invoke,
            domain = SextonionAlgebras.sextonionRing(f2),
            codomain = FixedMatAlgebras.mat2Ring(f2),
            arb = ArbSextonion.sextonionOf(arbF2)
        ).fullTest().throwIfFailed()
    }

    "ρ is a retraction: ρ ∘ ι = id" {
        checkAll(arbM) { a ->
            rho(iota(a)) shouldBe a
        }
    }

    "ker ρ = J, and every sextonion splits as x = ι(ρ(x)) + (0, ν)" {
        checkAll(arbV) { nu ->
            rho(j(nu)) shouldBe matrices.zero
        }
        checkAll(arbS) { x ->
            val (_, nu) = x
            sextonions.add(iota(rho(x)), j(nu)) shouldBe x
            (rho(x) == matrices.zero) shouldBe (x == j(nu))
        }
    }

    "ι ∘ ρ is not the identity: it kills the ideal J" {
        val n1 = j(Vec2(one, zero))
        iota(rho(n1)) shouldBe sextonions.zero
        iota(rho(n1)) shouldNotBe n1
    }

    // ---------- M₂(R) acting on J ----------

    "M₂ acts on J by (A, 0)(0, ν) = (0, νA) and (0, ν)(A, 0) = (0, ν·adj A)" {
        checkAll(arbM, arbV) { a, nu ->
            val (x, y) = nu
            val nuA = Vec2(x * a.a + y * a.c, x * a.b + y * a.d)
            val nuAdjA = Vec2(x * a.d - y * a.c, -x * a.b + y * a.a)
            sextonions.mul(iota(a), j(nu)) shouldBe j(nuA)
            sextonions.mul(j(nu), iota(a)) shouldBe j(nuAdjA)
        }
    }

    "J is a square-zero ideal" {
        checkAll(arbV, arbV) { nu, mu ->
            sextonions.mul(j(nu), j(mu)) shouldBe sextonions.zero
        }
        checkAll(arbS, arbV) { x, nu ->
            rho(sextonions.mul(x, j(nu))) shouldBe matrices.zero
            rho(sextonions.mul(j(nu), x)) shouldBe matrices.zero
        }
    }

    "the Cayley bimodule identity: x·n = n·x̄ for x in ι(M₂) and n in J" {
        checkAll(arbM, arbV) { a, nu ->
            val x = iota(a)
            sextonions.mul(x, j(nu)) shouldBe sextonions.mul(j(nu), sextonions.conj(x))
        }
    }

    "the left action on J is not an associative action: (AB)·ν ≠ A·(B·ν) for A = E₁₂, B = E₂₁, ν = (1, 0)" {
        val e12 = iota(Mat2(zero, one, zero, zero))
        val e21 = iota(Mat2(zero, zero, one, zero))
        val n1 = j(Vec2(one, zero))

        val left = sextonions.mul(sextonions.mul(e12, e21), n1)
        val right = sextonions.mul(e12, sextonions.mul(e21, n1))
        left shouldBe n1
        right shouldBe sextonions.zero
    }

    // ---------- Through the split-quaternions ----------

    "H′(ℚ) → S(ℚ) is an injective unital ring homomorphism" {
        val sigma = SextonionAlgebras.splitQuaternionToSextonionHomomorphism(q)
        NonAssociativeRingHomomorphismLaws(
            hom = sigma::invoke,
            domain = SplitQuaternionAlgebras.splitQuaternionRing(q),
            codomain = sextonions,
            arb = ArbSplitQuaternion.rationalSplitQuaternion
        ).fullTest().throwIfFailed()
        injectivityLaw(hom = sigma::invoke, arbA = ArbSplitQuaternion.rationalSplitQuaternion).test()
    }

    "H′ → S sends w + xi + yj + zk to ([[w − z, y − x], [x + y, w + z]], 0)" {
        val sigma = SextonionAlgebras.splitQuaternionToSextonionHomomorphism(q)
        checkAll(ArbSplitQuaternion.rationalSplitQuaternion) { h ->
            val (w, x, y, zz) = h
            sigma(h) shouldBe Sextonion(Mat2(w - zz, y - x, x + y, w + zz), zeroVec)
        }
    }

    "H′(ℤ) → S(ℤ) is an injective homomorphism but misses most of ι(M₂(ℤ)): the diagonal entries always have equal parity" {
        val sigmaZ = SextonionAlgebras.splitQuaternionToSextonionHomomorphism(z)
        NonAssociativeRingHomomorphismLaws(
            hom = sigmaZ::invoke,
            domain = SplitQuaternionAlgebras.splitQuaternionRing(z),
            codomain = SextonionAlgebras.sextonionRing(z),
            arb = ArbSplitQuaternion.integerSplitQuaternion
        ).fullTest().throwIfFailed()

        val two = BigInteger.TWO
        checkAll(ArbSplitQuaternion.integerSplitQuaternion) { h ->
            val (m, nu) = sigmaZ(h)
            (m.a - m.d).mod(two) shouldBe BigInteger.ZERO
            (m.b - m.c).mod(two) shouldBe BigInteger.ZERO
            nu shouldBe Vec2(BigInteger.ZERO, BigInteger.ZERO)
        }
        injectivityLaw(hom = sigmaZ::invoke, arbA = ArbSplitQuaternion.integerSplitQuaternion).test()
    }

    "S(ℚ) → H′(ℚ) is a unital ring homomorphism with kernel J that undoes H′ → S" {
        val sigma = SextonionAlgebras.splitQuaternionToSextonionHomomorphism(q)
        val pi = SextonionAlgebras.sextonionToSplitQuaternionHomomorphism(q)
        val splitQuaternions = SplitQuaternionAlgebras.splitQuaternionRing(q)
        NonAssociativeRingHomomorphismLaws(
            hom = pi::invoke,
            domain = sextonions,
            codomain = splitQuaternions,
            arb = arbS
        ).fullTest().throwIfFailed()

        checkAll(ArbSplitQuaternion.rationalSplitQuaternion) { h ->
            pi(sigma(h)) shouldBe h
        }
        checkAll(arbV) { nu ->
            pi(j(nu)) shouldBe splitQuaternions.zero
        }
    }

    "S → H′ inverts the matrix representation: ([[p, q], [r, s]], ν) ↦ (p + s)/2 + (r − q)/2·i + (q + r)/2·j + (s − p)/2·k" {
        val pi = SextonionAlgebras.sextonionToSplitQuaternionHomomorphism(q)
        val half = Rational.of(1, 2)
        checkAll(arbS) { x ->
            val (m, _) = x
            pi(x) shouldBe splitQuaternion(
                half * (m.a + m.d),
                half * (m.c - m.b),
                half * (m.b + m.c),
                half * (m.d - m.a)
            )
        }
    }

    "over 𝔽₃ both maps exist and S → H′ undoes H′ → S" {
        val sigma3 = SextonionAlgebras.splitQuaternionToSextonionHomomorphism(f3)
        val pi3 = SextonionAlgebras.sextonionToSplitQuaternionHomomorphism(f3)
        NonAssociativeRingHomomorphismLaws(
            hom = pi3::invoke,
            domain = SextonionAlgebras.sextonionRing(f3),
            codomain = SplitQuaternionAlgebras.splitQuaternionRing(f3),
            arb = ArbSextonion.sextonionOf(arbF3)
        ).fullTest().throwIfFailed()

        checkAll(ArbSplitQuaternion.splitQuaternionOf(arbF3)) { h ->
            pi3(sigma3(h)) shouldBe h
        }
    }

    "over 𝔽₂, H′ → S is a homomorphism but not injective (1 and k collide), and S → H′ throws IllegalArgumentException" {
        val sigma2 = SextonionAlgebras.splitQuaternionToSextonionHomomorphism(f2)
        NonAssociativeRingHomomorphismLaws(
            hom = sigma2::invoke,
            domain = SplitQuaternionAlgebras.splitQuaternionRing(f2),
            codomain = SextonionAlgebras.sextonionRing(f2),
            arb = ArbSplitQuaternion.splitQuaternionOf(arbF2)
        ).fullTest().throwIfFailed()

        val basis = SplitQuaternionAlgebras.basis(f2)
        val (unit, k) = basis[0] to basis[3]
        (unit == k) shouldBe false
        sigma2(unit) shouldBe sigma2(k)
        sigma2(k) shouldBe SextonionAlgebras.sextonionRing(f2).one

        shouldThrow<IllegalArgumentException> {
            SextonionAlgebras.sextonionToSplitQuaternionHomomorphism(f2)
        }
    }
})
