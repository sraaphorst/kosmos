package org.vorpal.kosmos.core.neighborhood

import org.vorpal.kosmos.core.FiniteSet

/**
 * Represents an object that can have a neighborhood. Examples include:
 *
 * - A vertex in a graph
 * - A search space in an algorithm like hill climbing
 * - A grid or move generator
 */
fun interface Neighborhood<T: Any> {
    fun neighbors(of: T): FiniteSet.Unordered<T>
}
