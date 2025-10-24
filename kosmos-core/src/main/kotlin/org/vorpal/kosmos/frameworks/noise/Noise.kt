package org.vorpal.kosmos.frameworks.noise

/**
 * General noise generator.
 * Implementations can be treated as instances of a functor from R^n -> R.
 */
fun interface Noise {
    operator fun invoke(x: Double, y: Double, z: Double): Double
}
