package org.vorpal.kosmos.hypercomplex.octonion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.quaternion.RationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.rationalQuaternion
import org.vorpal.kosmos.hypercomplex.quaternion.w
import org.vorpal.kosmos.hypercomplex.quaternion.x
import org.vorpal.kosmos.hypercomplex.quaternion.y
import org.vorpal.kosmos.hypercomplex.quaternion.z

/**
 * A [RationalOctonion] is a Cayley-Dickson doubling of the [RationalQuaternion] numbers.
 */
typealias RationalOctonion = CD<RationalQuaternion>

val RationalOctonion.w: Rational get() = a.w
val RationalOctonion.x: Rational get() = a.x
val RationalOctonion.y: Rational get() = a.y
val RationalOctonion.z: Rational get() = a.z

val RationalOctonion.u: Rational get() = b.w
val RationalOctonion.v: Rational get() = b.x
val RationalOctonion.s: Rational get() = b.y
val RationalOctonion.t: Rational get() = b.z

fun rationalOctonion(w: Rational, x: Rational, y: Rational, z: Rational,
                     u: Rational, v: Rational, s: Rational, t: Rational
): RationalOctonion {
    val a = rationalQuaternion(w, x, y, z)
    val b = rationalQuaternion(u, v, s, t)
    return RationalOctonion(a, b)
}