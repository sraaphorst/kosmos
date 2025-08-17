package org.vorpal.monoids

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.abs

interface Monoid<M> {
    val empty: M

    // This operation must be associative.
    fun combine(x: M, y: M): M
}

// A fun interface is simply a SAM type (single abstract method), like Java's Runnable, Comparator<T>, etc.
fun interface Eq<A> { fun eqv(a: A, b: A): Boolean }
fun <A> Eq<A>.assertEquals(x: A, y: A) = require(eqv(x, y)) {
    "Law failure: expected $x == $y"
}

object Monoids {
    val IntSumMonoid = object : Monoid<Int> {
        override val empty = 0
        override fun combine(x: Int, y: Int) = x + y
    }

    val IntProductMonoid = object : Monoid<Int> {
        override val empty = 1
        override fun combine(x: Int, y: Int) = x * y
    }

    val IntEq = Eq<Int> { a, b -> a == b }

    val DoubleSumMonoid = object : Monoid<Double> {
        override val empty = 0.0
        override fun combine(x: Double, y: Double) = x + y
    }

    val DoubleProductMonoid = object : Monoid<Double> {
        override val empty = 1.0
        override fun combine(x: Double, y: Double) = x * y
    }

    val approxDoubleEq = Eq<Double> { x, y ->
        if (x.isNaN() && y.isNaN()) true
        else {
            val diff = abs(x - y)
            diff <= 1e-9 || diff <= 1e-9 * maxOf(abs(x), abs(y))
        }
    }

    val StringConcatMonoid = object : Monoid<String> {
        override val empty = ""
        override fun combine(x: String, y: String) = x + y
    }

    val StringEq = Eq<String> { a, b -> a == b }

    fun <A> listMonoid() = object : Monoid<List<A>> {
        override val empty = emptyList<A>()
        override fun combine(x: List<A>, y: List<A>): List<A> = x + y
    }

    fun <A> listEq(eqA: Eq<A> = Eq { a1, a2 -> a1 == a2 }): Eq<List<A>> =
        Eq { a, b ->
            if (a.size != b.size) false
            else a.indices.all { i -> eqA.eqv(a[i], b[i]) }
        }

    // Convenience function to extend to an Eq<A> to handle lists.
    fun <A> Eq<A>.toListEq(): Eq<List<A>> = listEq(this)

    val listOfDoublesEq = listEq(approxDoubleEq)

    fun <A> nullableEq(inner: Eq<A>): Eq<A?> = Eq { x, y ->
        when {
            x === y -> true
            x == null || y == null -> false
            else -> inner.eqv(x, y)
        }
    }

    val BigIntegerSumMonoid = object : Monoid<BigInteger> {
        override val empty = BigInteger.ZERO
        override fun combine(x: BigInteger, y: BigInteger): BigInteger = x + y
    }

    val BigIntegerProductMonoid = object : Monoid<BigInteger> {
        override val empty = BigInteger.ONE
        override fun combine(x: BigInteger, y: BigInteger): BigInteger = x * y
    }

    val BigIntegerEq = Eq<BigInteger> { a, b -> a == b}

    val BigDecimalSumMonoid = object : Monoid<BigDecimal> {
        override val empty = BigDecimal.ZERO
        override fun combine(x: BigDecimal, y: BigDecimal): BigDecimal = x + y
    }

    val BigDecimalProductMonoid = object : Monoid<BigDecimal> {
        override val empty = BigDecimal.ONE
        override fun combine(x: BigDecimal, y: BigDecimal): BigDecimal = x * y
    }

    val BigDecimalEq = Eq<BigDecimal> { a, b -> a == b }
}
