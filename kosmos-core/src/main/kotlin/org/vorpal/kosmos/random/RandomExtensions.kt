package org.vorpal.kosmos.random

import kotlin.random.Random

/**
 * Perform a functional shuffle of a [Collection] to generate a randomly shuffled [List].
 * A [Random] can be supplied: else [Random.Default] is used.
 * The original collection is unchanged.
 */
fun <A> Collection<A>.shuffledFunctional(random: Random = Random.Default): List<A> {
    tailrec fun aux(remaining: List<A> = this.toList(), acc: List<A> = emptyList()): List<A> {
        if (remaining.isEmpty()) return acc
        val i = random.nextInt(remaining.size)
        val picked = remaining[i]
        val newRemaining = remaining.take(i) + remaining.drop(i + 1)
        return aux(newRemaining, acc + picked)
    }
    return aux()
}

/**
 * Perform an imperative shuffle of a [Collection] to generate a randomly shuffled [List].
 * A [Random] can be supplied: else [Random.Default] is used.
 * The original collection is unchanged.
 */
fun <A> Collection<A>.shuffled(random: Random = Random.Default): List<A> {
    val shuffled = toMutableList()
    for (i in shuffled.lastIndex downTo 1) {
        val j = random.nextInt(i + 1)
        shuffled[i] = shuffled[j].also { shuffled[j] = shuffled[i] }
    }
    return shuffled.toList()
}
