package org.vorpal.kosmos.noise.internal

/**
 * Internals for noise: hash64, mix64, maps.
 */

private const val KX: Long = -0x61C8864680B583EBL
private const val KY: Long = -0x3D4D51C2D82B14B1L
private const val KS: Long =  0x165667B19E3779F9L
private fun hash64(x: Int, y: Int, seed: Int): Long {
    val k: Long = x.toLong() * KX + y.toLong() * KY + seed.toLong() * KS
    return mix64(k)
}

/**
 * Use a SplitMix64 like integer hash for determinism and speed.
 * These need to be written as negative in Kotlin since we can't set the leading bit
 * to 1 and get a negative number as we would in Java.
 */
private const val C1: Long = -0x40A7B892E31B1A47L
private const val C2: Long = -0x6B2FB644ECCEEE15L
private fun mix64(z0: Long): Long {
    var z = z0
    z = (z xor (z ushr 30)) * C1
    z = (z xor (z ushr 27)) * C2
    return z xor (z ushr 31)
}

/**
 * Map of h to [0, 1).
 */
private const val INV_2_POW_53: Double = 1.0 / (1L shl 53).toDouble()
private fun longToUnit01(h: Long): Double =
    (h ushr 11).toDouble() * INV_2_POW_53

/**
 * Map of h through longToUnit01 to [0, 1) and then to [-1, 1).
 */
private fun longToMinus1To1(h: Long): Double =
    longToUnit01(h) * 2.0 - 1.0