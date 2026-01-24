package org.vorpal.kosmos.core.math

import java.math.BigInteger
import kotlin.math.PI

/**
 * Unclamped linear interpolation where t = 0 gives a and t = 1 gives b.
 */
@JvmName("lerpReal")
fun lerp(a: Real, b: Real, t: Real): Real =
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
fun clamp(x: Real, lo: Real, hi: Real): Real =
    Math.clamp(x, lo, hi)

/**
 * Clamp x down in the interval [lo, hi].
 */
fun clamp(x: Float, lo: Float, hi: Float): Float =
    Math.clamp(x, lo, hi)

/**
 * lerp with t clamped to the interval [0, 1], thus clamping lerp to [a, b].
 */
fun lerpClamped(a: Real, b: Real, t: Real): Real =
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
fun invLerp(a: Real, b: Real, x: Real): Real {
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
fun remap(x: Real, a0: Real, b0: Real, a1: Real, b1: Real): Real =
    lerp(a1, b1, invLerp(a0, b0, x))

fun remap(x: Float, a0: Float, b0: Float, a1: Float, b1: Float): Float =
    lerp(a1, b1, invLerp(a0, b0, x))

infix fun Int.modPositive(mod: Int): Int =
    ((this % mod) + mod) % mod

infix fun Long.modPositive(mod: Long): Long =
    ((this % mod) + mod) % mod

infix fun BigInteger.modPositive(mod: BigInteger): BigInteger =
    ((this % mod) + mod) % mod

fun Real.degToRad(): Real = this / 180.0 * PI
fun Real.radToDeg(): Real = this * 180.0 / PI


/**
 * Calculates (-1).pow(n).
 */
fun intSgn(n: Int): Int =
    if (n and 1 == 0) 1 else -1

/**
 * Calculates (-1L).pow(n).
 */
fun longSgn(n: Int): Long =
    if (n and 1 == 0) 1L else -1L

/**
 * Calculates (-BigInteger.ONE).pow(n).
 */
fun bigIntSgn(n: Int): BigInteger =
    if (n and 1 == 0) BigInteger.ONE else -BigInteger.ONE