package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.hypercomplex.quaternion.LipschitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.lipschitzQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.w
import org.vorpal.kosmos.hypercomplex.quaternion.x
import org.vorpal.kosmos.hypercomplex.quaternion.y
import org.vorpal.kosmos.hypercomplex.quaternion.z
import java.math.BigInteger

/**
 * A [CayleyOctonion] is a Cayley-Dickson doubling of the [LipschitzQuaternion] numbers.
 */
typealias CayleyOctonion = CD<LipschitzQuaternion>

val CayleyOctonion.w: BigInteger get() = a.w
val CayleyOctonion.x: BigInteger get() = a.x
val CayleyOctonion.y: BigInteger get() = a.y
val CayleyOctonion.z: BigInteger get() = a.z

val CayleyOctonion.u: BigInteger get() = b.w
val CayleyOctonion.v: BigInteger get() = b.x
val CayleyOctonion.s: BigInteger get() = b.y
val CayleyOctonion.t: BigInteger get() = b.z

fun cayleyOctonion(
    w: BigInteger, x: BigInteger, y: BigInteger, z: BigInteger,
    u: BigInteger, v: BigInteger, s: BigInteger, t: BigInteger
): CayleyOctonion {
    val a = lipschitzQuaternion(w, x, y, z)
    val b = lipschitzQuaternion(u, v, s, t)
    return CayleyOctonion(a, b)
}
