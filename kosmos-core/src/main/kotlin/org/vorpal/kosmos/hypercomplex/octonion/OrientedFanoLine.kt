package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.core.finiteset.FiniteSet

/**
 * A line in the Fano plane that has an orientation associated with it, i.e. `(a, b, c)`.
 */
data class OrientedFanoLine(
    val a: Int,
    val b: Int,
    val c: Int
) {
    init {
        require(setOf(a, b, c).size == 3) {
            "An oriented Fano line must contain exactly three distinct elements: got $this."
        }
        require(a in 1..7 && b in 1..7 && c in 1..7) {
            "An oriented Fano line must contain elements from the set {1, 2, 3, 4, 5, 6, 7}: got $this."
        }
    }

    /**
     * Returns `true` iff the point `i` is contained in the line.
     */
    operator fun contains(i: Int): Boolean =
        i == a || i == b || i == c

    /**
     * Make sure that the points `i` and `j` are distinct and contained in the line.
     */
    private fun checkPoints(i: Int, j: Int) {
        require(i != j) {
            "Points must be distinct: both points were $i."
        }
        require(i in this && j in this) {
            "Points must be contained in the line: got $i and $j, line is $this."
        }
    }

    /**
     * Given two points on this line, returns the third point.
     */
    fun thirdPoint(i: Int, j: Int): Int {
        checkPoints(i, j)
        return when {
            i != a && j != a -> a
            i != b && j != b -> b
            else -> c
        }
    }

    /**
     * Returns `+1` if `(i,j)` is positively oriented on this cyclic line, and `-1` if `(i,j)` is negatively oriented.
     */
    fun signOfOrderedPair(i: Int, j: Int): Int {
        checkPoints(i, j)
        return when (i to j) {
            (a to b) -> 1
            (b to c) -> 1
            (c to a) -> 1
            else -> -1
        }
    }

    fun toFiniteSet() =
        FiniteSet.unordered(a, b, c)

    override fun toString(): String =
        "($a, $b, $c)"
}
