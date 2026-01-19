package org.vorpal.kosmos.graphs

import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.*
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.math.nextReal
import kotlin.math.min

/**
 * A suite of Kotest generators for adjacency-set graphs.
 *
 * Conventions:
 *  • All undirected generators exclude loops and multi-edges.
 *  • All directed generators exclude loops and parallel arcs.
 *  • Size ranges are inclusive.
 *
 * You typically provide a vertex Arb (e.g., `Arb.int()` or `Arb.string()`) and a size range.
 * These generators will draw a fresh vertex set (no duplicates) and then add edges/arcs accordingly.
 */
object ArbGraph {

    /* ============================
     *  Undirected: Erdős–Rényi G(n,p)
     * ============================ */

    /**
     * Erdős–Rényi undirected G(n, p) with n ∈ [nRange], fixed p.
     *
     * Excludes loops and multi-edges. Edges are sampled independently.
     */
    fun <V : Any> undirectedGnP(
        vertexArb: Arb<V>,
        nRange: IntRange,
        p: Real
    ): Arb<AdjacencySetUndirectedGraph<V>> = arbitrary { rs ->
        require(p in 0.0..1.0) { "p must be in [0,1], got $p" }
        val n = Arb.int(nRange).next(rs)
        val vs: List<V> = Arb.set(vertexArb, size = n).next(rs).toList()
        val pairs = unorderedPairs(vs)
        val edges = pairs.mapNotNull { (u, w) ->
            if (rs.random.nextReal() < p) UndirectedEdge(u, w) else null
        }
        AdjacencySetUndirectedGraph.of(
            vs.toUnorderedFiniteSet(),
            edges.toUnorderedFiniteSet()
        )
    }

    /* ============================
     *  Undirected: GnM (uniform m)
     * ============================ */

    /**
     * Undirected G(n, m) with n ∈ [nRange], m ∈ [mRange] intersect [0, C(n,2)].
     *
     * Excludes loops and multi-edges. Chooses m distinct edges uniformly at random.
     */
    fun <V : Any> undirectedGnM(
        vertexArb: Arb<V>,
        nRange: IntRange,
        mRange: IntRange
    ): Arb<AdjacencySetUndirectedGraph<V>> = arbitrary { rs ->
        val n = Arb.int(nRange).next(rs)
        val vs: List<V> = Arb.set(vertexArb, size = n).next(rs).toList()
        val pairs = unorderedPairs(vs)
        val maxM = pairs.size
        val mBounded = mRange.first.coerceAtLeast(0)..min(mRange.last, maxM)
        val m = Arb.int(mBounded).next(rs)
        val chosen = chooseK(rs, pairs, m)
        val edges = chosen.map { (u, w) -> UndirectedEdge(u, w) }
        AdjacencySetUndirectedGraph.of(
            vs.toUnorderedFiniteSet(),
            edges.toUnorderedFiniteSet()
        )
    }

    /**
     * Undirected G(n, m) **connected** by construction: builds a random spanning tree (via Prüfer)
     * and then adds m − (n − 1) extra edges uniformly from remaining non-tree pairs.
     *
     * If requested m < n − 1, we clamp to m' = n − 1 to keep connectivity.
     */
    fun <V : Any> undirectedGnMConnected(
        vertexArb: Arb<V>,
        nRange: IntRange,
        mRange: IntRange
    ): Arb<AdjacencySetUndirectedGraph<V>> = arbitrary { rs ->
        val n = Arb.int(nRange).next(rs)
        val vs: List<V> = Arb.set(vertexArb, size = n).next(rs).toList()
        if (n <= 1) {
            return@arbitrary AdjacencySetUndirectedGraph.edgeless(vs.toUnorderedFiniteSet())
        }
        val allPairs = unorderedPairs(vs)
        val treePairs = pruferTreeEdges(vs, rs)
        val treeSet = treePairs.toSet()
        val remaining = allPairs.filterNot { it in treeSet }
        val minM = n - 1
        val maxM = allPairs.size
        val mReq = Arb.int(mRange).next(rs)
        val m = mReq.coerceIn(minM, maxM)
        val extrasNeeded = m - minM
        val extras = if (extrasNeeded > 0) chooseK(rs, remaining, extrasNeeded) else emptyList()
        val edges = (treePairs + extras).map { (u, w) -> UndirectedEdge(u, w) }
        AdjacencySetUndirectedGraph.of(
            vs.toUnorderedFiniteSet(),
            edges.toUnorderedFiniteSet()
        )
    }

