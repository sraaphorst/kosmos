package org.vorpal.kosmos.hypercomplex.zorn

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.splitoctonion.ArbSplitOctonion
import org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonionAlgebras
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.linear.values.Vec3

/**
 * The real Zorn vector-matrices, checked up to approximate equality.
 *
 * The exact algebraic facts are in [ZornSpec], [ZornStructuresSpec] and [ZornSplitOctonionSpec].
 */
class RealZornSpec : StringSpec({
    val ring = ZornAlgebras.RealZornAlgebra
    val eq = ZornAlgebras.eqZorn
    val pr = ZornAlgebras.printableZornRealPretty
    val arb = ArbZorn.smallZorn

    "RealZornStarAlgebra satisfies NonAssociativeStarAlgebraLaws" {
        NonAssociativeStarAlgebraLaws(
            algebra = ZornAlgebras.RealZornStarAlgebra,
            scalarArb = ArbReal.smallReal,
            algebraArb = arb,
            eqR = RealAlgebras.eqRealApprox,
            eqA = eq,
            prR = RealAlgebras.printableRealPretty,
            prA = pr
        ).fullTest().throwIfFailed()
    }

    "ZornRealVectorSpace satisfies VectorSpaceLaws and has dimension 8" {
        ZornAlgebras.ZornRealVectorSpace.dimension shouldBe 8
        VectorSpaceLaws(
            space = ZornAlgebras.ZornRealVectorSpace,
            scalarArb = ArbReal.smallReal,
            vectorArb = arb,
            eqF = RealAlgebras.eqRealApprox,
            eqV = eq,
            prF = RealAlgebras.printableRealPretty,
            prV = pr
        ).test().throwIfFailed()
    }

    "real Zorn multiplication is alternative" {
        AlternativityLaw(ring.mul.op, arb, eq, pr).test()
    }

    "the real Zorn norm is multiplicative" {
        NormSqMultiplicativeLaw(
            mulA = ring.mul.op,
            normSq = ring.normSq,
            mulN = RealAlgebras.RealField.mul.op,
            arbA = arb,
            eqN = RealAlgebras.eqRealApprox,
            prA = pr,
            prN = RealAlgebras.printableRealPretty
        ).test()
    }

    "the real basis: E_a and E_d are complementary idempotents and every basis element has norm 0" {
        ring.basis.size shouldBe 8
        val (ea, ed) = ring.basis.first() to ring.basis.last()
        check(eq(ring.mul(ea, ea), ea))
        check(eq(ring.mul(ed, ed), ed))
        check(eq(ring.mul(ea, ed), ring.zero))
        check(eq(ring.add(ea, ed), ring.one))
        ring.basis.forEach { e -> ring.normSq(e) shouldBe 0.0 }
    }

    "reciprocal: X·X⁻¹ = X⁻¹·X = 1 away from the null cone" {
        checkAll(ArbZorn.unitZorn) { x ->
            val inverse = ring.reciprocal(x)
            check(eq(ring.mul(x, inverse), ring.one))
            check(eq(ring.mul(inverse, x), ring.one))
        }
    }

    "the null cone is not invertible" {
        ring.hasReciprocal(ring.basis.first()) shouldBe false
        ring.hasReciprocal(ring.zero) shouldBe false
    }

    "𝕆′ ≅ Zorn<Real>: both directions are unital homomorphisms and mutually inverse" {
        val iso = ZornAlgebras.RealSplitOctonionZornIsomorphism
        val eqO = SplitOctonionAlgebras.eqSplitOctonion
        val prO = SplitOctonionAlgebras.printableSplitOctonionPretty

        NonAssociativeRingHomomorphismLaws(
            hom = iso::invoke,
            domain = SplitOctonionAlgebras.RealSplitOctonionAlgebra,
            codomain = ring,
            arb = ArbSplitOctonion.smallSplitOctonion,
            eqB = eq,
            prA = prO,
            prB = pr
        ).fullTest().throwIfFailed()

        NonAssociativeRingHomomorphismLaws(
            hom = iso.backward::invoke,
            domain = ring,
            codomain = SplitOctonionAlgebras.RealSplitOctonionAlgebra,
            arb = arb,
            eqB = eqO,
            prA = pr,
            prB = prO
        ).fullTest().throwIfFailed()

        checkAll(ArbSplitOctonion.smallSplitOctonion) { o ->
            check(eqO(iso.backward(iso(o)), o))
            check(RealAlgebras.eqRealApprox(ring.normSq(iso(o)), SplitOctonionAlgebras.RealSplitOctonionAlgebra.normSq(o)))
        }
        checkAll(arb) { x ->
            check(eq(iso(iso.backward(x)), x))
        }
    }

    "the real printables" {
        val x = Zorn(1.5, Vec3(0.0, -2.0, 3.25), Vec3(-1.0, 0.0, 0.5), -4.0)
        ZornAlgebras.printableZornRealStrict(x) shouldBe
            "[[1.5, (0, -2.0, 3.25)], [(-1.0, 0, 0.5), -4.0]]"
        ZornAlgebras.printableZornRealPretty(x) shouldBe ZornAlgebras.printableZornPretty(RealAlgebras.printableRealPretty)(x)
        ZornAlgebras.printableZornReal(x) shouldBe ZornAlgebras.printableZorn(RealAlgebras.printableReal)(x)
    }
})
