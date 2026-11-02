package org.vorpal.kosmos.linear.ops

import org.vorpal.kosmos.algebra.structures.CommutativeRng
import org.vorpal.kosmos.linear.values.Mat2

object FixedMatOps {
    fun <R : Any> det(
        rng: CommutativeRng<R>,
        mat2: Mat2<R>
    ): R =
        rng.run {
            add(mul(mat2.a, mat2.d), add.inverse(mul(mat2.b, mat2.c)))
        }

    fun <R : Any> trace(
        rng: CommutativeRng<R>,
        mat2: Mat2<R>
    ): R =
        rng.add(mat2.a, mat2.d)

    fun <R : Any> adjugate(
        rng: CommutativeRng<R>,
        mat2: Mat2<R>
    ): Mat2<R> =
        mat2.run {
            Mat2(d, rng.add.inverse(b), rng.add.inverse(c), a)
        }
}