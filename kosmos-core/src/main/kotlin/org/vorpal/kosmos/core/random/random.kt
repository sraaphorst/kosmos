package org.vorpal.kosmos.core.random

import java.math.BigInteger
import kotlin.math.max
import kotlin.random.Random

fun Random.nextBigInteger(bound: BigInteger): BigInteger {
    require(bound > BigInteger.ZERO) { "bound must be positive: $bound" }

    val bits = bound.bitLength()
    val byteLen = max(1, (bits + 7) / 8)

    while (true) {
        val bytes = ByteArray(byteLen)
        nextBytes(bytes)

        // Mask off extra high bits so value has at most `bits` bits.
        val extraBits = byteLen * 8 - bits
        if (extraBits > 0) {
            val mask = (0xFF ushr extraBits).toByte()
            bytes[0] = (bytes[0].toInt() and mask.toInt()).toByte()
        }

        val candidate = BigInteger(1, bytes)
        if (candidate < bound) {
            return candidate
        }
    }
}