package org.vorpal.kosmos.categories

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.core.FiniteSet

// Reused sets
private val set03 = FiniteSet.ordered(0..3)
private val set04 = FiniteSet.ordered(0..4)
private val set09 = FiniteSet.ordered(0..9)

class MonoEpiSpec : StringSpec({
    "identity on 0..4 is mono (injective)" {
        val id = Morphisms.identity<Int>()
        isMonoSet(id, set04, Int::equals) shouldBe true
    }

    "mono cancellability holds for identity" {
        val id = Morphisms.identity<Int>()
        val pairs = samplePairs(allFunctions(set04, set04), 16)
        MonoEpi.monoLawHolds(
            f = id,
            setA = set04,
            setX = set04,
            eqA = Int::equals,
            eqB = Int::equals,
            samples = pairs
        ) shouldBe true
    }

    "mod 5 is epi from 0..9 onto 0..4 (surjective)" {
        val mod5 = Morphism<Int, Int> { it % 5 }
        isEpiSet(mod5, set09, set04, Int::equals) shouldBe true
    }

    "epi cancellability holds for mod 5" {
        val mod5 = Morphism<Int, Int> { it % 5 }
        val pairs = samplePairs(allFunctions(set04, set03), 16)
        MonoEpi.epiLawHolds(
            f = mod5,
            setA = set09,
            setB = set04,
            setY = set03,
            eqB = Int::equals,
            eqY = Int::equals,
            samples = pairs
        ) shouldBe true
    }
})

private fun <X, A> samplePairs(
    from: Sequence<Morphism<X, A>>,
    maxPairs: Int): Sequence<Pair<Morphism<X, A>, Morphism<X, A>>> =
    from.take(maxPairs).flatMap { g1 -> from.take(maxPairs).map { g2 -> g1 to g2 } }
