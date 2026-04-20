package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.linear.instance.arbConstMat
import org.vorpal.kosmos.core.rational.ArbRational

class ConstantMatrixIsomorphismSpec : StringSpec({

    val iso = ConstantMatrixAlgebras.fieldIso(
        baseField = RationalAlgebras.RationalField,
        n = 6
    )

    "Ring isomorphism from RationalField and ConstantMatrixField holds" {
        RingHomomorphismLaws(
            hom = iso::invoke,
            domain = RationalAlgebras.RationalField,
            codomain = ConstantMatrixAlgebras.ConstantMatrixField(RationalAlgebras.RationalField, 6),
            arb = ArbRational.small,
            eqB = DenseMatAlgebras.liftEq(RationalAlgebras.eqRational),
            prA = RationalAlgebras.printableRationalPretty,
            prB = DenseMatAlgebras.liftPrintablePretty(RationalAlgebras.printableRationalPretty)
        ).fullTest().throwIfFailed()
    }

    "Ring isomorphism from ConstantMatrixField and RationalField holds" {
        RingHomomorphismLaws(
            hom = iso.backward::invoke,
            domain = ConstantMatrixAlgebras.ConstantMatrixField(RationalAlgebras.RationalField, 6),
            codomain = RationalAlgebras.RationalField,
            arb = arbConstMat(ArbRational.small, 6),
            eqB = RationalAlgebras.eqRational,
            prA = DenseMatAlgebras.liftPrintablePretty(RationalAlgebras.printableRationalPretty),
            prB = RationalAlgebras.printableRationalPretty
        ).fullTest().throwIfFailed()
    }

    "backward(forward(x)) = x over RationalField" {
        checkAll(ArbRational.small) { x ->
            val actual = iso.backward(iso(x))
            RationalAlgebras.eqRational(actual, x) shouldBe true
        }
    }

    "forward(backward(m)) = m over ConstantMatrixField" {
        val eqMat = DenseMatAlgebras.liftEq(RationalAlgebras.eqRational)

        checkAll(arbConstMat(ArbRational.small, 6)) { m ->
            val actual = iso(iso.backward(m))
            eqMat(actual, m) shouldBe true
        }
    }
})
