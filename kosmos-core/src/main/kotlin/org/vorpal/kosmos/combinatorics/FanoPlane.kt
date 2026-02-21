package org.vorpal.kosmos.combinatorics

import org.vorpal.kosmos.algebra.structures.Quasigroup
import org.vorpal.kosmos.combinatorics.blockdesign.FiniteProjectivePlane
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.multiset.Multiset
import org.vorpal.kosmos.core.ops.BinOp

/**
 * The canonical representation of the Fano plane, which is:
 * - The unique Steiner triple system of order 7 (STS(7)) up to isomorphism.
 * - THe projective plane / geometry, PG(2, 2).
 * - Its own dual: we can switch the roles of points and lines, and the structure remains the same.
 * Since the Fano plane is a STS, it can also serve the structure of a quasigroup, which coincides with the
 * multiplicative structure defined by the unit octonions (if the signs of the octonions are ignored).
 */
object FanoPlane : FiniteProjectivePlane<Int> {
    override val order: Int = 2
    override val points: FiniteSet<Int> = FiniteSet.of(1, 2, 3, 4, 5, 6, 7)

    data class Line(val a: Int, val b: Int, val c: Int) {
        operator fun contains(i: Int) = (i == a || i == b || i == c)
        val orderedPairs: List<Pair<Int, Int>> by lazy {
            val s = FiniteSet.of(a, b, c)
            s.cartesianProduct(s).mapNotNull { if (it.first == it.second) null else it }
        }
    }

    val lines: FiniteSet<Line> = FiniteSet.of(
        Line(1, 2, 3),
        Line(1, 4, 5),
        Line(1, 6, 7),
        Line(2, 4, 6),
        Line(2, 5, 7),
        Line(3, 4, 7),
        Line(3, 5, 6)
    )

    override val blocks: Multiset<FiniteSet<Int>> = Multiset.of(
        lines.map { FiniteSet.of(it.a, it.b, it.c) }.associateWith { 1 }
    )

    private val pointMap: Map<Pair<Int, Int>, Line> = lines.flatMap { line ->
        line.orderedPairs.map { it to line }
    }.toMap()

    fun lineThrough(i: Int, j: Int): Line =
        pointMap[i to j] ?: throw IllegalArgumentException("No line through points $i and $j")

    fun thirdPoint(i: Int, j: Int): Int =
        lineThrough(i, j).let { (setOf(it.a, it.b, it.c) - setOf(i, j)).first() }

    /**
     * As a Steiner triple system, the Fano plane's structure forms an idempotent symmetric quasigroup.
     */
    val steinerQuasigroup: Quasigroup<Int> by lazy {
        // a * a = a (idempotent), a * b = c where {a, b, c} is a line.
        val op = BinOp<Int>(Symbols.ASTERISK) { a, b ->
            if (a == b) a
            else thirdPoint(a, b)
        }
        Quasigroup.of(op, leftDiv = op, rightDiv = op)
    }
}
