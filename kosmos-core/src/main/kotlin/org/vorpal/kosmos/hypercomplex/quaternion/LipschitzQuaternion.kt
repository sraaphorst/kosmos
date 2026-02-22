package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.hypercomplex.complex.GaussianInt
import java.math.BigInteger

/**
 * A [LipschitzQuaternion] is a Cayley-Dickson doubling of the [GaussianInt] numbers.
 */
typealias LipschitzQuaternion = CD<GaussianInt>

val LipschitzQuaternion.w: BigInteger get() = a.re
val LipschitzQuaternion.x: BigInteger get() = a.im
val LipschitzQuaternion.y: BigInteger get() = b.re
val LipschitzQuaternion.z: BigInteger get() = b.im

fun lipschitzQuaternion(w: BigInteger,
                        x: BigInteger,
                        y: BigInteger,
                        z: BigInteger): LipschitzQuaternion =
    LipschitzQuaternion(
        GaussianInt(w, x),
        GaussianInt(y, z)
    )