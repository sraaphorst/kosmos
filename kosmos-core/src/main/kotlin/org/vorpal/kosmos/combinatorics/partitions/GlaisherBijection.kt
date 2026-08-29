package org.vorpal.kosmos.combinatorics.partitions

/**
 * Glaisher's theorem, and the bijection witnessing it.
 *
 * For any modulus `k >= 2`, the number of partitions of `n` into parts none of
 * which is divisible by `k` (*k-regular* partitions, see [Partition.hasNoPartDivisibleBy])
 * equals the number of partitions of `n` in which every part's multiplicity is
 * strictly less than `k` (see [Partition.hasMultiplicitiesLessThan]).
 *
 * The case k = 2 is Euler's theorem: odd-part partitions correspond to distinct-part
 * partitions (see [EulerGlaisherBijection], which implements exactly that case).
 *
 * ### The idea
 *
 * Write each part's multiplicity in base k. If a k-regular part `p` occurs `m` times,
 * expand `m = Σᵢ dᵢ kⁱ` in base k (each digit `0 <= dᵢ < k`), and replace those `m`
 * copies of `p` with `dᵢ` copies of `p·kⁱ` for every `i` with `dᵢ > 0`. Since `p` is
 * not divisible by `k`, the values `p·kⁱ` for different `(p, i)` pairs are pairwise
 * distinct (the exponent `i` is recoverable from `p·kⁱ` by repeatedly dividing out
 * factors of `k`), so no output value ever accumulates `k` or more copies. Reading
 * the construction backwards - strip the largest power of `k` dividing each part,
 * and re-expand its multiplicity as that power - inverts the map.
 *
 * For example, with k = 3 the part 5 (5-regular... well, 3-regular: 5 is not divisible
 * by 3) occurring 4 times has multiplicity 4 = 1·3⁰ + 1·3¹ in base 3, so `{5,5,5,5}`
 * maps to `{15,5}` (one copy of 5·3⁰, one copy of 5·3¹); each of those occurs only
 * once, so the multiplicity bound (< 3) holds.
 *
 * Both directions preserve weight and are mutual inverses on their respective
 * domains; see `GlaisherBijectionSpec` in kosmos-testkit.
 */
object GlaisherBijection {
    /**
     * Map a k-regular partition (see [Partition.hasNoPartDivisibleBy]) to the
     * corresponding partition with multiplicities strictly less than [k], by writing
     * each part's multiplicity in base [k] and distributing its digits across powers
     * of [k] times that part.
     *
     * @param partition a k-regular partition: `partition.hasNoPartDivisibleBy(k)` must hold.
     * @param k the modulus; must be at least 2.
     * @return a partition of the same weight in which every part occurs fewer than [k] times.
     * @throws IllegalArgumentException if `k < 2` or [partition] is not k-regular.
     * @throws ArithmeticException if scaling a part by a power of [k] would overflow [Int]
     *   (via [Math.multiplyExact]).
     */
    fun kRegularToBoundedMultiplicities(
        partition: Partition,
        k: Int
    ): Partition {
        require(k >= 2) {
            "k must be at least 2, but was: $k"
        }
        require(partition.hasNoPartDivisibleBy(k)) {
            "Expected a $k-regular partition, but got: $partition"
        }

        val result = buildList {
            partition.parts
                .groupingBy { it }
                .eachCount()
                .forEach { (part, multiplicity) ->
                    var remaining = multiplicity
                    var scaledPart = part

                    while (remaining > 0) {
                        val digit = remaining % k
                        repeat(digit) {
                            add(scaledPart)
                        }

                        remaining /= k
                        if (remaining > 0)
                            scaledPart = Math.multiplyExact(scaledPart, k)
                    }
                }
        }

        return Partition.of(result.sortedDescending())
    }

    /**
     * Map a partition whose multiplicities are strictly less than [k]
     * (see [Partition.hasMultiplicitiesLessThan]) to the corresponding k-regular
     * partition, by stripping the largest power of [k] dividing each part and
     * re-expanding it as that many copies of the remaining, now k-regular, factor.
     *
     * This is the inverse of [kRegularToBoundedMultiplicities]: applying one after
     * the other, in either order, recovers the original partition.
     *
     * @param partition a partition whose multiplicities are strictly less than k:
     *   `partition.hasMultiplicitiesLessThan(k)` must hold.
     * @param k the modulus; must be at least 2.
     * @return a k-regular partition of the same weight.
     * @throws IllegalArgumentException if `k < 2` or some part of [partition] occurs k or more times.
     */
    fun boundedMultiplicitiesToKRegular(
        partition: Partition,
        k: Int
    ): Partition {
        require(k >= 2) {
            "k must be at least 2, but was $k"
        }
        require(partition.hasMultiplicitiesLessThan(k)) {
            "Expected every part to occur fewer than $k times, but got: $partition"
        }

        val result = buildList {
            for (part in partition.parts) {
                var kRegularPart = part
                var multiplicity = 1

                while (kRegularPart % k == 0) {
                    kRegularPart /= k
                    // multiplicity * kRegularPart == part is an invariant of this loop, and
                    // kRegularPart >= k on every iteration that reaches here, so multiplicity
                    // can never exceed part (which is already a valid, positive Int). Unlike
                    // the forward direction, Math.multiplyExact can't actually overflow here
                    // for any valid Partition; it's kept as cheap, harmless insurance in case
                    // that invariant is ever weakened.
                    multiplicity = Math.multiplyExact(multiplicity, k)
                }

                repeat(multiplicity) {
                    add(kRegularPart)
                }
            }
        }

        return Partition.of(result.sortedDescending())
    }
}
