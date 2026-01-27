package org.vorpal.kosmos.linear.values

/**
 * An interface to an object that behaves like a matrix, i.e.:
 * - it has a size measured in [rows] and [cols]
 * - it has an accessor to get an entry in a given row and col
 */
interface MatLike<out A : Any> {
    val rows: Int
    val cols: Int
    operator fun get(r: Int, c: Int): A
}
