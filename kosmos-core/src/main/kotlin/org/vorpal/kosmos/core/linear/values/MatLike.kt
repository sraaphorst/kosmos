package org.vorpal.kosmos.core.linear.values

interface MatLike<out A : Any> {
    val rows: Int
    val cols: Int
    operator fun get(r: Int, c: Int): A
}
