package org.vorpal.kosmos.categories

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MonoEpiSpec : StringSpec({
    "identity on 0..4 is mono (injective)" {
        val A = FiniteSet.of(0..4)
        val id = Morphism<Int, Int> { it }
        isMonoSet(id, A, Int::equals) shouldBe true
    }

    "mono cancellability holds for identity" {
        val A = FiniteSet.of(0..4)
        val id = Morphism<Int, Int> { it }
        val pairs = samplePairs(allFunctions(A, A), 16)
        monoLawHolds(
            f = id,
            Xs = A,
            As = A,                 // ok even if unused in your impl
            eqA = Int::equals,
            eqB = Int::equals,
            samples = pairs
        ) shouldBe true
    }

    "mod 5 is epi from 0..9 onto 0..4 (surjective)" {
        val SurjA = FiniteSet.of(0..9)
        val SurjB = FiniteSet.of(0..4)
        val mod5 = Morphism<Int, Int> { it % 5 }
        isEpiSet(mod5, SurjA, SurjB, Int::equals) shouldBe true
    }

    "epi cancellability holds for mod 5" {
        val SurjA = FiniteSet.of(0..9)
        val SurjB = FiniteSet.of(0..4)
        val mod5 = Morphism<Int, Int> { it % 5 }
        val Ys = FiniteSet.of(0..3)
        val pairs = samplePairs(allFunctions(SurjB, Ys), 16)
        epiLawHolds(
            f = mod5,
            As = SurjA,
            Bs = SurjB,             // ok even if unused in your impl
            Ys = Ys,
            eqB = Int::equals,
            eqY = Int::equals,
            samples = pairs
        ) shouldBe true
    }
})

private fun <X, A> samplePairs(
    from: Sequence<Morphism<X, A>>,
    maxPairs: Int
): Sequence<Pair<Morphism<X, A>, Morphism<X, A>>> =
    from.take(maxPairs).flatMap { g1 -> from.take(maxPairs).map { g2 -> g1 to g2 } }