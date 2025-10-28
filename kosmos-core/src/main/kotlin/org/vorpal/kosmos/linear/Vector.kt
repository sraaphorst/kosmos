package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.Field

/**
 * Represents an abstract vector element with scalars from F.
 *
 * @param F the scalar type, typically from a [Field]
 * @param V the vector type, which must itself implement [Vector]
 */
interface Vector<F : Any, V : Vector<F, V>> {
    operator fun plus(other: V): V
    operator fun minus(other: V): V
    operator fun times(scalar: F): V
    val field: Field<F>
}
