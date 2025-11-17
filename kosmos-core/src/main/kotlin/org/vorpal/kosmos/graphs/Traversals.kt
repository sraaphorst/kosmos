package org.vorpal.kosmos.graphs

import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.functional.datastructures.Either

/** Early‑stop signal used only by the *Stop* variants. */
sealed interface FoldStep<out S, out R> {
    data class Continue<S>(val state: S) : FoldStep<S, Nothing>
    data class Stop<R>(val result: R) : FoldStep<Nothing, R>
}

/* ====================
 *  BFS — no early stop
 * ====================*/

/** Single‑source BFS fold over an undirected graph (no early stop). */
fun <V: Any, S> UndirectedGraph<V>.bfsFoldFrom(
    start: V,
    initial: S,
    onDiscover: (S, V) -> S,
    onEdge: (S, V, V, tree: Boolean) -> S
): S {
    require(start in vertices) { "bfsFoldFrom: start $start not in vertex set." }

    val visited = mutableSetOf<V>()
    val q = ArrayDeque<V>()

    var s = initial

    visited.add(start)
    q.add(start)
    s = onDiscover(s, start)

    while (q.isNotEmpty()) {
        val v = q.removeFirst()
        for (w in neighbors(v)) {
            val tree = visited.add(w)
            s = onEdge(s, v, w, tree)
            if (tree) {
                q.add(w)
                s = onDiscover(s, w)
            }
        }
    }
    return s
}

/** Fold all connected components with BFS, no early stop. */
fun <V: Any, S> UndirectedGraph<V>.bfsFoldComponents(
    initial: S,
    onDiscover: (S, V) -> S,
    onEdge: (S, V, V, tree: Boolean) -> S
): S {
    val visited = mutableSetOf<V>()
    var s = initial

    for (root in vertices) {
        if (!visited.add(root)) continue

        val q = ArrayDeque<V>()
        q.add(root)
        s = onDiscover(s, root)

        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            for (w in neighbors(v)) {
                val tree = visited.add(w)
                s = onEdge(s, v, w, tree)
                if (tree) {
                    q.add(w)
                    s = onDiscover(s, w)
                }
            }
        }
    }
    return s
}

/* ==========================
 *  BFS — early stop variants
 * ==========================*/

/** Fold all components with BFS and allow early stop via [FoldStep]. */
fun <V: Any, S, R> UndirectedGraph<V>.bfsFoldComponentsStop(
    initial: S,
    onDiscover: (S, V) -> FoldStep<S, R>,
    onEdge: (S, V, V, tree: Boolean) -> FoldStep<S, R>
): Either<R, S> {
    val visited = mutableSetOf<V>()
    var s = initial

    fun stepDiscover(v: V): Either<R, Unit> =
        when (val k = onDiscover(s, v)) {
            is FoldStep.Stop -> Either.left(k.result)
            is FoldStep.Continue -> { s = k.state; Either.right(Unit) }
        }

    fun stepEdge(v: V, w: V, tree: Boolean): Either<R, Unit> =
        when (val k = onEdge(s, v, w, tree)) {
            is FoldStep.Stop -> Either.left(k.result)
            is FoldStep.Continue -> { s = k.state; Either.right(Unit) }
        }

    for (root in vertices) {
        if (!visited.add(root)) continue

        val q = ArrayDeque<V>()
        q.add(root)
        when (val d = stepDiscover(root)) {
            is Either.Left -> return d
            is Either.Right -> {}
        }

        while (q.isNotEmpty()) {
            val v = q.removeFirst()
            for (w in neighbors(v)) {
                val tree = visited.add(w)
                when (val e = stepEdge(v, w, tree)) {
                    is Either.Left -> return e
                    is Either.Right -> {}
                }
                if (tree) {
                    q.add(w)
                    when (val d = stepDiscover(w)) {
                        is Either.Left -> return d
                        is Either.Right -> {}
                    }
                }
            }
        }
    }
    return Either.right(s)
}

/* ============================
 *  DFS with finish (iterative)
 * ============================*/

/**
 * Iterative DFS fold over all components with callbacks for root, discover, examine, and finish.
 * Useful for Tarjan/Kosaraju‑style algorithms, bridges, articulations, etc.
 */
fun <V: Any, S> dfsFoldComponentsWithFinish(
    vertices: FiniteSet.Unordered<V>,
    neighbors: (V) -> FiniteSet.Unordered<V>,
    initial: S,
    onRoot: (S, V) -> S = { s, _ -> s },
    onDiscover: (S, V) -> S,
    onExamine: (S, V, V, tree: Boolean) -> S,
    onFinish: (S, V) -> S
): S {
    val visited = mutableSetOf<V>()
    var s = initial

    data class Frame<V>(val v: V, val it: Iterator<V>, var discovered: Boolean)

    for (r in vertices) {
        if (r in visited) continue
        s = onRoot(s, r)

        val stack = ArrayDeque<Frame<V>>()
        stack.add(Frame(r, neighbors(r).iterator(), discovered = false))

        while (stack.isNotEmpty()) {
            val top = stack.last()

            if (!top.discovered) {
                visited.add(top.v)
                s = onDiscover(s, top.v)
                top.discovered = true
            }

            if (top.it.hasNext()) {
                val w = top.it.next()
                val tree = w !in visited
                s = onExamine(s, top.v, w, tree)
                if (tree) {
                    stack.add(Frame(w, neighbors(w).iterator(), discovered = false))
                }
            } else {
                s = onFinish(s, top.v)
                stack.removeLast()
            }
        }
    }
    return s
}
