package org.vorpal.kosmos.frameworks.noise

import org.vorpal.kosmos.combinatorics.Permutation

sealed interface PermutationStrategy {
    fun generate(n: Int, seed: Long): Permutation<Int>
}

object FisherYates
object Unranked