package org.vorpal.kosmos.std

import java.math.BigInteger

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