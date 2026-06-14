package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws

/**
 * Property tests for [QuaternionAlgebras.QuaternionStarAlgebra] as an (associative)
 * [org.vorpal.kosmos.algebra.structures.StarAlgebra] over ℝ.
 *
 * Unlike the octonions, ℍ is associative, so we use the associative [StarAlgebraLaws] here: it bundles
 * the non-associative star-algebra laws (bilinearity of the product, scalar compatibility with the
 * central real action, the involution and order-reversing `(xy)* = y* x*`) together with an
 * [org.vorpal.kosmos.laws.property.AssociativityLaw] on the product — which is exactly the property
 * that distinguishes ℍ from 𝕆.
 */
object QuaternionStarAlgebraSpec : StringSpec({
    "QuaternionStarAlgebra satisfies StarAlgebraLaws" {
        StarAlgebraLaws(
            algebra = QuaternionAlgebras.QuaternionStarAlgebra,
            scalarArb = ArbReal.smallReal,
            algebraArb = ArbQuaternion.quaternion,
            eqR = RealAlgebras.eqRealApprox,
            eqA = QuaternionAlgebras.eqQuaternion,
            prR = RealAlgebras.printableRealPretty,
            prA = QuaternionAlgebras.printableQuaternionPretty
        ).fullTest().throwIfFailed()
    }
})
