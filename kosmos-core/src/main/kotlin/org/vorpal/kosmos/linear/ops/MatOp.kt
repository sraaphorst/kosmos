package org.vorpal.kosmos.linear.ops

/**
 * An enum indicating how a `MatLike` should be treated inside an operation:
 * - [Normal] indicates that the `MatLike` should just be used as-is.
 * - [Trans] indicates that the transpose of the `MatLike` should be used.
 * - [ConjTrans] indicates that the transpose of the conj of the `MatLike` under the action of an `InvolutiveRing`
 *   should be used.
 */
enum class MatOp {
    Normal,
    Trans,
    ConjTrans
}
