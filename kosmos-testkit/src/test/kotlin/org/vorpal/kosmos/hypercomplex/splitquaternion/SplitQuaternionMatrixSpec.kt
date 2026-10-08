package org.vorpal.kosmos.hypercomplex.splitquaternion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.FiniteFieldAlgebras
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.linear.values.DenseMat
import java.math.BigInteger

/**
 * The split-quaternions are a matrix algebra: `SplitQuaternion<F> ≅ M₂(F)` when 2 is invertible.
 *
 * Checked exactly over ℚ: the map is a unital ring homomorphism, its stated inverse is a two-sided
 * inverse and is itself a homomorphism, and the norm is the determinant. Over ℤ the map is injective
 * but not surjective, and over 𝔽₂ the isomorphism is refused.
 */
class SplitQuaternionMatrixSpec : StringSpec({
    val q = RationalAlgebras.RationalField
    val ring = SplitQuaternionAlgebras.splitQuaternionRing(q)
    val normSq = SplitQuaternionAlgebras.normSq(q)
    val mono = SplitQuaternionAlgebras.splitQuaternionToRank2MatrixMonomorphism(q)
    val iso = SplitQuaternionAlgebras.splitQuaternionMatrixIsomorphism(q)
    val matrices = DenseMatAlgebras.DenseMatRing(q, 2)
    val arb = ArbSplitQuaternion.rationalSplitQuaternion

    val arbMatrix: Arb<DenseMat<Rational>> =
        ArbSplitQuaternion.smallRational.let { r ->
            Arb.bind(r, r, r, r) { a, b, c, d -> DenseMat.ofRows(listOf(listOf(a, b), listOf(c, d))) }
        }

    fun det(m: DenseMat<Rational>): Rational =
        m[0, 0] * m[1, 1] - m[0, 1] * m[1, 0]

    fun matrix(a: Int, b: Int, c: Int, d: Int): DenseMat<Rational> =
        DenseMat.ofRows(listOf(listOf(Rational.of(a), Rational.of(b)), listOf(Rational.of(c), Rational.of(d))))

    "the images of the units: i ↦ [[0,-1],[1,0]], j ↦ [[0,1],[1,0]], k ↦ [[-1,0],[0,1]]" {
        val (one, i, j, k) = SplitQuaternionAlgebras.basis(q)
        mono(one) shouldBe matrix(1, 0, 0, 1)
        mono(i) shouldBe matrix(0, -1, 1, 0)
        mono(j) shouldBe matrix(0, 1, 1, 0)
        mono(k) shouldBe matrix(-1, 0, 0, 1)
    }

    "SplitQuaternion<ℚ> ↪ M₂(ℚ) is a unital ring homomorphism" {
        RingHomomorphismLaws(
            hom = mono::invoke,
            domain = ring,
            codomain = matrices,
            arb = arb
        ).fullTest().throwIfFailed()
    }

    "SplitQuaternion<ℚ> ↪ M₂(ℚ) is injective" {
        injectivityLaw(hom = mono::invoke, arbA = arb).test()
    }

    "the norm is the determinant: N(q) = det(M(q))" {
        checkAll(arb) { x ->
            det(mono(x)) shouldBe normSq(x)
        }
    }

    "the isomorphism and its inverse are mutually inverse" {
        checkAll(arb) { x ->
            iso.backward(iso(x)) shouldBe x
        }
        checkAll(arbMatrix) { m ->
            iso(iso.backward(m)) shouldBe m
        }
    }

    "the inverse M₂(ℚ) → SplitQuaternion<ℚ> is a unital ring homomorphism" {
        RingHomomorphismLaws(
            hom = iso.backward::invoke,
            domain = matrices,
            codomain = ring,
            arb = arbMatrix
        ).fullTest().throwIfFailed()
    }

    "the matrix units correspond to (1 ± k)/2 and (j ∓ i)/2" {
        val half = Rational.of(1, 2)
        val zero = Rational.ZERO
        iso.backward(matrix(1, 0, 0, 0)) shouldBe splitQuaternion(half, zero, zero, -half)
        iso.backward(matrix(0, 0, 0, 1)) shouldBe splitQuaternion(half, zero, zero, half)
        iso.backward(matrix(0, 1, 0, 0)) shouldBe splitQuaternion(zero, -half, half, zero)
        iso.backward(matrix(0, 0, 1, 0)) shouldBe splitQuaternion(zero, half, half, zero)
    }

    "over ℤ the embedding is injective but not surjective: entries on each diagonal share a parity" {
        val z = IntegerAlgebras.IntegerCommutativeRing
        val integerMono = SplitQuaternionAlgebras.splitQuaternionToRank2MatrixMonomorphism(z)
        val two = BigInteger.TWO

        checkAll(ArbSplitQuaternion.integerSplitQuaternion) { x ->
            val m = integerMono(x)
            (m[0, 0] - m[1, 1]).mod(two) shouldBe BigInteger.ZERO
            (m[0, 1] - m[1, 0]).mod(two) shouldBe BigInteger.ZERO
        }
        injectivityLaw(hom = integerMono::invoke, arbA = ArbSplitQuaternion.integerSplitQuaternion).test()
    }

    "the isomorphism is refused in characteristic 2, where 2 is not invertible" {
        shouldThrow<IllegalArgumentException> {
            SplitQuaternionAlgebras.splitQuaternionMatrixIsomorphism(FiniteFieldAlgebras.F2)
        }
    }
})
