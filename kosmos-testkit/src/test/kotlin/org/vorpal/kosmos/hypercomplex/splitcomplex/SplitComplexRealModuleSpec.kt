package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.algebra.RModuleLaws

/**
 * Property tests for `SplitComplexAlgebras.splitComplexModule(RealField)`,
 * the canonical free `R`-module `SplitComplex<Real>` of rank 2.
 *
 * Although covered transitively by the algebra and star-algebra suites,
 * exercising the module laws on their own gives a clean target when only
 * the additive / scalar-action structure is being investigated.
 */
object SplitComplexRealModuleSpec : StringSpec({
    "splitComplexModule(Real) satisfies RModuleLaws" {
        RModuleLaws(
            module = SplitComplexAlgebras.splitComplexModule(RealAlgebras.RealField),
            scalarArb = ArbReal.smallReal,
            vectorArb = ArbSplitComplex.smallSplitComplex,
            eqR = RealAlgebras.eqRealApprox,
            eqM = SplitComplexAlgebras.eqSplitComplex,
            prR = RealAlgebras.printableRealPretty,
            prM = SplitComplexAlgebras.printableSplitComplexPretty
        ).fullTest().throwIfFailed()
    }
})
