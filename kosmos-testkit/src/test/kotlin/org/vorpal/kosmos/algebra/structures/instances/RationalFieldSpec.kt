package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.laws.algebra.FieldLaws
import org.vorpal.kosmos.core.rational.ArbRational

class RationalFieldSpec : StringSpec({
    val field = RationalAlgebras.RationalField

    "RationalField satisfies FieldLaws" {
        FieldLaws(
            field = field,
            arb = ArbRational.small,
            eq = RationalAlgebras.eqRational,
            pr = RationalAlgebras.printableRationalPretty
        ).fullTest().throwIfFailed()
    }

    "zero is Rational.ZERO" {
        field.zero shouldBe Rational.ZERO
    }

    "one is Rational.ONE" {
        field.one shouldBe Rational.ONE
    }

    "reciprocal throws on zero" {
        shouldThrow<IllegalArgumentException> {
            field.reciprocal(Rational.ZERO)
        }
    }

    "fromBigInt agrees with canonical embedding" {
        checkAll(ArbInteger.small) { z ->
            field.fromBigInt(z) shouldBe z.toRational()
        }
    }
})
