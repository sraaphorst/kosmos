package org.vorpal.kosmos.categories

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.expectSome

class IsoSpec : StringSpec({

    "identity iso works" {
        val set = FiniteSet.ordered(0..4)
        val iso = Isos.identity<Int>()
        set.toList().all { a -> (iso.g andThen iso.f).apply(a) == a } shouldBe true
        set.toList().all { a -> (iso.f andThen iso.g).apply(a) == a } shouldBe true
    }

    "bijective map a |-> a + 10 yields an iso between 0..4 and 10..14" {
        val setA = FiniteSet.ordered(0..4)
        val setB = FiniteSet.ordered(10..14)
        val f = Morphism<Int, Int> { it + 10 }

        // Set-theoretic sanity.
        isMonoSet(f, setA, Int::equals) shouldBe true
        isEpiSet(f, setA, setB, Int::equals) shouldBe true

        val iso = Isos.isoFromBijection(f, setA, setB).expectSome()

        iso.leftIdentity(setA) shouldBe true
        iso.rightIdentity(setB) shouldBe true

        // `andThen` is left-to-right: (p andThen q)(x) = q(p(x)).
        // For A: (f andThen g): A -> A
        setA.toList().all { a -> (iso.f andThen iso.g).apply(a) == a } shouldBe true

        // For B: (g andThen f) : B -> B
        setB.toList().all { b -> (iso.g andThen iso.f).apply(b) == b } shouldBe true
    }

    "bijective map a |-> 2a + 10 yields an iso between 0..5 and 10..20 step 2" {
        val setA = FiniteSet.ordered(0..5)
        val setB = FiniteSet.ordered(10..20 step 2)
        val f = Morphism<Int, Int> { 2 * it + 10 }

        isMonoSet(f, setA, Int::equals) shouldBe true
        isEpiSet(f, setA, setB, Int::equals) shouldBe true

        val iso = Isos.isoFromBijection(f, setA, setB).expectSome()

        iso.leftIdentity(setA) shouldBe true
        iso.rightIdentity(setB) shouldBe true

        setA.toList().all { a -> (iso.f andThen iso.g).apply(a) == a } shouldBe true
        setB.toList().all { b -> (iso.g andThen iso.f).apply(b) == b } shouldBe true
    }

    "non-bijective map does not yield an iso" {
        val setA = FiniteSet.ordered(0..4)
        val setB = FiniteSet.ordered(0..2)
        val f = Morphism<Int, Int> { it % 3 } // surjective but not injective on setA
        Isos.isoFromBijection(f, setA, setB) shouldBe Option.None
    }
})
