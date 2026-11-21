package org.vorpal.kosmos.geometry.lattices

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.quaternion.ArbHurwitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.ArbLipschitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.HurwitzQuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternionAlgebras
import org.vorpal.kosmos.laws.LawReport
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.leaves
import org.vorpal.kosmos.laws.property.BilinearFormLaw
import org.vorpal.kosmos.laws.property.BilinearityLaw
import org.vorpal.kosmos.laws.property.LeftBilinearFormLaw
import org.vorpal.kosmos.laws.property.RightBilinearFormLaw
import org.vorpal.kosmos.laws.property.SymmetryLaw
import org.vorpal.kosmos.laws.verify
import org.vorpal.kosmos.numberfields.quadratic.EisensteinInt
import org.vorpal.kosmos.numberfields.quadratic.EisensteinIntAlgebras
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import java.math.BigInteger

/*
 * The inner products of Euclidean lattices are symmetric and ℤ-bilinear. Lipschitz quaternions take integer values,
 * so their form is a bilinear form over ℤ; the Eisenstein and Hurwitz forms take values in ½ℤ ⊂ ℚ, so they are
 * ℤ-bilinear maps into ℚ.
 */

private val Z = IntegerAlgebras.IntegerCommutativeRing
private val Q = RationalAlgebras.RationalField
private val scalars: Arb<BigInteger> = Arb.int(-6..6).map(Int::toBigInteger)

/** ℤ acting on ℚ by multiplication. */
private val zOnQ: LeftAction<BigInteger, Rational> = LeftAction("·") { n, r -> Rational.of(n) * r }

private val eisenstein: Arb<EisensteinInt> =
    Arb.pair(Arb.int(-20..20), Arb.int(-20..20)).map { (a, b) -> EisensteinInt(a.toBigInteger(), b.toBigInteger()) }

private val lipschitz = LipschitzQuaternionAlgebras.LipschitzQuaternionLattice
private val hurwitz = HurwitzQuaternionAlgebras.HurwitzQuaternionLattice
private val eisensteinLattice = EisensteinIntAlgebras.EisensteinIntLattice
private val lq = ArbLipschitzQuaternion.lipschitzQuaternion

/** `f(p, q) = p.w · q.x`: bilinear, but not symmetric. */
private val skewed: (LipschitzQuaternion, LipschitzQuaternion) -> BigInteger = { p, q -> p.w * q.x }

/** `f(p, q) = N(p + q)`: symmetric, but quadratic rather than bilinear. */
private val notBilinear: (LipschitzQuaternion, LipschitzQuaternion) -> BigInteger = { p, q ->
    lipschitz.dot(lipschitz.addV.op(p, q), lipschitz.addV.op(p, q))
}

private fun LawReport.failingLabels(): Set<String> =
    root.leaves().filter { it.status.isProblem }.map { it.path.toString() }.toSet()

class EuclideanLatticeSpec : FunSpec({
    lawTests(lawGraph("Euclidean lattices") {

        branch("Lipschitz quaternions ℤ⁴") {
            suite(BilinearFormLaw(lipschitz.dot::invoke, Z.add.op, Z.mul.op, lipschitz.addV.op, lipschitz.scale, scalars, lq))
            law(SymmetryLaw(lipschitz.dot::invoke, lq))
        }

        branch("Hurwitz quaternions (the D₄ lattice)") {
            suite(BilinearityLaw(
                hurwitz.dot::invoke, Q.add.op, hurwitz.addV.op, hurwitz.addV.op,
                hurwitz.scale, hurwitz.scale, zOnQ,
                scalars, ArbHurwitzQuaternion.hurwitzQuaternion, ArbHurwitzQuaternion.hurwitzQuaternion
            ))
            law(SymmetryLaw(hurwitz.dot::invoke, ArbHurwitzQuaternion.hurwitzQuaternion))
        }

        branch("Eisenstein integers (the A₂ lattice)") {
            suite(BilinearityLaw(
                eisensteinLattice.dot::invoke, Q.add.op, eisensteinLattice.addV.op, eisensteinLattice.addV.op,
                eisensteinLattice.scale, eisensteinLattice.scale, zOnQ,
                scalars, eisenstein, eisenstein
            ))
            law(SymmetryLaw(eisensteinLattice.dot::invoke, eisenstein))
            local("⟨1, ω⟩ = −1/2: the basis is not orthogonal") {
                eisensteinLattice.dot(EisensteinInt(BigInteger.ONE, BigInteger.ZERO), EisensteinInt(BigInteger.ZERO, BigInteger.ONE)) shouldBe
                    Rational.of(-1, 2)
            }
        }

        branch("forms that fall short") {
            local("p.w · q.x is bilinear but not symmetric") {
                BilinearFormLaw(skewed, Z.add.op, Z.mul.op, lipschitz.addV.op, lipschitz.scale, scalars, lq).verify()
                    .failingLabels() shouldBe emptySet()
                SymmetryLaw(skewed, lq).verify().failingLabels().single() shouldContain "symmetry"
            }
            local("N(p + q) is symmetric but not linear in either argument") {
                SymmetryLaw(notBilinear, lq).verify().failingLabels() shouldBe emptySet()
                LeftBilinearFormLaw(notBilinear, Z.add.op, Z.mul.op, lipschitz.addV.op, lipschitz.scale, scalars, lq)
                    .verify().failingLabels().isNotEmpty() shouldBe true
                RightBilinearFormLaw(notBilinear, Z.add.op, Z.mul.op, lipschitz.addV.op, lipschitz.scale, scalars, lq)
                    .verify().failingLabels().isNotEmpty() shouldBe true
            }
        }
    })
})
