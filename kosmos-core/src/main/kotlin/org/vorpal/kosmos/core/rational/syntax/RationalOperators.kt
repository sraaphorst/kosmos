package org.vorpal.kosmos.core.rational.syntax

import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import java.math.BigInteger



fun String.toRational(): Rational {
    val trimmed = trim()
    return when {
        '/' in trimmed -> {
            val parts = trimmed.split('/')
            require(parts.size == 2) { "Invalid rational format: $this" }
            Rational.Companion.of(BigInteger(parts[0].trim()), BigInteger(parts[1].trim()))
        }
        else -> Rational.Companion.of(BigInteger(trimmed))
    }
}

/**
 * Extension function to find the nearest integer.
 */
fun Rational.toNearestInt(): BigInteger {
    val f = floor()
    val c = ceil()

    val df = this - f
    val dc = c.toRational() - this

    return if (df <= dc) f else c
}

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

