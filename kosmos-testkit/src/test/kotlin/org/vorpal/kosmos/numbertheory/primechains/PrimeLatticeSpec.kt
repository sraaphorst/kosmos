package org.vorpal.kosmos.numbertheory.primechains

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.math.BigInteger
import org.vorpal.kosmos.frameworks.lattice.LatticeAsIndexable
import org.vorpal.kosmos.frameworks.lattice.LatticeIndexFunction
import org.vorpal.kosmos.frameworks.lattice.RecurrenceLattice
import org.vorpal.kosmos.frameworks.lattice.asIndexFunction
import org.vorpal.kosmos.laws.core.IndexFunctionLaws
import org.vorpal.kosmos.laws.core.IndexableLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

class PrimeLatticeSpec : FunSpec({
    lawTests(lawGraph("prime lattice") {
        // --- Smoke tests -------------------------------------------------------
        local("PrimeLattice basic smoke tests") {
            PrimeLattice.index(1) shouldBe 2.toBigInteger()     // p₁ = 2
            PrimeLattice.index(2) shouldBe 3.toBigInteger()     // p₂ = 3
            PrimeLattice.index(3) shouldBe 5.toBigInteger()     // p₃ = 5
        }

        // --- Functional tests --------------------------------------------------
        local("first 10 primes via lattice index (1-based)") {
            val got = (1..10).map(PrimeLattice::index)
            val exp = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29).map(Int::toBigInteger)
            got shouldContainExactly exp
        }

        local("LatticeIndexFunction base yields primes") {
            val p = PrimeLattice.asIndexFunction() // n ↦ p_n
            val primes = p.values().take(10).toList()
            primes shouldContainExactly listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29).map(Int::toBigInteger)
        }

        local("repeat(2) yields primes with primality at least 2, i.e. p_{p_n}") {
            val p2 = PrimeLattice.asIndexFunction().repeat(2)
            val super2primes = p2.values().take(10).toList()
            super2primes shouldContainExactly listOf(3, 5, 11, 17, 31, 41, 59, 67, 83, 109).map(Int::toBigInteger)
        }

        local("repeat(3) yields primes with primality at least 3, i.e. p_{p_{p_n}}}") {
            val p3 = PrimeLattice.asIndexFunction().repeat(3)
            val super3primes = p3.values().take(10).toList()
            super3primes shouldContainExactly listOf(5, 11, 31, 59, 127, 179, 277, 331, 431, 599).map(Int::toBigInteger)
        }

        local("row(1) yield primes starting at position p_1 and growing in primality") {
            val p = PrimeLattice.row(1).take(10).toList()
            p shouldContainExactly listOf(2, 3, 5, 11, 31, 127, 709, 5381, 52711, 648391).map(Int::toBigInteger)
        }

        local("row(2) yield primes starting at position p_2 and growing in primality") {
            val p = PrimeLattice.row(2).take(9).toList()
            p shouldContainExactly listOf(3, 5, 11, 31, 127, 709, 5381, 52711, 648391).map(Int::toBigInteger)
        }

        local("row(4) yields primes starting at position p_4 and growing in primality") {
            val p4 = PrimeLattice.row(4).take(7).toList()
            p4 shouldContainExactly listOf(7, 17, 59, 277, 1787, 15299, 167449).map(Int::toBigInteger)
        }

        local("PrimeLattice satisfies IndexableLaws") {
            val ctx = IndexFunctionLaws.Context<
                    LatticeIndexFunction<RecurrenceLattice<BigInteger>>
                    >(
                Id = LatticeIndexFunction.id(PrimeLattice),
                sample = { LatticeIndexFunction.base(PrimeLattice) },
                pure = { f -> LatticeIndexFunction.pure(PrimeLattice, f) }
            )

            val i: LatticeAsIndexable<RecurrenceLattice<BigInteger>> =
                LatticeAsIndexable(PrimeLattice, "PrimeLattice")

            IndexableLaws.sanity(
                IndexableLaws.Config(
                    name = "PrimeLattice",
                    indexable = i,
                    ctx = ctx,
                    expectNone = true,
                    range = 1..1500
                )
            )
        }
    })
})