    /* ===============================================
     *  Undirected: d-regular (configuration model)
     * =============================================== */

    /**
     * Simple undirected d-regular graph with n ∈ [nRange], d ∈ [dRange].
     *
     * Uses the configuration model with rejection:
     *  • Requires 0 ≤ d < n and n·d even.
     *  • Retries internal pairing a bounded number of times to avoid loops/multi-edges.
     */
    fun <V : Any> undirectedDRegular(
        vertexArb: Arb<V>,
        nRange: IntRange,
        dRange: IntRange,
        maxAttempts: Int = 256
    ): Arb<AdjacencySetUndirectedGraph<V>> = arbitrary { rs ->
        val n = Arb.int(nRange).next(rs)
        val vs: List<V> = Arb.set(vertexArb, size = n).next(rs).toList()
        if (n <= 1) {
            return@arbitrary AdjacencySetUndirectedGraph.edgeless(vs.toUnorderedFiniteSet())
        }
        val d = Arb.int(dRange).next(rs).coerceIn(0, n - 1)
        require((n * d) % 2 == 0) { "No simple d-regular graph possible for n=$n, d=$d (n·d must be even)" }
        val idxPairs = configurationModelSimple(rs, n, d, maxAttempts)
        val edges = idxPairs.map { (i, j) -> UndirectedEdge(vs[i], vs[j]) }
        AdjacencySetUndirectedGraph.of(
            vs.toUnorderedFiniteSet(),
            edges.toUnorderedFiniteSet()
        )
    }

    /* ============================
     *  Directed: Erdős–Rényi G(n,p)
     * ============================ */

    /**
     * Directed G(n, p) with n ∈ [nRange], fixed p, no loops.
     *
     * Each ordered pair (u, v), u ≠ v, is included independently with probability p.
     */
    fun <V : Any> directedGnP(
        vertexArb: Arb<V>,
        nRange: IntRange,
        p: Real
    ): Arb<AdjacencySetDirectedGraph<V>> = arbitrary { rs ->
        require(p in 0.0..1.0) { "p must be in [0,1], got $p" }
        val n = Arb.int(nRange).next(rs)
        val vs: List<V> = Arb.set(vertexArb, size = n).next(rs).toList()
        val pairs = orderedPairsNoLoops(vs)
        val edges = pairs.mapNotNull { (u, w) ->
            if (rs.random.nextReal() < p) DirectedEdge(u, w) else null
        }
        AdjacencySetDirectedGraph.of(
            vs.toUnorderedFiniteSet(),
            edges.toUnorderedFiniteSet()
        )
    }

    /* ============================
     *  Directed: GnM with strongness
     * ============================ */

    /**
     * Directed G(n, m) that is **strongly connected** by construction:
     *  • starts with a Hamiltonian cycle to ensure strong connectivity,
     *  • adds m − n extra arcs uniformly from remaining ordered pairs, no loops.
     *
     * If requested m < n, we clamp to m' = n to keep strong connectivity.
     */
    fun <V : Any> directedGnMStrongByCycle(
        vertexArb: Arb<V>,
        nRange: IntRange,
        mRange: IntRange
    ): Arb<AdjacencySetDirectedGraph<V>> = arbitrary { rs ->
        val n = Arb.int(nRange).next(rs)
        val vs: MutableList<V> = Arb.set(vertexArb, size = n).next(rs).toMutableList()
        if (n <= 1) {
            return@arbitrary AdjacencySetDirectedGraph.edgeless(vs.toUnorderedFiniteSet())
        }
        vs.shuffle(rs.random)
        val cycle = (0 until n).map { i ->
            val a = vs[i]
            val b = vs[(i + 1) % n]
            DirectedEdge(a, b)
        }
        val existing = cycle.map { it.from to it.to }.toSet()
        val allPairs = orderedPairsNoLoops(vs)
        val remaining = allPairs.filterNot { it in existing }
        val minM = n
        val maxM = allPairs.size
        val mReq = Arb.int(mRange).next(rs)
        val m = mReq.coerceIn(minM, maxM)
        val extrasNeeded = m - minM
        val extras = if (extrasNeeded > 0) chooseK(rs, remaining, extrasNeeded) else emptyList()
        val edges = (cycle + extras.map { (u, w) -> DirectedEdge(u, w) })
        AdjacencySetDirectedGraph.of(
            vs.toUnorderedFiniteSet(),
            edges.toUnorderedFiniteSet()
        )
    }

