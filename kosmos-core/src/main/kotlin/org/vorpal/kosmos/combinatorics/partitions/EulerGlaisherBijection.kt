package org.vorpal.kosmos.combinatorics.partitions

/**
 * The Euler–Glaisher bijection between partitions into odd parts and
 * partitions into distinct parts of the same weight.
 *
 * The functions reject partitions outside their respective domains.
 */
object EulerGlaisherBijection {
    /**
     * Given a partition into odd parts, convert it to the corresponding partition
     * into distinct parts by interpreting each odd part's multiplicity as a binary expansion.
     */
    fun oddPartitionToDistinctPartition(partition: Partition): Partition {
        require(partition.hasOnlyOddParts) {
            "Expected a partition only containing odd parts, but got: $partition"
        }

        val result = buildList {
            partition.parts
                .groupingBy { it }
                .eachCount()
                .forEach { (oddPart, multiplicity) ->
                    var remaining = multiplicity
                    var powerTwo = 1

                    while (remaining > 0) {
                        if (remaining and 1 == 1)
                            add(oddPart * powerTwo)
                        remaining = remaining ushr 1
                        powerTwo *= 2
                    }
                }
        }

        return Partition.of(result.sortedDescending())
    }

    /**
     * Given a partition into distinct parts, convert it to the corresponding partition
     * into odd parts by writing every part as 2^k q, where q is odd.
     */
    fun distinctPartitionToOddPartition(partition: Partition): Partition {
        require(partition.hasDistinctParts) {
            "Expected a partition only containing distinct parts, but got: $partition"
        }

        val result = buildList {
            for (part in partition.parts) {
                var oddPart = part
                var multiplicity = 1

                while (oddPart and 1 == 0) {
                    oddPart = oddPart shr 1
                    multiplicity = multiplicity shl 1
                }

                repeat(multiplicity) {
                    add(oddPart)
                }
            }
        }

        return Partition.of(result.sortedDescending())
    }
}
