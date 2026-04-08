package org.vorpal.kosmos.hypercomplex.quaternion

/**
 * Parameterizes the six unital embeddings of a complex-like generator into a quaternion family
 * by choosing one of the three imaginary axes `I`, `J`, `K` and one of the two signs `±`.
 *
 * Concretely, this governs embeddings such as:
 * - `Complex -> Quaternion`
 * - `GaussianInt -> LipschitzQuaternion`
 * - `GaussianRat -> RationalQuaternion`
 *
 * by specifying where the distinguished imaginary generator is sent:
 * ```text
 * i ↦ ±i, ±j, or ±k.
 * ```
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
