package org.vorpal.kosmos.combinatorial.recurrence

/**
 * Nonlinear (general) recurrence where each next term may depend
 * arbitrarily on all previously generated terms.
 *
 * Example (Catalan):
 *   - C_0 = 1
 *   - C_{n+1} = Σ_{i=0}^{n} C_i * C_{n-i}
 */
data class NonlinearRecurrence<T>(
    override val initial: List<T>,
    val next: (List<T>) -> T
) : WindowedRecurrence<T> {

    init { require(initial.isNotEmpty()) { "initial cannot be empty" } }

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private val values = initial.toMutableList()
        private var idx = 0

        override fun hasNext(): Boolean = true

        override fun next(): T {
            if (idx < values.size) return values[idx++]
            val nv = next(values)
            values.add(nv)
            idx++
            return nv
        }
    }
}
