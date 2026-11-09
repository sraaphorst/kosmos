package org.vorpal.kosmos.linear.values

// The fixed-size vectors Vec0 through Vec4.
//
// These are the fixed-size counterparts of DenseVec: the length is part of the type, so
// operations on them need no dimension checks, and the coordinates can be destructured,
// `val (x, y, z) = v`. Like the other carriers in this package they hold no algebraic structure:
// operations that make sense in every dimension (dot products, sums, scaling) are in DenseVecOps,
// which accepts any VecLike; operations that only exist in one dimension (the cross product on R³)
// are in FixedVecOps; and the structures (additive groups, modules, vector spaces) are in
// FixedVecAlgebras.

/**
 * The unique vector of length 0. It has no coordinates; [unit] only gives the data class a field.
 */
data class Vec0<A : Any>(
    val unit: Unit = Unit
) : VecLike<A> {
    override val size: Int = 0

    /**
     * Always throws: a vector of length 0 has no entries.
     *
     * @throws IndexOutOfBoundsException for every [i]
     */
    override fun get(i: Int): A =
        throw IndexOutOfBoundsException("Invalid index for Vec0: $i")
}

/**
 * A vector of length 1 with coordinate [x].
 */
data class Vec1<A : Any>(
    val x: A
) : VecLike<A> {
    override val size: Int = 1

    /**
     * The coordinate at index [i]: `0 ↦ x`.
     *
     * @throws IndexOutOfBoundsException if [i] is not 0
     */
    override fun get(i: Int): A = when (i) {
        0 -> x
        else -> throw IndexOutOfBoundsException("Invalid index for Vec1: $i")
    }
}

/**
 * A vector of length 2 with coordinates [x] and [y].
 *
 * Whether it is read as a row or a column depends on the operation: see
 * [org.vorpal.kosmos.linear.ops.FixedMatOps.vecMat] (row vector times matrix) and
 * [org.vorpal.kosmos.linear.ops.FixedMatOps.matVec] (matrix times column vector).
 */
data class Vec2<A : Any>(
    val x: A,
    val y: A
) : VecLike<A> {
    override val size: Int = 2

    /**
     * The coordinate at index [i]: `0 ↦ x`, `1 ↦ y`.
     *
     * @throws IndexOutOfBoundsException if [i] is not 0 or 1
     */
    override fun get(i: Int): A = when (i) {
        0 -> x
        1 -> y
        else -> throw IndexOutOfBoundsException("Invalid index for Vec2: $i")
    }
}

/**
 * A vector of length 3 with coordinates [x], [y] and [z].
 */
data class Vec3<A : Any>(
    val x: A,
    val y: A,
    val z: A
) : VecLike<A> {
    override val size: Int = 3

    /**
     * The coordinate at index [i]: `0 ↦ x`, `1 ↦ y`, `2 ↦ z`.
     *
     * @throws IndexOutOfBoundsException if [i] is not 0, 1 or 2
     */
    override fun get(i: Int): A = when (i) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IndexOutOfBoundsException("Invalid index for Vec3: $i")
    }
}

/**
 * A vector of length 4 with coordinates [x], [y], [z] and [w].
 */
data class Vec4<A : Any>(
    val x: A,
    val y: A,
    val z: A,
    val w: A
) : VecLike<A> {
    override val size: Int = 4

    /**
     * The coordinate at index [i]: `0 ↦ x`, `1 ↦ y`, `2 ↦ z`, `3 ↦ w`.
     *
     * @throws IndexOutOfBoundsException if [i] is not 0, 1, 2 or 3
     */
    override fun get(i: Int): A = when (i) {
        0 -> x
        1 -> y
        2 -> z
        3 -> w
        else -> throw IndexOutOfBoundsException("Invalid index for Vec4: $i")
    }
}
