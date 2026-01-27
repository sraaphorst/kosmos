package org.vorpal.kosmos.graphs.sparse

import java.util.Arrays

/**
 * Compressed Spare Row Adjacency is a convenient way of storing edge adjacencies in graphs.
 * We do not allow loops when the builder's `allowLoops` is `False` (default).
 * We do not allow parallel edges when builder's `sortAndDedupe` is `True` (default).
 */
class CsrAdjacency internal constructor(
    val vertexCount: Int,
    var offsets: IntArray,
    var neighbors: IntArray,
) {
    init {
        require(vertexCount >= 0)
        require(offsets.size == vertexCount + 1)
        require(offsets[0] == 0)
        require(offsets[vertexCount] == neighbors.size)
        var i = 0
        while (i < vertexCount) {
            require(offsets[i] <= offsets[i + 1])
            i += 1
        }
    }

    fun rangeOf(v: Int): IntRange =
        offsets[v] until offsets[v + 1]
}

/**
 * A builder to construct a CsrAdjacency object.
 * Users must go through the builder to generate a [CsrAdjacency] instance.
 */
class CsrAdjacencyBuilder(
    private val vertexCount: Int,
    private val allowLoops: Boolean = false,
    private val sortAndDedupe: Boolean = true,
) {
    private val src = ArrayList<Int>()
    private val dst = ArrayList<Int>()

    fun addArc(u: Int, v: Int) {
        require(u in 0 until vertexCount)
        require(v in 0 until vertexCount)
        if (!allowLoops) require(u != v) { "loops not allowed: $u -> $v" }
        src.add(u)
        dst.add(v)
    }

    /**
     * Given this configured CsrAdjacencyBuilder, if indicated, sort and dedupe it, and then produce the
     * CsrAdjacency object that is represented.
     */
    fun build(): CsrAdjacency {
        val m = src.size
        val degrees = IntArray(vertexCount)

        var i = 0
        while (i < m) {
            degrees[src[i]] += 1
            i += 1
        }

        val offsets = IntArray(vertexCount + 1)
        var v = 0
        while (v < vertexCount) {
            offsets[v + 1] = offsets[v] + degrees[v]
            v += 1
        }

        val next = offsets.copyOf()
        val neighbors = IntArray(m)

        i = 0
        while (i < m) {
            val u = src[i]
            val w = dst[i]
            val pos = next[u]
            neighbors[pos] = w
            next[u] = pos + 1
            i += 1
        }

        val g = CsrAdjacency(vertexCount, offsets, neighbors)

        if (sortAndDedupe) {
            sortAndDedupeInPlace(g)
        }

        return g
    }

    private fun sortAndDedupeInPlace(g: CsrAdjacency) {
        // 1) sort each row segment in-place
        var v = 0
        while (v < g.vertexCount) {
            val start = g.offsets[v]
            val end = g.offsets[v + 1]
            Arrays.sort(g.neighbors, start, end)
            v += 1
        }

        // 2) compute new degrees after dedupe (without allocating per-row lists)
        val newOffsets = IntArray(g.vertexCount + 1)

        v = 0
        while (v < g.vertexCount) {
            val start = g.offsets[v]
            val end = g.offsets[v + 1]

            var count = 0
            var i = start
            var prev = Int.MIN_VALUE

            while (i < end) {
                val cur = g.neighbors[i]
                if (cur != prev) {
                    count += 1
                    prev = cur
                }
                i += 1
            }

            newOffsets[v + 1] = newOffsets[v] + count
            v += 1
        }

        // 3) fill compacted neighbor array
        val newNeighbors = IntArray(newOffsets[g.vertexCount])

        v = 0
        while (v < g.vertexCount) {
            val start = g.offsets[v]
            val end = g.offsets[v + 1]

            var write = newOffsets[v]
            var i = start
            var prev = Int.MIN_VALUE

            while (i < end) {
                val cur = g.neighbors[i]
                if (cur != prev) {
                    newNeighbors[write] = cur
                    write += 1
                    prev = cur
                }
                i += 1
            }

            v += 1
        }

        g.offsets = newOffsets
        g.neighbors = newNeighbors
    }
}