package org.vorpal.kosmos.core.rational

import org.vorpal.kosmos.core.rational.div
import org.vorpal.kosmos.core.rational.minus
import org.vorpal.kosmos.core.rational.plus
import org.vorpal.kosmos.core.rational.times
import java.math.BigInteger


/** Parse rational from string formats like "3/4", "5", "-2/7" */
fun String.toRational(): Rational {
    val trimmed = trim()
    return when {
        '/' in trimmed -> {
            val parts = trimmed.split('/')
            require(parts.size == 2) { "Invalid rational format: $this" }
            Rational.of(BigInteger(parts[0].trim()), BigInteger(parts[1].trim()))
        }
        else -> Rational.of(BigInteger(trimmed))
    }
}

// Pimp existing types to be able to convert to Rational.
fun Int.toRational(): Rational = Rational.of(this.toBigInteger(), BigInteger.ONE)
fun Long.toRational(): Rational = Rational.of(this.toBigInteger(), BigInteger.ONE)
fun BigInteger.toRational(): Rational = Rational.of(this, BigInteger.ONE)
fun Short.toRational(): Rational = Rational.of(this.toInt().toBigInteger(), BigInteger.ONE)
fun Byte.toRational(): Rational  = Rational.of(this.toInt().toBigInteger(), BigInteger.ONE)
fun UInt.toRational(): Rational  = Rational.of(this.toLong().toBigInteger(), BigInteger.ONE)
fun ULong.toRational(): Rational = Rational.of(this.toLong().toBigInteger(), BigInteger.ONE)

// Extension operators for left-hand integer operations
operator fun Int.plus(rational: Rational): Rational = this.toRational() + rational
operator fun Int.minus(rational: Rational): Rational = this.toRational() - rational
operator fun Int.times(rational: Rational): Rational = this.toRational() * rational
operator fun Int.div(rational: Rational): Rational = this.toRational() / rational

operator fun Long.plus(rational: Rational): Rational = this.toRational() + rational
operator fun Long.minus(rational: Rational): Rational = this.toRational() - rational
operator fun Long.times(rational: Rational): Rational = this.toRational() * rational
operator fun Long.div(rational: Rational): Rational = this.toRational() / rational

operator fun BigInteger.plus(rational: Rational): Rational = this.toRational() + rational
operator fun BigInteger.minus(rational: Rational): Rational = this.toRational() - rational
operator fun BigInteger.times(rational: Rational): Rational = this.toRational() * rational
operator fun BigInteger.div(rational: Rational): Rational = this.toRational() / rational
