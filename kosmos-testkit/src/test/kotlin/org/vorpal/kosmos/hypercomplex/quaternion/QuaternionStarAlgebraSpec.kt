package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests

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
class QuaternionStarAlgebraSpec : FunSpec({
    lawTests(lawGraph("ℍ star algebra") {
        branch("QuaternionStarAlgebra satisfies StarAlgebraLaws") {
            suite(StarAlgebraLaws(
                algebra = QuaternionAlgebras.QuaternionStarAlgebra,
                scalarArb = ArbReal.smallReal,
                algebraArb = ArbQuaternion.quaternion,
                eqR = RealAlgebras.eqRealApprox,
                eqA = QuaternionAlgebras.eqQuaternion,
                prR = RealAlgebras.printableRealPretty,
                prA = QuaternionAlgebras.printableQuaternionPretty
            ))
        }
    })
})
