package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.toUnorderedFiniteSet
import org.vorpal.kosmos.functional.datastructures.Either
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.Options.liftOption
import org.vorpal.kosmos.functional.datastructures.getOrNull
import org.vorpal.kosmos.functional.datastructures.isEmpty
import kotlin.math.max
import kotlin.math.min


// ---------- Connected components (UNDIRECTED) ----------
internal fun <V: Any> UndirectedGraph<V>.computeConnectedComponentVertexSets(): FiniteSet<FiniteSet<V>> {
    val remaining = vertices.toMutableSet()
    val comps = mutableListOf<FiniteSet<V>>()

    while (remaining.isNotEmpty()) {
        val start = remaining.first()
        val seen = mutableSetOf(start)
        val q = ArrayDeque<V>()
        q.add(start)

        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            for (w in neighbors(v))
                if (seen.add(w)) q.add(w)
        }

        val comp = seen.toUnorderedFiniteSet()
        comps += comp
        remaining.removeAll(comp)
    }

    return comps.toUnorderedFiniteSet()
}

/* ==========================================================
 *  Distances / eccentricity / radius / diameter (undirected)
 * ==========================================================*/

/**
 * Unweighted shortest-path distances from [source] within its connected component.
 *
 * Returns a map d such that `d[v] = dist(source, v)` for all vertices reachable from [source].
 *
 * Non-reachable vertices are omitted.
 *
 * Singletons yield `{ source → 0 }`.
 *
 * Complexity: `O(|V| + |E|) time, O(|V|)` space in the component.
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
 * Eccentricity of [v]: max shortest‑path distance from [v] across the entire graph.
 * @return
 * - [Option.Some] if connected,
 * - [Option.None] if disconnected.
 * @see [eccentricityLocal]
 */
fun <V: Any> UndirectedGraph<V>.eccentricityGlobal(v: V): Option<Int> {
    if (vertices.isEmpty) return Option.of(0)
    if (!isConnected()) return Option.None
    val d = distancesFrom(v)
    return Option.of(d.values.maxOrNull() ?: 0)
}

/**
 * Radius: minimum eccentricity across the entire graph.
 * @return
 * - [Option.Some] if connected,
 * - [Option.None] if disconnected.
 * @see [radiusLocal]
 */
fun <V: Any> UndirectedGraph<V>.radiusGlobal(): Option<Int> {
    if (vertices.isEmpty) return Option.of(0)
    if (!isConnected()) return Option.None
    var best = Int.MAX_VALUE
    for (v in vertices) {
        val ecc = eccentricityGlobal(v).getOrNull() ?: return Option.None
        if (ecc < best) best = ecc
    }
    return Option.of(best)
}

/**
 * Diameter: maximum eccentricity across the entire graph.
 * @return
 * - [Option.Some] if connected,
 * - [Option.None] if disconnected.
 * @see [diameterLocal]
 */
fun <V: Any> UndirectedGraph<V>.diameterGlobal(): Option<Int> {
    if (vertices.isEmpty) return Option.of(0)
    if (!isConnected()) return Option.None
    var worst = 0
    for (v in vertices) {
        val ecc = eccentricityGlobal(v).getOrNull() ?: return Option.None
        if (ecc > worst) worst = ecc
    }
    return Option.of(worst)
}

/**
 * @return All vertices whose local eccentricity equals [radiusGlobal].
 * @see [centersLocal]
 */
fun <V: Any> UndirectedGraph<V>.centersGlobal(): Option<FiniteSet.Unordered<V>> = when (val r = radiusGlobal()) {
    is Option.None -> Option.None
    is Option.Some -> Option.of(
        vertices.filter { v -> distancesFrom(v).values.maxOrNull() == r.value }
            .toUnorderedFiniteSet()
    )
}

/**
 * @return All vertices whose local eccentricity equals [diameterGlobal].
 * @see [peripheryLocal]
 */
fun <V: Any> UndirectedGraph<V>.peripheryGlobal(): Option<FiniteSet.Unordered<V>> = when (val d = diameterGlobal()) {
    is Option.None -> Option.None
    is Option.Some -> Option.of(
        vertices.filter { v -> distancesFrom(v).values.maxOrNull() == d.value }
            .toUnorderedFiniteSet()
    )
}

/**
 * @return Eccentricity within [v]’s connected component (always defined).
 * @see [eccentricityGlobal]
 */
