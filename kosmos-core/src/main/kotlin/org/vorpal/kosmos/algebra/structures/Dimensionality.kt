package org.vorpal.kosmos.algebra.structures

/**
 * Optional mixin for algebraic structures that have a well-defined finite dimension.
 *
 * This is *not* an algebraic law-bearing interface — it simply conveys
 * that the space is finite-dimensional, allowing for numerical algorithms,
 * coordinate-based representations, and visualization.
 */
interface Dimensionality {
    /**
     * The finite dimension of this structure.
     *
     * For example:
     * - A `Vec3R` has dimension 3
     * - A `FunctionSpace<F, X>` typically has no finite dimension (and thus won't implement this).
     */
    val dimension: Int
}
