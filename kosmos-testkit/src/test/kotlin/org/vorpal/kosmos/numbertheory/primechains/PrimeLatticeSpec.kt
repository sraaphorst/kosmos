package org.vorpal.kosmos.numbertheory.primechains

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.frameworks.lattice.LatticeAsIndexable
import org.vorpal.kosmos.frameworks.lattice.LatticeIndexFunction
import org.vorpal.kosmos.frameworks.lattice.asIndexFunction
import org.vorpal.kosmos.frameworks.lattice.RecurrenceLattice
import org.vorpal.kosmos.laws.core.IndexFunctionLaws
import org.vorpal.kosmos.laws.core.IndexableFixedPointLaws
import org.vorpal.kosmos.laws.core.IndexableLaws
import java.math.BigInteger

class PrimeLatticeSpec : StringSpec({

    // --- Smoke tests -------------------------------------------------------
    "PrimeLattice basic smoke tests" {
        PrimeLattice.index(1) shouldBe 2.toBigInteger()     // p₁ = 2
        PrimeLattice.index(2) shouldBe 3.toBigInteger()     // p₂ = 3
        PrimeLattice.index(3) shouldBe 5.toBigInteger()     // p₃ = 5
    }

    // --- Functional tests --------------------------------------------------
    "first 10 primes via lattice index (1-based)" {
        val got = (1..10).map(PrimeLattice::index)
        val exp = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29).map(Int::toBigInteger)
        got shouldContainExactly exp
    }

    "LatticeIndexFunction base yields primes; repeat(2) yields superprimes p_{p_n}" {
        val p = PrimeLattice.asIndexFunction() // n ↦ p_n
        val primes = p.values().take(10).toList()
        primes shouldContainExactly listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29).map(Int::toBigInteger)

        val p2 = p.repeat(2) // n ↦ p_{p_n}
        val superprimes = p2.values().take(10).toList()
        superprimes shouldContainExactly listOf(3, 5, 11, 17, 31, 41, 59, 67, 83, 109).map(Int::toBigInteger)
    }

    // --- Lawkit via adapter ------------------------------------------------

    "no fixed points p_n = n in range 1..2000" {
        // Concrete adapter type so generics are fully determined
        val i: LatticeAsIndexable<RecurrenceLattice<BigInteger>> =
            LatticeAsIndexable(PrimeLattice, "PrimeLattice")

        val ctx = IndexableFixedPointLaws.Context(
            instance = i,
            range = 1..2000,
            expectNone = true
        )
        IndexableFixedPointLaws.verifyFixedPoints(ctx)
    }

    "PrimeLattice satisfies IndexableLaws" {
        // Concrete types for both T and S:
        // T = LatticeIndexFunction<RecurrenceLattice<BigInteger>>
        // S = LatticeAsIndexable<RecurrenceLattice<BigInteger>>
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
