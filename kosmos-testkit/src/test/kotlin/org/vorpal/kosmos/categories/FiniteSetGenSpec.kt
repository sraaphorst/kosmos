package org.vorpal.kosmos.categories

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.finiteSet
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

class FiniteSetGenSpec : FunSpec({
    lawTests(lawGraph("finite set generators") {
        local("identity is mono for arbitrary FiniteSet<Int>") {
            val id = Morphism<Int, Int> { it }
            lawCheckAll(Arb.int().finiteSet(0..6)) { A ->
                isMonoSet(id, A) shouldBe true
            }
        }

        local("surjectivity to residues holds iff |A| >= k") {
            lawCheckAll(Arb.int().finiteSet(0..12), Arb.int(1..6)) { A, k ->
                val B = FiniteSet.ordered(0 until k)         // canonical residues 0..k-1
                val table = A.toList().mapIndexed { i, a -> a to (i % k) }.toMap()
                val f = Morphism<Int, Int> { a -> table.getValue(a) }  // total on A

                isEpiSet(f, A, B) shouldBe (A.size >= k)
            }
        }
    })
})