fun <V: Any> UndirectedGraph<V>.eccentricityLocal(v: V): Int {
    require(v in vertices) { "eccentricityLocal($v): not a vertex" }
    val d = distancesFrom(v)  // BFS on the (undirected) graph
    return d.values.maxOrNull() ?: 0   // singleton component -> 0
}

/**
 * Component-wise radius: min of local eccentricities.
 * @return
 * - `0` if the graph is empty,
 * - else min of local eccentricities.
 * @see [radiusGlobal]
 */
fun <V: Any> UndirectedGraph<V>.radiusLocal(): Int =
    vertices.minOfOrNull(::eccentricityLocal) ?: 0

/**
 * Component-wise diameter: max of local eccentricities.
 * @return
 * - `0` if the graph is empty,
 * - else max of local eccentricities.
 * @see [diameterGlobal]
 */
fun <V: Any> UndirectedGraph<V>.diameterLocal(): Int =
    vertices.maxOfOrNull(::eccentricityLocal) ?: 0

/**
 * @return All vertices whose local eccentricity equals [radiusLocal].
 * @see [centersGlobal]
 */
fun <V: Any> UndirectedGraph<V>.centersLocal(): FiniteSet.Unordered<V> {
    val r = radiusLocal()
    return vertices.filter { v -> eccentricityLocal(v) == r }.toUnorderedFiniteSet()
}

/**
 * @return All vertices whose local eccentricity equals [diameterLocal].
 * @see [peripheryGlobal]
 */
fun <V: Any> UndirectedGraph<V>.peripheryLocal(): FiniteSet.Unordered<V> {
    val d = diameterLocal()
    return vertices.filter { v -> eccentricityLocal(v) == d }.toUnorderedFiniteSet()
}


// ---------- Directed distances helpers ----------

/**
 * Directed shortest-path distances following OUT-edges only.
 *
 * Returns `d` with `d[v] = dist_out(source, v)` for vertices reachable by directed paths.
 *
 * Non-reachable vertices are omitted.
 *
 * Singletons yield `{ source → 0 }`.
 *
 * Complexity: `O(|V| + |E|)`.
 */
fun <V: Any> DirectedGraph<V>.directedDistancesFrom(source: V): Map<V, Int> {
    require(source in vertices) { "directedDistancesFrom($source): not a vertex" }
    val dist = mutableMapOf<V, Int>()
    val q = ArrayDeque<V>()
    dist[source] = 0
    q.add(source)
    while (q.isNotEmpty()) {
        val v = q.removeFirst()
        val dv = dist.getValue(v)
        for (w in outNeighbors(v)) {
            if (w !in dist) {
                dist[w] = dv + 1
                q.add(w)
            }
        }
    }
    return dist
}

/**
 * Weak distances: unweighted distances in the underlying undirected graph.
 *
 * Convenience wrapper around toUndirectedGraph().distancesFrom(source).
 */
fun <V: Any> DirectedGraph<V>.weakDistancesFrom(source: V): Map<V, Int> =
    toUndirectedGraph().distancesFrom(source)


// ---------- Weakly connected components (DIRECTED) ----------
internal fun <V: Any> DirectedGraph<V>.computeWeaklyConnectedComponentVertexSets(): FiniteSet<FiniteSet<V>> {
    val remaining = vertices.toMutableSet()
    val comps = mutableListOf<FiniteSet<V>>()

    while (remaining.isNotEmpty()) {
        val start = remaining.first()
        val seen = mutableSetOf(start)
        val q = ArrayDeque<V>()
        q.add(start)

        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            for (w in allNeighbors(v))
                if (seen.add(w)) q.add(w)
        }

        val comp = seen.toUnorderedFiniteSet()
        comps += comp
        remaining.removeAll(comp)
    }

    return comps.toUnorderedFiniteSet()
}

// ---------- Weak (underlying undirected) eccentricity / radius / diameter ----------

/**
 * Weak eccentricity of [v] in a digraph: max shortest-path distance from [v] in the
 * underlying undirected graph.
 *
 * @return
 *  - [Option.Some] `0` if the graph is empty,
 *  - [Option.None] if the digraph is not weakly connected,
 *  - [Option.Some] `k` otherwise.
 */
fun <V: Any> DirectedGraph<V>.eccentricityWeak(v: V): Option<Int> {
    if (vertices.isEmpty) return Option.of(0)
    // Weak connectivity test: single weak component
    if (weaklyConnectedComponentsVertexSets().size != 1) return Option.None
    val d = weakDistancesFrom(v)
    return Option.of(d.values.maxOrNull() ?: 0)
}

