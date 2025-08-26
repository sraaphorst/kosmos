package org.vorpal.kosmos.algebra.ops

/**
 * The action of one type on another.
 * As an example, we can use it in the definition of a vector space:
 * We can have S = Q and V = Q^n for some n.
 * Then apply would perform scalar multiplication using elements of Q on elements of Q^n.
 */
fun interface Action<S, V> {
    fun apply(s: S, v: V): V
}
