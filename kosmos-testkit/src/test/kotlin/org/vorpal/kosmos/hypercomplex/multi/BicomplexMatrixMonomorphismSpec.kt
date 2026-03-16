package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex

class BicomplexMatrixMonomorphismSpec : StringSpec({
    val ring = BicomplexAlgebras.BicomplexCommutativeRing
    val canonical = BicomplexAlgebras.BicomplexToComplexMatrixMonomorphism
    val diagonal = BicomplexAlgebras.BicomplexToDiagonalMatrixMonomorphism
    val eqMat = org.vorpal.kosmos.linear.instances.DenseMatAlgebras.liftEq(
        ComplexAlgebras.eqComplex
    )

    "canonical matrix map preserves addition" {
        val x = Bicomplex.ofStandard(1.0, 2.0, 3.0, 4.0)
        val y = Bicomplex.ofStandard(-2.0, 1.5, 0.5, -3.0)

        val left = canonical(ring.add(x, y))
        val right = canonical.codomain.add(canonical(x), canonical(y))

        check(eqMat(left, right))
    }

    "canonical matrix map preserves multiplication" {
        val x = Bicomplex.ofStandard(1.0, 2.0, 3.0, 4.0)
        val y = Bicomplex.ofStandard(-2.0, 1.5, 0.5, -3.0)

        val left = canonical(ring.mul(x, y))
        val right = canonical.codomain.mul(canonical(x), canonical(y))

        check(eqMat(left, right))
    }

    "diagonal matrix map preserves multiplication" {
        val x = Bicomplex.ofIdempotent(complex(1.0, 2.0), complex(3.0, 4.0))
        val y = Bicomplex.ofIdempotent(complex(-1.0, 0.5), complex(2.0, -3.0))

        val left = diagonal(ring.mul(x, y))
        val right = diagonal.codomain.mul(diagonal(x), diagonal(y))

        check(eqMat(left, right))
    }
})