/**
 * Weak radius: minimum weak eccentricity.
 * @return
 * - [Option.Some] `0` if the graph is empty,
 * - [Option.None] if not weakly connected,
 * - [Option.Some] `k` otherwise.
 */
fun <V: Any> DirectedGraph<V>.radiusWeak(): Option<Int> {
    if (vertices.isEmpty) return Option.of(0)
    if (weaklyConnectedComponentsVertexSets().size != 1) return Option.None
    val liftMin = {a: Int, b: Int -> min(a, b) }.liftOption()
    return vertices.fold(Option.of(Int.MAX_VALUE)) { best, v ->
        val ecc = eccentricityWeak(v)
        if (ecc.isEmpty()) return@fold Option.None
        liftMin(ecc, best)
    }
}

/**
 * Weak diameter: maximum weak eccentricity.
 * @return
 * - [Option.Some] `0` if the graph is empty,
 * - [Option.None] if not weakly connected,
 * - [Option.Some] `k` otherwise.
 */
fun <V: Any> DirectedGraph<V>.diameterWeak(): Option<Int> {
    if (vertices.isEmpty) return Option.of(0)
    if (weaklyConnectedComponentsVertexSets().size != 1) return Option.None
    val liftMax = {a: Int, b: Int -> max(a, b) }.liftOption()
    return vertices.fold(Option.of(0)) { worst, v ->
        val ecc = eccentricityWeak(v)
        if (ecc.isEmpty()) return@fold Option.None
        liftMax(ecc, worst)
    }
}

/** Weak centers/periphery: vertices attaining weak radius/diameter (None if not weakly connected). */
fun <V: Any> DirectedGraph<V>.centersWeak(): Option<FiniteSet.Unordered<V>> = when (val r = radiusWeak()) {
    is Option.None -> Option.None
    is Option.Some -> {
        val rVal = r.value
        Option.of(vertices.filter { v -> weakDistancesFrom(v).values.maxOrNull() == rVal }
            .toUnorderedFiniteSet())
    }
}

/** Vertices whose weak eccentricity attains the weak diameter; None if not weakly connected. */
fun <V: Any> DirectedGraph<V>.peripheryWeak(): Option<FiniteSet.Unordered<V>> = when (val d = diameterWeak()) {
    is Option.None -> Option.None
    is Option.Some -> {
        val dVal = d.value
        Option.of(vertices.filter { v -> weakDistancesFrom(v).values.maxOrNull() == dVal }
            .toUnorderedFiniteSet())
    }
}


// ---------- Strong (direction-respecting) eccentricity / radius / diameter ----------

/**
 * Strong eccentricity of [v]: max directed distance from [v] following OUT-edges only.
 * @return
 *  - [Option.Some] `0` if the graph is empty,
 *  - [Option.None] if the digraph is not strongly connected (i.e. some vertex is unreachable from `v`),
 *  - [Option.Some] `k` otherwise.
 */
fun <V: Any> DirectedGraph<V>.eccentricityStrong(v: V): Option<Int> {
    if (vertices.isEmpty) return Option.of(0)
    // Strongly connected iff exactly one SCC of size |V|
    val sccs = stronglyConnectedComponentsVertexSets()
    if (sccs.size != 1 || sccs.first().size != vertices.size) return Option.None

    // In a strongly connected digraph, all vertices are reachable
    val d = directedDistancesFrom(v)
    if (d.size != vertices.size) return Option.None
    return Option.of(d.values.maxOrNull() ?: 0)
}

/**
 * Strong radius: min strong eccentricity.
 * @return
 * - [Option.Some] `0` if the graph is empty,
 * - [Option.None] if the graph is not strongly connected (i.e. some vertex is unreachable from `v`),
 * - [Option.Some] `k` otherwise.
 */
fun <V: Any> DirectedGraph<V>.radiusStrong(): Option<Int> {
    if (vertices.isEmpty) return Option.of(0)
    val sccs = stronglyConnectedComponentsVertexSets()
    if (sccs.size != 1 || sccs.first().size != vertices.size) return Option.None
    var best = Int.MAX_VALUE
    for (v in vertices) {
        val ecc = directedDistancesFrom(v).values.maxOrNull() ?: return Option.None
        if (ecc < best) best = ecc
    }
    return Option.of(best)
}

/**
 * Strong diameter: max strong eccentricity.
 * @return
 * - [Option.Some] `0` if the graph is empty,
 * - [Option.None] if the graph is not strongly connected (i.e. some vertex is unreachable from `v`),
 * - [Option.Some] `k` otherwise.
 */
