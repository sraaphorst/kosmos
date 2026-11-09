package org.vorpal.kosmos.algebra.poly

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.poly.kit.PolyKit
import org.vorpal.kosmos.algebra.poly.structures.polyKit
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.NaturalAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternionAlgebras
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import java.math.BigInteger

/**
 * The [PolyKit] bundle: its accessors and factories, and multivariate towers `R[x][y][z]` built
 * with [PolyKit.extend].
 *
 * In a tower each new kit has the previous polynomial ring as its coefficients, the old generators
 * lifted to constants, and the new variable `0 + 1·y` last.
 */
class PolyKitSpec : FunSpec({

    val zz = IntegerAlgebras.IntegerCommutativeRing
    val eqZ = IntegerAlgebras.eqInteger

    /* --------------------------
     * Univariate kits
     * -------------------------- */

    test("the accessors expose the coefficient and polynomial structures") {
        val kit = zz.polyKit(eqZ)

        kit.zeroA shouldBe BigInteger.ZERO
        kit.oneA shouldBe BigInteger.ONE
        kit.zeroP shouldBe kit.poly.zero
        kit.oneP shouldBe kit.poly.one
        kit.addP shouldBe kit.poly.add.op
        kit.mulP shouldBe kit.poly.mul.op
        kit.coeff shouldBe zz
    }

    test("a univariate kit has one generator, x = [0, 1], named \"x\" by default") {
        val kit = zz.polyKit(eqZ)

        kit.arity shouldBe 1
        kit.name(0) shouldBe "x"
        kit.varNames shouldContainExactly listOf("x")
        kit.gen(0) shouldBe kit.x()
        kit.vars shouldBe kit.gens
        kit.x().coeffs shouldBe ints(0, 1)
    }

    test("the variable can be renamed") {
        zz.polyKit(eqZ, name = "t").name(0) shouldBe "t"
    }

    test("the companion factories match the polyKit extensions") {
        val viaCommutative = PolyKit.fromCommutativeRing(zz, eqZ)
        val viaRing = PolyKit.fromRing(zz, eqZ)
        val viaSemiring = PolyKit.fromSemiring(zz, eqZ)

        viaCommutative.poly.shouldBeInstanceOf<CommutativeRing<*>>()
        viaRing.poly.shouldBeInstanceOf<Ring<*>>()
        viaRing.poly.shouldNotBeInstanceOf<CommutativeRing<*>>()
        viaSemiring.poly.shouldNotBeInstanceOf<Ring<*>>()

        // Same arithmetic regardless of which factory built the kit.
        val p = ArbPoly.polyOf(viaCommutative, ints(1, -2, 3))
        listOf(viaCommutative, viaRing, viaSemiring).forEach { kit ->
            kit.mulP(p, p).coeffs shouldBe ints(1, -4, 10, -12, 9)
        }
    }

    test("neg is available exactly when the kit was built from a ring") {
        PolyKit.fromRing(zz, eqZ).neg(ArbPoly.polyOf(zz.polyKit(eqZ), ints(1, -2))).coeffs shouldBe ints(-1, 2)

        val nKit = NaturalAlgebras.NaturalCommutativeSemiring.polyKit(NaturalAlgebras.eqNatural)
        shouldThrow<IllegalStateException> { nKit.neg(nKit.x()) }
    }

    /* --------------------------
     * Towers
     * -------------------------- */

    val zx = zz.polyKit(eqZ)
    val zxy = zx.extend("y")
    val zxyz = zxy.extend("z")

    test("ℤ[x][y]: two generators named x and y, with x lifted to a constant") {
        zxy.arity shouldBe 2
        zxy.varNames shouldContainExactly listOf("x", "y")
        zxy.name(1) shouldBe "y"

        // x is the constant polynomial whose one coefficient is x ∈ ℤ[x].
        zxy.gen(0).coeffs shouldBe listOf(zx.x())
        // y = 0 + 1·y over ℤ[x].
        zxy.gen(1).coeffs shouldBe listOf(zx.zeroP, zx.oneP)
        zxy.x() shouldBe zxy.gen(1)
    }

    test("ℤ[x][y]: the coefficients are ℤ[x], and the structure stays commutative") {
        zxy.coeff shouldBe zx.poly
        zxy.zeroA shouldBe zx.zeroP
        zxy.oneA shouldBe zx.oneP
        zxy.poly.shouldBeInstanceOf<CommutativeRing<*>>()
    }

    test("ℤ[x][y] satisfies CommutativeRingLaws") {
        val inner = ArbPoly.poly(zx, ArbPoly.smallInteger, maxDegree = 2)
        @Suppress("UNCHECKED_CAST")
        CommutativeRingLaws(
            ring = zxy.poly as CommutativeRing<Poly<Poly<BigInteger>>>,
            arb = ArbPoly.poly(zxy, inner, maxDegree = 2),
            eq = polyEq(polyEq(eqZ))
        ).fullTest().throwIfFailed()
    }

    test("ℤ[x][y]: (x + y)² = x² + 2xy + y²") {
        val x = zxy.gen(0)
        val y = zxy.gen(1)
        fun add(p: Poly<Poly<BigInteger>>, q: Poly<Poly<BigInteger>>) = zxy.addP(p, q)
        fun mul(p: Poly<Poly<BigInteger>>, q: Poly<Poly<BigInteger>>) = zxy.mulP(p, q)
        val two = zxy.embed(zx.embed(2.toBigInteger()))

        val lhs = mul(add(x, y), add(x, y))
        val rhs = add(add(mul(x, x), mul(two, mul(x, y))), mul(y, y))
        lhs shouldBe rhs

        // As a polynomial in y with coefficients in ℤ[x]: x² + (2x)·y + 1·y².
        lhs.coeffs shouldBe listOf(
            ArbPoly.polyOf(zx, ints(0, 0, 1)),
            ArbPoly.polyOf(zx, ints(0, 2)),
            zx.oneP
        )
    }

    test("ℤ[x][y]: evaluating at y := x lands in ℤ[x]") {
        val x = zxy.gen(0)
        val y = zxy.gen(1)
        val xPlusY = zxy.addP(x, y)
        val xy = zxy.mulP(x, y)

        zxy.eval(xPlusY, zx.x()) shouldBe ArbPoly.polyOf(zx, ints(0, 2))
        zxy.eval(xy, zx.x()) shouldBe ArbPoly.polyOf(zx, ints(0, 0, 1))
    }

    test("ℤ[x][y]: negation works in the tower") {
        val x = zxy.gen(0)
        val y = zxy.gen(1)
        val p = zxy.addP(x, y)
        zxy.addP(p, zxy.neg(p)) shouldBe zxy.zeroP
    }

    test("ℤ[x][y][z]: three generators that commute pairwise") {
        zxyz.arity shouldBe 3
        zxyz.varNames shouldContainExactly listOf("x", "y", "z")
        zxyz.poly.shouldBeInstanceOf<CommutativeRing<*>>()

        val gens = zxyz.gens
        gens.forEach { a ->
            gens.forEach { b ->
                zxyz.mulP(a, b) shouldBe zxyz.mulP(b, a)
            }
        }
        // x is lifted twice: a constant whose coefficient is a constant whose coefficient is x.
        zxyz.gen(0).coeffs shouldBe listOf(zxy.gen(0))
        zxyz.gen(1).coeffs shouldBe listOf(zxy.gen(1))
    }

    test("ℍ_ℚ[x][y] is a Ring but not a CommutativeRing, and x, y commute with each other") {
        val hq = RationalQuaternionAlgebras.RationalQuaternionDivisionRing
        val kit = hq.polyKit(Eq.default()).extend("y")

        kit.poly.shouldBeInstanceOf<Ring<*>>()
        kit.poly.shouldNotBeInstanceOf<CommutativeRing<*>>()
        kit.mulP(kit.gen(0), kit.gen(1)) shouldBe kit.mulP(kit.gen(1), kit.gen(0))
        kit.addP(kit.gen(0), kit.neg(kit.gen(0))) shouldBe kit.zeroP
    }

    test("ℕ[x][y] stays a semiring without negation") {
        val kit = NaturalAlgebras.NaturalCommutativeSemiring.polyKit(NaturalAlgebras.eqNatural).extend("y")

        kit.poly.shouldNotBeInstanceOf<Ring<*>>()
        shouldThrow<IllegalStateException> { kit.neg(kit.gen(1)) }
        kit.mulP(kit.gen(0), kit.gen(1)) shouldBe kit.mulP(kit.gen(1), kit.gen(0))
    }

    test("extend can override the operation symbols of the new level") {
        val kit = zx.extend("y", Option.Some("⊕"), Option.Some("⊗"))
        kit.poly.add.op.symbol shouldBe "⊕"
        kit.poly.mul.op.symbol shouldBe "⊗"
    }

    test("ℤ[x][y]: arbitrary sums and products are canonical at both levels") {
        val inner = ArbPoly.poly(zx, ArbPoly.smallInteger, maxDegree = 2)
        val outer = ArbPoly.poly(zxy, inner, maxDegree = 2)
        fun canonical(p: Poly<Poly<BigInteger>>): Boolean =
            (p.coeffs.isEmpty() || p.coeffs.last() != zx.zeroP) &&
                p.coeffs.all { c -> c.coeffs.isEmpty() || c.coeffs.last() != BigInteger.ZERO }

        checkAll(outer, outer) { p, q ->
            canonical(zxy.addP(p, q)) shouldBe true
            canonical(zxy.mulP(p, q)) shouldBe true
        }
    }
})
