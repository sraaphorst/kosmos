package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.combinatorics.FanoPlane
import org.vorpal.kosmos.core.finiteset.toUnorderedFiniteSet

/**
 * Verifies that [OctonionFanoOrientations.deriveFromCurrentBasis] recovers the *signed* Fano-line
 * orientation that is actually used by [OctonionAlgebras.OctonionDivisionAlgebraReal], rather than
 * one taken from memory or a diagram.
 *
 * The orientation derived from the multiplication is then checked against the multiplication itself,
 * including the [OctonionFanoOrientation.sign]/[OctonionFanoOrientation.thirdPoint] API over every
 * ordered pair of distinct basis indices.
 *
 * NOTE: octonion products are compared with [OctonionAlgebras.eqOctonionStrict], NOT with Kotest's
 * structural `shouldBe`. The generated `CD`/`data class` `equals` boxes its `Double` components, so
 * `(0.0).equals(-0.0)` is `false`; additive inversion of a basis unit produces `-0.0` components, which
 * would spuriously fail a structural comparison even though the values are mathematically equal. The
 * algebra's own `Eq` compares through real equality, where `0.0 == -0.0`.
 */
class OctonionFanoOrientationSpec : StringSpec({

    val ring = OctonionAlgebras.OctonionDivisionAlgebraReal
    val eq = OctonionAlgebras.eqOctonionStrict
    val pr = OctonionAlgebras.printableOctonionStrict

    val basis = (1..7).associateWith { ring.basisMap.getValue(it) }
    fun neg(i: Int) = ring.add.inverse(basis.getValue(i))

    // Signed-zero-safe octonion equality assertion (see class kdoc).
    infix fun Octonion.shouldEqualOctonion(expected: Octonion) {
        if (eq.neqv(this, expected))
            fail("expected ${pr(expected)} but was ${pr(this)}")
    }

    "derived orientation matches the underlying (unoriented) Fano plane" {
        val orientation = OctonionFanoOrientations.deriveFromCurrentBasis()
        val underlying = orientation.lines.map { it.toFiniteSet() }.toUnorderedFiniteSet()
        val expected = FanoPlane.lines.map { it.toFiniteSet() }.toUnorderedFiniteSet()
        underlying shouldBe expected
    }

    "each oriented line (a,b,c) is a positively cyclic quaternionic triple: e_a e_b = e_c, e_b e_c = e_a, e_c e_a = e_b" {
        val orientation = OctonionFanoOrientations.deriveFromCurrentBasis()
        orientation.lines.forEach { line ->
            ring.mul(basis.getValue(line.a), basis.getValue(line.b)) shouldEqualOctonion basis.getValue(line.c)
            ring.mul(basis.getValue(line.b), basis.getValue(line.c)) shouldEqualOctonion basis.getValue(line.a)
            ring.mul(basis.getValue(line.c), basis.getValue(line.a)) shouldEqualOctonion basis.getValue(line.b)
        }
    }

    // This is the strongest check: it ties the OctonionFanoOrientation API (sign + thirdPoint),
    // i.e. OrientedFanoLine.signOfOrderedPair, directly to the octonion product for ALL 42 ordered
    // pairs of distinct basis indices. Every distinct pair lies on exactly one Fano line, so this
    // exhausts the multiplication table for the imaginary basis units.
    "sign(i,j) and thirdPoint(i,j) reproduce the basis multiplication for every ordered pair" {
        val orientation = OctonionFanoOrientations.deriveFromCurrentBasis()
        for (i in 1..7) for (j in 1..7) {
            if (i == j) continue
            val k = orientation.thirdPoint(i, j)
            val sign = orientation.sign(i, j)
            val expected = if (sign == 1) basis.getValue(k) else neg(k)
            ring.mul(basis.getValue(i), basis.getValue(j)) shouldEqualOctonion expected
        }
    }

    // signOfOrderedPair must be exactly antisymmetric: reversing an ordered pair flips the sign,
    // mirroring anticommutativity e_i e_j = - e_j e_i of the imaginary octonion units.
    "orientation sign is antisymmetric in its arguments" {
        val orientation = OctonionFanoOrientations.deriveFromCurrentBasis()
        for (i in 1..7) for (j in 1..7) {
            if (i == j) continue
            orientation.sign(i, j) shouldBe -orientation.sign(j, i)
        }
    }
})