    /**
     * Directed G(n, m) that is **weakly connected** by construction:
     *  • builds an undirected random spanning tree via Prüfer,
     *  • orients each tree edge randomly either way (one arc per tree edge),
     *  • adds m − (n − 1) extra arcs from remaining ordered pairs.
     *
     * If requested m < n − 1, we clamp to m' = n − 1 to keep weak connectivity.
     */
    fun <V : Any> directedGnMWeakByTree(
        vertexArb: Arb<V>,
        nRange: IntRange,
        mRange: IntRange
    ): Arb<AdjacencySetDirectedGraph<V>> = arbitrary { rs ->
        val n = Arb.int(nRange).next(rs)
        val vs: List<V> = Arb.set(vertexArb, size = n).next(rs).toList()
        if (n <= 1) {
            return@arbitrary AdjacencySetDirectedGraph.edgeless(vs.toUnorderedFiniteSet())
        }
        val undirectedTree = pruferTreeEdges(vs, rs)
        val treeArcs = undirectedTree.map { (a, b) ->
            if (rs.random.nextBoolean()) DirectedEdge(a, b) else DirectedEdge(b, a)
        }
        val existing = treeArcs.map { it.from to it.to }.toSet()
        val allPairs = orderedPairsNoLoops(vs)
        val remaining = allPairs.filterNot { it in existing }
        val minM = n - 1
        val maxM = allPairs.size
        val mReq = Arb.int(mRange).next(rs)
        val m = mReq.coerceIn(minM, maxM)
        val extrasNeeded = m - minM
        val extras = if (extrasNeeded > 0) chooseK(rs, remaining, extrasNeeded) else emptyList()
        val edges = (treeArcs + extras.map { (u, w) -> DirectedEdge(u, w) })
        AdjacencySetDirectedGraph.of(
            vs.toUnorderedFiniteSet(),
            edges.toUnorderedFiniteSet()
        )
    }

    /* ==============================================
     *  Directed: d-regular (in = out = d), no loops
     * ============================================== */

    /**
     * Simple directed d-regular digraph with n ∈ [nRange], d ∈ [dRange], no loops and no parallel arcs.
     *
     * Construction: take d independent random derangements π₁..π_d on V and include arcs v → π_i(v).
     * Retries if parallel arcs collide until reaching `maxAttempts`.
     *
     * Requires 0 ≤ d ≤ n − 1. For n ≤ 1, returns edgeless.
     */
    fun <V : Any> directedDRegular(
        vertexArb: Arb<V>,
        nRange: IntRange,
        dRange: IntRange,
        maxAttempts: Int = 256
    ): Arb<AdjacencySetDirectedGraph<V>> = arbitrary { rs ->
        val n = Arb.int(nRange).next(rs)
        val vs: List<V> = Arb.set(vertexArb, size = n).next(rs).toList()
        if (n <= 1) {
            return@arbitrary AdjacencySetDirectedGraph.edgeless(vs.toUnorderedFiniteSet())
        }
        val d = Arb.int(dRange).next(rs).coerceIn(0, n - 1)
        if (d == 0) {
            return@arbitrary AdjacencySetDirectedGraph.edgeless(vs.toUnorderedFiniteSet())
        }
        var attempt = 0
        while (attempt < maxAttempts) {
            attempt += 1
            val arcs = mutableSetOf<Pair<Int, Int>>()
            var ok = true
            repeat(d) {
                val perm = randomDerangementIndices(n, rs)
                for (i in 0 until n) {
                    val j = perm[i]
                    val pair = i to j
                    if (!arcs.add(pair)) {
                        ok = false
                        break
                    }
                }
                if (!ok) return@repeat
            }
            if (ok) {
                val edges = arcs.map { (i, j) -> DirectedEdge(vs[i], vs[j]) }
                return@arbitrary AdjacencySetDirectedGraph.of(
                    vs.toUnorderedFiniteSet(),
                    edges.toUnorderedFiniteSet()
                )
            }
        }
        error("directedDRegular: failed to build after $maxAttempts attempts for n=$n, d=$d")
    }

