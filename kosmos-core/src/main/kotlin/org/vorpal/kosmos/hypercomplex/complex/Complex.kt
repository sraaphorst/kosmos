package org.vorpal.kosmos.hypercomplex.complex

import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.core.math.Real

/**
 * A Complex is a Cayley-Dickson doubling of the Real numbers.
 */
typealias Complex = CD<Real>

val Complex.re: Real get() = a
val Complex.im: Real get() = b
fun complex(re: Real, im: Real): Complex = Complex(re, im)
