package org.vorpal.kosmos.categories

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class IsoSpec : StringSpec({

    "identity iso works" {
        val A = FiniteSet.of(0..4)
        val iso = isoRefl<Int>()
        A.toList().all { a -> (iso.g then iso.f).apply(a) == a } shouldBe true
        A.toList().all { a -> (iso.f then iso.g).apply(a) == a } shouldBe true
    }

    "bijective map a |-> a+10 yields an iso between 0..4 and 10..14" {
        val A = FiniteSet.of(0..4)
        val B = FiniteSet.of(10 .. 14)
        val f = Morphism<Int, Int> { it + 10 }

        // Set-theoretic sanity
        isMonoSet(f, A, Int::equals) shouldBe true
        isEpiSet(f, A, B, Int::equals) shouldBe true

        val iso = requireNotNull(isoFromBijective(f, A, B, Int::equals, Int::equals))

        iso.leftIdentity(A, Int::equals) shouldBe true
        iso.rightIdentity(B, Int::equals) shouldBe true

        // Identities hold on these finite sets
        // Correct: check on A with f∘g? No: f∘g is B->B.
        // For A, use g∘f? No: g∘f is B->B.
        // Careful: our `then` is left-to-right: (p then q)(x) = q(p(x))

        // f: add 10
        // For A: (f then g) : A -> A
        A.toList().all { a -> (iso.f then iso.g).apply(a) == a } shouldBe true

        // For B: (g then f) : B -> B
        B.toList().all { b -> (iso.g then iso.f).apply(b) == b } shouldBe true
    }

    "bijective map a |-> a+10 yields an iso between 0..5 and 10..20 step 2" {
        val A = FiniteSet.of(0..5)
        val B = FiniteSet.of(10 .. 20 step 2)
        val f = Morphism<Int, Int> { 2 * it + 10 }

        // Set-theoretic sanity
        isMonoSet(f, A, Int::equals) shouldBe true
        isEpiSet(f, A, B, Int::equals) shouldBe true

        val iso = requireNotNull(isoFromBijective(f, A, B, Int::equals, Int::equals))

        iso.leftIdentity(A, Int::equals) shouldBe true
        iso.rightIdentity(B, Int::equals) shouldBe true

        // Identities hold on these finite sets
        // Correct: check on A with f∘g? No: f∘g is B->B.
        // For A, use g∘f? No: g∘f is B->B.
        // Careful: our `then` is left-to-right: (p then q)(x) = q(p(x))

        // f: add 10
        // For A: (f then g) : A -> A
        A.toList().all { a -> (iso.f then iso.g).apply(a) == a } shouldBe true

        // For B: (g then f) : B -> B
        B.toList().all { b -> (iso.g then iso.f).apply(b) == b } shouldBe true
    }

    "non-bijective map does not yield an iso" {
        val A = FiniteSet.of(0..4)
        val B = FiniteSet.of(0..2)
        val f = Morphism<Int, Int> { it % 3 } // surjective but not injective on A
        isoFromBijective(f, A, B, Int::equals, Int::equals) shouldBe null
    }
})