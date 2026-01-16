package org.vorpal.kosmos.graphs

// These take advantage of the Csr-family of graphs by avoiding generating the FiniteSet of neighbors in traversals.

/**
 * Optional capability: iterate neighbors without allocating a FiniteSet.
 */
interface FastNeighbors<V : Any> {
    fun forEachNeighbor(of: V, f: (V) -> Unit)
}

/**
 * Optional capability: iterate out-neighbors without allocating a FiniteSet.
 */
interface FastOutNeighbors<V : Any> {
    fun forEachOutNeighbor(of: V, f: (V) -> Unit)
}

/**
 * Optional capability: iterate in-neighbors without allocating a FiniteSet.
 */
interface FastInNeighbors<V : Any> {
    fun forEachInNeighbor(of: V, f: (V) -> Unit)
}

interface FastNeighborsStop<V : Any> {
    fun forEachNeighborUntil(of: V, f: (V) -> Boolean)
}