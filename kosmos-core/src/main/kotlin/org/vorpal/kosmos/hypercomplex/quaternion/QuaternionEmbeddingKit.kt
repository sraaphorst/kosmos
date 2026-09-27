package org.vorpal.kosmos.hypercomplex.quaternion

import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * Shares the logic that takes a complex-like type:
 * - `GaussianInt`
 * - `GaussianRat`
 * - `Complex` (Real)
 * and turns it into a quaternion-type.
 *
 * The AxisSignEmbedding is the specification, and the QuaternionEmbeddingKit is the implementation.
 */
internal object QuaternionEmbeddingKit {
    /**
     * Take a complex-like value `re + im·i` and the information for an embedding, which can map:
     * ```kotlin
     * i ↦ ± i
     * i ↦ ± j
     * i ↦ ± k
     * ```
     *
     * [mkQuaternion] builds a quaternion from its four coordinates `[w, x, y, z]`.
     */
    internal fun <A : Any, Q : Any> embedComplexLike(
        axisSign: AxisSignEmbeddings.AxisSignEmbedding,
        re: A,
        im: A,
        zero: A,
        negate: Endo<A>,
        mkQuaternion: UnaryOp<List<A>, Q>
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
        return mkQuaternion(
            when (axisSign.axis) {
                AxisSignEmbeddings.ImagAxis.I -> listOf(re, signedIm, zero, zero)
                AxisSignEmbeddings.ImagAxis.J -> listOf(re, zero, signedIm, zero)
                AxisSignEmbeddings.ImagAxis.K -> listOf(re, zero, zero, signedIm)
            }
        )
    }
}
