package org.vorpal.kosmos.frameworks.lattice.instances

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.frameworks.lattice.*
import org.vorpal.kosmos.laws.core.IndexFunctionLaws
import org.vorpal.kosmos.laws.core.IndexableLaws
import java.math.BigInteger

private fun bi(n: Int) = BigInteger.valueOf(n.toLong())

class FibonacciLatticeSpec : StringSpec({

    // --- Smoke ---
    "FibonacciLattice basic smoke tests" {
        FibonacciLattice.index(1) shouldBe bi(1)
        FibonacciLattice.index(2) shouldBe bi(1)
        FibonacciLattice.index(3) shouldBe bi(2)
    }

    // --- Sequence values ---
    "first 11 Fibonacci numbers via lattice index (1-based)" {
        val got = (1..10).map(FibonacciLattice::index)
        val exp = listOf(1, 1, 2, 3, 5, 8, 13, 21, 34, 55).map(::bi)
        got shouldContainExactly exp
    }

    "FibonacciLattice satisfies IndexableLaws" {
        val lawsCtx = IndexFunctionLaws.Context<
                LatticeIndexFunction<RecurrenceLattice<BigInteger>>
                >(
            Id = LatticeIndexFunction.id(FibonacciLattice),
            sample = { LatticeIndexFunction.base(FibonacciLattice) },
            pure = { f -> LatticeIndexFunction.pure(FibonacciLattice, f) }
        )

        println("Running IndexableLaws for FibonacciLattice...")

        // two fixed points F_1 = 1 and F_5 = 5
        IndexableLaws.sanity(
            IndexableLaws.Config(
                name = "FibonacciLattice",
                indexable = LatticeAsIndexable(FibonacciLattice, "FibonacciLattice"),
                ctx = lawsCtx,
                expectNone = false,
                expectedFixedPoints = listOf(1, 5),
                range = 1..500
            )
        )
    }
})
