package org.vorpal.kosmos.linear.values

data class Vec0<F : Any>(
    val unit: Unit = Unit
): VecLike<F> {
    override val size: Int = 0
    override fun get(i: Int): F =
        throw IndexOutOfBoundsException("Invalid index for Vec0: $i")
}

data class Vec1<F: Any>(
    val x: F
): VecLike<F> {
    override val size: Int = 1
    override fun get(i: Int): F = when (i) {
        0 -> x
        else -> throw IndexOutOfBoundsException("Invalid index for Vec1: $i")
    }
}

data class Vec2<F : Any>(
    val x: F,
    val y: F
): VecLike<F> {
    override val size: Int = 2
    override fun get(i: Int): F = when (i) {
        0 -> x
        1 -> y
        else -> throw IndexOutOfBoundsException("Invalid index for Vec2: $i")
    }
}

data class Vec3<F: Any>(
    val x: F,
    val y: F,
    val z: F
): VecLike<F> {
    override val size: Int = 3
    override fun get(i: Int): F = when (i) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IndexOutOfBoundsException("Invalid index for Vec3: $i")
    }
}

data class Vec4<F : Any>(
    val x: F,
    val y: F,
    val z: F,
    val w: F
): VecLike<F> {
    override val size: Int = 4
    override fun get(i: Int): F = when (i) {
        0 -> x
        1 -> y
        2 -> z
        3 -> w
        else -> throw IndexOutOfBoundsException("Invalid index for Vec4: $i")
    }
}
