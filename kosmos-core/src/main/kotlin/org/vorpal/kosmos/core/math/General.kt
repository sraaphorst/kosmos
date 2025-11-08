package org.vorpal.kosmos.core.math

import java.math.BigInteger
import kotlin.math.PI

/**
 * Unclamped linear interpolation where t = 0 gives a and t = 1 gives b.
 */
@JvmName("lerpDouble")
fun lerp(a: Double, b: Double, t: Double): Double =
    Math.fma(t, (b - a), a)

/**
 * Unclamped linear interpolation where t = 0 gives a and t = 1 gives b.
 */
@JvmName("lerpFloat")
fun lerp(a: Float, b: Float, t: Float): Float =
    Math.fma(t, (b - a), a)

/**
 * Clamp x down in the interval [lo, hi].
 */
fun clamp(x: Double, lo: Double, hi: Double): Double =
    Math.clamp(x, lo, hi)

/**
 * Clamp x down in the interval [lo, hi].
 */
fun clamp(x: Float, lo: Float, hi: Float): Float =
    Math.clamp(x, lo, hi)

/**
 * lerp with t clamped to the interval [0, 1], thus clamping lerp to [a, b].
 */
fun lerpClamped(a: Double, b: Double, t: Double): Double =
    lerp(a, b, clamp(t, 0.0, 1.0))

fun lerpClamped(a: Float, b: Float, t: Float): Float =
    lerp(a, b, clamp(t, 0.0f, 1.0f))

/**
 * Inverse lerp: calculates a number in the interval [0, 1] where x falls between a and b.
 * When x = a, invLerp returns 0.
 * When x = b, invLerp returns 1.
 * Values outside [0, 1] can be returned if x < a or x > b.
 * Precondition: a != b.
 */
fun invLerp(a: Double, b: Double, x: Double): Double {
    require( a != b) { "invLerp requires a ≠ b."}
    return (x - a) / (b - a)
}

fun invLerp(a: Float, b: Float, x: Float): Float {
    require(a != b) { "invLerp requires a ≠ b."}
    return (x - a) / (b - a)
}

/**
 * Given a value x in an interval [a0, b0], remap it to its corresponding position
 * in the interval [a1, b1].
 */
fun remap(x: Double, a0: Double, b0: Double, a1: Double, b1: Double): Double =
    lerp(a1, b1, invLerp(a0, b0, x))

fun remap(x: Float, a0: Float, b0: Float, a1: Float, b1: Float): Float =
    lerp(a1, b1, invLerp(a0, b0, x))

infix fun Int.modPositive(mod: Int): Int =
    ((this % mod) + mod) % mod

infix fun Long.modPositive(mod: Long): Long =
    ((this % mod) + mod) % mod

infix fun BigInteger.modPositive(mod: BigInteger): BigInteger =
    ((this % mod) + mod) % mod

fun Double.degToRad(): Double = this / 180.0 * PI
fun Double.radToDeg(): Double = this * 180.0 / PI
