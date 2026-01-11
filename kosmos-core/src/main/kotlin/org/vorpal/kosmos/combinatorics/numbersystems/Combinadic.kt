package org.vorpal.kosmos.combinatorics.numbersystems

import org.vorpal.kosmos.combinatorics.Binomial
import java.math.BigInteger

/**
 * Combinadic (combinatorial number system) representation of a k-combination in colex order.
 *
 * This type represents a k-element subset of [0, n) as a strictly increasing list of indices.
 * The colex rank of indices a1 < a2 < ... < ak is:
 *
 *    rank = C(a1, 1) + C(a2, 2) + ... + C(ak, k)
 *
 * Unranking is the greedy inverse of this formula.
 *
 * Digits here are not radix digits: they are the chosen indices.
 */
@ConsistentCopyVisibility
data class Combinadic internal constructor(
    val n: Int,
    val indices: List<Int>
) {
    val k: Int =
        indices.size

    init {
        require(n >= 0) { "n must be nonnegative: $n" }
        require(indices.all { it in 0 until n }) { "Indices out of range [0, $n): $indices" }
        require(indices.zipWithNext().all { (a, b) -> a < b }) { "Indices must be strictly increasing: $indices" }
    }

    /**
     * Decode to the colex rank: Σ C(indices[i], i+1).
     */
    fun decode(): BigInteger =
        indices.withIndex().fold(BigInteger.ZERO) { acc, (idx, a) ->
            acc + Binomial(a, idx + 1)
        }

    override fun toString(): String =
        if (indices.isEmpty()) "{}"
        else indices.joinToString(prefix = "{", postfix = "}", separator = ",")

    companion object {
        /**
         * Encode (unrank) the k-combination of [0, n) with the given colex rank.
         *
         * @throws IllegalArgumentException if rank is out of range [0, C(n,k) - 1].
         */
        fun encode(n: Int, k: Int, rank: BigInteger): Combinadic {
            require(n >= 0) { "n must be nonnegative: $n" }
            require(k in 0..n) { "k must be in 0..n: (n=$n, k=$k)" }
            require(rank >= BigInteger.ZERO) { "rank must be nonnegative: $rank" }

            val maxRank = Binomial(n, k) - BigInteger.ONE
            require(k == 0 || rank <= maxRank) {
                "rank out of range for (n=$n,k=$k): $rank (max=$maxRank)"
            }

            fun maxA(upper: Int, i: Int, r: BigInteger): Int {
                tailrec fun bs(lo: Int, hi: Int): Int {
                    if (lo >= hi) return hi
                    val mid = (lo + hi + 1) / 2
                    val c = Binomial(mid, i)

                    return if (c <= r) bs(mid, hi)
                    else bs(lo, mid - 1)
                }

                return bs(i - 1, upper)
            }

            tailrec fun build(i: Int, upper: Int, r: BigInteger, accDesc: MutableList<Int>): List<Int> {
                if (i == 0) {
                    return accDesc.asReversed()
                }

                val a = maxA(upper, i, r)
                accDesc.add(a)

                return build(i - 1, a - 1, r - Binomial(a, i), accDesc)
            }

            val indices =
                if (k == 0) {
                    emptyList()
                } else {
                    build(k, n - 1, rank, mutableListOf())
                }

            return Combinadic(n, indices)
        }

        /**
         * Construct directly from indices (validation-only, no normalization).
         */
        fun fromIndices(n: Int, indices: List<Int>): Combinadic =
            Combinadic(n, indices)

        /**
         * Generate the equivalent of a zero for a given n and k.
         */
        fun zero(n: Int, k: Int): Combinadic =
            encode(n, k, BigInteger.ZERO)

        fun max(n: Int, k: Int): Combinadic =
            encode(n, k, Binomial(n, k) - BigInteger.ONE)

        fun one(n: Int, k: Int): Combinadic {
            require(Binomial(n, k) > BigInteger.ONE) { "ONE is undefined for (n=$n,k=$k) with only one combination" }
            return encode(n, k, BigInteger.ONE)
        }
    }
}
