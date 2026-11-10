package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.algebra.FieldLaws
import org.vorpal.kosmos.testing.laws.lawTests

class RationalFieldSpec : FunSpec({
    val field = RationalAlgebras.RationalField

    // The full Field law graph, one Kotest test per law (e.g. Field > DivisionRing > ... > associativity (*)).
    lawTests(
        FieldLaws(
            field = field,
            arb = ArbRational.small,
            eq = RationalAlgebras.eqRational,
            pr = RationalAlgebras.printableRationalPretty
        ),
        LawMode.FULL
    )

    test("zero is Rational.ZERO") {
        field.zero shouldBe Rational.ZERO
    }

    test("one is Rational.ONE") {
        field.one shouldBe Rational.ONE
    }

    test("reciprocal throws on zero") {
        shouldThrow<ArithmeticException> {
            field.reciprocal(Rational.ZERO)
        }
    }

    test("fromBigInt agrees with canonical embedding") {
        checkAll(ArbInteger.small) { z ->
            field.fromBigInt(z) shouldBe z.toRational()
        }
    }
})
