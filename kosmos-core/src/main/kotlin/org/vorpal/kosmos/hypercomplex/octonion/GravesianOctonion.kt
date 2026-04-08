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
 * A [GravesianOctonion] is an octonion with integer coefficients in the standard
 * Cayley-Dickson basis. Equivalently, it is obtained by applying the usual
 * Cayley-Dickson doubling construction to the Lipschitz quaternions.
 *
 * Additively, the Gravesian octonions form a free `ℤ`-module of rank 8, i.e. the
 * standard lattice `ℤ⁸` in the chosen basis. Multiplicatively, they inherit the
 * nonassociative octonion product and are closed under that multiplication.
 *
 * This should not be confused with the Cayley integers, which form a more refined
 * integral octonion lattice related to `E₈`.
 */
typealias GravesianOctonion = CD<LipschitzQuaternion>

val GravesianOctonion.w: BigInteger get() = a.w
val GravesianOctonion.x: BigInteger get() = a.x
val GravesianOctonion.y: BigInteger get() = a.y
val GravesianOctonion.z: BigInteger get() = a.z

val GravesianOctonion.u: BigInteger get() = b.w
val GravesianOctonion.v: BigInteger get() = b.x
val GravesianOctonion.s: BigInteger get() = b.y
val GravesianOctonion.t: BigInteger get() = b.z

fun gravesianOctonion(
    w: BigInteger, x: BigInteger, y: BigInteger, z: BigInteger,
    u: BigInteger, v: BigInteger, s: BigInteger, t: BigInteger
): GravesianOctonion {
    val a = lipschitzQuaternion(w, x, y, z)
    val b = lipschitzQuaternion(u, v, s, t)
    return GravesianOctonion(a, b)
}
