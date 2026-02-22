package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.hypercomplex.quaternion.Quaternion
import org.vorpal.kosmos.hypercomplex.quaternion.quaternion
import org.vorpal.kosmos.hypercomplex.quaternion.w
import org.vorpal.kosmos.hypercomplex.quaternion.x
import org.vorpal.kosmos.hypercomplex.quaternion.y
import org.vorpal.kosmos.hypercomplex.quaternion.z

/**
 * An [Octonion] is a Cayley-Dickson doubling of the [Quaternion] numbers.
 */
typealias Octonion = CD<Quaternion>

val Octonion.w: Real get() = a.w
val Octonion.x: Real get() = a.x
val Octonion.y: Real get() = a.y
val Octonion.z: Real get() = a.z

val Octonion.u: Real get() = b.w
val Octonion.v: Real get() = b.x
val Octonion.s: Real get() = b.y
val Octonion.t: Real get() = b.z

fun octonion(
    w: Real, x: Real, y: Real, z: Real,
    u: Real, v: Real, s: Real, t: Real
): Octonion {
    val a = quaternion(w, x, y, z)
    val b = quaternion(u, v, s, t)
    return Octonion(a, b)
}