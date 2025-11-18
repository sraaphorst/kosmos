package org.vorpal.kosmos.graphs

import io.kotest.property.RandomSource
import java.util.TreeSet

/**
 * Unordered distinct pairs from a list (i < j).
 */
fun <V> unorderedPairs(vs: List<V>): List<Pair<V, V>> {
    val out = ArrayList<Pair<V, V>>()
    for (i in 0 until vs.size) {
        for (j in i + 1 until vs.size) {
            out += vs[i] to vs[j]
        }
    }
    return out
}

/**
 * Ordered pairs (u, v) with u ≠ v.
 */
fun <V> orderedPairsNoLoops(vs: List<V>): List<Pair<V, V>> {
    val out = ArrayList<Pair<V, V>>()
    for (i in 0 until vs.size) {
        for (j in 0 until vs.size) {
            if (i == j) continue
            out += vs[i] to vs[j]
        }
    }
    return out
}

/**
 * Uniform k-subset of [pool] without replacement.
 * Assumes 0 ≤ k ≤ pool.size.
 */
fun <T> chooseK(rs: RandomSource, pool: List<T>, k: Int): List<T> {
    require(k in 0..pool.size) { "chooseK: k=$k out of bounds for pool size=${pool.size}" }
    if (k == 0) return emptyList()
    if (k == pool.size) return pool.toList()
    val idx = (pool.indices).toMutableList()
    idx.shuffle(rs.random)
    val chosen = idx.subList(0, k).sorted()
    return chosen.map { pool[it] }
}

/**
 * Random spanning tree on [vs] via Prüfer code (uniform among all labeled trees).
 * Returns edges as unordered pairs.
 */
fun <V> pruferTreeEdges(vs: List<V>, rs: RandomSource): List<Pair<V, V>> {
    val n = vs.size
    require(n >= 2) { "pruferTreeEdges requires at least 2 vertices" }
    val deg = IntArray(n) { 1 }
    val code = IntArray(n - 2)
    for (i in code.indices) {
        val pick = rs.random.nextInt(n)
        code[i] = pick
        deg[pick] += 1
    }
    val leaves = TreeSet<Int>()
    for (i in 0 until n) if (deg[i] == 1) leaves.add(i)
    val edges = ArrayList<Pair<V, V>>(n - 1)
    for (x in code) {
        val leaf = leaves.first()
        leaves.remove(leaf)
        edges += vs[leaf] to vs[x]
        deg[leaf] -= 1
        deg[x] -= 1
        if (deg[x] == 1) leaves.add(x)
    }
    val u = leaves.first()
    val v = leaves.last()
    edges += vs[u] to vs[v]
    return edges
}