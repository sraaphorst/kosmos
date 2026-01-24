package org.vorpal.kosmos.linear.values

import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.linear.instances.DenseMatKernel

object DenseMatOps {
    fun <A : Any> matMul(
        semiring: Semiring<A>,
        mat1: MatLike<A>,
        mat2: MatLike<A>
    ): DenseMat<A> = DenseMatKernel.matMul(semiring, mat1, mat2)

    fun <A : Any> matVec(
        semiring: Semiring<A>,
        mat: MatLike<A>,
        vec: VecLike<A>
    ): DenseVec<A> = DenseMatKernel.matVec(semiring, mat, vec)

    fun <R : Any> hadamard(
        semiring: Semiring<R>,
        mat1: MatLike<R>,
        mat2: MatLike<R>
    ): DenseMat<R> = DenseMatKernel.hadamard(semiring, mat1, mat2)

    fun <R : Any> kronecker(
        semiring: Semiring<R>,
        mat1: MatLike<R>,
        mat2: MatLike<R>
    ): DenseMat<R> = DenseMatKernel.kronecker(semiring, mat1, mat2)

    
}