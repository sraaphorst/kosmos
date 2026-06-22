package org.vorpal.kosmos.linear.instance

import io.kotest.property.Arb
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.linear.values.DenseVec

/**
 * Generate an arbitrary [DenseVec] of exactly [size] entries drawn from [arbF].
 *
 * [size] may be zero, which yields the empty vector.
 */
fun <F : Any> arbDenseVec(
    arbF: Arb<F>,
    size: Int,
): Arb<DenseVec<F>> {
    require(size >= 0) { "size must be nonnegative, got $size" }
    return Arb.list(arbF, size..size).map { DenseVec.of(it) }
}

/**
 * Generate an arbitrary [DenseVec] whose length is uniformly chosen in `[minSize, maxSize]`
 * and whose entries are drawn from [arbF].
 */
fun <F : Any> arbDenseVecOfVaryingSize(
    arbF: Arb<F>,
    minSize: Int,
    maxSize: Int,
): Arb<DenseVec<F>> {
    require(minSize >= 0) { "minSize must be nonnegative, got $minSize" }
    require(maxSize >= minSize) { "maxSize must be >= minSize, got minSize=$minSize, maxSize=$maxSize" }
    return Arb.int(minSize..maxSize).flatMap { n -> arbDenseVec(arbF, n) }
}

/**
 * Generate a pair of equal-length [DenseVec]s (length uniformly chosen in `[minSize, maxSize]`),
 * each with entries drawn from [arbF]. Useful for binary vector laws such as the dot product.
 */
fun <F : Any> arbDenseVecPair(
    arbF: Arb<F>,
    minSize: Int,
    maxSize: Int,
): Arb<Pair<DenseVec<F>, DenseVec<F>>> {
    require(minSize >= 0) { "minSize must be nonnegative, got $minSize" }
    require(maxSize >= minSize) { "maxSize must be >= minSize, got minSize=$minSize, maxSize=$maxSize" }
    return Arb.int(minSize..maxSize).flatMap { n ->
        arbDenseVec(arbF, n).flatMap { x ->
            arbDenseVec(arbF, n).map { y -> x to y }
        }
    }
}
