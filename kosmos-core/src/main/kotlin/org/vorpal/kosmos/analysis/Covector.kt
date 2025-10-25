package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * A [Covector] is a linear functional ω : V → 𝔽 on a [VectorSpace].
 */
fun interface Covector<F, V> where F: Any, V: VectorSpace<F, V> {
    operator fun invoke(v: V): F
}