fun <V: Any> DirectedGraph<V>.diameterStrong(): Option<Int> {
    if (vertices.isEmpty) return Option.of(0)
    val sccs = stronglyConnectedComponentsVertexSets()
    if (sccs.size != 1 || sccs.first().size != vertices.size) return Option.None
    var worst = 0
    for (v in vertices) {
        val ecc = directedDistancesFrom(v).values.maxOrNull() ?: return Option.None
        if (ecc > worst) worst = ecc
    }
    return Option.of(worst)
}

/**
 * Strong centers: vertices at strong radius.
 * @return
 * - [Option.None] if not strongly connected,
 * - [Option.Some] [FiniteSet.Unordered] otherwise.
 */
fun <V: Any> DirectedGraph<V>.centersStrong(): Option<FiniteSet.Unordered<V>> = when (val r = radiusStrong()) {
    is Option.None -> Option.None
    is Option.Some -> {
        val rVal = r.value
        Option.of(vertices.filter { v -> directedDistancesFrom(v).values.maxOrNull() == rVal }
            .toUnorderedFiniteSet())
    }
}

/**
 * Strong periphery: vertices at strong diameter.
 * @return
 * - [Option.None] if not strongly connected,
 * - [Option.Some] [FiniteSet.Unordered] otherwise.
 */
fun <V: Any> DirectedGraph<V>.peripheryStrong(): Option<FiniteSet.Unordered<V>> = when (val d = diameterStrong()) {
    is Option.None -> Option.None
    is Option.Some -> {
        val dVal = d.value
        Option.of(vertices.filter { v -> directedDistancesFrom(v).values.maxOrNull() == dVal }
            .toUnorderedFiniteSet())
    }
}


/**
 * Weak-local eccentricity: max undirected distance from [v] within its weak component.
 * Always defined (empty graph → 0).
 */
fun <V: Any> DirectedGraph<V>.eccentricityWeakLocal(v: V): Int {
    require(v in vertices) { "eccentricityWeakLocal($v): not a vertex" }
    val d = weakDistancesFrom(v)  // via toUndirectedGraph().distancesFrom
    return d.values.maxOrNull() ?: 0
}

/** Min of weak-local eccentricities.
 * @return
 * - `0` if the graph is empty,
 * - `k` otherwise.
 */
fun <V: Any> DirectedGraph<V>.radiusWeakLocal(): Int {
    if (vertices.isEmpty) return 0
    return vertices.fold(Int.MAX_VALUE) { best, v ->
        val ecc = eccentricityWeakLocal(v)
        min(best, ecc)
    }
}

/**
 * Max of weak-local eccentricities.
 * @return
 * - `0` if the graph is empty,
 * - `k` otherwise.
 */
fun <V: Any> DirectedGraph<V>.diameterWeakLocal(): Int {
    if (vertices.isEmpty) return 0
    return vertices.fold(0) { worst, v ->
        val ecc = eccentricityWeakLocal(v)
        max(worst, ecc)
    }
}

/**
 * @return Vertices whose weak local eccentricity attains the weak local radius.
 */
fun <V: Any> DirectedGraph<V>.centersWeakLocal(): FiniteSet.Unordered<V> {
    if (vertices.isEmpty) return vertices
    val r = radiusWeakLocal()
    return vertices.filter { v -> eccentricityWeakLocal(v) == r }.toUnorderedFiniteSet()
}

/**
 * @return Vertices whose weak local eccentricity attains the weak local diameter.
 */
fun <V: Any> DirectedGraph<V>.peripheryWeakLocal(): FiniteSet.Unordered<V> {
    if (vertices.isEmpty) return vertices
    val d = diameterWeakLocal()
    return vertices.filter { v -> eccentricityWeakLocal(v) == d }.toUnorderedFiniteSet()
}


/** BFS distances restricted to an allowed subset (e.g., v’s SCC). */
private fun <V: Any> DirectedGraph<V>.directedDistancesFromRestricted(
    source: V,
    allowed: FiniteSet<V>
): Map<V, Int> {
    require(source in allowed) { "directedDistancesFromRestricted: source not in allowed set" }
    val dist = mutableMapOf<V, Int>()
    val q = ArrayDeque<V>()
    dist[source] = 0
    q.add(source)
    while (q.isNotEmpty()) {
        val v = q.removeFirst()
        val dv = dist.getValue(v)
        for (w in outNeighbors(v)) {
            if (w in allowed && w !in dist) {
                dist[w] = dv + 1
                q.add(w)
            }
        }
    }
    return dist
}

