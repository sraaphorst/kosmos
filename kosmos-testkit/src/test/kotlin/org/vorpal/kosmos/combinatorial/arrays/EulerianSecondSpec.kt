package org.vorpal.kosmos.combinatorial.arrays

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigInteger

class EulerianSecondSpec : StringSpec({

    "Eulerian second kind base cases" {
        EulerianSecond(0, 0) shouldBe BigInteger.ONE
        EulerianSecond(1, 0) shouldBe BigInteger.ONE
    }

    "Eulerian second kind matches known small triangle" {
        val expected = listOf(
            listOf(1),
            listOf(1),
            listOf(1, 2),
            listOf(1, 8, 6),
            listOf(1, 22, 58, 24),
            listOf(1, 52, 328, 444, 120)
        ).map { row -> row.map { BigInteger.valueOf(it.toLong()) } }

        expected.forEachIndexed { n, row ->
            val actual = (0 until row.size).map { k -> EulerianSecond(n, k) }
            actual shouldBe row
        }
    }

    "Eulerian second kind vanish outside valid range" {
        EulerianSecond(4, -1) shouldBe BigInteger.ZERO
        EulerianSecond(4, 4) shouldBe BigInteger.ZERO
    }
})