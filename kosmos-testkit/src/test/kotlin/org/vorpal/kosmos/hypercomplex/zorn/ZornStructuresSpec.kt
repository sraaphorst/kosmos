package org.vorpal.kosmos.hypercomplex.zorn

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import java.math.BigInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.splitquaternion.ArbSplitQuaternion
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.values.Vec3
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * The module, star-algebra, scalar-embedding and printable structure on the Zorn matrices.
 */
class ZornStructuresSpec : FunSpec({
    lawTests(lawGraph("Zorn structures") {
        val q = RationalAlgebras.RationalField
        val z = IntegerAlgebras.IntegerCommutativeRing
        val ring = ZornAlgebras.zornRing(q)
        val arb = ArbZorn.rationalZorn
        val scalarArb = ArbSplitQuaternion.smallRational
        val arbInt = Arb.int(-20..20).map(Int::toBigInteger)

        fun zi(a: Int, v: List<Int>, w: List<Int>, d: Int): Zorn<BigInteger> {
            fun vec(xs: List<Int>) = Vec3(xs[0].toBigInteger(), xs[1].toBigInteger(), xs[2].toBigInteger())
            return Zorn(a.toBigInteger(), vec(v), vec(w), d.toBigInteger())
        }

        branch("the Zorn matrices over ℚ are a free ℚ-module satisfying RModuleLaws") {
            suite(RModuleLaws(
                module = ZornAlgebras.zornModule(q),
                scalarArb = scalarArb,
                vectorArb = arb
            ))
        }

        branch("the Zorn matrices over ℤ are a free ℤ-module satisfying RModuleLaws") {
            suite(RModuleLaws(
                module = ZornAlgebras.zornModule(z),
                scalarArb = arbInt,
                vectorArb = ArbZorn.integerZorn
            ))
        }

        branch("the Zorn matrices over ℚ satisfy NonAssociativeStarAlgebraLaws") {
            suite(NonAssociativeStarAlgebraLaws(
                algebra = ZornAlgebras.zornStarAlgebra(q),
                scalarArb = scalarArb,
                algebraArb = arb
            ))
        }

        branch("the Zorn matrices over ℤ satisfy NonAssociativeStarAlgebraLaws") {
            suite(NonAssociativeStarAlgebraLaws(
                algebra = ZornAlgebras.zornStarAlgebra(z),
                scalarArb = arbInt,
                algebraArb = ArbZorn.integerZorn
            ))
        }

        local("the scalar action is multiplication by the embedded scalar (r, 0, 0, r), on either side") {
            val action = ZornAlgebras.zornLeftAction(q)
            val embed = ZornAlgebras.scalarEmbedding(q)
            lawCheckAll(scalarArb, arb) { r, x ->
                action(r, x) shouldBe ring.mul(embed(r), x)
                action(r, x) shouldBe ring.mul(x, embed(r))
            }
        }

        branch("the scalar embedding ℚ ↪ Zorn<ℚ> is an injective unital homomorphism") {
            val embed = ZornAlgebras.scalarEmbedding(q)
            suite(NonAssociativeRingHomomorphismLaws(
                hom = embed::invoke,
                domain = q,
                codomain = ring,
                arb = scalarArb
            ))
            law(injectivityLaw(hom = embed::invoke, arbA = scalarArb))
            local("1 ↦ 1") {
                embed(Rational.ONE) shouldBe ring.one
            }
        }

        branch("the scalar embedding ℤ ↪ Zorn<ℤ> is an injective unital homomorphism") {
            val embed = ZornAlgebras.scalarEmbedding(z)
            suite(NonAssociativeRingHomomorphismLaws(
                hom = embed::invoke,
                domain = z,
                codomain = ZornAlgebras.zornRing(z),
                arb = arbInt
            ))
            law(injectivityLaw(hom = embed::invoke, arbA = arbInt))
        }

        local("printableZorn renders one line, row by row") {
            val pr = ZornAlgebras.printableZorn(IntegerAlgebras.printableInteger)
            pr(zi(1, listOf(2, 0, -3), listOf(0, -1, 4), -5)) shouldBe
                "[[1, (2, 0, -3)], [(0, -1, 4), -5]]"
            pr(ZornAlgebras.zornRing(z).zero) shouldBe
                "[[0, (0, 0, 0)], [(0, 0, 0), 0]]"

            val prQ = ZornAlgebras.printableZorn(RationalAlgebras.printableRational)
            val h = Rational.of(-1, 2)
            val zero = Rational.ZERO
            prQ(Zorn(h, Vec3(zero, h, zero), Vec3(zero, zero, Rational.ONE), Rational.ONE)) shouldBe
                "[[-1/2, (0, -1/2, 0)], [(0, 0, 1), 1]]"
        }

        local("printableZornPretty renders two aligned lines") {
            val pr = ZornAlgebras.printableZornPretty(IntegerAlgebras.printableInteger)
            pr(zi(1, listOf(2, 0, -3), listOf(0, -1, 4), -5)) shouldBe
                "⎛          1  (2, 0, -3) ⎞\n" +
                "⎝ (0, -1, 4)          -5 ⎠"
            pr(ZornAlgebras.zornRing(z).one) shouldBe
                "⎛         1  (0, 0, 0) ⎞\n" +
                "⎝ (0, 0, 0)          1 ⎠"
        }
    })
})