private fun <V: Any> DirectedGraph<V>.sccContaining(v: V): FiniteSet<V> {
    require(v in vertices) { "sccContaining($v): not a vertex" }
    val sccs = stronglyConnectedComponentsVertexSets()
    // Every vertex belongs to exactly one SCC
    return sccs.first { comp -> v in comp }
}

/**
 * Strong-local eccentricity: max directed distance from [v] within its SCC.
 * Always defined (empty graph → 0).
 */
fun <V: Any> DirectedGraph<V>.eccentricityStrongLocal(v: V): Int {
    if (vertices.isEmpty) return 0
    val scc = sccContaining(v)
    val d = directedDistancesFromRestricted(v, scc)
    // In an SCC, all nodes are reachable; fallback 0 for singleton SCC
    return d.values.maxOrNull() ?: 0
}

/** Min of strong-local eccentricities (empty graph → 0). */
fun <V: Any> DirectedGraph<V>.radiusStrongLocal(): Int {
    if (vertices.isEmpty) return 0
    return vertices.fold(Int.MAX_VALUE) { best, v ->
        val ecc = eccentricityStrongLocal(v)
        min(best, ecc)
    }
}

/** Max of strong-local eccentricities (empty graph → 0). */
fun <V: Any> DirectedGraph<V>.diameterStrongLocal(): Int {
    if (vertices.isEmpty) return 0
    return vertices.fold(0) { worst, v ->
        val ecc = eccentricityStrongLocal(v)
        max(worst, ecc)
    }
}

fun <V: Any> DirectedGraph<V>.centersStrongLocal(): FiniteSet.Unordered<V> {
    if (vertices.isEmpty) return vertices
    val r = radiusStrongLocal()
    return vertices.filter { v -> eccentricityStrongLocal(v) == r }.toUnorderedFiniteSet()
}

fun <V: Any> DirectedGraph<V>.peripheryStrongLocal(): FiniteSet.Unordered<V> {
    if (vertices.isEmpty) return vertices
    val d = diameterStrongLocal()
    return vertices.filter { v -> eccentricityStrongLocal(v) == d }.toUnorderedFiniteSet()
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
 * @return
 *  - [Either.Left] (cycle): an odd cycle witnessing non-bipartiteness (path whose first/last are adjacent), or
 *  - [Either.Right] (L, R): a bipartition of the vertex set (coloring by parity of BFS depth).
 *
 * Works across components; isolated vertices are colored arbitrarily (Left part).
 *
 * Complexity: `O(|V| + |E|)`.
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
        return left + right
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

/** True iff no odd cycle exists. */
fun <V: Any> UndirectedGraph<V>.isBipartite(): Boolean =
    when (bipartitionOrOddCycle()) {
        is Either.Left -> false
        is Either.Right -> true
    }

/** The bipartition if bipartite; [Option.None] otherwise. */
fun <V: Any> UndirectedGraph<V>.bipartition(): Option<Pair<FiniteSet.Unordered<V>, FiniteSet.Unordered<V>>> =
    when (val r = bipartitionOrOddCycle()) {
        is Either.Left -> Option.None
        is Either.Right -> Option.of(r.value)
    }

/** An odd cycle if non-bipartite; [Option.None] otherwise. */
fun <V: Any> UndirectedGraph<V>.oddCycle(): Option<List<V>> =
    when (val r = bipartitionOrOddCycle()) {
        is Either.Left -> Option.of(r.value)
        is Either.Right -> Option.None
    }

/* =====================================
 *  Bridges & articulations (undirected)
 * =====================================*/

/**
 * Tarjan-style computation of bridges and articulation points in an [UndirectedGraph] using iterative DFS.
 *
 * A bridge is an edge whose removal increases the number of connected components.
 * An articulation point is a vertex whose removal increases the number of connected components.
 *
 * Root articulation rule: a DFS root is an articulation iff it has ≥ 2 DFS children.
 * Non-root rule: a vertex p is an articulation if it has a child `v` with `low[v] ≥ disc[p]`.
 *
 * Complexity: `O(|V| + |E|)`.
 */
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
                // Only count DFS children for actual root vertices
                if (st.parent[v] == null)
                    st.rootChildren[v] = st.rootChildren.getOrDefault(v, 0) + 1
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
                if (st.low.getValue(v) > st.disc.getValue(p))
                    st.bridges += UndirectedEdge(p, v)

                // articulation (non‑root): removing p disconnects v‑subtree
                if (st.parent[p] != null && st.low.getValue(v) >= st.disc.getValue(p))
                    st.articulations += p
            } else {
                // root articulation: at least two DFS children
                if (st.rootChildren.getValue(v) >= 2) st.articulations += v
            }
            st
        }
    )

    return BridgeArt(bridges = s.bridges.toSet(), articulations = s.articulations.toSet())
}

