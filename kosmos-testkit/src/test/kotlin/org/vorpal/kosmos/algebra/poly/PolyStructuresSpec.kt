package org.vorpal.kosmos.algebra.poly

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.poly.structures.polyCommutativeRing
import org.vorpal.kosmos.algebra.poly.structures.polyKit
import org.vorpal.kosmos.algebra.poly.structures.polyRing
import org.vorpal.kosmos.algebra.poly.structures.polySemiring
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.NaturalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.RingLaws
import org.vorpal.kosmos.laws.algebra.SemiringLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import java.math.BigInteger

/**
 * The structures `polySemiring`, `polyRing` and `polyCommutativeRing` satisfy the law suites of
 * the structure they claim to be, over a spread of coefficient rings, and the `polyKit` extension
 * functions pick the strongest structure the receiver's static type allows.
 */
class PolyStructuresSpec : FunSpec({

    val zz = IntegerAlgebras.IntegerCommutativeRing
    val eqZ = IntegerAlgebras.eqInteger
    val nn = NaturalAlgebras.NaturalCommutativeSemiring
    val qq = RationalAlgebras.RationalField
    val hq = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
    val z4 = integersMod(4)

    /* --------------------------
     * Law suites
     * -------------------------- */

    test("ℕ[x] satisfies SemiringLaws") {
        val kit = nn.polyKit(NaturalAlgebras.eqNatural)
        SemiringLaws(
            semiring = nn.polySemiring(NaturalAlgebras.eqNatural),
            arb = ArbPoly.poly(kit, ArbPoly.smallNatural),
            eq = polyEq(NaturalAlgebras.eqNatural)
        ).fullTest().throwIfFailed()
    }

    test("ℕ[x] multiplication is commutative, though the type is only Semiring") {
        val kit = nn.polyKit(NaturalAlgebras.eqNatural)
        val polys = ArbPoly.poly(kit, ArbPoly.smallNatural)
        checkAll(polys, polys) { p, q ->
            kit.mulP(p, q) shouldBe kit.mulP(q, p)
        }
    }

    test("ℤ[x] satisfies CommutativeRingLaws") {
        CommutativeRingLaws(
            ring = zz.polyCommutativeRing(eqZ),
            arb = ArbPoly.poly(zz.polyKit(eqZ), ArbPoly.smallInteger),
            eq = polyEq(eqZ)
        ).fullTest().throwIfFailed()
    }

    test("ℚ[x] satisfies CommutativeRingLaws") {
        CommutativeRingLaws(
            ring = qq.polyCommutativeRing(Eq.default()),
            arb = ArbPoly.poly(qq.polyKit(Eq.default()), ArbPoly.smallRational, maxDegree = 3),
        ).fullTest().throwIfFailed()
    }

    listOf(2, 3, 5).forEach { p ->
        test("𝔽_$p[x] satisfies CommutativeRingLaws") {
            val field = PrimeField(p.toBigInteger())
            CommutativeRingLaws(
                ring = field.polyCommutativeRing(Eq.default()),
                arb = ArbPoly.poly(field.polyKit(Eq.default()), ArbPoly.residue(p)),
            ).fullTest().throwIfFailed()
        }
    }

    test("ℤ/4[x] satisfies CommutativeRingLaws despite its zero divisors") {
        CommutativeRingLaws(
            ring = z4.polyCommutativeRing(Eq.default()),
            arb = ArbPoly.poly(z4.polyKit(Eq.default()), ArbPoly.residue(4)),
        ).fullTest().throwIfFailed()
    }

    test("ℍ_ℚ[x] satisfies RingLaws") {
        RingLaws(
            ring = hq.polyRing(Eq.default()),
            arb = ArbPoly.poly(hq.polyKit(Eq.default()), ArbPoly.smallQuaternion, maxDegree = 2),
        ).fullTest().throwIfFailed()
    }

    test("ℤ[x] seen only as a Ring still satisfies RingLaws") {
        val asRing: Ring<BigInteger> = zz
        RingLaws(
            ring = asRing.polyRing(eqZ),
            arb = ArbPoly.poly(zz.polyKit(eqZ), ArbPoly.smallInteger),
        ).fullTest().throwIfFailed()
    }

    /* --------------------------
     * Noncommutative coefficients
     * -------------------------- */

    test("ℍ_ℚ[x]: x is central (xp = px), but ℍ_ℚ[x] is not commutative") {
        val kit = hq.polyKit(Eq.default())
        val polys = ArbPoly.poly(kit, ArbPoly.smallQuaternion, maxDegree = 3)
        checkAll(polys) { p ->
            kit.mulP(kit.x(), p) shouldBe kit.mulP(p, kit.x())
        }

        val zero = Rational.ZERO
        val one = Rational.ONE
        val i = kit.embed(quaternion(zero, one, zero, zero))
        val j = kit.embed(quaternion(zero, zero, one, zero))
        (kit.mulP(i, j) == kit.mulP(j, i)) shouldBe false
    }

    /* --------------------------
     * Constants
     * -------------------------- */

    test("embed: ℤ → ℤ[x] is an injective ring homomorphism") {
        val kit = zz.polyKit(eqZ)
        RingHomomorphismLaws(
            hom = kit::embed,
            domain = zz,
            codomain = zz.polyCommutativeRing(eqZ),
            arb = ArbPoly.smallInteger,
        ).fullTest().throwIfFailed()
        injectivityLaw(hom = kit::embed, arbA = ArbPoly.smallInteger).test()
    }

    test("embed: ℍ_ℚ → ℍ_ℚ[x] is an injective ring homomorphism") {
        val kit = hq.polyKit(Eq.default())
        RingHomomorphismLaws(
            hom = kit::embed,
            domain = hq,
            codomain = hq.polyRing(Eq.default()),
            arb = ArbPoly.smallQuaternion,
        ).fullTest().throwIfFailed()
        injectivityLaw(hom = kit::embed, arbA = ArbPoly.smallQuaternion).test()
    }

    test("zero is [] and one is [1] in every lifted structure") {
        val semi = nn.polySemiring(NaturalAlgebras.eqNatural)
        val ring = hq.polyRing(Eq.default())
        val comm = zz.polyCommutativeRing(eqZ)

        semi.zero.coeffs shouldBe emptyList()
        semi.one.coeffs shouldBe listOf(nn.one)
        ring.zero.coeffs shouldBe emptyList()
        ring.one.coeffs shouldBe listOf(hq.one)
        comm.zero.coeffs shouldBe emptyList()
        comm.one.coeffs shouldBe listOf(BigInteger.ONE)
    }

    /* --------------------------
     * Structure selection and symbols
     * -------------------------- */

    test("polyKit picks the structure from the receiver's static type") {
        val asSemiring: Semiring<BigInteger> = zz
        val asRing: Ring<BigInteger> = zz

        zz.polyKit(eqZ).poly.shouldBeInstanceOf<CommutativeRing<*>>()
        qq.polyKit(Eq.default()).poly.shouldBeInstanceOf<CommutativeRing<*>>()

        asRing.polyKit(eqZ).poly.shouldBeInstanceOf<Ring<*>>()
        asRing.polyKit(eqZ).poly.shouldNotBeInstanceOf<CommutativeRing<*>>()
        hq.polyKit(Eq.default()).poly.shouldNotBeInstanceOf<CommutativeRing<*>>()

        asSemiring.polyKit(eqZ).poly.shouldNotBeInstanceOf<Ring<*>>()
    }

    test("a kit built from a Semiring has no negation, even if the coefficients are a ring") {
        val asSemiring: Semiring<BigInteger> = zz
        val kit = asSemiring.polyKit(eqZ)
        shouldThrow<IllegalStateException> { kit.neg(kit.x()) }
    }

    test("the operation symbols default to the coefficient ring's and can be overridden") {
        val default = zz.polyCommutativeRing(eqZ)
        default.add.op.symbol shouldBe zz.add.op.symbol
        default.mul.op.symbol shouldBe zz.mul.op.symbol
        default.add.inverse.symbol shouldBe Symbols.MINUS

        val custom = zz.polyCommutativeRing(eqZ, Option.Some("⊕"), Option.Some("⊗"))
        custom.add.op.symbol shouldBe "⊕"
        custom.mul.op.symbol shouldBe "⊗"

        val semi = nn.polySemiring(NaturalAlgebras.eqNatural, Option.Some("∨"), Option.Some("∧"))
        semi.add.op.symbol shouldBe "∨"
        semi.mul.op.symbol shouldBe "∧"
    }
})
