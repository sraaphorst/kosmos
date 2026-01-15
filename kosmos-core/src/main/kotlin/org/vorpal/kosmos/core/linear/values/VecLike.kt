package org.vorpal.kosmos.core.linear.values

interface VecLike<out A : Any> {
    val size: Int
    operator fun get(i: Int): A
}
