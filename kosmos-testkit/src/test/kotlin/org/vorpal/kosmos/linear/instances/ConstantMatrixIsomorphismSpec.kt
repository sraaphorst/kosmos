package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.instance.arbConstMat
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

class ConstantMatrixIsomorphismSpec : FunSpec({
    lawTests(lawGraph("constant matrix isomorphism") {
        val iso = ConstantMatrixAlgebras.fieldIso(
            baseField = RationalAlgebras.RationalField,
            n = 6
        )

        branch("Ring isomorphism from RationalField and ConstantMatrixField holds") {
            suite(RingHomomorphismLaws(
                hom = iso::invoke,
                domain = RationalAlgebras.RationalField,
                codomain = ConstantMatrixAlgebras.ConstantMatrixField(RationalAlgebras.RationalField, 6),
                arb = ArbRational.small,
                eqB = DenseMatAlgebras.liftEq(RationalAlgebras.eqRational),
                prA = RationalAlgebras.printableRationalPretty,
                prB = DenseMatAlgebras.liftPrintablePretty(RationalAlgebras.printableRationalPretty)
            ))
        }

        branch("Ring isomorphism from ConstantMatrixField and RationalField holds") {
            suite(RingHomomorphismLaws(
                hom = iso.backward::invoke,
                domain = ConstantMatrixAlgebras.ConstantMatrixField(RationalAlgebras.RationalField, 6),
                codomain = RationalAlgebras.RationalField,
                arb = arbConstMat(ArbRational.small, 6),
                eqB = RationalAlgebras.eqRational,
                prA = DenseMatAlgebras.liftPrintablePretty(RationalAlgebras.printableRationalPretty),
                prB = RationalAlgebras.printableRationalPretty
            ))
        }

        local("backward(forward(x)) = x over RationalField") {
            lawCheckAll(ArbRational.small) { x ->
                val actual = iso.backward(iso(x))
                RationalAlgebras.eqRational(actual, x) shouldBe true
            }
        }

        local("forward(backward(m)) = m over ConstantMatrixField") {
            val eqMat = DenseMatAlgebras.liftEq(RationalAlgebras.eqRational)

            lawCheckAll(arbConstMat(ArbRational.small, 6)) { m ->
                val actual = iso(iso.backward(m))
                eqMat(actual, m) shouldBe true
            }
        }
    })
})
