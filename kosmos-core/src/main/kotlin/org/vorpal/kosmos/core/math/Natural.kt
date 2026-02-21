package org.vorpal.kosmos.core.math

import java.math.BigInteger

/**
 * A simple value class representation for natural numbers, which must be non-negative.
 */
@JvmInline
value class Natural(val n: BigInteger) : Comparable<Natural> {
    constructor(n: Int): this(n.toBigInteger())
    constructor(n: Long): this(n.toBigInteger())

    init {
        require(n >= BigInteger.ZERO) { "Natural number must be non-negative: $n"}
    }

    operator fun plus(other: Natural): Natural = Natural(n + other.n)
    operator fun times(other: Natural): Natural = Natural(n * other.n)
    operator fun minus(other: Natural): Natural = Natural(n - other.n)
    operator fun div(other: Natural): Natural = Natural(n / other.n)
    operator fun rem(other: Natural): Natural = Natural(n % other.n)
    operator fun plus(other: Int): Natural = Natural(n + other.toBigInteger())
    operator fun times(other: Int): Natural = Natural(n * other.toBigInteger())
    operator fun minus(other: Int): Natural = Natural(n - other.toBigInteger())
    operator fun div(other: Int): Natural = Natural(n / other.toBigInteger())
    operator fun rem(other: Int): Natural = Natural(n % other.toBigInteger())
    operator fun plus(other: Long): Natural = Natural(n + other.toBigInteger())
    operator fun times(other: Long): Natural = Natural(n * other.toBigInteger())
    operator fun minus(other: Long): Natural = Natural(n - other.toBigInteger())
    operator fun div(other: Long): Natural = Natural(n / other.toBigInteger())
    operator fun rem(other: Long): Natural = Natural(n % other.toBigInteger())
    override operator fun compareTo(other: Natural): Int = n.compareTo(other.n)
    operator fun compareTo(other: Int): Int = n.compareTo(other.toBigInteger())
    operator fun compareTo(other: Long): Int = n.compareTo(other.toBigInteger())

    companion object {
        val ZERO = Natural(BigInteger.ZERO)
        val ONE = Natural(BigInteger.ONE)
        val TWO = Natural(BigInteger.TWO)
        val TEN = Natural(BigInteger.TEN)
    }
}

fun Int.toNatural(): Natural = Natural(toBigInteger())
fun Long.toNatural(): Natural = Natural(toBigInteger())

fun <T> Iterable<T>.sumOf(selector: (T) -> Natural): Natural =
    fold(Natural.ZERO) { acc, element -> acc + selector(element) }
