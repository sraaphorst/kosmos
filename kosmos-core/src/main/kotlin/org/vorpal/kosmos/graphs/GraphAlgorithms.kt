
package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.toUnorderedFiniteSet
import org.vorpal.kosmos.functional.datastructures.Either

/* ==========================================================
 *  Distances / eccentricity / radius / diameter (undirected)
 * ==========================================================*/

/**
 * BFS distances from [source] to all vertices in its connected component.
 * Returns a map `v ↦ d(source, v)` for the vertices reachable from [source].
 */
fun <V: Any> UndirectedGraph<V>.distancesFrom(source: V): Map<V, Int> {
    require(source in vertices) { "distancesFrom: source $source not in vertex set." }

    data class S<V: Any>(val dist: MutableMap<V, Int>)
    val init = S<V>(mutableMapOf())
    init.dist[source] = 0

    val out: S<V> = bfsFoldFrom(
        start = source,
        initial = init,
        onDiscover = { s, _ -> s }, // already seeded source; tree edges set others
        onEdge = { s, v, w, tree ->
            if (tree) s.dist[w] = s.dist.getValue(v) + 1
            s
        }
    )
    return out.dist
}

/**
 * Eccentricity of [v]: max shortest‑path distance from [v] to any vertex in its component;
 * null if graph disconnected.
 */
fun <V: Any> UndirectedGraph<V>.eccentricity(v: V): Int? {
    if (vertices.isEmpty) return 0
    if (!isConnected()) return null
    val d = distancesFrom(v)
    return d.values.maxOrNull() ?: 0
}

/** Radius: minimum eccentricity; null if graph disconnected. */
fun <V: Any> UndirectedGraph<V>.radius(): Int? {
    if (vertices.isEmpty) return 0
    if (!isConnected()) return null
    var best = Int.MAX_VALUE
    for (v in vertices) {
        val ecc = eccentricity(v) ?: return null
        if (ecc < best) best = ecc
    }
    return best
}

/** Diameter: maximum eccentricity; null if graph disconnected. */
fun <V: Any> UndirectedGraph<V>.diameter(): Int? {
    if (vertices.isEmpty) return 0
    if (!isConnected()) return null
    var worst = 0
    for (v in vertices) {
        val ecc = eccentricity(v) ?: return null
        if (ecc > worst) worst = ecc
    }
    return worst
}

/* ===========================
 *  Bipartiteness (undirected)
 * ===========================*/

/** Result container for bridges and articulation points. */
data class BridgeArt<V: Any>(
    val bridges: Set<UndirectedEdge<V>>,
    val articulations: Set<V>
)

/**
 * Either returns an odd cycle (as a vertex sequence whose first and last are adjacent)
 * or a bipartition `(Left, Right)` of the vertex set.
 */
fun <V: Any> UndirectedGraph<V>.bipartitionOrOddCycle():
        Either<List<V>, Pair<FiniteSet.Unordered<V>, FiniteSet.Unordered<V>>> {

    val color = mutableMapOf<V, Boolean>()
    val parent = mutableMapOf<V, V?>()

    fun pathToRoot(v0: V): MutableList<V> {
        val p = mutableListOf<V>()
        var x: V? = v0
        while (x != null) {
            p += x
            x = parent[x]
        }
        return p
    }

    fun rebuildOddCycle(v: V, w: V): List<V> {
        val pv = pathToRoot(v)
        val pw = pathToRoot(w)
        var lca: V? = null
        while (pv.isNotEmpty() && pw.isNotEmpty() && pv.last() == pw.last()) {
            lca = pv.last()
            pv.removeLast()
            pw.removeLast()
        }
        val left = if (lca != null) pv + lca else pv
        val right = pw.asReversed()
        return left + right + v
    }

    val res = bfsFoldComponentsStop(
        initial = Unit,
        onDiscover = { _, v ->
            if (v !in color) {
                color[v] = false
                parent[v] = null
            }
            FoldStep.Continue(Unit)
        },
        onEdge = { _, v, w, tree ->
            val cv = color.getValue(v)
            val cw = color[w]
            if (tree) {
                color[w] = !cv
                parent[w] = v
                FoldStep.Continue(Unit)
            } else {
                if (cw != null && cw == cv) FoldStep.Stop(rebuildOddCycle(v, w))
                else FoldStep.Continue(Unit)
            }
        }
    )

    return when (res) {
        is Either.Left -> res
        is Either.Right -> {
            val left = color.filterValues { !it }.keys.toUnorderedFiniteSet()
            val right = color.filterValues { it }.keys.toUnorderedFiniteSet()
            Either.Right(left to right)
        }
    }
}

/** True iff the graph is bipartite. */
fun <V: Any> UndirectedGraph<V>.isBipartite(): Boolean =
    when (bipartitionOrOddCycle()) {
        is Either.Left -> false
        is Either.Right -> true
    }

/** The bipartition if bipartite, else null. */
fun <V: Any> UndirectedGraph<V>.bipartition(): Pair<FiniteSet.Unordered<V>, FiniteSet.Unordered<V>>? =
    when (val r = bipartitionOrOddCycle()) {
        is Either.Left -> null
        is Either.Right -> r.value
    }

/** An odd cycle if non‑bipartite, else null. */
fun <V: Any> UndirectedGraph<V>.oddCycle(): List<V>? =
    when (val r = bipartitionOrOddCycle()) {
        is Either.Left -> r.value
        is Either.Right -> null
    }

