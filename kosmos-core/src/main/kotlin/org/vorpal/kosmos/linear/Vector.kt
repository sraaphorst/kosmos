package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.Vec2RSpace

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

    companion object {
        internal const val FIELD_MATCH_ERROR = "Vector fields must match."
    }
}

fun <F: Any, V: Vector<F, V>> FiniteVectorSpace<F, V>.vector(vararg xs: F): Vector<F, *> = when (dimension) {
    0 -> Vec0(this.field)
    1 -> Vec1(xs[0], this.field)
    2 -> Vec2(xs[0], xs[1], this.field)
    3 -> Vec3(xs[0], xs[1], xs[2], this.field)
    4 -> Vec4(xs[0], xs[1], xs[2], xs[3], this.field)
    else -> {
        check(xs.size == this.dimension) { "Cannot create ${xs.size}-dim vector from ${dimension}-dim vector space." }
        NVec(xs.toList(), this.field)
    }
}

fun main() {
    val v = Vec2RSpace.vector(1.0, 2.0, 3.0)
    println(v)
}

//fun <F: Any, V: Vector<F, V>> FiniteVectorSpace<F, V>.vec(vararg xs: F): V = when {
//    dimension == 0 -> Vec0(this.field)
//    dimension == 1 -> Vec1(xs[0], this.field)
//    dimension == 2 -> Vec2(xs[0], xs[1], this.field)
//    dimension == 3 -> Vec3(xs[0], xs[1], xs[2], this.field)
//    dimension == 4 -> Vec4(xs[0], xs[1], xs[2], xs[3], this.field)
//    else           -> {
//        check(xs.size == this.dimension) { "Cannot create ${xs.size}-dim vector from ${dimension}-dim vector space." }
//        NVec(xs.toList(), this.field)
//    }
//}