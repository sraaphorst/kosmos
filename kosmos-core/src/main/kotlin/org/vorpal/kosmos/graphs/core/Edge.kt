package org.vorpal.kosmos.graphs.core

/**
 * An edge in a graph is a relation between points.
 */
data class Edge<V>(val from: V, val to: V)
