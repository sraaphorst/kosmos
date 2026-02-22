package org.vorpal.kosmos.combinatorics.blockdesign

/**
 * A finite projective plane is always a SteinerSystem (where `q` is a prime power) with parameters:
 * - `q^2 + q + 1` points
 * - `q + 1` points per block / line
 *
 * So it is a SteinerSystem with parameters S(`2`, `q + 1`, `q^2 + q + 1`).
 *
 * This makes it also a BlockDesign with parameters `2`-(`q^2 + q + 1`, `q + 1`, `1`).
 *
 * It is related to a FiniteAffinePlane by removing one line and its corresponding points,
 * which is an S(`2`, `q`, `q^2`).
 */
interface FiniteProjectivePlane<T : Any> : SteinerSystem<T> {
    override val t: Int
        get() = 2

    /** Note: order must be a prime power. */
    val order: Int

    override val k: Int
        get() = order + 1
    override val v: Int
        get() = order * order + order + 1
}