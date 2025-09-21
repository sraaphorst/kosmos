package org.vorpal.kosmos.categories

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.checkAll
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.testing.finiteSet

class FiniteSetGenSpec : StringSpec({

    "identity is mono for arbitrary FiniteSet<Int>" {
        val id = Morphism<Int, Int> { it }
        checkAll(Arb.int().finiteSet(0..6)) { A ->
            isMonoSet(id, A, Int::equals) shouldBe true
        }
    }

    "surjectivity to residues holds iff |A| >= k" {
        checkAll(Arb.int().finiteSet(0..12), Arb.int(1..6)) { A, k ->
            val B = FiniteSet.ordered(0 until k)              // canonical residues 0..k-1
            val table = A.toList().mapIndexed { i, a -> a to (i % k) }.toMap()
            val f = Morphism<Int, Int> { a -> table.getValue(a) }  // total on A

            isEpiSet(f, A, B, Int::equals) shouldBe (A.size >= k)
        }
    }
})