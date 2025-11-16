package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.toUnorderedFiniteSet

/**
 * Breadth–first search starting from [start] in an undirected graph.
 *
 * The returned [Sequence] yields vertices in BFS order within the connected
 * component of [start], following undirected adjacency [neighbors].
 *
 * Complexity:
 *  - Time: O(|V| + |E|) over the reachable component.
 *  - Space: O(|V|) for the visited set and queue.
 *
 * @throws IllegalArgumentException if [start] is not a vertex of this graph.
 */
fun <V: Any> UndirectedGraph<V>.bfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::neighbors, ArrayDeque<V>::removeFirst)

/**
 * Depth–first search starting from [start] in an undirected graph.
 *
 * This is implemented using an explicit stack via [traversalImpl] and yields
 * vertices in a DFS order over the connected component of [start].
 *
 * Complexity:
 *  - Time: O(|V| + |E|) over the reachable component.
 *  - Space: O(|V|) for the visited set and stack.
 *
 * @throws IllegalArgumentException if [start] is not a vertex of this graph.
 */
fun <V: Any> UndirectedGraph<V>.dfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::neighbors, ArrayDeque<V>::removeLast)

/**
 * BFS on the underlying undirected graph of this digraph: uses all neighbors
 * (in- and out-) instead of just out-neighbors.
 */
fun <V : Any> DirectedGraph<V>.weakBfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::allNeighbors, ArrayDeque<V>::removeFirst)

/**
 * DFS on the underlying undirected graph of this digraph: uses all neighbors
 * (in- and out-) instead of just out-neighbors.
 */
fun <V : Any> DirectedGraph<V>.weakDfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::allNeighbors, ArrayDeque<V>::removeLast)

/**
 * Breadth–first search starting from [start] in a directed graph.
 *
 * Yields vertices in BFS order following outgoing edges only.
 *
 * Complexity:
 *  - Time: O(|V| + |E|) over the reachable component.
 *  - Space: O(|V|) for the visited set and queue.
 *
 * @throws IllegalArgumentException if [start] is not a vertex of this graph.
 */
fun <V: Any> DirectedGraph<V>.bfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::outNeighbors, ArrayDeque<V>::removeFirst)

/**
 * Depth–first search starting from [start] in a directed graph.
 *
 * Yields vertices in BFS order following outgoing edges only.
 *
 * Complexity:
 *  - Time: O(|V| + |E|) over the reachable component.
 *  - Space: O(|V|) for the visited set and stack.
 *
 * @throws IllegalArgumentException if [start] is not a vertex of this graph.
 */
fun <V: Any> DirectedGraph<V>.dfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::outNeighbors, ArrayDeque<V>::removeLast)

/**
 * Strongly connected vertex sets (SCCs) via Kosaraju’s algorithm.
 *
 * Two vertices lie in the same set iff each is reachable from the other.
 * Steps:
 *  1) DFS on G to record vertices in increasing finish time.
 *  2) DFS on Gᵗ in decreasing finish time to peel off SCCs.
 *
 * Complexity: O(|V| + |E|).
 */
internal fun <V : Any> DirectedGraph<V>.stronglyConnectedComponentSets(): FiniteSet<FiniteSet<V>> {
    // 1) First pass: DFS on G to get finishing order List<V>, smallest finish times first.
    val order = finishingOrder()
    val orderDescending = order.asReversed()

    // 2) Transpose of the graph.
    val gt: DirectedGraph<V> = this.toTransposeGraph()

    // 3) Second pass: DFS on Gᵗ in decreasing finish-time order
    val visited = mutableSetOf<V>()
    val components = mutableListOf<FiniteSet<V>>()

    for (v in orderDescending) {
        if (visited.contains(v)) continue

        // Collect vertices in this SCC using a simple DFS/stack on Gᵗ
        val stack = ArrayDeque<V>()
        val compVertices = mutableListOf<V>()

        visited.add(v)
        stack.add(v)

        while (stack.isNotEmpty()) {
            val x = stack.removeLast()
            compVertices.add(x)

            // In Gᵗ, neighbors(x) are the reversed edges; same method name works.
            for (w in gt.outNeighbors(x)) {
                if (visited.add(w)) {
                    stack.add(w)
                }
            }
        }

        val compVertexSet = compVertices.toUnorderedFiniteSet()
        components.add(compVertexSet)
    }

    return components.toUnorderedFiniteSet()
}

