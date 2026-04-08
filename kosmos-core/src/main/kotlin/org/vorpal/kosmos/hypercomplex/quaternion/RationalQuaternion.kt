package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.numberfields.quadratic.GaussianRat

/**
 * A [RationalQuaternion] is a Cayley-Dickson doubling of the [GaussianRat] numbers.
 */
typealias RationalQuaternion = CD<GaussianRat>
val RationalQuaternion.w: Rational get() = a.re
val RationalQuaternion.x: Rational get() = a.im
val RationalQuaternion.y: Rational get() = b.re
val RationalQuaternion.z: Rational get() = b.im

fun rationalQuaternion(w: Rational,
                       x: Rational,
                       y: Rational,
                       z: Rational): RationalQuaternion =
    RationalQuaternion(
        GaussianRat(w, x),
        GaussianRat(y, z),
    )
