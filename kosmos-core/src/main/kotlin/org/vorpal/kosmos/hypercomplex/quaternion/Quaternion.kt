package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.hypercomplex.complex.Complex

typealias Quaternion = CD<Complex>

val Quaternion.w: Real get() = a.a
val Quaternion.x: Real get() = a.b
val Quaternion.y: Real get() = b.a
val Quaternion.z: Real get() = b.b

/**
 * Create a Quaternion of the form:
 *
 *     w + xi + yj + zk
 */
fun quaternion(
    w: Real,
    x: Real,
    y: Real,
    z: Real
): Quaternion {
    val a = CD(w, x)
    val b = CD(y, z)
    return CD(a, b)
}