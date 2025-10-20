package org.vorpal.kosmos.categories

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.core.FiniteSet

class IsoSpec : StringSpec({

    "identity iso works" {
        val set = FiniteSet.ordered(0..4)
        val iso = Isos.identity<Int>()
        set.toList().all { a -> (iso.g then iso.f).apply(a) == a } shouldBe true
        set.toList().all { a -> (iso.f then iso.g).apply(a) == a } shouldBe true
    }

    "bijective map a |-> a+10 yields an iso between 0..4 and 10..14" {
        val setA = FiniteSet.ordered(0..4)
        val setB = FiniteSet.ordered(10 .. 14)
        val f = Morphism<Int, Int> { it + 10 }

        // Set-theoretic sanity
        isMonoSet(f, setA, Int::equals) shouldBe true
        isEpiSet(f, setA, setB, Int::equals) shouldBe true

        val iso = requireNotNull(Isos.isoFromBijection(f, setA, setB, Int::equals, Int::equals))

        iso.leftIdentity(setA, Int::equals) shouldBe true
        iso.rightIdentity(setB, Int::equals) shouldBe true

        // Identities hold on these finite sets
        // Correct: check on A with f∘g? No: f∘g is B->B.
        // For A, use g∘f? No: g∘f is B->B.
        // Careful: our `then` is left-to-right: (p then q)(x) = q(p(x))

        // f: add 10
        // For A: (f then g) : A -> A
        setA.toList().all { a -> (iso.f then iso.g).apply(a) == a } shouldBe true

        // For B: (g then f) : B -> B
        setB.toList().all { b -> (iso.g then iso.f).apply(b) == b } shouldBe true
    }

    "bijective map a |-> a+10 yields an iso between 0..5 and 10..20 step 2" {
        val setA = FiniteSet.ordered(0..5)
        val setB = FiniteSet.ordered(10 .. 20 step 2)
        val f = Morphism<Int, Int> { 2 * it + 10 }

        // Set-theoretic sanity
        isMonoSet(f, setA, Int::equals) shouldBe true
        isEpiSet(f, setA, setB, Int::equals) shouldBe true

        val iso = requireNotNull(Isos.isoFromBijection(f, setA, setB, Int::equals, Int::equals))

        iso.leftIdentity(setA, Int::equals) shouldBe true
        iso.rightIdentity(setB, Int::equals) shouldBe true

        // Identities hold on these finite sets
        // Correct: check on A with f∘g? No: f∘g is B->B.
        // For A, use g∘f? No: g∘f is B->B.
        // Careful: our `then` is left-to-right: (p then q)(x) = q(p(x))

        // f: add 10
        // For A: (f then g) : A -> A
        setA.toList().all { a -> (iso.f then iso.g).apply(a) == a } shouldBe true

        // For B: (g then f) : B -> B
        setB.toList().all { b -> (iso.g then iso.f).apply(b) == b } shouldBe true
    }

    "non-bijective map does not yield an iso" {
        val A = FiniteSet.ordered(0..4)
        val B = FiniteSet.ordered(0..2)
        val f = Morphism<Int, Int> { it % 3 } // surjective but not injective on A
        Isos.isoFromBijection(f, A, B, Int::equals, Int::equals) shouldBe null
    }
})