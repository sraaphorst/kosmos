package org.vorpal.kosmos.algebra.structures

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.dual.ArbDual
import org.vorpal.kosmos.hypercomplex.dual.Dual
import org.vorpal.kosmos.hypercomplex.dual.DualAlgebras
import org.vorpal.kosmos.hypercomplex.octonion.ArbRationalOctonion
import org.vorpal.kosmos.hypercomplex.octonion.OctonionAlgebras
import org.vorpal.kosmos.hypercomplex.quaternion.ArbLipschitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.Quaternion
import org.vorpal.kosmos.hypercomplex.quaternion.QuaternionAlgebras
import org.vorpal.kosmos.hypercomplex.sextonion.ArbSextonion
import org.vorpal.kosmos.hypercomplex.sextonion.Sextonion
import org.vorpal.kosmos.hypercomplex.sextonion.SextonionAlgebras
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplex
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplexAlgebras
import org.vorpal.kosmos.laws.LawReport
import org.vorpal.kosmos.laws.algebra.RngLaws
import org.vorpal.kosmos.laws.homomorphism.RngHomomorphismLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.leaves
import org.vorpal.kosmos.laws.property.AssociativityLaw
import org.vorpal.kosmos.laws.property.CommutativityLaw
import org.vorpal.kosmos.laws.property.JordanIdentityLaw
import org.vorpal.kosmos.laws.property.LeftNoZeroDivisorsLaw
import org.vorpal.kosmos.laws.property.MedialLaw
import org.vorpal.kosmos.laws.property.NilpotentLaw
import org.vorpal.kosmos.laws.property.NoZeroDivisorsLaw
import org.vorpal.kosmos.laws.property.NormLaw
import org.vorpal.kosmos.laws.property.PowerAssociativityLaw
import org.vorpal.kosmos.laws.property.RightNoZeroDivisorsLaw
import org.vorpal.kosmos.laws.verify
import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.Vec2
import org.vorpal.kosmos.numberfields.quadratic.ArbGaussianInt
import org.vorpal.kosmos.numberfields.quadratic.GaussianIntAlgebras
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import java.math.BigInteger

/*
 * Identities weaker or stranger than associativity (Jordan, medial, power-associative), nilpotency, zero divisors,
 * norms, and rng homomorphisms: each checked on a structure that has the property and rejected on one that does not.
 */

private val Z = IntegerAlgebras.IntegerCommutativeRing
private val Q = RationalAlgebras.RationalField
private val smallInts: Arb<BigInteger> = Arb.int(-20..20).map(Int::toBigInteger)
private fun q(n: Int): Rational = Rational.of(n)

private val lipschitz = QuaternionAlgebras.quaternionRing(Z)
private val rationalOctonions = OctonionAlgebras.octonionRing(Q)
private val rationalSextonions = SextonionAlgebras.sextonionRing(Q)
private val rationalDuals = DualAlgebras.dualRing(Q)

/** The Jordan product `a ∘ b = ab + ba` (twice the usual one, so it stays integral; the identity is unaffected). */
private val jordanLipschitz: BinOp<Quaternion<BigInteger>> = BinOp("∘") { a, b ->
    lipschitz.add.op(lipschitz.mul.op(a, b), lipschitz.mul.op(b, a))
}

/** A commutative operation on ℤ that is neither Jordan nor power-associative: `a ∘ b = ab + 1`. */
private val abPlusOne: BinOp<BigInteger> = BinOp("∘₁") { a, b -> a * b + BigInteger.ONE }

/** The midpoint `(a + b) / 2` on ℚ: medial, commutative, idempotent, not associative. */
private val midpoint: BinOp<Rational> = BinOp("mid") { a, b -> (a + b) * Rational.of(1, 2) }

/** Subtraction on ℤ: medial, although neither commutative nor associative (Toyoda–Bruck: x − y = α(x) + β(y)). */
private val minus: BinOp<BigInteger> = BinOp("−") { a, b -> a - b }

/** The nilpotent ideal εℚ of the dual numbers. */
private val epsilonQ: Arb<Dual<Rational>> = ArbRational.small.map { Dual(Rational.ZERO, it) }

/** The radical V = {(0, ν)} of the sextonions over ℚ. */
private val sextonionRadical: Arb<Sextonion<Rational>> = Arb.pair(ArbRational.small, ArbRational.small).map { (x, y) ->
    Sextonion(Mat2(Rational.ZERO, Rational.ZERO, Rational.ZERO, Rational.ZERO), Vec2(x, y))
}

