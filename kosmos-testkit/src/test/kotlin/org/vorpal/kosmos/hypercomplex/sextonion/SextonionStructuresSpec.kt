package org.vorpal.kosmos.hypercomplex.sextonion

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.splitquaternion.ArbSplitQuaternion
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.homomorphism.NonAssociativeRingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.Vec2
import java.math.BigInteger

/**
 * The module, star-algebra, scalar-embedding, equality and printable structure on the sextonions.
 */
class SextonionStructuresSpec : StringSpec({
    val q = RationalAlgebras.RationalField
    val z = IntegerAlgebras.IntegerCommutativeRing
    val ring = SextonionAlgebras.sextonionRing(q)
    val arb = ArbSextonion.rationalSextonion
    val scalarArb = ArbSplitQuaternion.smallRational
    val arbInt = Arb.int(-20..20).map(Int::toBigInteger)

    fun si(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int): Sextonion<BigInteger> =
        Sextonion(
            Mat2(a.toBigInteger(), b.toBigInteger(), c.toBigInteger(), d.toBigInteger()),
            Vec2(e.toBigInteger(), f.toBigInteger())
        )

    "the sextonions over ℚ and ℤ are free modules satisfying RModuleLaws" {
        RModuleLaws(SextonionAlgebras.sextonionModule(q), scalarArb, arb).fullTest().throwIfFailed()
        RModuleLaws(SextonionAlgebras.sextonionModule(z), arbInt, ArbSextonion.integerSextonion)
            .fullTest().throwIfFailed()
        SextonionAlgebras.basis(q).size shouldBe 6
    }

    "the sextonions over ℚ and ℤ satisfy NonAssociativeStarAlgebraLaws" {
        NonAssociativeStarAlgebraLaws(
            algebra = SextonionAlgebras.sextonionStarAlgebra(q),
            scalarArb = scalarArb,
            algebraArb = arb
        ).fullTest().throwIfFailed()
        NonAssociativeStarAlgebraLaws(
            algebra = SextonionAlgebras.sextonionStarAlgebra(z),
            scalarArb = arbInt,
            algebraArb = ArbSextonion.integerSextonion
        ).fullTest().throwIfFailed()
    }

    "the algebra and the star algebra share the ring and the scalar action" {
        val algebra = SextonionAlgebras.sextonionAlgebra(q)
        checkAll(scalarArb, arb, arb) { r, x, y ->
            algebra.mul(x, y) shouldBe ring.mul(x, y)
            algebra.leftAction(r, x) shouldBe SextonionAlgebras.sextonionLeftAction(q)(r, x)
        }
    }

    "the scalar action is multiplication by the embedded scalar (rI, 0), on either side" {
        val action = SextonionAlgebras.sextonionLeftAction(q)
        val embed = SextonionAlgebras.scalarEmbedding(q)
        checkAll(scalarArb, arb) { r, x ->
            action(r, x) shouldBe ring.mul(embed(r), x)
            action(r, x) shouldBe ring.mul(x, embed(r))
        }
    }

    "the scalar embeddings ℚ ↪ Sextonion<ℚ> and ℤ ↪ Sextonion<ℤ> are injective unital homomorphisms" {
        val embed = SextonionAlgebras.scalarEmbedding(q)
        NonAssociativeRingHomomorphismLaws(hom = embed::invoke, domain = q, codomain = ring, arb = scalarArb)
            .fullTest().throwIfFailed()
        injectivityLaw(hom = embed::invoke, arbA = scalarArb).test()
        embed(Rational.ONE) shouldBe ring.one

        val embedZ = SextonionAlgebras.scalarEmbedding(z)
        NonAssociativeRingHomomorphismLaws(
            hom = embedZ::invoke,
            domain = z,
            codomain = SextonionAlgebras.sextonionRing(z),
            arb = arbInt
        ).fullTest().throwIfFailed()
        injectivityLaw(hom = embedZ::invoke, arbA = arbInt).test()
    }

    "eqSextonion compares both parts entrywise" {
        val eq = SextonionAlgebras.eqSextonion(RationalAlgebras.eqRational)
        checkAll(arb) { x -> eq(x, x) shouldBe true }
        val x = Sextonion(Mat2(Rational.ONE, Rational.ZERO, Rational.ZERO, Rational.ONE), Vec2(Rational.ZERO, Rational.ONE))
        eq(x, x.copy(nu = Vec2(Rational.ZERO, Rational.ZERO))) shouldBe false
        eq(x, x.copy(m = Mat2(Rational.ONE, Rational.ONE, Rational.ZERO, Rational.ONE))) shouldBe false
    }

    "printableSextonion renders one line, as the pair (A, ν)" {
        val pr = SextonionAlgebras.printableSextonion(IntegerAlgebras.printableInteger)
        pr(si(1, -2, 10, 3, 4, -5)) shouldBe "([[1, -2], [10, 3]], (4, -5))"
        pr(SextonionAlgebras.sextonionRing(z).one) shouldBe "([[1, 0], [0, 1]], (0, 0))"
    }

    "printableSextonionPretty renders the matrix on two aligned lines, then the vector" {
        val pr = SextonionAlgebras.printableSextonionPretty(IntegerAlgebras.printableInteger)
        pr(si(1, -2, 10, 3, 4, -5)) shouldBe
            "⎛  1  -2 ⎞\n" +
            "⎝ 10   3 ⎠ ⊕ (4, -5)"
    }
})
