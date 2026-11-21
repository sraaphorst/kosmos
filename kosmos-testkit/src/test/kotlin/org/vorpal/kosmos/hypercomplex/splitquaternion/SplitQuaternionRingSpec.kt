package org.vorpal.kosmos.hypercomplex.splitquaternion

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.laws.property.NormSqMultiplicativeLaw
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Exact tests of the split-quaternions `(-1, +1)_R` over ℚ and ℤ.
 *
 * Arithmetic is exact, so associativity, the involution laws and multiplicativity of the norm are
 * verified with strict equality.
 */
class SplitQuaternionRingSpec : FunSpec({
    lawTests(lawGraph("split quaternion rings") {
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

        branch("the split-quaternions over ℚ satisfy InvolutiveRingLaws (associative, with an involution)") {
            suite(InvolutiveRingLaws(ring, arb, pr = pr))
        }

        branch("the split-quaternions over ℤ satisfy InvolutiveRingLaws") {
            suite(InvolutiveRingLaws(
                SplitQuaternionAlgebras.splitQuaternionRing(IntegerAlgebras.IntegerCommutativeRing),
                ArbSplitQuaternion.integerSplitQuaternion
            ))
        }

        local("the basis is 1, i, j, k and 1 is the identity") {
            one shouldBe ring.one
            i shouldBe splitQuaternion(Rational.ZERO, Rational.ONE, Rational.ZERO, Rational.ZERO)
            j shouldBe splitQuaternion(Rational.ZERO, Rational.ZERO, Rational.ONE, Rational.ZERO)
            k shouldBe splitQuaternion(Rational.ZERO, Rational.ZERO, Rational.ZERO, Rational.ONE)
        }

        local("i² = -1, j² = +1, k² = +1") {
            ring.mul(i, i) shouldBe negOne
            ring.mul(j, j) shouldBe one
            ring.mul(k, k) shouldBe one
        }

        local("ij = k, jk = -i, ki = j, and distinct units anticommute") {
            ring.mul(i, j) shouldBe k
            ring.mul(j, k) shouldBe neg(i)
            ring.mul(k, i) shouldBe j

            ring.mul(j, i) shouldBe neg(k)
            ring.mul(k, j) shouldBe i
            ring.mul(i, k) shouldBe neg(j)
        }

        local("ijk = +1 (in contrast to ijk = -1 in ℍ)") {
            ring.mul(ring.mul(i, j), k) shouldBe one
        }

        local("conjugation negates the three imaginary coordinates") {
            lawCheckAll(arb) { x ->
                ring.conj(x) shouldBe splitQuaternion(x.w, -x.x, -x.y, -x.z)
            }
        }

        local("normSq formula: N(w + xi + yj + zk) = w² + x² - y² - z²") {
            lawCheckAll(arb) { x ->
                normSq(x) shouldBe x.w * x.w + x.x * x.x - x.y * x.y - x.z * x.z
            }
        }

        local("the norm is the scalar q·conj(q) = conj(q)·q") {
            val embed = SplitQuaternionAlgebras.scalarEmbedding(q)
            lawCheckAll(arb) { x ->
                ring.mul(x, ring.conj(x)) shouldBe embed(normSq(x))
                ring.mul(ring.conj(x), x) shouldBe embed(normSq(x))
            }
        }

        law("normSq is exactly multiplicative: N(pq) = N(p)N(q)", NormSqMultiplicativeLaw(
                mulA = ring.mul.op,
                normSq = normSq,
                mulN = q.mul.op,
                arbA = arb,
                prA = pr
            ))

        local("zero divisors: (1 + j)(1 - j) = 0 and (1 + k)(1 - k) = 0, and both factors have norm 0") {
            listOf(j, k).forEach { u ->
                val plus = ring.add(one, u)
                val minus = ring.add(one, neg(u))
                ring.mul(plus, minus) shouldBe ring.zero
                normSq(plus) shouldBe Rational.ZERO
                normSq(minus) shouldBe Rational.ZERO
            }
        }

        local("nilpotents exist: (i + j)² = 0") {
            val n = ring.add(i, j)
            ring.mul(n, n) shouldBe ring.zero
        }

        local("nontrivial idempotents exist: e = (1 + j)/2 satisfies e² = e") {
            val half = Rational.of(1, 2)
            val e = splitQuaternion(half, Rational.ZERO, half, Rational.ZERO)
            ring.mul(e, e) shouldBe e
        }

        local("reciprocal: q⁻¹ = conj(q)/N(q), and q·q⁻¹ = q⁻¹·q = 1 off the null cone") {
            val action = SplitQuaternionAlgebras.splitQuaternionLeftAction(q)
            lawCheckAll(ArbSplitQuaternion.unitRationalSplitQuaternion) { x ->
                withReciprocal.hasReciprocal(x) shouldBe true
                val inverse = withReciprocal.reciprocal(x)
                inverse shouldBe action(normSq(x).reciprocal(), ring.conj(x))
                ring.mul(x, inverse) shouldBe one
                ring.mul(inverse, x) shouldBe one
            }
        }

        local("the null cone is not invertible") {
            val nullElement = ring.add(one, j)
            withReciprocal.hasReciprocal(nullElement) shouldBe false
            withReciprocal.hasReciprocal(ring.zero) shouldBe false
            shouldThrow<ArithmeticException> { withReciprocal.reciprocal(nullElement) }
            shouldThrow<ArithmeticException> { withReciprocal.reciprocal(ring.zero) }
        }

        branch("the split-quaternions over ℚ are a free ℚ-module satisfying RModuleLaws") {
            suite(RModuleLaws(
                module = SplitQuaternionAlgebras.splitQuaternionModule(q),
                scalarArb = ArbSplitQuaternion.smallRational,
                vectorArb = arb,
                prM = pr
            ))
        }

        branch("the split-quaternions over ℚ satisfy StarAlgebraLaws") {
            suite(StarAlgebraLaws(
                algebra = SplitQuaternionAlgebras.splitQuaternionStarAlgebra(q),
                scalarArb = ArbSplitQuaternion.smallRational,
                algebraArb = arb,
                prA = pr
            ))
        }

        local("the scalar action is multiplication by the embedded scalar, on either side") {
            val action = SplitQuaternionAlgebras.splitQuaternionLeftAction(q)
            val embed = SplitQuaternionAlgebras.scalarEmbedding(q)
            lawCheckAll(ArbSplitQuaternion.smallRational, arb) { r, x ->
                action(r, x) shouldBe ring.mul(embed(r), x)
                action(r, x) shouldBe ring.mul(x, embed(r))
            }
        }

        local("printable renders in the 1, I, J, K basis") {
            pr(splitQuaternion(Rational.of(1), Rational.of(2), Rational.of(-3), Rational.ZERO)).isNotEmpty() shouldBe true
            pr(ring.zero).isNotEmpty() shouldBe true
        }
    })
})
