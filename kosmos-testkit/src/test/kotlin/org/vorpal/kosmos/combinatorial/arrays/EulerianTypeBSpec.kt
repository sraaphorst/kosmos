package org.vorpal.kosmos.combinatorial.arrays

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigInteger

class EulerianTypeBSpec : StringSpec({

    "Type B Eulerian base cases" {
        EulerianTypeB(0, 0) shouldBe BigInteger.ONE
        EulerianTypeB(1, 0) shouldBe BigInteger.ONE
        EulerianTypeB(1, 1) shouldBe BigInteger.ONE
    }

    "Type B Eulerian numbers match known small triangle" {
        val expected = listOf(
            listOf(1),
            listOf(1, 1),
            listOf(1, 6, 1),
            listOf(1, 23, 23, 1),
            listOf(1, 76, 230, 76, 1)
        ).map { row -> row.map { BigInteger.valueOf(it.toLong()) } }

        expected.forEachIndexed { n, row ->
            val actual = (0..n).map { k -> EulerianTypeB(n, k) }
            actual shouldBe row
        }
    }

    "Type B Eulerian numbers vanish outside valid range" {
        EulerianTypeB(3, -1) shouldBe BigInteger.ZERO
        EulerianTypeB(3, 4) shouldBe BigInteger.ZERO
    }
})