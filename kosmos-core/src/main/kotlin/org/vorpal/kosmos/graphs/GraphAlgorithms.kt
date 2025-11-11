package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.toUnorderedFiniteSet

fun <V: Any> UndirectedGraph<V>.bfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::neighbors, ArrayDeque<V>::removeFirst)

fun <V: Any> UndirectedGraph<V>.dfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::neighbors, ArrayDeque<V>::removeLast)

fun <V : Any> UndirectedGraph<V>.connectedComponents(): FiniteSet<UndirectedGraph<V>> =
    componentsImpl(
        bfs = { start -> bfs(start) },
        inducedSubgraph = { vs -> inducedSubgraph(vs) }
    )


fun <V: Any> DirectedGraph<V>.bfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::outNeighbors, ArrayDeque<V>::removeFirst)

fun <V: Any> DirectedGraph<V>.dfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::outNeighbors, ArrayDeque<V>::removeLast)

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
 * In a DirectedGraph, the term "connected component" has two definitions.
 *
 * Imagine the graph with vertices 0123 and arcs 01, 12, 23, 31.
 * 1. If we start traversing at 1, we discover vertices 1, 2, and 3.
 * 2. If we start traversing at 0, we discover vertices 0, 1, 2, and 3.
 *
 * In a weakly connected graph, if we can get from a vertex u to a vertex v, they are in the same component.
 */
fun <V : Any> DirectedGraph<V>.weaklyConnectedComponents(): FiniteSet<DirectedGraph<V>> =
    componentsImpl(
        bfs = { s -> weakBfs(s) },
        inducedSubgraph = { vs -> inducedSubgraph(vs) }
    )

/**
 * In a DirectedGraph, the term "connected component" has two definitions.
 *
 * Imagine the graph with vertices 0123 and arcs 01, 12, 23, 31.
 * 1. If we start traversing at 1, we discover vertices 1, 2, and 3.
 * 2. If we start traversing at 0, we discover vertices 0, 1, 2, and 3.
 *
 * In a strongly connected graph, for two vertices u and v, if we can get:
 * - from u to v; and
 * - from v to u
 *
 * then they are in the same component.
 *
 * The implementation uses Kosaraju's implementation and returns the sets of vertices in each
 * strongly connected component.
 */
fun <V : Any> DirectedGraph<V>.stronglyConnectedComponentSets(): FiniteSet<FiniteSet.Unordered<V>> {
    // 1) First pass: DFS on G to get finishing order List<V>, smallest finish times first.
    val order = finishingOrder()
    val orderDescending = order.asReversed()

    // 2) Transpose of the graph (we’ll assume you have this)
    val gt: DirectedGraph<V> = this.toTransposeGraph()

    // 3) Second pass: DFS on Gᵗ in decreasing finish-time order
    val visited = mutableSetOf<V>()
    val components = mutableListOf<FiniteSet.Unordered<V>>()

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
 * In a DirectedGraph, the term "connected component" has two definitions.
 *
 * Imagine the graph with vertices 0123 and arcs 01, 12, 23, 31.
 * 1. If we start traversing at 1, we discover vertices 1, 2, and 3.
 * 2. If we start traversing at 0, we discover vertices 0, 1, 2, and 3.
 *
 * In a strongly connected graph, for two vertices u and v, if we can get:
 * - from u to v; and
 * - from v to u
 *
 * then they are in the same component.
 *
 * The implementation uses Kosaraju's implementation and returns the strongly connected components
 * as subgraphs of the original directed graph.
 */
fun <V : Any> DirectedGraph<V>.stronglyConnectedComponents(): FiniteSet<DirectedGraph<V>> =
    stronglyConnectedComponentSets().map(::inducedSubgraph)

/**
 * The condensation graph of a directed graph has:
 * - The strongly connected component vertex sets each as a vertex
 * - An edge from SCC1 to SCC2 if there is an edge from SCC1 to SCC2.
 *
 * Note that there will never be parallel edges between two vertices: otherwise, by definition of SCCs, they
 * would have been merged into one SCC.
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
fun <V: Any> DirectedGraph<V>.condensation(): DirectedGraph<FiniteSet.Unordered<V>> {
    // Compute the SCC vertex sets. These are the vertices of the condensation.
    val comps: List<FiniteSet.Unordered<V>> = stronglyConnectedComponentSets().toList()

    // Map each vertex to its component index.
    val compIndex: Map<V, Int> = comps.withIndex().flatMap { (idx, comp) ->
        comp.map { v -> v to idx }
    }.toMap()

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
private fun <V : Any> traversalImpl(
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

/**
 * This is the common logic to extract:
 * - the connected components from an undirected graph
 * - the weakly connected components from a directed graph.
 */
private fun <V : Any, G : Graph<V>> G.componentsImpl(
    bfs: G.(V) -> Sequence<V>,
    inducedSubgraph: G.(FiniteSet<V>) -> G
): FiniteSet<G> {
    val remaining = vertices.toMutableSet()
    val comps = mutableListOf<G>()

    while (remaining.isNotEmpty()) {
        val start = remaining.first()
        val subvertices = bfs(start).toUnorderedFiniteSet()
        remaining.removeAll(subvertices)
        comps.add(inducedSubgraph(subvertices))
    }

    return comps.toUnorderedFiniteSet()
}
