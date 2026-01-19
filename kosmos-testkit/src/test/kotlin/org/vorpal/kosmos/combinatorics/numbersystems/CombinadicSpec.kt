package org.vorpal.kosmos.combinatorics.numbersystems

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import org.vorpal.kosmos.combinatorics.Binomial
import org.vorpal.kosmos.combinatorics.numbersystems.render.CombinadicPrintable
import org.vorpal.kosmos.core.random.nextBigInteger
import java.math.BigInteger

class CombinadicSpec : FunSpec({

    // Generator for (n, k) pairs where 0 <= k <= n
    val nkPairs = arbitrary { rs ->
        val n = rs.random.nextInt(0, 20)
        val k = rs.random.nextInt(0, n + 1)
        Pair(n, k)
    }

    // Generator for valid (n, k, rank) triples
    val validRankTriple = arbitrary { rs ->
        val n = rs.random.nextInt(0, 15)
        val k = rs.random.nextInt(0, n + 1)

        val maxRank = Binomial(n, k) // = C(n,k)
        val rank =
            if (maxRank == BigInteger.ZERO) BigInteger.ZERO
            else rs.random.nextBigInteger(maxRank) // uniform in [0, C(n,k))

        Triple(n, k, rank)
    }

    // Generator for valid Combinadic instances
    val validCombinadic = arbitrary { rs ->
        val n = rs.random.nextInt(0, 20)
        val k = rs.random.nextInt(0, n + 1)

        if (k == 0) {
            Combinadic.fromIndices(n, emptyList())
        } else {
            // Generate k distinct elements from [0, n) and sort
            val indices = (0 until n).shuffled(rs.random).take(k).sorted()
            Combinadic.fromIndices(n, indices)
        }
    }

    context("empty combination special case") {
        test("empty indices decode to rank 0") {
            Combinadic.fromIndices(5, emptyList()).decode() shouldBe BigInteger.ZERO
            Combinadic.fromIndices(0, emptyList()).decode() shouldBe BigInteger.ZERO
            Combinadic.fromIndices(10, emptyList()).decode() shouldBe BigInteger.ZERO
        }

        test("rank 0 with k=0 encodes to empty indices") {
            Combinadic.encode(5, 0, BigInteger.ZERO).indices shouldBe emptyList()
            Combinadic.encode(0, 0, BigInteger.ZERO).indices shouldBe emptyList()
            Combinadic.encode(10, 0, BigInteger.ZERO).indices shouldBe emptyList()
        }
    }

    context("round trip properties") {
        test("encode → decode is identity") {
            checkAll(validRankTriple) { (n, k, rank) ->
                val encoded = Combinadic.encode(n, k, rank)
                val decoded = encoded.decode()
                decoded shouldBe rank
            }
        }

        test("decode → encode is identity for valid representations") {
            checkAll(validCombinadic) { comb ->
                val decoded = comb.decode()
                val encoded = Combinadic.encode(comb.n, comb.k, decoded)
                encoded.indices shouldBe comb.indices
            }
        }
    }

    context("structural properties") {
        test("encoded indices are strictly increasing") {
            checkAll(validRankTriple) { (n, k, rank) ->
                val encoded = Combinadic.encode(n, k, rank)
                encoded.indices.zipWithNext().all { (a, b) -> a < b } shouldBe true
            }
        }

        test("encoded indices are within [0, n)") {
            checkAll(validRankTriple) { (n, k, rank) ->
                val encoded = Combinadic.encode(n, k, rank)
                encoded.indices.all { it in 0 until n } shouldBe true
            }
        }

        test("encoded result has exactly k indices") {
            checkAll(validRankTriple) { (n, k, rank) ->
                val encoded = Combinadic.encode(n, k, rank)
                encoded.k shouldBe k
                encoded.indices.size shouldBe k
            }
        }

        test("encoded result has correct n") {
            checkAll(validRankTriple) { (n, k, rank) ->
                val encoded = Combinadic.encode(n, k, rank)
                encoded.n shouldBe n
            }
        }
    }

    context("known values") {
        test("k=1 combinations (single elements)") {
            // For k=1, rank i corresponds to indices [i]
            // C(i, 1) = i, so rank = i
            for (i in 0 until 5) {
                val comb = Combinadic.encode(5, 1, i.toBigInteger())
                comb.indices shouldBe listOf(i)
                comb.decode() shouldBe i.toBigInteger()
            }
        }

        test("k=2 combinations from 5 elements in colex order") {
            // Colex order for C(5,2):
            // rank 0: {0,1} -> C(0,1) + C(1,2) = 0 + 0 = 0
            // rank 1: {0,2} -> C(0,1) + C(2,2) = 0 + 1 = 1
            // rank 2: {1,2} -> C(1,1) + C(2,2) = 1 + 1 = 2
            // rank 3: {0,3} -> C(0,1) + C(3,2) = 0 + 3 = 3
            // rank 4: {1,3} -> C(1,1) + C(3,2) = 1 + 3 = 4
            // rank 5: {2,3} -> C(2,1) + C(3,2) = 2 + 3 = 5
            // rank 6: {0,4} -> C(0,1) + C(4,2) = 0 + 6 = 6
            // rank 7: {1,4} -> C(1,1) + C(4,2) = 1 + 6 = 7
            // rank 8: {2,4} -> C(2,1) + C(4,2) = 2 + 6 = 8
            // rank 9: {3,4} -> C(3,1) + C(4,2) = 3 + 6 = 9
            val expected = listOf(
                listOf(0, 1), // rank 0
                listOf(0, 2), // rank 1
                listOf(1, 2), // rank 2
                listOf(0, 3), // rank 3
                listOf(1, 3), // rank 4
                listOf(2, 3), // rank 5
                listOf(0, 4), // rank 6
                listOf(1, 4), // rank 7
                listOf(2, 4), // rank 8
                listOf(3, 4)  // rank 9
            )

            expected.forEachIndexed { rank, indices ->
                val encoded = Combinadic.encode(5, 2, rank.toBigInteger())
                encoded.indices shouldBe indices
                Combinadic.fromIndices(5, indices).decode() shouldBe rank.toBigInteger()
            }
        }

        test("k=3 combinations from 5 elements in colex order") {
            // Colex order for C(5,3):
            // rank 0: {0,1,2} -> C(0,1) + C(1,2) + C(2,3) = 0 + 0 + 0 = 0
            // rank 1: {0,1,3} -> C(0,1) + C(1,2) + C(3,3) = 0 + 0 + 1 = 1
            // rank 2: {0,2,3} -> C(0,1) + C(2,2) + C(3,3) = 0 + 1 + 1 = 2
            // rank 3: {1,2,3} -> C(1,1) + C(2,2) + C(3,3) = 1 + 1 + 1 = 3
            // rank 4: {0,1,4} -> C(0,1) + C(1,2) + C(4,3) = 0 + 0 + 4 = 4
            // rank 5: {0,2,4} -> C(0,1) + C(2,2) + C(4,3) = 0 + 1 + 4 = 5
            // rank 6: {1,2,4} -> C(1,1) + C(2,2) + C(4,3) = 1 + 1 + 4 = 6
            // rank 7: {0,3,4} -> C(0,1) + C(3,2) + C(4,3) = 0 + 3 + 4 = 7
            // rank 8: {1,3,4} -> C(1,1) + C(3,2) + C(4,3) = 1 + 3 + 4 = 8
            // rank 9: {2,3,4} -> C(2,1) + C(3,2) + C(4,3) = 2 + 3 + 4 = 9
            val testCases = listOf(
                0 to listOf(0, 1, 2),
                1 to listOf(0, 1, 3),
                2 to listOf(0, 2, 3),
                3 to listOf(1, 2, 3),
                4 to listOf(0, 1, 4),
                5 to listOf(0, 2, 4),
                6 to listOf(1, 2, 4),
                7 to listOf(0, 3, 4),
                8 to listOf(1, 3, 4),
                9 to listOf(2, 3, 4)
            )

            testCases.forEach { (rank, indices) ->
                Combinadic.encode(5, 3, rank.toBigInteger()).indices shouldBe indices
                Combinadic.fromIndices(5, indices).decode() shouldBe rank.toBigInteger()
            }
        }

        test("full combination (k=n)") {
            // {0,1,2,3,4} is the only 5-combination of 5 elements
            val comb = Combinadic.encode(5, 5, BigInteger.ZERO)
            comb.indices shouldBe listOf(0, 1, 2, 3, 4)
            comb.decode() shouldBe BigInteger.ZERO
        }

        test("larger known values") {
            // C(10, 4) = 210 combinations
            // rank 0: {0,1,2,3}
            Combinadic.encode(10, 4, BigInteger.ZERO).indices shouldBe listOf(0, 1, 2, 3)

            // rank 209 (max): {6,7,8,9}
            Combinadic.encode(10, 4, 209.toBigInteger()).indices shouldBe listOf(6, 7, 8, 9)

            // Verify decode matches
            Combinadic.fromIndices(10, listOf(6, 7, 8, 9)).decode() shouldBe 209.toBigInteger()
        }
    }

    context("encode error handling") {
        test("rejects negative n") {
            shouldThrow<IllegalArgumentException> {
                Combinadic.encode(-1, 0, BigInteger.ZERO)
            }
        }

        test("rejects k > n") {
            shouldThrow<IllegalArgumentException> {
                Combinadic.encode(3, 4, BigInteger.ZERO)
            }

            shouldThrow<IllegalArgumentException> {
                Combinadic.encode(0, 1, BigInteger.ZERO)
            }
        }

        test("rejects negative k") {
            shouldThrow<IllegalArgumentException> {
                Combinadic.encode(5, -1, BigInteger.ZERO)
            }
        }

        test("rejects negative rank") {
            shouldThrow<IllegalArgumentException> {
                Combinadic.encode(5, 2, (-1).toBigInteger())
            }

            checkAll(100, Arb.bigInt(-1000..-1)) { negative ->
                shouldThrow<IllegalArgumentException> {
                    Combinadic.encode(5, 2, negative)
                }
            }
        }

        test("rejects rank >= C(n,k)") {
            // C(5,2) = 10, so max rank is 9
            shouldThrow<IllegalArgumentException> {
                Combinadic.encode(5, 2, 10.toBigInteger())
            }

            shouldThrow<IllegalArgumentException> {
                Combinadic.encode(5, 2, 100.toBigInteger())
            }

            // C(6,3) = 20, so max rank is 19
            shouldThrow<IllegalArgumentException> {
                Combinadic.encode(6, 3, 20.toBigInteger())
            }

            // C(10,5) = 252
            shouldThrow<IllegalArgumentException> {
                Combinadic.encode(10, 5, 252.toBigInteger())
            }
        }

        test("accepts valid boundary ranks") {
            // C(5,2) = 10, rank 9 is valid
            val comb1 = Combinadic.encode(5, 2, 9.toBigInteger())
            comb1.indices shouldBe listOf(3, 4)

            // C(6,3) = 20, rank 19 is valid
            val comb2 = Combinadic.encode(6, 3, 19.toBigInteger())
            comb2.indices shouldBe listOf(3, 4, 5)
        }
    }

    context("fromIndices error handling") {
        test("rejects negative n") {
            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(-1, emptyList())
            }

            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(-5, listOf(0, 1))
            }
        }

        test("rejects indices out of range (too large)") {
            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(5, listOf(5)) // 5 is out of [0, 5)
            }

            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(5, listOf(0, 1, 6))
            }

            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(3, listOf(0, 1, 3))
            }
        }

        test("rejects negative indices") {
            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(5, listOf(-1))
            }

            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(5, listOf(-1, 0, 1))
            }
        }

        test("rejects duplicate indices") {
            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(5, listOf(1, 1, 2))
            }

            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(5, listOf(0, 0))
            }

            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(10, listOf(2, 4, 4, 6))
            }
        }

        test("rejects non-increasing indices") {
            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(5, listOf(2, 1, 3))
            }

            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(5, listOf(0, 2, 1))
            }

            shouldThrow<IllegalArgumentException> {
                Combinadic.fromIndices(10, listOf(5, 3, 7))
            }
        }

        test("accepts valid edge cases") {
            // Single element
            Combinadic.fromIndices(5, listOf(0)).indices shouldBe listOf(0)
            Combinadic.fromIndices(5, listOf(4)).indices shouldBe listOf(4)

            // Full set
            Combinadic.fromIndices(4, listOf(0, 1, 2, 3)).indices shouldBe listOf(0, 1, 2, 3)

            // Empty with various n
            Combinadic.fromIndices(0, emptyList()).indices shouldBe emptyList()
            Combinadic.fromIndices(100, emptyList()).indices shouldBe emptyList()
        }
    }

    context("mathematical properties") {
        test("number of valid combinations equals C(n,k)") {
            val testCases = listOf(
                Pair(5, 2) to 10,
                Pair(6, 3) to 20,
                Pair(4, 0) to 1,
                Pair(4, 4) to 1,
                Pair(7, 3) to 35,
                Pair(8, 4) to 70
            )

            testCases.forEach { (nk, expected) ->
                val (n, k) = nk
                Binomial(n, k) shouldBe expected.toBigInteger()

                // Verify all ranks in [0, C(n,k)-1] produce valid distinct combinations
                val allCombinations = (0 until expected).map { rank ->
                    Combinadic.encode(n, k, rank.toBigInteger()).indices
                }
                allCombinations.distinct().size shouldBe expected
            }
        }

        test("minimum rank has smallest indices {0, 1, ..., k-1}") {
            checkAll(100, nkPairs.filter { (_, k) -> k > 0 }) { (n, k) ->
                val minComb = Combinadic.encode(n, k, BigInteger.ZERO)
                minComb.indices shouldBe (0 until k).toList()
            }
        }

        test("maximum rank has largest indices {n-k, n-k+1, ..., n-1}") {
            checkAll(100, nkPairs.filter { (_, k) -> k > 0 }) { (n, k) ->
                val maxRank = Binomial(n, k) - BigInteger.ONE
                val maxComb = Combinadic.encode(n, k, maxRank)
                maxComb.indices shouldBe ((n - k) until n).toList()
            }
        }

        test("colex ordering produces unique combinations for all ranks") {
            checkAll(100, nkPairs.filter { (n, k) -> k > 0 && Binomial(n, k) <= 500.toBigInteger() }) { (n, k) ->
                val maxRank = Binomial(n, k).intValueExact()
                val allIndices = (0 until maxRank).map { rank ->
                    Combinadic.encode(n, k, rank.toBigInteger()).indices
                }
                allIndices.distinct().size shouldBe maxRank
            }
        }

        test("decode formula: rank = Σ C(indices[i], i+1)") {
            checkAll(validCombinadic) { comb ->
                val expectedRank = comb.indices.withIndex().fold(BigInteger.ZERO) { acc, (idx, a) ->
                    acc + Binomial(a, idx + 1)
                }
                comb.decode() shouldBe expectedRank
            }
        }
    }

    context("toString formatting") {
        test("empty indices format as {}") {
            Combinadic.fromIndices(5, emptyList()).toString() shouldBe "{}"
            Combinadic.fromIndices(0, emptyList()).toString() shouldBe "{}"
        }

        test("non-empty indices format with braces and commas") {
            Combinadic.fromIndices(5, listOf(0, 2, 4)).toString() shouldBe "{0,2,4}"
            Combinadic.fromIndices(10, listOf(1, 3, 5, 7)).toString() shouldBe "{1,3,5,7}"
            Combinadic.fromIndices(5, listOf(2)).toString() shouldBe "{2}"
        }
    }

    context("zero / max / one helpers") {

        test("zero(n,k) is rank 0 and yields {0,1,...,k-1}") {
            checkAll(200, nkPairs) { (n, k) ->
                val z = Combinadic.zero(n, k)

                z.n shouldBe n
                z.k shouldBe k
                z.decode() shouldBe BigInteger.ZERO
                z.indices shouldBe (0 until k).toList()
            }
        }

        test("max(n,k) is rank C(n,k)-1 and yields {n-k,...,n-1}") {
            checkAll(200, nkPairs) { (n, k) ->
                val m = Combinadic.max(n, k)

                m.n shouldBe n
                m.k shouldBe k

                val expectedRank = Binomial(n, k) - BigInteger.ONE
                m.decode() shouldBe expectedRank
                m.indices shouldBe ((n - k) until n).toList()
            }
        }

        test("one(n,k) is rank 1 when defined") {
            checkAll(200, nkPairs) { (n, k) ->
                val total = Binomial(n, k)

                if (total <= BigInteger.ONE) {
                    shouldThrow<IllegalArgumentException> {
                        Combinadic.one(n, k)
                    }
                } else {
                    val o = Combinadic.one(n, k)

                    o.n shouldBe n
                    o.k shouldBe k
                    o.decode() shouldBe BigInteger.ONE

                    // Also sanity-check it matches encode(…, rank=1)
                    val e = Combinadic.encode(n, k, BigInteger.ONE)
                    o.indices shouldBe e.indices
                }
            }
        }

        test("zero(n,k) and max(n,k) coincide exactly when C(n,k)=1") {
            checkAll(200, nkPairs) { (n, k) ->
                val total = Binomial(n, k)

                if (total == BigInteger.ONE) {
                    Combinadic.zero(n, k).indices shouldBe Combinadic.max(n, k).indices
                    Combinadic.zero(n, k).decode() shouldBe BigInteger.ZERO
                    Combinadic.max(n, k).decode() shouldBe BigInteger.ZERO
                }
            }
        }

        test("max(n,k) equals encode(n, k, C(n,k) - 1) and zero(n, k) equals encode(n, k, 0)") {
            checkAll(200, nkPairs) { (n, k) ->
                val z = Combinadic.zero(n, k)
                val m = Combinadic.max(n, k)

                z.indices shouldBe Combinadic.encode(n, k, BigInteger.ZERO).indices

                val maxRank = Binomial(n, k) - BigInteger.ONE
                m.indices shouldBe Combinadic.encode(n, k, maxRank).indices
            }
        }
    }

    test("CombinadicPrintable produces expected results") {
        CombinadicPrintable(Combinadic.fromIndices(5, listOf(0, 2, 4))) shouldBe "(0 2 4)_C(5,3)"
        CombinadicPrintable(Combinadic.fromIndices(10, emptyList())) shouldBe "()_C(10,0)"
        CombinadicPrintable(Combinadic.encode(6, 2, 5.toBigInteger())) shouldBe "(2 3)_C(6,2)"
        CombinadicPrintable(Combinadic.encode(5, 3, BigInteger.ZERO)) shouldBe "(0 1 2)_C(5,3)"
    }
})