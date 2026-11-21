package org.vorpal.kosmos.combinatorics

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Unit tests for the [Semifactorial] recurrence.
 *
 * Verifies the first few values of the double factorial function n!!
 * against the canonical sequence (OEIS A006882).
 */
class SemifactorialSpec : FunSpec({
    lawTests(lawGraph("semifactorials") {
        // Sequence: 0!!, 1!!, 2!! ... 19!!
        val expected = listOf(
            1, 1, 2, 3, 8, 15, 48, 105, 384, 945,
            3840, 10395, 46080, 135135, 645120, 2027025,
            10321920, 34459425, 185794560, 654729075
        ).map(Int::toBigInteger)

        local("Semifactorial numbers match the first known values (OEIS A006882)") {
            expected.forEachIndexed { n, expectedVal ->
                Semifactorial(n) shouldBe expectedVal
            }
        }

        local("Verifies the recursive property n!! = n * (n-2)!!") {
            // Verify consistency for a value likely outside the immediate cache of 'expected'
            val n = 25
            Semifactorial(n) shouldBe (n.toBigInteger() * Semifactorial(n - 2))
        }

        local("Negative n throws an exception") {
            // Your implementation uses error(), which throws IllegalStateException
            shouldThrow<IllegalStateException> {
                Semifactorial(-1)
            }
        }

        local("Iterator produces correct prefix of sequence") {
            val values = Semifactorial.take(expected.size).toList()
            values shouldBe expected
        }
    })
})