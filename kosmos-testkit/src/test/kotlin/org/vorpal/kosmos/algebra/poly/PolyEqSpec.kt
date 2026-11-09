package org.vorpal.kosmos.algebra.poly

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.poly.structures.polyKit
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.core.Eq
import java.math.BigInteger

/**
 * [polyEq]: equality of canonical polynomials, coefficient by coefficient.
 */
class PolyEqSpec : FunSpec({

    val zz = IntegerAlgebras.IntegerCommutativeRing
    val eqZ = IntegerAlgebras.eqInteger
    val kit = zz.polyKit(eqZ)
    val polys = ArbPoly.poly(kit, ArbPoly.smallInteger)
    val eq = polyEq(eqZ)

    test("on canonical polynomials, polyEq agrees with structural equality") {
        checkAll(polys, polys) { p, q ->
            eq(p, q) shouldBe (p == q)
        }
    }

    test("polyEq is reflexive and symmetric") {
        checkAll(polys, polys) { p, q ->
            eq(p, p) shouldBe true
            eq(p, q) shouldBe eq(q, p)
        }
    }

    test("polynomials of different lengths are never equal") {
        eq(kit.x(), kit.oneP) shouldBe false
        eq(kit.zeroP, kit.oneP) shouldBe false
    }

    test("coefficients are compared with the given Eq, not with ==") {
        // Equality mod 3, with a kit that normalizes using the same Eq.
        val mod3 = Eq<BigInteger> { a, b -> (a - b).mod(3.toBigInteger()) == BigInteger.ZERO }
        val kit3 = zz.polyKit(mod3)
        val eq3 = polyEq(mod3)

        // 4 + 3x normalizes to [4] since 3 ≡ 0, and [4] ≡ [1].
        val p = ArbPoly.polyOf(kit3, ints(4, 3))
        p.coeffs shouldBe ints(4)
        eq3(p, kit3.oneP) shouldBe true
        (p == kit3.oneP) shouldBe false
    }
})
