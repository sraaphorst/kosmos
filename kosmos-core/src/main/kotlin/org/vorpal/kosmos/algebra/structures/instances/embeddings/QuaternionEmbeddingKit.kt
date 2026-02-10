package org.vorpal.kosmos.algebra.structures.instances.embeddings

/**
 * Shares the logic that takes a complex-like type:
 * - `GaussianInt`
 * - `GaussianRat`
 * - `Complex<Real>`
 * and turns it into a quaternion-type.
 */
internal object QuaternionEmbeddingKit {
    /**
     * Take a complex-like type and the information for an embedding, which can map:
     * ```kotlin
     * i ↦ ± i
     * i ↦ ± j
     * i ↦ ± k
     * ```
     */
    internal fun <A : Any, Q: Any> embedComplexLike(
        axisSign: AxisSignEmbeddings.AxisSignEmbedding,
        re: A,
        im: A,
        zero: A,
        negate: (A) -> A,
        mkQuaternion: (A, A, A, A) -> Q
    ): Q {
        // Determine if `i ↦ ± <destination axis>`
        // If `signedIm` is `im`, `i` maps positively to its destination axis.
        // If `signedIm` is `negate(im)`, `i` maps negatively to its destination axis.
        val signedIm =
            if (axisSign.sign == AxisSignEmbeddings.Sign.PLUS) im
            else negate(im)

        // The maps:
        // 1. `i ↦ ± i`
        // 2. `i ↦ ± j`
        // 3. `i ↦ ± k`
        return when (axisSign.axis) {
            AxisSignEmbeddings.ImagAxis.I -> mkQuaternion(re, signedIm, zero, zero)
            AxisSignEmbeddings.ImagAxis.J -> mkQuaternion(re, zero, signedIm, zero)
            AxisSignEmbeddings.ImagAxis.K -> mkQuaternion(re, zero, zero, signedIm)
        }
    }
}