/* =====================================
 *  Bridges & articulations (undirected)
 * =====================================*/

/** Tarjan‑style computation of bridges and articulation points using iterative DFS. */
fun <V: Any> UndirectedGraph<V>.bridgesAndArticulations(): BridgeArt<V> {
    data class S<V: Any>(
        val disc: MutableMap<V, Int>,
        val low:  MutableMap<V, Int>,
        val parent: MutableMap<V, V?>,
        val rootChildren: MutableMap<V, Int>,
        var time: Int,
        val bridges: MutableSet<UndirectedEdge<V>>,
        val articulations: MutableSet<V>
    )

    val init = S<V>(
        disc = mutableMapOf(),
        low = mutableMapOf(),
        parent = mutableMapOf(),
        rootChildren = mutableMapOf(),
        time = 0,
        bridges = mutableSetOf(),
        articulations = mutableSetOf()
    )

    val s = dfsFoldComponentsWithFinish(
        vertices = vertices,
        neighbors = { v -> neighbors(v) },
        initial = init,
        onRoot = { st, r ->
            st.parent[r] = null
            st.rootChildren[r] = 0
            st
        },
        onDiscover = { st, v ->
            st.disc[v] = st.time
            st.low[v]  = st.time
            st.time += 1
            st
        },
        onExamine = { st, v, w, tree ->
            if (tree) {
                st.parent[w] = v
                if (st.parent[v] == null) {
                    st.rootChildren[v] = st.rootChildren.getOrDefault(v, 0) + 1
                }
            } else if (st.parent[v] != w) { // back‑edge in undirected; ignore edge to parent
                st.low[v] = minOf(st.low.getValue(v), st.disc.getValue(w))
            }
            st
        },
        onFinish = { st, v ->
            val p = st.parent[v]
            if (p != null) {
                // propagate lowlink upward
                st.low[p] = minOf(st.low.getValue(p), st.low.getValue(v))

                // bridge: no back‑edge from v‑subtree above p
                if (st.low.getValue(v) > st.disc.getValue(p)) {
                    st.bridges += UndirectedEdge(p, v)
                }

                // articulation (non‑root): removing p disconnects v‑subtree
                if (st.parent[p] != null && st.low.getValue(v) >= st.disc.getValue(p)) {
                    st.articulations += p
                }
            } else {
                // root articulation: at least two DFS children
                if (st.rootChildren.getValue(v) >= 2) st.articulations += v
            }
            st
        }
    )

    return BridgeArt(bridges = s.bridges.toSet(), articulations = s.articulations.toSet())
}

/* ===============================
 *  SCCs & condensation (directed)
 * ===============================*/

/** Kosaraju’s algorithm: vertex sets of all SCCs (implementation‑agnostic over [DirectedGraph]). */
internal fun <V: Any> DirectedGraph<V>.stronglyConnectedComponentSets(): FiniteSet<FiniteSet<V>> {
    // 1) first pass: DFS postorder on G
    val order = finishingOrder()
    val orderDescending = order.asReversed()

    // 2) transpose
    val gt: DirectedGraph<V> = this.toTransposeGraph()

    // 3) second pass: DFS on Gᵗ in decreasing finish time
    val visited = mutableSetOf<V>()
    val components = mutableListOf<FiniteSet<V>>()

    for (v in orderDescending) {
        if (!visited.add(v)) continue

        val stack = ArrayDeque<V>()
        val comp = mutableListOf<V>()
        stack.add(v)

        while (stack.isNotEmpty()) {
            val x = stack.removeLast()
            comp += x
            for (w in gt.outNeighbors(x)) {
                if (visited.add(w)) stack.add(w)
            }
        }
        components += comp.toUnorderedFiniteSet()
    }

    return components.toUnorderedFiniteSet()
}

/** Condensation DAG: contracts each SCC to a single vertex. */
fun <V: Any> DirectedGraph<V>.condensation(): DirectedGraph<FiniteSet<V>> {
    val comps: List<FiniteSet<V>> = stronglyConnectedComponentSets().toList()

    // Map each original vertex to its SCC index
    val compIndex: Map<V, Int> = buildMap {
        comps.forEachIndexed { idx, c -> c.forEach { put(it, idx) } }
    }

    val condVertices = comps.toUnorderedFiniteSet()

    val condEdges = edges
        .mapNotNull { e ->
            val c1 = compIndex.getValue(e.from)
            val c2 = compIndex.getValue(e.to)
            if (c1 != c2) DirectedEdge(comps[c1], comps[c2]) else null
        }
        .toUnorderedFiniteSet()

    return AdjacencySetDirectedGraph.of(condVertices, condEdges)
}

/* Helpers for Kosaraju first pass */
private fun <V: Any> DirectedGraph<V>.dfsPostOrder(v: V, vis: MutableSet<V>, order: MutableList<V>) {
    if (!vis.add(v)) return
    for (w in outNeighbors(v)) dfsPostOrder(w, vis, order)
    order += v
}

private fun <V: Any> DirectedGraph<V>.finishingOrder(): List<V> {
    val vis = mutableSetOf<V>()
    val order = mutableListOf<V>()
    for (v in vertices) if (v !in vis) dfsPostOrder(v, vis, order)
    return order
}

