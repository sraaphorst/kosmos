package org.vorpal.kosmos.hypercomplex.zorn

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.splitoctonion.ArbSplitOctonion
import org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonionAlgebras
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.AlternativityLaw
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.linear.values.Vec3
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * The real Zorn vector-matrices, checked up to approximate equality.
 *
 * The exact algebraic facts are in [ZornSpec], [ZornStructuresSpec] and [ZornSplitOctonionSpec].
 */
class RealZornSpec : FunSpec({
    lawTests(lawGraph("Zorn over ℝ") {
        val ring = ZornAlgebras.RealZornAlgebra
        val eq = ZornAlgebras.eqZorn
        val pr = ZornAlgebras.printableZornRealPretty
        val arb = ArbZorn.smallZorn

        branch("RealZornStarAlgebra satisfies NonAssociativeStarAlgebraLaws") {
            suite(NonAssociativeStarAlgebraLaws(
                algebra = ZornAlgebras.RealZornStarAlgebra,
                scalarArb = ArbReal.smallReal,
                algebraArb = arb,
                eqR = RealAlgebras.eqRealApprox,
                eqA = eq,
                prR = RealAlgebras.printableRealPretty,
                prA = pr
            ))
        }

        branch("ZornRealVectorSpace satisfies VectorSpaceLaws and has dimension 8") {
            suite(VectorSpaceLaws(
                space = ZornAlgebras.ZornRealVectorSpace,
                scalarArb = ArbReal.smallReal,
                vectorArb = arb,
                eqF = RealAlgebras.eqRealApprox,
                eqV = eq,
                prF = RealAlgebras.printableRealPretty,
                prV = pr
            ))
            local("dimension 8") {
                ZornAlgebras.ZornRealVectorSpace.dimension shouldBe 8
            }
        }

        branch("real Zorn multiplication is alternative") {
            suite(AlternativityLaw(ring.mul.op, arb, eq, pr))
        }

        law("the real Zorn norm is multiplicative", NormSqMultiplicativeLaw(
                mulA = ring.mul.op,
                normSq = ring.normSq,
                mulN = RealAlgebras.RealField.mul.op,
                arbA = arb,
                eqN = RealAlgebras.eqRealApprox,
                prA = pr,
                prN = RealAlgebras.printableRealPretty
            ))

        local("the real basis: E_a and E_d are complementary idempotents and every basis element has norm 0") {
            ring.basis.size shouldBe 8
            val (ea, ed) = ring.basis.first() to ring.basis.last()
            lawCheck(eq(ring.mul(ea, ea), ea))
            lawCheck(eq(ring.mul(ed, ed), ed))
            lawCheck(eq(ring.mul(ea, ed), ring.zero))
            lawCheck(eq(ring.add(ea, ed), ring.one))
            ring.basis.forEach { e -> ring.normSq(e) shouldBe 0.0 }
        }

        local("reciprocal: X·X⁻¹ = X⁻¹·X = 1 away from the null cone") {
            lawCheckAll(ArbZorn.unitZorn) { x ->
                val inverse = ring.reciprocal(x)
                lawCheck(eq(ring.mul(x, inverse), ring.one))
                lawCheck(eq(ring.mul(inverse, x), ring.one))
            }
        }

        local("the null cone is not invertible") {
            ring.hasReciprocal(ring.basis.first()) shouldBe false
            ring.hasReciprocal(ring.zero) shouldBe false
        }

        branch("𝕆′ ≅ Zorn<Real>: both directions are unital homomorphisms and mutually inverse") {
            val iso = ZornAlgebras.RealSplitOctonionZornIsomorphism
            val eqO = SplitOctonionAlgebras.eqSplitOctonion
            val prO = SplitOctonionAlgebras.printableSplitOctonionPretty

            suite(NonAssociativeRingHomomorphismLaws(
                hom = iso::invoke,
                domain = SplitOctonionAlgebras.RealSplitOctonionAlgebra,
                codomain = ring,
                arb = ArbSplitOctonion.smallSplitOctonion,
                eqB = eq,
                prA = prO,
                prB = pr
            ))
            suite(NonAssociativeRingHomomorphismLaws(
                hom = iso.backward::invoke,
                domain = ring,
                codomain = SplitOctonionAlgebras.RealSplitOctonionAlgebra,
                arb = arb,
                eqB = eqO,
                prA = pr,
                prB = prO
            ))
            local("mutually inverse, and the norm is preserved") {
                lawCheckAll(ArbSplitOctonion.smallSplitOctonion) { o ->
                    lawCheck(eqO(iso.backward(iso(o)), o))
                    lawCheck(RealAlgebras.eqRealApprox(ring.normSq(iso(o)), SplitOctonionAlgebras.RealSplitOctonionAlgebra.normSq(o)))
                }
                lawCheckAll(arb) { x ->
                    lawCheck(eq(iso(iso.backward(x)), x))
                }
            }
        }

        local("the real printables") {
            val x = Zorn(1.5, Vec3(0.0, -2.0, 3.25), Vec3(-1.0, 0.0, 0.5), -4.0)
            ZornAlgebras.printableZornRealStrict(x) shouldBe
                "[[1.5, (0, -2.0, 3.25)], [(-1.0, 0, 0.5), -4.0]]"
            ZornAlgebras.printableZornRealPretty(x) shouldBe ZornAlgebras.printableZornPretty(RealAlgebras.printableRealPretty)(x)
            ZornAlgebras.printableZornReal(x) shouldBe ZornAlgebras.printableZorn(RealAlgebras.printableReal)(x)
        }
    })
})
