package org.vorpal.kosmos.monoids

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import org.vorpal.monoids.Monoids

class MonoidLawSpec : StringSpec({
    val finiteDouble: Arb<Double> =
        Arb.double(min = -1e150, max = 1e150).filter { it.isFinite() }

    registerMonoidLaws("Int +",
        MonoidLaws(Monoids.IntSumMonoid, Arb.int(), Monoids.IntEq))

    registerMonoidLaws("Int *",
        MonoidLaws(Monoids.IntProductMonoid, Arb.int(), Monoids.IntEq))

    registerMonoidLaws("String concat",
        MonoidLaws(Monoids.StringConcatMonoid, Arb.string(), Monoids.StringEq))

    registerMonoidLaws(
        "Double + (safe range)",
        MonoidLaws(Monoids.DoubleSumMonoid, finiteDouble, Monoids.approxDoubleEq)
    )

    registerMonoidLaws(
        "Double * (safe range)",
        MonoidLaws(Monoids.DoubleProductMonoid, finiteDouble, Monoids.approxDoubleEq)
    )
})