    /* ==================================
     *  Undirected: Havel–Hakimi degrees
     * ================================== */

    /**
     * Simple undirected graph with exact degree sequence d(v) drawn from [degArb],
     * validated by Havel–Hakimi and realized as a simple graph if graphical.
     *
     * If sampled sequence is non-graphical, this generator retries up to [maxAttempts].
     */
    fun <V : Any> undirectedWithDegreeSequence(
        vertexArb: Arb<V>,
        nRange: IntRange,
        degArb: (n: Int) -> Arb<List<Int>>,
        maxAttempts: Int = 128
    ): Arb<AdjacencySetUndirectedGraph<V>> = arbitrary { rs ->
        val n = Arb.int(nRange).next(rs)
        val vs: List<V> = Arb.set(vertexArb, size = n).next(rs).toList()
        if (n == 0) {
            return@arbitrary AdjacencySetUndirectedGraph.edgeless(FiniteSet.empty())
        }
        var attempt = 0
        while (attempt < maxAttempts) {
            attempt += 1
            val degs = degArb(n).next(rs)
            if (degs.size != n) continue
            if (!isGraphicalDegreeSequence(degs)) continue
            val pairs = havelHakimiRealize(vs, degs)
            if (pairs != null) {
                val edges = pairs.map { (u, w) -> UndirectedEdge(u, w) }
                return@arbitrary AdjacencySetUndirectedGraph.of(
                    vs.toUnorderedFiniteSet(),
                    edges.toUnorderedFiniteSet()
                )
            }
        }
        error("undirectedWithDegreeSequence: could not realize a simple graph after $maxAttempts attempts")
    }

    /* =========================
     *  Internal helper kernels
     * ========================= */

    // Configuration model with rejection to avoid loops and multi-edges
    private fun configurationModelSimple(
        rs: RandomSource,
        n: Int,
        d: Int,
        maxAttempts: Int
    ): List<Pair<Int, Int>> {
        val stubsTotal = n * d
        val stubsProto = ArrayList<Int>(stubsTotal)
        var attempt = 0
        while (attempt < maxAttempts) {
            attempt += 1
            stubsProto.clear()
            repeat(n) { v ->
                repeat(d) { stubsProto.add(v) }
            }
            stubsProto.shuffle(rs.random)
            val edges = ArrayList<Pair<Int, Int>>(stubsTotal / 2)
            val seen = HashSet<Pair<Int, Int>>()
            var ok = true
            var i = 0
            while (i < stubsProto.size) {
                val a = stubsProto[i]
                val b = stubsProto[i + 1]
                i += 2
                if (a == b) {
                    ok = false
                    break
                }
                val u = minOf(a, b)
                val w = maxOf(a, b)
                val e = u to w
                if (!seen.add(e)) {
                    ok = false
                    break
                }
                edges.add(e)
            }
            if (ok) return edges
        }
        error("configurationModelSimple: failed after $maxAttempts attempts")
    }

    // Derangement on indices [0, n) using shuffle + repair
    private fun randomDerangementIndices(n: Int, rs: RandomSource): IntArray {
        require(n >= 2) { "derangement requires n ≥ 2, got $n" }
        val p = IntArray(n) { it }
        p.asListMutable().shuffle(rs.random)
        var fixed = indicesWithFixedPoints(p)
        if (fixed.isEmpty()) return p
        if (fixed.size == 1) {
            val i = fixed.first()
            val j = if (i == 0) 1 else 0
            p.swap(i, j)
            fixed = indicesWithFixedPoints(p)
            if (fixed.isEmpty()) return p
        }
        for (k in fixed.indices step 2) {
            if (k + 1 < fixed.size) {
                val i = fixed[k]
                val j = fixed[k + 1]
                p.swap(i, j)
            }
        }
        if (indicesWithFixedPoints(p).isNotEmpty()) {
            return randomDerangementIndices(n, rs)
        }
        return p
    }

    private fun indicesWithFixedPoints(p: IntArray): IntArray {
        val out = ArrayList<Int>()
        for (i in p.indices) if (p[i] == i) out.add(i)
        return out.toIntArray()
    }

