package org.vorpal.kosmos.algebra.structures.instances.embeddings

/**
 * These define the maps from one step before the quaternions (i.e. complex-like types) to the unital embeddings
 * into the quaternion families. For example:
 * 1. Complex (Real) to Quaternion (Real) (6 embeddings)
 * 2. GaussianInt to LipschitzQuaternion (6 embeddings)
 * 3. GaussianRat to RationalQuaternion (6 embeddings)
 *
 * # Exceptions:
 *
 * 1. The HurwitzQuaternion family has no nice embeddings into it. We can map the LipschitzQuaternions
 * into the HurwitzQuaternions to get six embeddings, but this does not give the entire family.
 * (We can start at the GaussianInt and get the six embeddings.)
 * The same holds for the GaussianRat via the RationalQuaternions.
 *
 * 2. EisensteinInt are not defined by where `i` goes, but by where `ω` goes, with:
 * ```kotlin
 * ω^2 + ω + 1 = 0
 * ```
 * The nice canonical choice is to pick a pure imaginary axis unit `u` (one of `±i`, `±j`, `±k`)
 * and then set:
 * ```kotlin
 * ω ↦ (-1 + u * √3)/2
 * ```
 * in `ℍ(ℝ)`. (This uses `√3`, so it does not land in integral coefficient rings.)
 *
 * Algebraically, if you want to stay in an exact setting, you typically work over a coefficient ring
 * that already contains an element:
 * - `s` with `s^2 = -3 (a √-3)`; and realize
 * - `ω` via `(-1 + s)/2`.
 *
 * If the coefficient ring is `ℤ[ω]`, then building a quaternion algebra over it is different:
 * you're no longer embedding Eisenstein integers "into" quaternions: you're constructing quaternions
 * "over" them.
 */
object AxisSignEmbeddings {
    enum class ImagAxis { I, J, K }

    enum class Sign(val factor: Int) {
        PLUS(+1),
        MINUS(-1)
    }

    data class AxisSignEmbedding(
        val axis: ImagAxis,
        val sign: Sign = Sign.PLUS
    ) {
        companion object {
            /**
             * The canonical mapping, which maps:
             * ```kotlin
             * i ↦ i
             * ```
             */
            @JvmField
            val canonical: AxisSignEmbedding =
                AxisSignEmbedding(ImagAxis.I, Sign.PLUS)

            @JvmField
            val all: List<AxisSignEmbedding> =
                ImagAxis.entries.flatMap { axis ->
                    Sign.entries.map { sign -> AxisSignEmbedding(axis, sign) }
                }
        }
    }
}
