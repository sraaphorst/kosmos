package org.vorpal.kosmos.linear.values

/**
 * A 2x2 matrix over a commutative ring with unit.
 */
data class Mat2<A : Any>(
    val a: A, val b: A,
    val c: A, val d: A
): MatLike<A> {
    override val rows: Int = 2
    override val cols: Int = 2

    override fun get(r: Int, c: Int): A = when (r to c) {
        0 to 0 -> a
        0 to 1 -> b
        1 to 0 -> this@Mat2.c
        1 to 1 -> d
        else -> throw IndexOutOfBoundsException("M_2 has no entry: $r,$c")
    }
}