package org.vorpal.kosmos.hypercomplex.splitquaternion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw

/**
 * Exact tests of the split-quaternions `(-1, +1)_R` over ℚ and ℤ.
 *
 * Arithmetic is exact, so associativity, the involution laws and multiplicativity of the norm are
 * verified with strict equality.
 */
class SplitQuaternionRingSpec : StringSpec({
    val q = RationalAlgebras.RationalField
    val ring = SplitQuaternionAlgebras.splitQuaternionRing(q)
    val withReciprocal = SplitQuaternionAlgebras.splitQuaternionRingWithReciprocal(q)
    val normSq = SplitQuaternionAlgebras.normSq(q)
    val arb = ArbSplitQuaternion.rationalSplitQuaternion
    val pr = SplitQuaternionAlgebras.printableSplitQuaternion(
        q, RationalAlgebras.SignedRational, RationalAlgebras.printableRational, RationalAlgebras.eqRational
    )

    val (one, i, j, k) = SplitQuaternionAlgebras.basis(q)
    val negOne = ring.negOne
    fun neg(x: SplitQuaternion<Rational>) = ring.add.inverse(x)

    "the split-quaternions over ℚ satisfy InvolutiveRingLaws (associative, with an involution)" {
        InvolutiveRingLaws(ring, arb, pr = pr).fullTest().throwIfFailed()
    }

    "the split-quaternions over ℤ satisfy InvolutiveRingLaws" {
        InvolutiveRingLaws(
            SplitQuaternionAlgebras.splitQuaternionRing(IntegerAlgebras.IntegerCommutativeRing),
            ArbSplitQuaternion.integerSplitQuaternion
        ).fullTest().throwIfFailed()
    }

    "the basis is 1, i, j, k and 1 is the identity" {
        one shouldBe ring.one
        i shouldBe splitQuaternion(Rational.ZERO, Rational.ONE, Rational.ZERO, Rational.ZERO)
        j shouldBe splitQuaternion(Rational.ZERO, Rational.ZERO, Rational.ONE, Rational.ZERO)
        k shouldBe splitQuaternion(Rational.ZERO, Rational.ZERO, Rational.ZERO, Rational.ONE)
    }

    "i² = -1, j² = +1, k² = +1" {
        ring.mul(i, i) shouldBe negOne
        ring.mul(j, j) shouldBe one
        ring.mul(k, k) shouldBe one
    }

    "ij = k, jk = -i, ki = j, and distinct units anticommute" {
        ring.mul(i, j) shouldBe k
        ring.mul(j, k) shouldBe neg(i)
        ring.mul(k, i) shouldBe j

        ring.mul(j, i) shouldBe neg(k)
        ring.mul(k, j) shouldBe i
        ring.mul(i, k) shouldBe neg(j)
    }

    "ijk = +1 (in contrast to ijk = -1 in ℍ)" {
        ring.mul(ring.mul(i, j), k) shouldBe one
    }

    "conjugation negates the three imaginary coordinates" {
        checkAll(arb) { x ->
            ring.conj(x) shouldBe splitQuaternion(x.w, -x.x, -x.y, -x.z)
        }
    }

    "normSq formula: N(w + xi + yj + zk) = w² + x² - y² - z²" {
        checkAll(arb) { x ->
            normSq(x) shouldBe x.w * x.w + x.x * x.x - x.y * x.y - x.z * x.z
        }
    }

    "the norm is the scalar q·conj(q) = conj(q)·q" {
        val embed = SplitQuaternionAlgebras.scalarEmbedding(q)
        checkAll(arb) { x ->
            ring.mul(x, ring.conj(x)) shouldBe embed(normSq(x))
            ring.mul(ring.conj(x), x) shouldBe embed(normSq(x))
        }
    }

    "normSq is exactly multiplicative: N(pq) = N(p)N(q)" {
        NormSqMultiplicativeLaw(
            mulA = ring.mul.op,
            normSq = normSq,
            mulN = q.mul.op,
            arbA = arb,
            prA = pr
        ).test()
    }

    "zero divisors: (1 + j)(1 - j) = 0 and (1 + k)(1 - k) = 0, and both factors have norm 0" {
        listOf(j, k).forEach { u ->
            val plus = ring.add(one, u)
            val minus = ring.add(one, neg(u))
            ring.mul(plus, minus) shouldBe ring.zero
            normSq(plus) shouldBe Rational.ZERO
            normSq(minus) shouldBe Rational.ZERO
        }
    }

    "nilpotents exist: (i + j)² = 0" {
        val n = ring.add(i, j)
        ring.mul(n, n) shouldBe ring.zero
    }

    "nontrivial idempotents exist: e = (1 + j)/2 satisfies e² = e" {
        val half = Rational.of(1, 2)
        val e = splitQuaternion(half, Rational.ZERO, half, Rational.ZERO)
        ring.mul(e, e) shouldBe e
    }

    "reciprocal: q⁻¹ = conj(q)/N(q), and q·q⁻¹ = q⁻¹·q = 1 off the null cone" {
        val action = SplitQuaternionAlgebras.splitQuaternionLeftAction(q)
        checkAll(ArbSplitQuaternion.unitRationalSplitQuaternion) { x ->
            withReciprocal.hasReciprocal(x) shouldBe true
            val inverse = withReciprocal.reciprocal(x)
            inverse shouldBe action(normSq(x).reciprocal(), ring.conj(x))
            ring.mul(x, inverse) shouldBe one
            ring.mul(inverse, x) shouldBe one
        }
    }

    "the null cone is not invertible" {
        val nullElement = ring.add(one, j)
        withReciprocal.hasReciprocal(nullElement) shouldBe false
        withReciprocal.hasReciprocal(ring.zero) shouldBe false
        shouldThrow<ArithmeticException> { withReciprocal.reciprocal(nullElement) }
        shouldThrow<ArithmeticException> { withReciprocal.reciprocal(ring.zero) }
    }

    "the split-quaternions over ℚ are a free ℚ-module satisfying RModuleLaws" {
        RModuleLaws(
            module = SplitQuaternionAlgebras.splitQuaternionModule(q),
            scalarArb = ArbSplitQuaternion.smallRational,
            vectorArb = arb,
            prM = pr
        ).fullTest().throwIfFailed()
    }

    "the split-quaternions over ℚ satisfy StarAlgebraLaws" {
        StarAlgebraLaws(
            algebra = SplitQuaternionAlgebras.splitQuaternionStarAlgebra(q),
            scalarArb = ArbSplitQuaternion.smallRational,
            algebraArb = arb,
            prA = pr
        ).fullTest().throwIfFailed()
    }

    "the scalar action is multiplication by the embedded scalar, on either side" {
        val action = SplitQuaternionAlgebras.splitQuaternionLeftAction(q)
        val embed = SplitQuaternionAlgebras.scalarEmbedding(q)
        checkAll(ArbSplitQuaternion.smallRational, arb) { r, x ->
            action(r, x) shouldBe ring.mul(embed(r), x)
            action(r, x) shouldBe ring.mul(x, embed(r))
        }
    }

    "printable renders in the 1, I, J, K basis" {
        pr(splitQuaternion(Rational.of(1), Rational.of(2), Rational.of(-3), Rational.ZERO)).isNotEmpty() shouldBe true
        pr(ring.zero).isNotEmpty() shouldBe true
    }
})
