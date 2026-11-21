package org.vorpal.kosmos.core.finiteset.algebra

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.laws.LawGraphBuilder
import org.vorpal.kosmos.laws.algebra.AbelianGroupLaws
import org.vorpal.kosmos.laws.algebra.BooleanAlgebraLaws
import org.vorpal.kosmos.laws.algebra.CommutativeMonoidLaws
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.SemiringLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local
import java.math.BigInteger

/*
 * The power-set structures of FiniteSetAlgebras. Their carrier is FiniteSet.Unordered, so equality is set equality.
 * (They used to take any FiniteSet and return Ø as an ordered set, which never equals an unordered result: the
 * Boolean algebra failed its identity, annihilator and complement laws.)
 *
 * Each universe is small enough that the generators draw from *every* subset, and an exhaustive local law checks
 * every pair of subsets directly, so the representation cannot hide behind random sampling.
 */

/** Every subset of [universe], via bitmasks. */
private fun <A> powerSet(universe: FiniteSet.Unordered<A>): List<FiniteSet.Unordered<A>> {
    val elements = universe.toList()
    return (0 until (1 shl elements.size)).map { mask ->
        FiniteSet.unordered(elements.filterIndexed { i, _ -> mask and (1 shl i) != 0 })
    }
}

private val f2: Arb<BigInteger> = Arb.element(BigInteger.ZERO, BigInteger.ONE)

/** All the universe-bound structures over [universe], with their complete suites. */
private fun <A> LawGraphBuilder.powerSetStructures(universe: FiniteSet.Unordered<A>) {
    val subsets = powerSet(universe)
    val arb = Arb.element(subsets)

    suite(BooleanAlgebraLaws(FiniteSetAlgebras.finiteSetBooleanAlgebra(universe), arb))
    suite(CommutativeRingLaws(FiniteSetAlgebras.finiteSetBooleanRing(universe), arb))
    suite(SemiringLaws(FiniteSetAlgebras.finiteSetSemiring(universe), arb))
    suite(CommutativeMonoidLaws(FiniteSetAlgebras.finiteSetIntersectionMonoid(universe), arb))
    suite(CommutativeMonoidLaws(FiniteSetAlgebras.finiteSetUnionMonoid(universe), arb))
    suite(AbelianGroupLaws(FiniteSetAlgebras.finiteSetSymmetricDifferenceAbelianGroup(universe), arb))
    suite(VectorSpaceLaws(FiniteSetAlgebras.finiteSetF2VectorSpace(universe), f2, arb))

    local("every pair of subsets: ∪, ∩, Δ and complement give the set-theoretic answer, as unordered sets") {
        val ba = FiniteSetAlgebras.finiteSetBooleanAlgebra(universe)
        val ring = FiniteSetAlgebras.finiteSetBooleanRing(universe)
        val u = universe.toSet()
        ba.bottom.shouldBeInstanceOf<FiniteSet.Unordered<A>>()
        ba.bottom.toSet() shouldBe emptySet()
        for (a in subsets) {
            ba.not(a).shouldBeInstanceOf<FiniteSet.Unordered<A>>()
            ba.not(a).toSet() shouldBe u - a.toSet()
            for (b in subsets) {
                val results = listOf(
                    ba.join(a, b) to a.toSet() + b.toSet(),
                    ba.meet(a, b) to a.toSet().intersect(b.toSet()),
                    ring.add(a, b) to (a.toSet() - b.toSet()) + (b.toSet() - a.toSet())
                )
                results.forEach { (actual, expected) ->
                    actual.shouldBeInstanceOf<FiniteSet.Unordered<A>>()
                    actual.toSet() shouldBe expected
                }
                ba.join(a, b) shouldBe ba.join(b, a)
                ba.meet(a, b) shouldBe ba.meet(b, a)
            }
        }
    }
}

/** Arbitrary finite sets of small ints, for the structures with no universe. */
private val anySets: Arb<FiniteSet.Unordered<Int>> = Arb.list(Arb.int(0..6), 0..7).map { FiniteSet.unordered(it) }

class FiniteSetAlgebrasSpec : FunSpec({
    lawTests(lawGraph("finite set algebras") {

        branch("over {0, 1, 2}") { powerSetStructures(FiniteSet.unordered(0, 1, 2)) }
        branch("over {a, b, c, d}") { powerSetStructures(FiniteSet.unordered('a', 'b', 'c', 'd')) }
        branch("over the empty universe: P(Ø) = {Ø}, the one-element algebra") {
            suite(BooleanAlgebraLaws(
                FiniteSetAlgebras.finiteSetBooleanAlgebra(FiniteSet.emptyUnordered<Int>()),
                Arb.element(listOf(FiniteSet.emptyUnordered<Int>()))
            ))
            suite(AbelianGroupLaws(
                FiniteSetAlgebras.finiteSetSymmetricDifferenceAbelianGroup(FiniteSet.emptyUnordered<Int>()),
                Arb.element(listOf(FiniteSet.emptyUnordered<Int>()))
            ))
            local("⊥ = ⊤ = Ø") {
                val ba = FiniteSetAlgebras.finiteSetBooleanAlgebra(FiniteSet.emptyUnordered<Int>())
                ba.bottom shouldBe ba.top
            }
        }

        branch("with no universe") {
            suite(CommutativeMonoidLaws(FiniteSetAlgebras.finiteSetUnionMonoid(), anySets))
            suite(AbelianGroupLaws(FiniteSetAlgebras.finiteSetSymmetricDifferenceAbelianGroup(), anySets))
        }

        local("an ordered universe or operand is used through toUnordered(), and its order is forgotten") {
            val universe = FiniteSet.ordered(2, 0, 1).toUnordered()
            universe shouldBe FiniteSet.unordered(0, 1, 2)
            val ba = FiniteSetAlgebras.finiteSetBooleanAlgebra(universe)
            val ab = FiniteSet.ordered(1, 2).toUnordered()
            val ba2 = FiniteSet.ordered(2, 1).toUnordered()
            ab shouldBe ba2
            ba.join(ab, ba.bottom) shouldBe ba2
            ba.meet(ab, ba.not(ba2)) shouldBe ba.bottom
        }
    })
})