    // Mutable view of IntArray for shuffle
    private fun IntArray.asListMutable(): MutableList<Int> = object : MutableList<Int> {
        override val size: Int get() = this@asListMutable.size
        override fun add(element: Int): Boolean = throw UnsupportedOperationException()
        override fun add(index: Int, element: Int) = throw UnsupportedOperationException()
        override fun addAll(index: Int, elements: Collection<Int>): Boolean = throw UnsupportedOperationException()
        override fun addAll(elements: Collection<Int>): Boolean = throw UnsupportedOperationException()
        override fun contains(element: Int): Boolean = element in this
        override fun containsAll(elements: Collection<Int>): Boolean = elements.all { it in this }
        override fun clear() = throw UnsupportedOperationException()
        override fun get(index: Int): Int = this@asListMutable[index]
        override fun indexOf(element: Int): Int = this@asListMutable.indexOf(element)
        override fun isEmpty(): Boolean = this@asListMutable.isEmpty()
        override fun iterator(): MutableIterator<Int> = listIterator()
        override fun lastIndexOf(element: Int): Int = this@asListMutable.lastIndexOf(element)
        override fun listIterator(): MutableListIterator<Int> = listIterator(0)
        override fun listIterator(index: Int): MutableListIterator<Int> = object : MutableListIterator<Int> {
            private var i = index
            override fun hasNext(): Boolean = i < size
            override fun hasPrevious(): Boolean = i > 0
            override fun next(): Int = this@asListMutable[i++]
            override fun nextIndex(): Int = i
            override fun previous(): Int = this@asListMutable[--i]
            override fun previousIndex(): Int = i - 1
            override fun add(element: Int) = throw UnsupportedOperationException()
            override fun remove() = throw UnsupportedOperationException()
            override fun set(element: Int) {
                this@asListMutable[i - 1] = element
            }
        }
        override fun remove(element: Int): Boolean = throw UnsupportedOperationException()
        override fun removeAll(elements: Collection<Int>): Boolean = throw UnsupportedOperationException()
        override fun removeAt(index: Int): Int = throw UnsupportedOperationException()
        override fun retainAll(elements: Collection<Int>): Boolean = throw UnsupportedOperationException()
        override fun set(index: Int, element: Int): Int {
            val old = this@asListMutable[index]
            this@asListMutable[index] = element
            return old
        }
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<Int> = throw UnsupportedOperationException()
    }

    private fun IntArray.swap(i: Int, j: Int) {
        val t = this[i]
        this[i] = this[j]
        this[j] = t
    }

    // Havel–Hakimi: test graphical
    private fun isGraphicalDegreeSequence(d: List<Int>): Boolean {
        if (d.any { it < 0 }) return false
        if (d.sum() % 2 != 0) return false
        var seq = d.sortedDescending().toMutableList()
        while (seq.isNotEmpty()) {
            val k = seq.removeAt(0)
            if (k == 0) return true
            if (k > seq.size) return false
            for (i in 0 until k) {
                val v = seq[i] - 1
                if (v < 0) return false
                seq[i] = v
            }
            seq.sortDescending()
        }
        return true
    }

    // Havel–Hakimi: build a realization on the provided vertex list, or null if failed
    private fun <V : Any> havelHakimiRealize(vs: List<V>, degrees: List<Int>): List<Pair<V, V>>? {
        data class Node<V : Any>(val v: V, var d: Int)
        val nodes = vs.zip(degrees).map { Node(it.first, it.second) }.toMutableList()
        if (nodes.any { it.d < 0 }) return null
        val edges = mutableListOf<Pair<V, V>>()
        while (true) {
            nodes.sortByDescending { it.d }
            if (nodes.isEmpty()) break
            if (nodes[0].d == 0) break
            val u = nodes.removeAt(0)
            val k = u.d
            if (k > nodes.size) return null
            for (i in 0 until k) {
                val w = nodes[i]
                w.d -= 1
                if (w.d < 0) return null
                edges += u.v to w.v
            }
            u.d = 0
        }
        // remove duplicates and self loops, then check degrees match
        val simple = edges.map { (a, b) ->
            if (a === b) return null
            if (a.hashCode() <= b.hashCode()) a to b else b to a
        }.toSet()
        return simple.toList()
    }
}