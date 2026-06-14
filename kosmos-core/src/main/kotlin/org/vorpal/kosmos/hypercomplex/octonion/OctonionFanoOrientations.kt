package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.combinatorics.FanoPlane
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet

/**
 * This is the most important part of the Fano plane octonion classes:
 *
 * We derive the orientation of the Fano plane lines from the actial octonion multiplication, and not
 * from memory or a diagram.
 */
object OctonionFanoOrientations {
    private val ring = OctonionAlgebras.OctonionDivisionAlgebraReal

    private val basis: Map<Int, Octonion> =
        (1..7).associateWith { ring.basisMap.getValue(it) }

    private fun neg(o: Octonion): Octonion =
        ring.add.inverse(o)

    /**
     * Returns `(k, sign)` where `e_i * e_j = sign * e_k`.
     */
    private fun signedBasisProduct(i: Int, j: Int): Pair<Int, Int> {
        require(i in 1..7 && j in 1..7 && i != j) {
            "Basis indices must be distinct elements of {1, ..., 7}: got $i and $j."
        }

        val eq = OctonionAlgebras.eqOctonionStrict
        val product = ring.mul(basis.getValue(i), basis.getValue(j))

        return (1..7).firstNotNullOfOrNull { k ->
            val ek = basis.getValue(k)
            when {
                eq(product, ek) -> k to 1
                eq(product, neg(ek)) -> k to -1
                else -> null
            }
        } ?: error("Product of e$i and e$j was not ±ek for any k in {1, ..., 7}.")
    }

    fun deriveFromCurrentBasis(): OctonionFanoOrientation {
        val orientedLines = FanoPlane.lines.map { line ->
            val points = line.toList()

            val candidates = listOf(
                points[0] to points[1],
                points[0] to points[2],
                points[1] to points[2]
            )

            val triple = candidates.firstNotNullOfOrNull { (i, j) ->
                val (k, sign) = signedBasisProduct(i, j)
                if (sign == 1 && k in points) Triple(i, j, k)
                else null
            } ?: error("Could not orient Fano line $points from basis multiplication.")

            OrientedFanoLine(
                a = triple.first,
                b = triple.second,
                c = triple.third
            )
        }.toUnorderedFiniteSet()

        return OctonionFanoOrientation(orientedLines)
    }
}