/**
 * Builds the condensation DAG of this digraph:
 *  - Vertices are the SCCs.
 *  - Edge C_i → C_j exists iff ∃(u,v)∈E with u∈C_i, v∈C_j and i≠j.
 *
 * Multiple original edges between the same SCC pair are collapsed to one edge.
 * The result is always a DAG.
 *
 * More formally:
 *
 * Let `~` be the equivalence relation that defines the SCCs of `G`, i.e. the SCCs of `G` are `V/~`.
 *
 * Define the canonical projection: `f: V -> V/~`, `f(u) = [u]`.
 *
 * Then `C(G) = (V/~, { ([C_i], [C_j]) | (u, v) ∈ E(G), u ∈ C_i, v ∈ C_j, i ≠ j} )`
 *
 * We thus have that `f` is a homomorphism with kernel defined by `~`.
 */
fun <V: Any> DirectedGraph<V>.condensation(): DirectedGraph<FiniteSet<V>> {
    // Compute the SCC vertex sets. These are the vertices of the condensation.
    val comps: List<FiniteSet<V>> = stronglyConnectedComponentSets().toList()

    // Map each vertex to its component index.
    val compIndex = buildMap(capacity = comps.sumOf { it.size }) {
        comps.forEachIndexed { idx, comp ->
            comp.forEach { v -> put(v, idx) }
        }
    }

    // Put into vertex form.
    val condVertices = comps.toUnorderedFiniteSet()

    // Build the condensation edges.
    val condEdges = edges
        .mapNotNull { e ->
            val c1 = compIndex.getValue(e.from)
            val c2 = compIndex.getValue(e.to)
            if (c1 != c2) DirectedEdge(comps[c1], comps[c2]) else null
        }.toUnorderedFiniteSet()

    return AdjacencySetDirectedGraph.of(condVertices, condEdges)
}

private fun <V : Any> DirectedGraph<V>.dfsPostOrder(
    v: V,
    visited: MutableSet<V>,
    order: MutableList<V>
) {
    if (!visited.add(v)) return

    for (w in outNeighbors(v))
        // dfsPostOrder already checks adding w to visited and returns immediately if it cannot.
        dfsPostOrder(w, visited, order)

    // Vertex v is now considered "finished:" we record it after exploring all outgoing edges.
    order.add(v)
}

private fun <V : Any> DirectedGraph<V>.finishingOrder(): List<V> {
    val visited = mutableSetOf<V>()
    val order = mutableListOf<V>()

    for (v in vertices)
        if (v !in visited)
            dfsPostOrder(v, visited, order)

    // In increasing finish time: reversed() is decreasing.
    return order
}

/**
 * This is a generic graph traversal routine that can be used by both directed and undirected graphs to
 * perform both depth-first and breadth-first traversals of graphs given:
 * - a starting vertex;
 * - the set of vertices;
 * - the function to get the neighbors of a given vertex (which differ between directed and undirected graphs); and
 * - and the way to pick the next vertex from the ArrayQueue, which determines if we are performing BFS or DFS.
 */
internal fun <V : Any> traversalImpl(
    start: V,
    vertices: FiniteSet.Unordered<V>,
    neighbors: (V) -> FiniteSet.Unordered<V>,
    pickNext: (ArrayDeque<V>) -> V
): Sequence<V> = sequence {
    require(start in vertices) {
        "Cannot perform traversal: start vertex $start is not in the graph's vertex set."
    }

    val visited = mutableSetOf<V>()
    val queue = ArrayDeque<V>()

    visited.add(start)
    queue.add(start)

    while (queue.isNotEmpty()) {
        val v = pickNext(queue)
        yield(v)

        for (n in neighbors(v)) {
            if (visited.add(n)) {   // add() returns true iff element was not present
                queue.add(n)
            }
        }
    }
}

internal fun <V : Any, G : Graph<V>> G.componentsVerticesImpl(
    bfs: G.(V) -> Sequence<V>,
): FiniteSet<FiniteSet<V>> {
    val remaining = vertices.toMutableSet()
    val compsVertices = mutableListOf<FiniteSet.Unordered<V>>()

    while (remaining.isNotEmpty()) {
        val start = remaining.first()
        val subvertices = bfs(start).toUnorderedFiniteSet()
        remaining.removeAll(subvertices)
        compsVertices.add(subvertices)
    }

    return compsVertices.toUnorderedFiniteSet()
}
