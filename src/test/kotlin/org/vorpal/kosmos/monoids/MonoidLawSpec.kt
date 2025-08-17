package org.vorpal.kosmos.monoids

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.bigInt
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.triple
import org.vorpal.monoids.Eq
import org.vorpal.monoids.Monoids
import kotlin.math.abs

class MonoidLawSpec : StringSpec({
    val finiteDouble: Arb<Double> =
        Arb.double(min = -1e150, max = 1e150).filter { it.isFinite() }

    val safeTriple: Arb<Triple<Double, Double, Double>> =
        Arb.triple(finiteDouble, finiteDouble, finiteDouble)
            .filter { (a, b, c) ->
                val t1 = (a + b) + c
                val t2 = a + (b + c)
                t1.isFinite() && t2.isFinite()
            }


    val finiteDoubleMul: Arb<Double> =
        Arb.double(min = -1e100, max = 1e100)  // within ±UPPER

    // Ensure BOTH (a*b)*c and a*(b*c) are finite (no NaN/Inf).
    val safeTripleMul: Arb<Triple<Double, Double, Double>> =
        Arb.triple(finiteDoubleMul, finiteDoubleMul, finiteDoubleMul).filter { (a, b, c) ->
            val t1 = (a * b) * c
            val t2 = a * (b * c)
            t1.isFinite() && t2.isFinite()
        }

    registerMonoidLaws("Int +",
        MonoidLaws(Monoids.IntSumMonoid, Arb.int(), Monoids.IntEq))

    registerMonoidLaws("Int *",
        MonoidLaws(Monoids.IntProductMonoid, Arb.int(), Monoids.IntEq))

    registerMonoidLaws("String concat",
        MonoidLaws(Monoids.StringConcatMonoid, Arb.string(), Monoids.StringEq))

    // Use this for DoubleProductMonoid is Monoids.approxDoubleEq doesn't work: ULP = unit in the last place.
    val ulp8Eq = Eq<Double> { a, b ->
        if (a == b) true  // covers +0.0 and -0.0
        else {
            val diff = abs(a - b)
            val scale = maxOf(Math.ulp(a), Math.ulp(b)) * 8
            diff <= scale
        }
    }

    registerMonoidLaws("Double + (safe range)",
//        MonoidLaws(Monoids.DoubleSumMonoid, finiteDouble, Monoids.approxDoubleEq, assocFrom(safeTriple))
        MonoidLaws(Monoids.DoubleSumMonoid, finiteDouble, ulp8Eq, assocFrom(safeTriple))
    )

    registerMonoidLaws("Double * (safe range)",
        MonoidLaws(Monoids.DoubleProductMonoid, finiteDoubleMul, ulp8Eq, assocFrom(safeTripleMul))
    )

    registerMonoidLaws("BigInteger +",
        MonoidLaws(Monoids.BigIntegerSumMonoid, Arb.bigInt(200), Monoids.BigIntegerEq))

    registerMonoidLaws("BigInteger *",
        MonoidLaws(Monoids.BigIntegerProductMonoid, Arb.bigInt(200), Monoids.BigIntegerEq))

    registerMonoidLaws("BigDecimal +",
        MonoidLaws(Monoids.BigDecimalSumMonoid, Arb.bigDecimal(), Monoids.BigDecimalEq))

    registerMonoidLaws("BigDecimal *",
        MonoidLaws(Monoids.BigDecimalProductMonoid, Arb.bigDecimal(), Monoids.BigDecimalEq))
})