package org.vorpal.kosmos.random

import kotlin.random.Random

fun <A> List<A>.shuffledFunctional(random: Random): List<A> {
    tailrec fun go(remaining: List<A>, acc: List<A>): List<A> {
        if (remaining.isEmpty()) return acc
        val i = random.nextInt(remaining.size)
        val picked = remaining[i]
        val newRemaining = remaining.take(i) + remaining.drop(i + 1)
        return go(newRemaining, acc + picked)
    }
    return go(this, emptyList())
}

fun <A> Collection<A>.shuffled(random: Random): List<A> {
    val shuffled = toMutableList()
    for (i in shuffled.lastIndex downTo 1) {
        val j = random.nextInt(i + 1)
        shuffled[i] = shuffled[j].also { shuffled[j] = shuffled[i] }
    }
    return shuffled.toList()
}
