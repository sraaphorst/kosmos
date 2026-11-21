package org.vorpal.kosmos.linear.instances

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import java.math.BigInteger
import org.vorpal.kosmos.algebra.structures.PrimeField
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.NaturalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.math.Natural
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.hypercomplex.quaternion.ArbRationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.splitquaternion.ArbSplitQuaternion
import org.vorpal.kosmos.hypercomplex.splitquaternion.SplitQuaternionAlgebras
import org.vorpal.kosmos.laws.algebra.AlgebraLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.LeftRModuleLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.algebra.RSBiModuleLaws
import org.vorpal.kosmos.laws.algebra.RightRModuleLaws
import org.vorpal.kosmos.laws.algebra.RingLaws
import org.vorpal.kosmos.laws.algebra.SemiringLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.ops.FixedMatOps
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.arbMat2
import org.vorpal.kosmos.linear.values.arbVec2
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * The structures in [FixedMatAlgebras], checked exactly:
 * - `M₂` as a semiring over ℕ, a ring over ℤ, 𝔽₅ and the (non-commuting) rational quaternions,
 *   an algebra and a star algebra (under the adjugate) over ℤ and ℚ, a module and a vector space;
 * - `Vec2` as a left module (columns), a right module (rows) and an `(R, M₂(R))`-bimodule;
 * - the units over a field, counted exhaustively over 𝔽₃ against `|GL₂(𝔽₃)| = 48`;
 * - the scalar embedding, the identification with 2 × 2 `DenseMat`s, and the split-quaternion
 *   picture: the `SplitQuaternion → Mat2` embedding sends conjugation to the adjugate and the
 *   norm to the determinant.
 */
