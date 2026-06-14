package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.combinatorics.FanoPlane
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet

/**
 * A Fano plane where the lines are all oriented, used to dictate the multiplication table of the octonions.
 */
data class OctonionFanoOrientation(
    val lines: FiniteSet<OrientedFanoLine>
) {
    init {
        require(lines.size == 7) {
            "There must be exactly 7 lines in the Fano plane."
        }

        val underlying = lines.map { it.toFiniteSet() }.toUnorderedFiniteSet()
        val expected = FanoPlane.lines.map { it.toFiniteSet() }.toUnorderedFiniteSet()
        require(underlying == expected) {
            "The oriented lines must match the underlying Fano plane lines."
        }
    }

    fun orientedLineThrough(i: Int, j: Int): OrientedFanoLine =
        lines.firstOrNull { i in it && j in it } ?: error("No line through $i and $j")

    fun thirdPoint(i: Int, j: Int): Int =
        orientedLineThrough(i, j).thirdPoint(i, j)

    fun sign(i: Int, j: Int): Int =
        orientedLineThrough(i, j).signOfOrderedPair(i, j)
}