/* ============================
 *  Undirected radius-d balls
 * ============================ */

/** Vertices within distance ≤ d from [source] (inclusive). */
fun <V : Any> UndirectedGraph<V>.dNeighborsFrom(source: V, d: Int): FiniteSet.Unordered<V> {
    require(source in vertices) { "dNeighborsFrom: $source not in vertex set" }
    require(d >= 0) { "dNeighborsFrom: d must be nonnegative" }

    val acc = LinkedHashSet<V>()
    bfsDepthLimitedFrom(
        start = source,
        neighbors = { v -> neighbors(v) },
        maxDepth = d,
        initial = Unit,
        onDiscover = { _, v, _ ->
            acc.add(v)
            Unit
        },
        onEdge = { s, _, _, _, _ -> s }
    )
    return acc.toUnorderedFiniteSet()
}

/** For each v, the set of vertices at distance ≤ d from v (inclusive). */
fun <V : Any> UndirectedGraph<V>.dNeighbors(d: Int): Map<V, FiniteSet.Unordered<V>> {
    require(d >= 0) { "dNeighbors: d must be nonnegative" }
    if (vertices.isEmpty) return emptyMap()
    val out = LinkedHashMap<V, FiniteSet.Unordered<V>>(vertices.size)
    for (v in vertices) out[v] = dNeighborsFrom(v, d)
    return out
}

/* ============================
 *  Directed radius-d reachability
 * ============================ */

/** Out-reachability within ≤ d steps from [source] following OUT-arcs. */
fun <V : Any> DirectedGraph<V>.dReachableOutFrom(source: V, d: Int): FiniteSet.Unordered<V> {
    require(source in vertices) { "dReachableOutFrom: $source not in vertex set" }
    require(d >= 0) { "dReachableOutFrom: d must be nonnegative" }

    val acc = LinkedHashSet<V>()
    bfsDepthLimitedFrom(
        start = source,
        neighbors = { v -> outNeighbors(v) },
        maxDepth = d,
        initial = Unit,
        onDiscover = { _, v, _ ->
            acc.add(v)
            Unit
        },
        onEdge = { s, _, _, _, _ -> s }
    )
    return acc.toUnorderedFiniteSet()
}

/** In-reachability within ≤ d steps to [source] (i.e., along IN-arcs). */
fun <V : Any> DirectedGraph<V>.dReachableInFrom(source: V, d: Int): FiniteSet.Unordered<V> {
    // Use the transpose to reuse the OUT-BFS
    return this.toTransposeGraph().dReachableOutFrom(source, d)
}

/** For each v, the set of vertices reachable within ≤ d steps along OUT-arcs. */
fun <V : Any> DirectedGraph<V>.dReachableOut(d: Int): Map<V, FiniteSet.Unordered<V>> {
    require(d >= 0) { "dReachableOut: d must be nonnegative" }
    if (vertices.isEmpty) return emptyMap()
    val out = LinkedHashMap<V, FiniteSet.Unordered<V>>(vertices.size)
    for (v in vertices) out[v] = dReachableOutFrom(v, d)
    return out
}


/* ===============================
 *  SCCs & condensation (directed)
 * ===============================*/

/**
 * Kosaraju’s algorithm to compute vertex sets of all strongly connected components.
 *
 * Steps:
 *  1) DFS on `G` to get finishing order,
 *  2) DFS on `Gᵗ` in decreasing finish time, collecting components.
 *
 * Implementation is graph-implementation-agnostic (works with any [DirectedGraph]).
 * Complexity: `O(|V| + |E|)`.
 */
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
            for (w in gt.outNeighbors(x))
                if (visited.add(w)) stack.add(w)
        }
        components += comp.toUnorderedFiniteSet()
    }

    return components.toUnorderedFiniteSet()
}

/**
 * Condensation DAG `C(G)`: contracts each SCC to a single vertex, with an arc `C_i → C_j`
 * iff there exists `(u → v)` in `G` with `u ∈ C_i`, `v ∈ C_j`, `i ≠ j`.
 * `C(G)` is always a DAG.
 *
 * Complexity: `O(|V| + |E|)` to compute SCCs and scan edges.
 */
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

