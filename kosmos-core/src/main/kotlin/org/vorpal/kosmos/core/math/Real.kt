package org.vorpal.kosmos.core.math

import java.math.BigInteger
import kotlin.random.Random

/**
 * An early typealias for Real, so that it can be used without introducing dependencies
 * instead of using Double.
 */
typealias Real = Double
typealias RealArray = DoubleArray

/**
 * Default tolerance(s) to be used in Real calculations.
 */
object RealTolerances {
    const val DEFAULT: Real = 1e-10
}

// Real converters.
fun Byte.toReal(): Real = toDouble()
fun Char.toReal(): Real = code.toDouble()
fun Short.toReal(): Real = toDouble()
fun Int.toReal(): Real = toDouble()
fun UInt.toReal(): Real = toDouble()
fun Long.toReal(): Real = toDouble()
fun ULong.toReal(): Real = toDouble()
fun BigInteger.toReal(): Real = toDouble()
fun Float.toReal(): Real = toDouble()

fun Random.nextReal(): Real = nextDouble()
