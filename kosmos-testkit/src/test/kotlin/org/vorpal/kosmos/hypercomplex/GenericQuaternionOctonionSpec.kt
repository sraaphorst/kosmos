package org.vorpal.kosmos.hypercomplex

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.multi.Bicomplex
import org.vorpal.kosmos.hypercomplex.multi.BicomplexAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.ArbRationalOctonion
import org.vorpal.kosmos.hypercomplex.octonion.Octonion
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.RationalOctonionAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.octonion
import org.vorpal.kosmos.hypercomplex.quaternion.ArbRationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.Quaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternion
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternionAlgebras
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeInvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.MoufangLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import java.math.BigInteger

/**
 * The generic `Quaternion<R>` and `Octonion<R>` constructions, over base rings other than the three
 * named ones (ℝ, ℚ, ℤ), and their agreement with the named structures.
 *
 * - Over the finite field `𝔽₇` every law is checked exactly, and the quaternions are seen *not* to
 *   be a division ring: the norm is isotropic.
 * - Over ℚ the generic constructions are checked to be the same operations as
 *   `RationalQuaternionDivisionRing` and `RationalOctonionDivisionAlgebra`.
 * - The value-class carriers keep algebras that share a Cayley-Dickson pair apart.
 */
class GenericQuaternionOctonionSpec : StringSpec({
    val q = RationalAlgebras.RationalField
    val f7 = PrimeField(BigInteger.valueOf(7))

    val arbF7: Arb<BigInteger> = Arb.int(0..6).map(Int::toBigInteger)
    val arbQuaternionF7: Arb<Quaternion<BigInteger>> =
        Arb.bind(arbF7, arbF7, arbF7, arbF7) { w, x, y, z -> quaternion(w, x, y, z) }
    val arbOctonionF7: Arb<Octonion<BigInteger>> =
        Arb.bind(arbF7, arbF7, arbF7, arbF7, arbF7, arbF7, arbF7, arbF7) { w, x, y, z, u, v, s, t ->
            octonion(w, x, y, z, u, v, s, t)
        }
    val smallRational: Arb<Rational> =
        Arb.bind(Arb.int(-12..12), Arb.int(1..12)) { p, d -> Rational.of(p, d) }

    fun f(n: Int): BigInteger = n.toBigInteger()

    // ── Quaternions over a finite field ──────────────────────────────────────

    "Quaternion<𝔽₇> satisfies InvolutiveRingLaws" {
        InvolutiveRingLaws(QuaternionAlgebras.quaternionRing(f7), arbQuaternionF7).fullTest().throwIfFailed()
    }

    "Quaternion<𝔽₇>: i² = j² = k² = ijk = -1" {
        val ring = QuaternionAlgebras.quaternionRing(f7)
        val (one, i, j, k) = QuaternionAlgebras.basis(f7)
        one shouldBe ring.one
        listOf(i, j, k).forEach { u -> ring.mul(u, u) shouldBe ring.negOne }
        ring.mul(ring.mul(i, j), k) shouldBe ring.negOne
        ring.mul(i, j) shouldBe k
    }

    "Quaternion<𝔽₇> has a multiplicative norm" {
        NormSqMultiplicativeLaw(
            mulA = QuaternionAlgebras.quaternionRing(f7).mul.op,
            normSq = QuaternionAlgebras.normSq(f7),
            mulN = f7.mul.op,
            arbA = arbQuaternionF7
        ).test()
    }

    "Quaternion<𝔽₇> is not a division ring: i + 2j + 3k is a nonzero element of norm 1 + 4 + 9 ≡ 0" {
        val ring = QuaternionAlgebras.quaternionRingWithReciprocal(f7)
        val isotropic = quaternion(f(0), f(1), f(2), f(3))
        (isotropic == ring.zero) shouldBe false
        ring.normSq(isotropic) shouldBe BigInteger.ZERO
        ring.hasReciprocal(isotropic) shouldBe false
        shouldThrow<ArithmeticException> { ring.reciprocal(isotropic) }
        // It is a zero divisor: q · conj(q) = N(q) = 0.
        ring.mul(isotropic, ring.conj(isotropic)) shouldBe ring.zero
    }

    "Quaternion<𝔽₇>: every element of nonzero norm is invertible, with q·q⁻¹ = q⁻¹·q = 1" {
        val ring = QuaternionAlgebras.quaternionRingWithReciprocal(f7)
        checkAll(arbQuaternionF7) { x ->
            if (ring.hasReciprocal(x)) {
                val inverse = ring.reciprocal(x)
                ring.mul(x, inverse) shouldBe ring.one
                ring.mul(inverse, x) shouldBe ring.one
            } else {
                ring.normSq(x) shouldBe BigInteger.ZERO
            }
        }
    }

    // ── Octonions over a finite field ────────────────────────────────────────

    "Octonion<𝔽₇> satisfies NonAssociativeInvolutiveRingLaws and is alternative and Moufang" {
        val ring = OctonionAlgebras.octonionRing(f7)
        NonAssociativeInvolutiveRingLaws(ring, arbOctonionF7).fullTest().throwIfFailed()
        AlternativityLaw(ring.mul.op, arbOctonionF7).test()
        MoufangLaw(ring.mul.op, arbOctonionF7).test()
    }

    "Octonion<𝔽₇> is not associative, and its norm is multiplicative" {
        val ring = OctonionAlgebras.octonionRing(f7)
        val basis = OctonionAlgebras.basis(f7)
        basis.size shouldBe 8
        val (e1, e2, e4) = listOf(1, 2, 4).map(basis::get)
        ring.mul(ring.mul(e1, e2), e4) shouldBe ring.add.inverse(ring.mul(e1, ring.mul(e2, e4)))
        (ring.mul(ring.mul(e1, e2), e4) == ring.mul(e1, ring.mul(e2, e4))) shouldBe false

        NormSqMultiplicativeLaw(
            mulA = ring.mul.op,
            normSq = OctonionAlgebras.normSq(f7),
            mulN = f7.mul.op,
            arbA = arbOctonionF7
        ).test()
    }

    "Octonion<𝔽₇>: elements of nonzero norm have two-sided inverses" {
        val ring = OctonionAlgebras.octonionRingWithReciprocal(f7)
        checkAll(arbOctonionF7) { x ->
            if (ring.hasReciprocal(x)) {
                val inverse = ring.reciprocal(x)
                ring.mul(x, inverse) shouldBe ring.one
                ring.mul(inverse, x) shouldBe ring.one
            }
        }
        ring.hasReciprocal(ring.zero) shouldBe false
        shouldThrow<ArithmeticException> { ring.reciprocal(ring.zero) }
    }

    // ── Agreement with the named rational structures ─────────────────────────

    "the generic quaternion ring over ℚ is RationalQuaternionDivisionRing" {
        val generic = QuaternionAlgebras.quaternionRingWithReciprocal(q)
        val named = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        generic.zero shouldBe named.zero
        generic.one shouldBe named.one
        QuaternionAlgebras.basis(q) shouldBe named.basis

        val arb = ArbRationalQuaternion.smallRationalQuaternion
        checkAll(arb, arb) { x, y ->
            generic.add(x, y) shouldBe named.add(x, y)
            generic.mul(x, y) shouldBe named.mul(x, y)
            generic.conj(x) shouldBe named.conj(x)
            generic.normSq(x) shouldBe named.normSq(x)
            generic.hasReciprocal(x) shouldBe named.hasReciprocal(x)
            if (named.hasReciprocal(x)) generic.reciprocal(x) shouldBe named.reciprocal(x)
        }
    }

    "the generic octonion ring over ℚ is RationalOctonionDivisionAlgebra" {
        val generic = OctonionAlgebras.octonionRingWithReciprocal(q)
        val named = RationalOctonionAlgebras.RationalOctonionDivisionAlgebra
        generic.zero shouldBe named.zero
        generic.one shouldBe named.one
        OctonionAlgebras.basis(q) shouldBe named.basis

        val arb = ArbRationalOctonion.smallRationalOctonion
        checkAll(arb, arb) { x, y ->
            generic.add(x, y) shouldBe named.add(x, y)
            generic.mul(x, y) shouldBe named.mul(x, y)
            generic.conj(x) shouldBe named.conj(x)
            generic.normSq(x) shouldBe named.normSq(x)
            if (named.hasReciprocal(x)) generic.reciprocal(x) shouldBe named.reciprocal(x)
        }
    }

    // ── Modules, algebras and embeddings over ℚ ──────────────────────────────

    "Quaternion<ℚ> is a free module and a star algebra over ℚ" {
        val arb = ArbRationalQuaternion.smallRationalQuaternion
        RModuleLaws(QuaternionAlgebras.quaternionModule(q), smallRational, arb).fullTest().throwIfFailed()
        StarAlgebraLaws(QuaternionAlgebras.quaternionStarAlgebra(q), smallRational, arb).fullTest().throwIfFailed()
        QuaternionAlgebras.quaternionAlgebra(q).scalars shouldBe q
    }

    "Octonion<ℚ> is a free module and a nonassociative star algebra over ℚ" {
        val arb = ArbRationalOctonion.smallRationalOctonion
        RModuleLaws(OctonionAlgebras.octonionModule(q), smallRational, arb).fullTest().throwIfFailed()
        NonAssociativeStarAlgebraLaws(OctonionAlgebras.octonionStarAlgebra(q), smallRational, arb)
            .fullTest().throwIfFailed()
        OctonionAlgebras.octonionAlgebra(q).scalars shouldBe q
    }

    "the scalar embeddings ℚ ↪ Quaternion<ℚ> and ℚ ↪ Octonion<ℚ> are injective unital homomorphisms" {
        val toQuaternion = QuaternionAlgebras.scalarEmbedding(q)
        RingHomomorphismLaws(
            hom = toQuaternion::invoke,
            domain = q,
            codomain = QuaternionAlgebras.quaternionRing(q),
            arb = smallRational
        ).fullTest().throwIfFailed()
        injectivityLaw(hom = toQuaternion::invoke, arbA = smallRational).test()

        val toOctonion = OctonionAlgebras.scalarEmbedding(q)
        NonAssociativeRingHomomorphismLaws(
            hom = toOctonion::invoke,
            domain = q,
            codomain = OctonionAlgebras.octonionRing(q),
            arb = smallRational
        ).fullTest().throwIfFailed()
        injectivityLaw(hom = toOctonion::invoke, arbA = smallRational).test()
    }

    "the generic canonical embedding Quaternion<R> ↪ Octonion<R> is q ↦ (q, 0), over ℚ and over 𝔽₇" {
        val overQ = OctonionAlgebras.quaternionToOctonionMonomorphism(q)
        NonAssociativeRingHomomorphismLaws(
            hom = overQ::invoke,
            domain = QuaternionAlgebras.quaternionRing(q),
            codomain = OctonionAlgebras.octonionRing(q),
            arb = ArbRationalQuaternion.smallRationalQuaternion
        ).fullTest().throwIfFailed()
        injectivityLaw(hom = overQ::invoke, arbA = ArbRationalQuaternion.smallRationalQuaternion).test()
        checkAll(ArbRationalQuaternion.smallRationalQuaternion) { x ->
            overQ(x) shouldBe RationalOctonionAlgebras.RationalQuaternionToRationalOctonionMonomorphism(x)
        }

        val overF7 = OctonionAlgebras.quaternionToOctonionMonomorphism(f7)
        NonAssociativeRingHomomorphismLaws(
            hom = overF7::invoke,
            domain = QuaternionAlgebras.quaternionRing(f7),
            codomain = OctonionAlgebras.octonionRing(f7),
            arb = arbQuaternionF7
        ).fullTest().throwIfFailed()
        QuaternionAlgebras.basis(f7).map(overF7::invoke) shouldBe OctonionAlgebras.basis(f7).take(4)
    }

    "generic Eq and Printable instances work over any base" {
        val eq = QuaternionAlgebras.eqQuaternion(q, RationalAlgebras.eqRational)
        val x = quaternion(Rational.of(1), Rational.of(-2), Rational.ZERO, Rational.of(1, 2))
        eq(x, quaternion(Rational.of(2, 2), Rational.of(-2), Rational.ZERO, Rational.of(2, 4))) shouldBe true
        eq(x, QuaternionAlgebras.quaternionRing(q).conj(x)) shouldBe false

        val pr = QuaternionAlgebras.printableQuaternion(
            q, RationalAlgebras.SignedRational, RationalAlgebras.printableRational, RationalAlgebras.eqRational
        )
        pr(x) shouldBe RationalQuaternionAlgebras.printableRationalQuaternion(x)

        val eqO = OctonionAlgebras.eqOctonion(q, RationalAlgebras.eqRational)
        val prO = OctonionAlgebras.printableOctonion(
            q, RationalAlgebras.SignedRational, RationalAlgebras.printableRational, RationalAlgebras.eqRational
        )
        checkAll(ArbRationalOctonion.smallRationalOctonion) { o ->
            eqO(o, o) shouldBe true
            prO(o) shouldBe RationalOctonionAlgebras.printableRationalOctonion(o)
        }
    }

    // ── Carriers ─────────────────────────────────────────────────────────────

    "accessors, coefficients and components read the same coordinates" {
        val x = quaternion(1, 2, 3, 4)
        x.coefficients() shouldBe listOf(1, 2, 3, 4)
        listOf(x.w, x.x, x.y, x.z) shouldBe listOf(1, 2, 3, 4)
        val (w, i, j, k) = x
        listOf(w, i, j, k) shouldBe listOf(1, 2, 3, 4)
        x.cd shouldBe CD(CD(1, 2), CD(3, 4))

        val o = octonion(1, 2, 3, 4, 5, 6, 7, 8)
        o.coefficients() shouldBe (1..8).toList()
        listOf(o.w, o.x, o.y, o.z, o.u, o.v, o.s, o.t) shouldBe (1..8).toList()
        o.cd shouldBe CD(CD(CD(1, 2), CD(3, 4)), CD(CD(5, 6), CD(7, 8)))
    }

    "algebras that share a Cayley-Dickson carrier are distinct types with distinct products" {
        // The same raw pair (0, 1) names j in ℍ, j in ℍ′ and j in 𝔹.
        val raw = CD(complex(0.0, 0.0), complex(1.0, 0.0))
        val quaternionJ = Quaternion(raw)
        val splitJ = SplitQuaternion(raw)
        val bicomplexJ = Bicomplex(raw)

        val any: List<Any> = listOf(quaternionJ, splitJ, bicomplexJ)
        any[0] shouldNotBe any[1]
        any[0] shouldNotBe any[2]
        any[1] shouldNotBe any[2]

        val quaternions = QuaternionAlgebras.QuaternionDivisionRing
        val splitQuaternions = SplitQuaternionAlgebras.RealSplitQuaternionRing
        val bicomplex = BicomplexAlgebras.BicomplexCommutativeRing

        // Compared up to approximate equality: the products contain signed zeros.
        val eqQ = QuaternionAlgebras.eqQuaternion
        val eqS = SplitQuaternionAlgebras.eqSplitQuaternion
        val eqB = BicomplexAlgebras.eqBicomplex

        check(eqQ(quaternions.mul(quaternionJ, quaternionJ), quaternions.negOne))               // j² = -1
        check(eqS(splitQuaternions.mul(splitJ, splitJ), splitQuaternions.one))                  // j² = +1
        check(eqB(bicomplex.mul(bicomplexJ, bicomplexJ), bicomplex.add.inverse(bicomplex.one))) // j² = -1

        // ℍ and 𝔹 agree on j² but differ on ij versus ji: ℍ anticommutes, 𝔹 commutes.
        val rawI = CD(complex(0.0, 1.0), complex(0.0, 0.0))
        val (qi, bi) = Quaternion(rawI) to Bicomplex(rawI)
        check(eqQ(quaternions.mul(qi, quaternionJ), quaternions.add.inverse(quaternions.mul(quaternionJ, qi))))
        check(!eqQ(quaternions.mul(qi, quaternionJ), quaternions.mul(quaternionJ, qi)))
        check(eqB(bicomplex.mul(bi, bicomplexJ), bicomplex.mul(bicomplexJ, bi)))
    }
})
