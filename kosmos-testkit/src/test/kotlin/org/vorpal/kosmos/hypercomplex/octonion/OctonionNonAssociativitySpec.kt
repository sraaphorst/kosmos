package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec

/**
 * Confirms that [OctonionAlgebras.OctonionDivisionAlgebraReal] is *genuinely* non-associative, i.e. that
 * the Cayley-Dickson doubling of the quaternions did not collapse into something accidentally associative.
 *
 * The alternativity and Moufang law suites confirm the *weak* associativity that the octonions DO satisfy;
 * this spec is the complementary negative check: it exhibits an explicit associator that does not vanish.
 *
 * Witness: three imaginary units that do NOT share a Fano line, e.g. e1, e2, e4 (the line through 1 and 2
 * is {1,2,3}, which excludes 4). For such a triple the associator
 *
 *     [a, b, c] = (a b) c - a (b c)
 *
 * is non-zero; concretely (e1 e2) e4 - e1 (e2 e4) = 2 e7. For an on-line (quaternionic) triple the
 * associator vanishes, since each Fano line spans an associative ℍ-subalgebra.
 *
 * Octonion products are compared with [OctonionAlgebras.eqOctonionStrict] (signed-zero-safe), never the
 * structural `shouldBe`; see [OctonionFanoOrientationSpec].
 */
object OctonionNonAssociativitySpec : StringSpec({

    val ring = OctonionAlgebras.OctonionDivisionAlgebraReal
    val eq = OctonionAlgebras.eqOctonionStrict
    val pr = OctonionAlgebras.printableOctonionStrict

    fun e(i: Int): Octonion = ring.basisMap.getValue(i)

    /** The associator [a, b, c] = (a b) c - a (b c). */
    fun associator(a: Octonion, b: Octonion, c: Octonion): Octonion =
        ring.add(
            ring.mul(ring.mul(a, b), c),
            ring.add.inverse(ring.mul(a, ring.mul(b, c)))
        )

    infix fun Octonion.shouldEqualOctonion(expected: Octonion) {
        if (eq.neqv(this, expected))
            fail("expected ${pr(expected)} but was ${pr(this)}")
    }

    "octonion multiplication is genuinely non-associative: the off-line associator [e1, e2, e4] is non-zero" {
        val assoc = associator(e(1), e(2), e(4))
        if (eq(assoc, ring.zero))
            fail("expected a non-zero associator [e1, e2, e4], proving non-associativity, but it vanished")
        // Sharper, structural form: the associator is exactly 2 e7.
        assoc shouldEqualOctonion ring.add(e(7), e(7))
    }

    "the associator vanishes within an on-line (quaternionic) triple [e1, e2, e3]" {
        associator(e(1), e(2), e(3)) shouldEqualOctonion ring.zero
    }
})