/** Exact small reals (integers as doubles), so N(a) = 0 only at a = 0. */
private val smallExactReal: Arb<Double> = Arb.int(-3..3).map(Int::toDouble)
private val exactQuaternions = Arb.bind(smallExactReal, smallExactReal, smallExactReal, smallExactReal) { w, x, y, z ->
    Quaternion(w, x, y, z)
}
private val exactComplex = Arb.bind(smallExactReal, smallExactReal) { re, im -> Complex(re, im) }
private val exactSplitComplex = Arb.bind(smallExactReal, smallExactReal) { re, hy -> SplitComplex(re, hy) }
private val exactDual = Arb.bind(smallExactReal, smallExactReal) { a, b -> Dual(a, b) }

/** 2ℤ, the even integers: a rng with the operations of ℤ and no multiplicative identity. */
private val twoZ: Rng<BigInteger> = Rng.of(Z.add, Z.mul)
private val evens: Arb<BigInteger> = smallInts.map { it * BigInteger.TWO }

private fun LawReport.failingLabels(): Set<String> =
    root.leaves().filter { it.status.isProblem }.map { it.path.toString() }.toSet()

class AlgebraicPropertiesSpec : FunSpec({
    lawTests(lawGraph("algebraic properties") {

        branch("Jordan identity: x ∘ (y ∘ x²) = (x ∘ y) ∘ x²") {
            branch("the Jordan product ab + ba on the Lipschitz quaternions is a Jordan algebra") {
                law(CommutativityLaw(jordanLipschitz, ArbLipschitzQuaternion.lipschitzQuaternion))
                law(JordanIdentityLaw(jordanLipschitz, ArbLipschitzQuaternion.lipschitzQuaternion))
                law(PowerAssociativityLaw(jordanLipschitz, ArbLipschitzQuaternion.lipschitzQuaternion))
            }
            local("the Jordan product is not associative") {
                AssociativityLaw(jordanLipschitz, ArbLipschitzQuaternion.lipschitzQuaternion).verify().failingLabels()
                    .single() shouldContain "associativity"
            }
            branch("alternative algebras satisfy it (Artin: x and y generate an associative subalgebra)") {
                law(JordanIdentityLaw(rationalOctonions.mul.op, ArbRationalOctonion.smallRationalOctonion))
                law(JordanIdentityLaw(rationalSextonions.mul.op, ArbSextonion.rationalSextonion))
            }
            local("ab + 1 on ℤ is commutative but not Jordan") {
                JordanIdentityLaw(abPlusOne, smallInts).verify().failingLabels().single() shouldContain "jordan"
            }
        }

        branch("medial: (a ∘ b) ∘ (c ∘ d) = (a ∘ c) ∘ (b ∘ d)") {
            law(MedialLaw(Z.add.op, smallInts))
            law(MedialLaw(midpoint, ArbRational.small))
            law(MedialLaw(minus, smallInts))
            local("subtraction is medial without being commutative or associative") {
                CommutativityLaw(minus, smallInts).verify().failingLabels().size shouldBe 1
                AssociativityLaw(minus, smallInts).verify().failingLabels().size shouldBe 1
            }
            local("quaternion multiplication is not medial: it needs bc = cb") {
                MedialLaw(lipschitz.mul.op, ArbLipschitzQuaternion.lipschitzQuaternion).verify().failingLabels()
                    .single() shouldContain "medial"
            }
        }

        branch("power-associativity: (xᵐ)(xⁿ) = xᵐ⁺ⁿ") {
            law(PowerAssociativityLaw(rationalOctonions.mul.op, ArbRationalOctonion.smallRationalOctonion))
            law(PowerAssociativityLaw(rationalSextonions.mul.op, ArbSextonion.rationalSextonion))
            local("ab + 1 on ℤ is not power-associative: x²x² ≠ x⁴") {
                PowerAssociativityLaw(abPlusOne, smallInts).verify().failingLabels().single() shouldContain
                    "power-associativity"
            }
        }

        branch("nilpotency") {
            law(NilpotentLaw(rationalDuals.mul.op, 2, rationalDuals.zero, epsilonQ))
            law(NilpotentLaw(rationalSextonions.mul.op, 2, rationalSextonions.zero, sextonionRadical))
            local("the dual numbers are not nilpotent (1 · 1 = 1), and εℚ has index exactly 2, not 3") {
                NilpotentLaw(rationalDuals.mul.op, 2, rationalDuals.zero, ArbDual.rationalDual).verify()
                    .failingLabels().size shouldBe 1
                NilpotentLaw(rationalDuals.mul.op, 3, rationalDuals.zero, epsilonQ).verify()
                    .failingLabels().size shouldBe 1
            }
        }

        branch("no zero divisors") {
            branch("ℤ") {
                law(NoZeroDivisorsLaw(Z.mul.op, Z.zero, smallInts))
                law(LeftNoZeroDivisorsLaw(Z.mul.op, Z.zero, smallInts))
                law(RightNoZeroDivisorsLaw(Z.mul.op, Z.zero, smallInts))
            }
            branch("the Gaussian integers") {
                val gi = GaussianIntAlgebras.GaussianIntEuclideanDomain
                law(NoZeroDivisorsLaw(gi.mul.op, gi.zero, ArbGaussianInt.small))
            }
            branch("the rational octonions (a division algebra)") {
                law(NoZeroDivisorsLaw(rationalOctonions.mul.op, rationalOctonions.zero, ArbRationalOctonion.smallRationalOctonion))
                law(LeftNoZeroDivisorsLaw(rationalOctonions.mul.op, rationalOctonions.zero, ArbRationalOctonion.smallRationalOctonion))
                law(RightNoZeroDivisorsLaw(rationalOctonions.mul.op, rationalOctonions.zero, ArbRationalOctonion.smallRationalOctonion))
            }
            local("zero divisors: ε · ε = 0, (1 + j)(1 − j) = 0, and the sextonion radical squares to zero") {
                val eps = Arb.element(Dual(Rational.ZERO, q(1)), Dual(Rational.ZERO, q(2)))
                NoZeroDivisorsLaw(rationalDuals.mul.op, rationalDuals.zero, eps).verify().failingLabels().size shouldBe 1

                val split = SplitComplexAlgebras.splitComplexRing(Z)
                val oneJ = SplitComplex(BigInteger.ONE, BigInteger.ONE)
                val oneMinusJ = SplitComplex(BigInteger.ONE, BigInteger.ONE.negate())
                split.mul.op(oneJ, oneMinusJ) shouldBe split.zero
                NoZeroDivisorsLaw(split.mul.op, split.zero, Arb.element(oneJ, oneMinusJ)).verify()
                    .failingLabels().size shouldBe 1

                val nu = Arb.element(
                    Sextonion(Mat2(q(0), q(0), q(0), q(0)), Vec2(q(1), q(0))),
                    Sextonion(Mat2(q(0), q(0), q(0), q(0)), Vec2(q(0), q(1)))
                )
                NoZeroDivisorsLaw(rationalSextonions.mul.op, rationalSextonions.zero, nu).verify()
                    .failingLabels().size shouldBe 1
            }
        }

        branch("norms: N(a) ≥ 0 and N(a) = 0 ⇔ a = 0") {
            law(NormLaw(QuaternionAlgebras.QuaternionDivisionRing.normSq, QuaternionAlgebras.QuaternionDivisionRing.zero, exactQuaternions))
            law(NormLaw(ComplexAlgebras.normSq(RealAlgebras.RealField), ComplexAlgebras.ComplexField.zero, exactComplex))
            local("the split-complex norm a² − b² is indefinite, and the dual norm a² is degenerate") {
                val splitZero = SplitComplexAlgebras.RealSplitComplexRing.zero
                NormLaw(SplitComplexAlgebras.splitComplexNormSq, splitZero, exactSplitComplex).verify()
                    .failingLabels().size shouldBe 1

                val dualNorm = DualAlgebras.normSq(RealAlgebras.RealField)
                dualNorm(Dual(0.0, 1.0)) shouldBe 0.0
                NormLaw(dualNorm, DualAlgebras.RealDualRing.zero, exactDual).verify().failingLabels().size shouldBe 1
            }
        }

        branch("rng homomorphisms") {
            branch("2ℤ is a rng") { suite(RngLaws(twoZ, evens)) }
            branch("2ℤ ↪ ℤ") { suite(RngHomomorphismLaws({ it }, twoZ, Z, evens)) }
            local("x ↦ 2x on ℤ preserves addition and 0 but not multiplication") {
                val failing = RngHomomorphismLaws({ x: BigInteger -> x * BigInteger.TWO }, Z, Z, smallInts).verify()
                    .failingLabels()
                failing.single() shouldContain "multiplicat"
            }
        }
    })
})
