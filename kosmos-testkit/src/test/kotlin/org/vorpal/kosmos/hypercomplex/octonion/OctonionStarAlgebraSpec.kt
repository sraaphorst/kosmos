package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.NonAssociativeStarAlgebraLaws

/**
 * Property tests for [OctonionAlgebras.OctonionStarAlgebra] as a
 * [org.vorpal.kosmos.algebra.structures.NonAssociativeStarAlgebra] over ℝ.
 *
 * We deliberately use [NonAssociativeStarAlgebraLaws] rather than [org.vorpal.kosmos.laws.algebra.StarAlgebraLaws]:
 * the octonions are non-associative, so the associativity law bundled into the latter must NOT be imposed.
 * This suite covers the non-associative algebra laws (bilinearity of the product, scalar compatibility with
 * the central real action), the conjugation laws (involution and the order-reversing `(xy)* = y* x*`), and
 * commutation of the star with the scalar action.
 */
object OctonionStarAlgebraSpec : StringSpec({
    "OctonionStarAlgebra satisfies NonAssociativeStarAlgebraLaws" {
        NonAssociativeStarAlgebraLaws(
            algebra = OctonionAlgebras.OctonionStarAlgebra,
            scalarArb = ArbReal.smallReal,
            algebraArb = ArbOctonion.octonion,
            eqR = RealAlgebras.eqRealApprox,
            eqA = OctonionAlgebras.eqOctonion,
            prR = RealAlgebras.printableRealPretty,
            prA = OctonionAlgebras.printableOctonionPretty
        ).fullTest().throwIfFailed()
    }
})
