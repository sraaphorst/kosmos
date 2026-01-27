package org.vorpal.kosmos.algebra.structures

import java.math.BigInteger

/**
 * Optional capability for structures with a known characteristic.
 *
 * characteristic = 0 means characteristic zero.
 * characteristic > 0 means finite characteristic.
 */
interface HasCharacteristic {
    val characteristic: BigInteger
}
