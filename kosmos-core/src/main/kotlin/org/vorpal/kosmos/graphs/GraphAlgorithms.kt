package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet

fun <V: Any> UndirectedGraph<V>.bfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::neighbors, ArrayDeque<V>::removeFirst)

fun <V: Any> UndirectedGraph<V>.dfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::neighbors, ArrayDeque<V>::removeLast)

fun <V: Any> DirectedGraph<V>.bfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::outNeighbors, ArrayDeque<V>::removeFirst)

fun <V: Any> DirectedGraph<V>.dfs(start: V): Sequence<V> =
    traversalImpl(start, vertices, this::outNeighbors, ArrayDeque<V>::removeLast)

private fun <V : Any> traversalImpl(
    start: V,
    vertices: FiniteSet.Unordered<V>,
    neighbors: (V) -> FiniteSet.Unordered<V>,
    pickNext: (ArrayDeque<V>) -> V
): Sequence<V> = sequence {
    require(start in vertices) {
        "Cannot perform BFS: start vertex $start is not in the graph's vertex set."
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
