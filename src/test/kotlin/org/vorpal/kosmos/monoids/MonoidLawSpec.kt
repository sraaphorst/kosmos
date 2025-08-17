package org.vorpal.kosmos.monoids

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.bigInt
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import org.vorpal.monoids.Monoids

class MonoidLawSpec : StringSpec({
    registerMonoidLaws("Int +",
        MonoidLaws(Monoids.IntSumMonoid, Arb.int(), Monoids.IntEq))

    registerMonoidLaws("Int *",
        MonoidLaws(Monoids.IntProductMonoid, Arb.int(), Monoids.IntEq))

    registerMonoidLaws("String concat",
        MonoidLaws(Monoids.StringConcatMonoid, Arb.string(), Monoids.StringEq))

    registerMonoidLaws("BigInteger +",
        MonoidLaws(Monoids.BigIntegerSumMonoid, Arb.bigInt(200), Monoids.BigIntegerEq))

    registerMonoidLaws("BigInteger *",
        MonoidLaws(Monoids.BigIntegerProductMonoid, Arb.bigInt(200), Monoids.BigIntegerEq))

    registerMonoidLaws("BigDecimal +",
        MonoidLaws(Monoids.BigDecimalSumMonoid, Arb.bigDecimal(), Monoids.BigDecimalEq))

    registerMonoidLaws("BigDecimal *",
        MonoidLaws(Monoids.BigDecimalProductMonoid, Arb.bigDecimal(), Monoids.BigDecimalEq))

    registerListMonoidLaws("List<Int> concat",Arb.int(), Monoids.IntEq)
    registerListMonoidLaws("List<String> concat", Arb.string(), Monoids.StringEq)
    registerListMonoidLaws("List<List<Int>> concat", Arb.int().lists(), Monoids.IntEq.lists())

    registerSetMonoidLaws("Set<Int> union", Arb.int())
    registerSetMonoidLaws("Set<Set<Int>> union", Arb.int().sets())

    val arbNullableInt: Arb<Int?> = Arb.int().orNull(0.2)
    val arbListNullableInt: Arb<List<Int?>> = Arb.list(arbNullableInt, 0..20)

    registerMonoidLaws("List<Int?> concat",
        MonoidLaws(Monoids.listMonoid<Int?>(), arbListNullableInt, Monoids.listEq(Monoids.nullableEq(Monoids.IntEq))))
})