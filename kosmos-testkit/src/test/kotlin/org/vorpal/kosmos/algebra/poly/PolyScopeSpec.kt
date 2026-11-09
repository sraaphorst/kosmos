package org.vorpal.kosmos.algebra.poly

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.poly.scope.PolyIntLiteralScope
import org.vorpal.kosmos.algebra.poly.scope.PolyScope
import org.vorpal.kosmos.algebra.poly.scope.polyScope
import org.vorpal.kosmos.algebra.poly.structures.polyKit
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.NaturalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.math.Natural
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion
import java.math.BigInteger

/**
 * The operator DSL: [PolyScope] (semiring: `+`, `·`, scalars), `PolyRingScope` (adds `-`) and
 * [PolyIntLiteralScope] (adds `Int`/`Long`/`BigInteger` literals), entered via `polyScope { ... }`.
 *
 * Each expression is checked against the same polynomial built directly through the kit, so the
 * DSL is only sugar over arithmetic already tested in `PolyArithmeticSpec`.
 *
 * Known issue: `Semiring.polyScope`, `Ring.polyScope` and `CommutativeRing.polyScope` overload on
 * the receiver, but their `block` parameters have different receiver types (`PolyScope` vs
 * `PolyIntLiteralScope`). For a ring receiver the Semiring overload is more specific in `block`
 * while the ring overloads are more specific in the receiver, so Kotlin reports an overload
 * resolution ambiguity. Only `Semiring.polyScope` is usable today (from a receiver that is not a
 * ring, such as ℕ).
 */
class PolyScopeSpec : FunSpec({

    val zz = IntegerAlgebras.IntegerCommutativeRing
    val eqZ = IntegerAlgebras.eqInteger
    val zKit = zz.polyKit(eqZ)

    fun zPoly(vararg cs: Int) = ArbPoly.polyOf(zKit, ints(*cs))

    // The Ring / CommutativeRing overloads of `polyScope` cannot currently be called: every call
    // with a ring receiver is ambiguous against the Semiring overload (see the class KDoc). Until
    // that is resolved, the ring scopes are entered by constructing them directly.
    fun <R> zScope(name: String = "x", block: PolyIntLiteralScope<BigInteger>.() -> R): R =
        PolyIntLiteralScope(zz.polyKit(eqZ, name), zz).block()

    test("ℤ: 5x² + 3x + 1 reads the way it is written") {
        zScope { 5 * x * x + 3 * x + 1 } shouldBe zPoly(1, 3, 5)
    }

    test("ℤ: subtraction, unary minus, and literals on either side") {
        zScope {
            x - 1 shouldBe zPoly(-1, 1)
            1 - x shouldBe zPoly(1, -1)
            -x shouldBe zPoly(0, -1)
            x - x shouldBe zero
            2 + x shouldBe zPoly(2, 1)
            x + 2 shouldBe zPoly(2, 1)
            x * 3 shouldBe zPoly(0, 3)
            (x - 1) * (x + 1) shouldBe x * x - 1
        }
    }

    test("ℤ: fromInt, fromLong and fromBigInt all embed the same constant") {
        zScope {
            fromInt(7) shouldBe zPoly(7)
            fromLong(7L) shouldBe zPoly(7)
            fromBigInt(7.toBigInteger()) shouldBe zPoly(7)
            fromInt(0) shouldBe zero
        }
    }

    test("ℤ: zero, one, c, x, gen and vars") {
        zScope(name = "t") {
            zero shouldBe zKit.zeroP
            one shouldBe zKit.oneP
            c(BigInteger.TEN) shouldBe zPoly(10)
            x shouldBe zPoly(0, 1)
            gen(0) shouldBe x
            vars shouldBe listOf(x)
            kit.name(0) shouldBe "t"
        }
    }

    test("ℤ: eval and subst as extension functions") {
        zScope {
            val p = x * x + 1
            p.eval(3.toBigInteger()) shouldBe 10.toBigInteger()
            p.subst(x + 1) shouldBe x * x + 2 * x + 2
        }
    }

    test("ℤ: the DSL agrees with the kit on random polynomials") {
        val polys = ArbPoly.poly(zKit, ArbPoly.smallInteger)
        checkAll(polys, polys, ArbPoly.smallInteger) { p, q, a ->
            zScope {
                p + q shouldBe zKit.addP(p, q)
                p * q shouldBe zKit.mulP(p, q)
                p - q shouldBe zKit.addP(p, zKit.neg(q))
                a * p shouldBe zKit.mulP(zKit.embed(a), p)
                p * a shouldBe zKit.mulP(p, zKit.embed(a))
            }
        }
    }

    test("the block's result is returned") {
        val degree: Int = zScope { (x * x * x + 1).degree }
        degree shouldBe 3
    }

    test("ℚ: rational scalars act on polynomials") {
        val qq = RationalAlgebras.RationalField
        val half = Rational.of(1, 2)
        PolyIntLiteralScope(qq.polyKit(Eq.default()), qq).run {
            val p = half * x + 1
            p.coeffs shouldBe listOf(Rational.ONE, half)
            (2 * p).coeffs shouldBe listOf(Rational.of(2), Rational.ONE)
        }
    }

    test("𝔽₂: literals are reduced, so 2x = 0 and (x + 1)² = x² + 1") {
        val f2 = PrimeField(2.toBigInteger())
        PolyIntLiteralScope(f2.polyKit(Eq.default()), f2).run {
            2 * x shouldBe zero
            (x + 1) * (x + 1) shouldBe x * x + 1
            x + 1 shouldBe x - 1
        }
    }

    test("ℍ_ℚ: left and right scalar actions differ, i·(j·x) = k·x but (j·x)·i = -k·x") {
        val hq = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        val zero = Rational.ZERO
        val one = Rational.ONE
        val i = quaternion(zero, one, zero, zero)
        val j = quaternion(zero, zero, one, zero)
        val k = quaternion(zero, zero, zero, one)

        PolyIntLiteralScope(hq.polyKit(Eq.default()), hq).run {
            val jx = j * x
            i * jx shouldBe k * x
            jx * i shouldBe -(k * x)
            // Integer literals still work in a noncommutative ring.
            (2 * x - 1).coeffs shouldBe listOf(hq.add.inverse(hq.one), hq.fromInt(2))
        }
    }

    test("ℕ: the semiring scope offers + and · and constants") {
        val nn = NaturalAlgebras.NaturalCommutativeSemiring
        val two = Natural(2.toBigInteger())
        nn.polyScope(NaturalAlgebras.eqNatural) {
            val p = x * x + c(two) * x + one
            p.coeffs shouldBe listOf(Natural.ONE, two, Natural.ONE)
            p.eval(Natural(3.toBigInteger())) shouldBe Natural(16.toBigInteger())
        }
    }
})