class FixedMatAlgebrasSpec : FunSpec({
    lawTests(lawGraph("fixed-size matrix algebras") {
        val z = IntegerAlgebras.IntegerCommutativeRing
        val arbZ = Arb.int(-20..20).map(Int::toBigInteger)
        val arbMZ = arbMat2(arbZ)

        val q = RationalAlgebras.RationalField
        val eqQ = RationalAlgebras.eqRational
        val arbQ = ArbRational.small
        val arbMQ = arbMat2(arbQ)

        val f5 = PrimeField(BigInteger.valueOf(5))
        val arbF5 = Arb.int(0..4).map(Int::toBigInteger)

        // ---------- The matrices ----------

        branch("M₂(ℕ) satisfies SemiringLaws") {
            val n = NaturalAlgebras.NaturalCommutativeSemiring
            val arbN = Arb.int(0..20).map { Natural(it.toBigInteger()) }
            suite(SemiringLaws(FixedMatAlgebras.mat2Semiring(n), arbMat2(arbN)))
        }

        branch("M₂(ℤ) and M₂(𝔽₅) satisfy RingLaws") {
            suite(RingLaws(FixedMatAlgebras.mat2Ring(z), arbMZ))
            suite(RingLaws(FixedMatAlgebras.mat2Ring(f5), arbMat2(arbF5)))
        }

        branch("M₂ over the rational quaternions, whose entries do not commute, satisfies RingLaws") {
            val h = QuaternionAlgebras.quaternionRing(q)
            suite(RingLaws(FixedMatAlgebras.mat2Ring(h), arbMat2(ArbRationalQuaternion.smallRationalQuaternion)))
        }

        branch("M₂(ℤ) satisfies RModuleLaws and M₂(ℚ) satisfies VectorSpaceLaws") {
            suite(RModuleLaws(FixedMatAlgebras.mat2Module(z), arbZ, arbMZ))
            suite(VectorSpaceLaws(FixedMatAlgebras.mat2Space(q), arbQ, arbMQ, eqF = eqQ))
            local("dimension 4") {
                FixedMatAlgebras.mat2Space(q).dimension shouldBe 4
            }
        }

        branch("M₂(ℤ) and M₂(ℚ) satisfy AlgebraLaws") {
            suite(AlgebraLaws(FixedMatAlgebras.mat2Algebra(z), arbZ, arbMZ))
            suite(AlgebraLaws(FixedMatAlgebras.mat2Algebra(q), arbQ, arbMQ, eqR = eqQ))
        }

        branch("under the adjugate, M₂(ℤ), M₂(ℚ) and M₂(𝔽₅) satisfy StarAlgebraLaws") {
            suite(StarAlgebraLaws(FixedMatAlgebras.mat2StarAlgebra(z), arbZ, arbMZ))
            suite(StarAlgebraLaws(FixedMatAlgebras.mat2StarAlgebra(q), arbQ, arbMQ, eqR = eqQ))
            suite(StarAlgebraLaws(FixedMatAlgebras.mat2StarAlgebra(f5), arbF5, arbMat2(arbF5)))
        }

        local("the norm is the determinant: M · conj(M) = conj(M) · M = N(M)·1, and N is multiplicative") {
            val star = FixedMatAlgebras.mat2StarAlgebra(z)
            val normSq = FixedMatAlgebras.normSq(z)
            lawCheckAll(arbMZ, arbMZ) { m, n ->
                val scalarN = star.leftAction(normSq(m), star.one)
                star.mul(m, star.conj(m)) shouldBe scalarN
                star.mul(star.conj(m), m) shouldBe scalarN
                normSq(star.mul(m, n)) shouldBe normSq(m) * normSq(n)
            }
        }

        local("the matrix units multiply as EᵢⱼEₖₗ = δⱼₖEᵢₗ") {
            val ring = FixedMatAlgebras.mat2Ring(z)
            val basis = FixedMatAlgebras.basis(z)
            val indices = listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1)
            for ((x, ij) in basis.zip(indices)) {
                for ((y, kl) in basis.zip(indices)) {
                    val expected =
                        if (ij.second == kl.first) basis[indices.indexOf(ij.first to kl.second)]
                        else ring.zero
                    ring.mul(x, y) shouldBe expected
                }
            }
            val one = BigInteger.ONE
            basis.fold(ring.zero, ring.add::invoke) shouldBe Mat2(one, one, one, one)
        }

        // ---------- Units ----------

        local("over ℚ, M·M⁻¹ = M⁻¹·M = I on the units, and a singular matrix throws ArithmeticException") {
            val units = FixedMatAlgebras.mat2RingWithReciprocal(q)
            val invertible = arbMQ.filter { FixedMatOps.det(q, it) != Rational.ZERO }
            lawCheckAll(invertible) { m ->
                val inv = units.reciprocal(m)
                units.mul(m, inv) shouldBe units.one
                units.mul(inv, m) shouldBe units.one
            }
            val singular = Mat2(Rational.ONE, Rational.of(2), Rational.of(2), Rational.of(4))
            units.hasReciprocal(singular) shouldBe false
            shouldThrow<ArithmeticException> { units.reciprocal(singular) }
        }

        local("over 𝔽₃ there are exactly |GL₂(𝔽₃)| = (3² − 1)(3² − 3) = 48 units, each with a two-sided inverse") {
            val f3 = PrimeField(BigInteger.valueOf(3))
            val units = FixedMatAlgebras.mat2RingWithReciprocal(f3)
            val elements = (0..2).map(Int::toBigInteger)
            val all = elements.flatMap { a -> elements.flatMap { b -> elements.flatMap { c -> elements.map { d ->
                Mat2(a, b, c, d)
            } } } }
            all.size shouldBe 81
            val invertible = all.filter(units::hasReciprocal)
            invertible.size shouldBe 48
            invertible.forEach { m ->
                units.mul(m, units.reciprocal(m)) shouldBe units.one
                units.mul(units.reciprocal(m), m) shouldBe units.one
            }
        }

        // ---------- Vec2 over Mat2 ----------

        branch("column vectors over ℤ and over the rational quaternions form a left M₂-module") {
            suite(LeftRModuleLaws(FixedMatAlgebras.vec2LeftMat2Module(z), arbMZ, arbVec2(arbZ)))
            val h = QuaternionAlgebras.quaternionRing(q)
            val arbH = ArbRationalQuaternion.smallRationalQuaternion
            suite(LeftRModuleLaws(FixedMatAlgebras.vec2LeftMat2Module(h), arbMat2(arbH), arbVec2(arbH)))
        }

        branch("row vectors over ℤ and over the rational quaternions form a right M₂-module") {
            suite(RightRModuleLaws(FixedMatAlgebras.vec2RightMat2Module(z), arbVec2(arbZ), arbMZ))
            val h = QuaternionAlgebras.quaternionRing(q)
            val arbH = ArbRationalQuaternion.smallRationalQuaternion
            suite(RightRModuleLaws(FixedMatAlgebras.vec2RightMat2Module(h), arbVec2(arbH), arbMat2(arbH)))
        }

        branch("row vectors over ℤ form a (ℤ, M₂(ℤ))-bimodule") {
            suite(RSBiModuleLaws(FixedMatAlgebras.vec2Mat2BiModule(z), arbZ, arbVec2(arbZ), arbMZ))
        }

        // ---------- Morphisms ----------

        branch("the scalar embedding ℤ ↪ M₂(ℤ) is an injective unital ring homomorphism onto the center") {
            val embed = FixedMatAlgebras.scalarEmbedding(z)
            val ring = FixedMatAlgebras.mat2Ring(z)
            suite(RingHomomorphismLaws(hom = embed::invoke, domain = z, codomain = ring, arb = arbZ))
            law(injectivityLaw(hom = embed::invoke, arbA = arbZ))
            local("the image is central") {
                lawCheckAll(arbZ, arbMZ) { r, m ->
                    ring.mul(embed(r), m) shouldBe ring.mul(m, embed(r))
                }
            }
        }

        branch("Mat2 ≅ 2 × 2 DenseMat: both directions are unital ring homomorphisms, and mutually inverse") {
            val iso = FixedMatAlgebras.denseMatIsomorphism(z)
            val arbDense = arbMZ.map { m -> iso(m) }
            suite(RingHomomorphismLaws(hom = iso::invoke, domain = iso.domain, codomain = iso.codomain, arb = arbMZ))
            suite(RingHomomorphismLaws(
                hom = iso.backward::invoke,
                domain = iso.codomain,
                codomain = iso.domain,
                arb = arbDense
            ))
            local("mutually inverse, and a wrong size is rejected") {
                lawCheckAll(arbMZ) { m -> iso.backward(iso(m)) shouldBe m }
                lawCheckAll(arbDense) { d -> iso(iso.backward(d)) shouldBe d }
                shouldThrow<IllegalArgumentException> {
                    iso.backward(DenseMat.ofRows(listOf(listOf(BigInteger.ONE))))
                }
            }
        }

        branch("SplitQuaternion<ℚ> ↪ Mat2<ℚ> is a ring homomorphism sending conj to adj and N to det") {
            val phi = SplitQuaternionAlgebras.splitQuaternionToRank2MatrixHomomorphism(q)
            val star = FixedMatAlgebras.mat2StarAlgebra(q)
            val splitQuaternions = SplitQuaternionAlgebras.splitQuaternionRing(q)
            val normSq = SplitQuaternionAlgebras.normSq(q)
            val arb = ArbSplitQuaternion.rationalSplitQuaternion

            suite(RingHomomorphismLaws(hom = phi::invoke, domain = splitQuaternions, codomain = star, arb = arb))
            suite(InvolutiveRingLaws(star, arbMQ))
            local("conj ↦ adj and N ↦ det") {
                lawCheckAll(arb) { x ->
                    phi(splitQuaternions.conj(x)) shouldBe star.conj(phi(x))
                    FixedMatAlgebras.normSq(q)(phi(x)) shouldBe normSq(x)
                }
            }
        }

        // ---------- Equality and printing ----------

        local("eqMat2 compares entrywise, and the printables render row by row") {
            val eq = FixedMatAlgebras.eqMat2(eqQ)
            eq(Mat2(Rational.of(1, 2), Rational.ZERO, Rational.ZERO, Rational.ONE),
                Mat2(Rational.of(2, 4), Rational.ZERO, Rational.ZERO, Rational.ONE)) shouldBe true
            eq(Mat2(Rational.ONE, Rational.ZERO, Rational.ZERO, Rational.ONE),
                Mat2(Rational.ZERO, Rational.ONE, Rational.ZERO, Rational.ONE)) shouldBe false

            val pr = Printable<Int> { it.toString() }
            FixedMatAlgebras.printableMat2(pr)(Mat2(1, -2, 10, 3)) shouldBe "[[1, -2], [10, 3]]"
            FixedMatAlgebras.printableMat2Pretty(pr)(Mat2(1, -2, 10, 3)) shouldBe
                "⎛  1  -2 ⎞\n" +
                "⎝ 10   3 ⎠"
        }
    })
})
