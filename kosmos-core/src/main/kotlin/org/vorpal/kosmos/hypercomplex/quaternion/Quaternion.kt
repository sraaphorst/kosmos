package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.complex.im
import org.vorpal.kosmos.hypercomplex.complex.re

/**
 * [Quaternion] is the Cayley-Dickson doubling of the [Complex] numbers.
 */
typealias Quaternion = CD<Complex>
val Quaternion.w: Real get() = a.re
val Quaternion.x: Real get() = a.im
val Quaternion.y: Real get() = b.re
val Quaternion.z: Real get() = b.im

/**
 * Convenience constructor for a quaternion:
 *
 *    complex(w + x i_c), complex(y + z i_c)
 */
fun quaternion(w: Real,
               x: Real,
               y: Real,
               z: Real
): Quaternion {
    val a = complex(w, x)
    val b = complex(y, z)
    return Quaternion(a, b)
}
