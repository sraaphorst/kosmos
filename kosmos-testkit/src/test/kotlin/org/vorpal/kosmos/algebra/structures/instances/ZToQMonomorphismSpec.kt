package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw

class ZToQMonomorphismSpec: StringSpec({
    val mono = IntegerAlgebras.ZToQMonomorphism
    val arbZ = ArbInteger.small
    val eqZ = IntegerAlgebras.eqInteger
    val eqQ = RationalAlgebras.eqRational
    val prZ = IntegerAlgebras.printableInteger
    val prQ = RationalAlgebras.printableRationalPretty

    "ZToQMonomorphism satisfies UnitalRingHomomorphismLaws" {
        RingHomomorphismLaws(
            hom = mono::invoke,
            domain = mono.domain,
            codomain = mono.codomain,
            arb = arbZ,
            eqB = eqQ,
            prA = prZ,
            prB = prQ
        ).fullTest().throwIfFailed()
    }

    "ZToQMonomorphism agrees with canonical rational embedding" {
        checkAll(arbZ) { z ->
            mono(z) shouldBe z.toRational()
        }
    }

    "ZToQMonomorphism agrees with RationalField.fromBigInt" {
        checkAll(arbZ) { z ->
            mono(z) shouldBe
                RationalAlgebras.RationalField.fromBigInt(z)
        }
    }

    "ZToQMonomorphism is injective on tested inputs" {
        injectivityLaw(
            hom = mono::invoke,
            arbA = arbZ,
            eqA = eqZ,
            eqB = eqQ,
            prA = prZ,
            prB = prQ
        ).test()
    }
})
