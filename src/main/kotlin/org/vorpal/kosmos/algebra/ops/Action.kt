package org.vorpal.kosmos.algebra.ops

fun interface Action<S, V> {
    fun apply(s: S, v: V): V
}
