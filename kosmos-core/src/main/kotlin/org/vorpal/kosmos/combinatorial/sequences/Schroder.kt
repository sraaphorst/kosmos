package org.vorpal.kosmos.combinatorial.sequences

import org.vorpal.kosmos.combinatorial.Binomial
import org.vorpal.kosmos.combinatorial.Binomial.invoke
import org.vorpal.kosmos.combinatorial.arrays.SchroderTriangle
import org.vorpal.kosmos.combinatorial.recurrence.NonlinearRecurrence
import org.vorpal.kosmos.combinatorial.recurrence.Recurrence
import java.math.BigInteger
import kotlin.plus

/**
 * Schröder numbers.
 *
 * S_n describes the number of lattice paths from the southwest corner (0,0) of an n x n grid to the northeast corner
 * (n,n) using only single steps north (0,1), east (1,0), or northeast (1,1) that do not rise above the SW-NE
 * diagonal.
 *
 * They also count the number of ways to divide a rectangle into n+1 smaller rectangles using n cuts through n points
 * given inside the rectangle in general position, each cut intersecting one of the points and dividing only a
 * single rectangle in two. (The number of structurally different guillotine partitions.)
 *
 * These numbers are calculated from the [SchroderTriangle][org.vorpal.kosmos.combinatorial.arrays.SchroderTriangle]
 * as the diagonal values on the triangle.
 */
object Schroder : Recurrence<BigInteger> {
    override val initial = emptyList<BigInteger>()

    override fun iterator(): Iterator<BigInteger> = object : Iterator<BigInteger> {
        private val values = initial.toMutableList()
        private var idx = 0

        override fun hasNext(): Boolean = true

        override fun next(): BigInteger {
            if (idx < values.size) return values[idx++]
            val nv = SchroderTriangle(idx, idx)
            values.add(nv)
            idx++
            return nv
        }
    }
}
