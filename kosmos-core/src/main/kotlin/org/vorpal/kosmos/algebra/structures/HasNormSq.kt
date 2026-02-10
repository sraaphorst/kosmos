package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * An object that has a conjugation-induced norm squared function.
 */
interface HasNormSq<A : Any, N: Any> {
    val normSq: UnaryOp<A, N>
}
