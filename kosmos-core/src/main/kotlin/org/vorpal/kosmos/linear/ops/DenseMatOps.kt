package org.vorpal.kosmos.linear.ops

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.linear.instances.DenseMatKernel
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.MatLike
import org.vorpal.kosmos.linear.values.VecLike
import org.vorpal.kosmos.linear.views.transposeView

object DenseMatOps {
    /**
     * Given
     * - A [Semiring] over [A]
     * - [mat1] and [mat2], two matrices of the same size over [A]
     * Return a matrix that comprises the sum of [mat1] and [mat2] using the [semiring].
     */
    fun <A : Any> matAdd(
        semiring: Semiring<A>,
        mat1: MatLike<A>,
        mat2: MatLike<A>
    ): DenseMat<A> = DenseMatKernel.entrywise(semiring.add, mat1, mat2)


    /**
     * Given:
     * - A [Semiring] over [A]
     * - A matrix of shape `m×n` over [A]
     * - A matrix of shape `n×p` over [A]
     * multiply them together to get a matrix of shape `m×p` over [A].
     */
    fun <A : Any> matMul(
        semiring: Semiring<A>,
        mat1: MatLike<A>,
        mat2: MatLike<A>
    ): DenseMat<A> = DenseMatKernel.matMul(semiring, mat1, mat2)


    /**
     * Given a matrix of shape `m×n` and a vector of length `n` all over [A], multiply them together to
     * get a vector of length `m`.
     */
    fun <A : Any> matVec(
        semiring: Semiring<A>,
        mat: MatLike<A>,
        vec: VecLike<A>
    ): DenseVec<A> = DenseMatKernel.matVec(semiring, mat, vec)

    /**
     * Given a [Semiring], create the `n×n` identity matrix.
     */
    fun <A : Any> identity(
        semiring: Semiring<A>,
        n: Int
    ): DenseMat<A> = DenseMatKernel.identity(semiring, n)

    /**
     * Given a value [a], create a constant `m×n` matrix of [a].
     */
    fun <A : Any> constMat(
        a: A,
        rows: Int,
        cols: Int
    ): DenseMat<A> = DenseMatKernel.constMat(a, rows, cols)

    /**
     * Given a [Semiring], create an ``m×n` matrix consisting of the additive monoid identity.
     */
    fun <A : Any> zero(
        semiring: Semiring<A>,
        rows: Int,
        cols: Int
    ): DenseMat<A> = constMat(semiring.add.identity, rows, cols)

    /**
     * Given a [Semiring], create an ``m×n` matrix consisting of the multiplicative monoid identity.
     */
    fun <A: Any> one(
        semiring: Semiring<A>,
        rows: Int,
        cols: Int
    ): DenseMat<A> = constMat(semiring.mul.identity, rows, cols)

    /**
     * Hadamard product: A ⊙ B
     *
     * Given two matrices of the same size `m×n`, calculate their Hadamard product, i.e, their pointwise product.
     *
     * This simply requires a multiplication: we take a [Semiring].
     */
    fun <R : Any> hadamard(
        semiring: Semiring<R>,
        mat1: MatLike<R>,
        mat2: MatLike<R>
    ): DenseMat<R> = DenseMatKernel.entrywise(semiring.mul, mat1, mat2)


    /**
     * Determine if a matrix [mat] is a unit under the Hadamard operation, i.e. has no zero entries.
     */
    fun <A : Any> isHadamardUnit(
        field: Field<A>,
        mat: MatLike<A>,
    ): Boolean = DenseMatKernel.isHadamardUnit(field, mat)

    /**
     * Kronecker product A⊗B: given two matrices
     * - `A` which is `m×n` over [R]
     * - `B` which is `p×q` over [R]
     * for a [Semiring] over [R], calculate an `(mp)×(nq)` matrix such that:
     * ```
     * A⊗B[ip + r, jq + c] = A[i,j] * B[r,c]
     * ```
     */
    fun <R : Any> kronecker(
        semiring: Semiring<R>,
        mat1: MatLike<R>,
        mat2: MatLike<R>
    ): DenseMat<R> = DenseMatKernel.kronecker(semiring, mat1, mat2)

    /**
     * Calculates the Gram matrix `G = M^T M`.
     *
     * This computes the inner products of the **columns** of [m].
     *
     * If the semiring’s multiplication is commutative, the result is symmetric.
     * The resulting matrix has dimensions `c×c` (where `c` is the number of columns in [m]).
     *
     * In the context of combinatorial design theory, if [m] is a point-block incidence matrix
     * (rows are points, columns are blocks), this calculates the **concurrence matrix** (or block intersection matrix).
     * - Entry `(i, j)` represents the size of the intersection between block `i` and block `j` (`|B_i ∩ B_j|`).
     * - Diagonal entries represent the block sizes (`k`).
     *
     * @param semiring The semiring definition for the multiplication and addition operations.
     * @param m The source matrix.
     * @return A `c×c` matrix where `G_ij = ⟨col_i, col_j⟩`.
     */
    fun <A : Any> gramMatrix(
        semiring: Semiring<A>,
        m: MatLike<A>
    ): DenseMat<A> =
        DenseMatKernel.matMul(semiring, m.transposeView(), m)

    /**
     * Calculates the intersection matrix `B = M M^T`.
     *
     * This computes the inner products of the **rows** of [m].
     *
     * If the semiring’s multiplication is commutative, the result is symmetric.
     * The resulting matrix has dimensions `r×r` (where $r$ is the number of rows in [m]).
     *
     * In the context of combinatorial design theory, if [m] is a point-block incidence matrix
     * (rows are points, columns are blocks), this calculates the point-connectivity.
     * - Entry `(i, j)` represents the number of blocks containing both point `i` and point `j` (`λ_ij`).
     * - Diagonal entries represent the replication numbers (`r`) for each point.
     *
     * @param semiring The semiring definition for the multiplication and addition operations.
     * @param m The source matrix.
     * @return An `r×r` matrix where `B_ij = ⟨row_i, row_j⟩`.
     */
    fun <A: Any> intersectionMatrix(
        semiring: Semiring<A>,
        m: MatLike<A>
    ): DenseMat<A> =
        DenseMatKernel.matMul(semiring, m, m.transposeView())


    /**
     * Instead of a separate `BlockConstraintMatrix` class, we can blow up points using this [pointInflation] function.
     * This is what is needed for a GDD (group divisible design) construction.
     */
    fun <R: Any> pointInflation(
        mat: DenseMat<R>,
        blockSize: Int,
    ): DenseMat<R> = DenseMatKernel.pointInflation(mat, blockSize)

    /**
     * Calculate the trace of the square `n×n` matrix [mat], i.e. the sum of the entries on the diagonal
     * using the given [Semiring].
     */
    fun <A : Any> trace(
        semiring: Semiring<A>,
        mat: MatLike<A>,
    ): A = DenseMatKernel.trace(semiring, mat)

    /**
     * Calculate the trace of a rectangular matrix `m×n` matrix [mat], i.e. the sum of the entries on the diagonal
     * using the given [Semiring]. This is the sum of the entries `mat[0, 0]` through to `mat[i, i]` where
     * `i = min(mat.rows, mat.cols) - 1`.
     */
    fun <A : Any> traceRect(
        semiring: Semiring<A>,
        mat: MatLike<A>
    ): A = DenseMatKernel.traceRect(semiring, mat)

    /**
     * Instead of generating a no-memory, no-allocating view of the transpose of a matrix via
     * `MatrixViews.TransposeMatView`, this creates a full-fledged transpose of [mat].
     */
    fun <A : Any> generateTranspose(
        mat: MatLike<A>
    ): DenseMat<A> = DenseMatKernel.generateTranspose(mat)

    /**
     * Given an `m×n` matrix [mat], calculate the sum of the rows to form a vector of size `m` using the [Semiring].
     */
    fun <A: Any> rowSums(
        semiring: Semiring<A>,
        mat: MatLike<A>
    ): DenseVec<A> = DenseMatKernel.rowSums(semiring, mat)

    /**
     * Given an `m×n` matrix [mat], calculate the sum of the columns to form a vector of size `n` using the [Semiring].
     */
    fun <A: Any> colSums(
        semiring: Semiring<A>,
        mat: MatLike<A>
    ): DenseVec<A> = DenseMatKernel.colSums(semiring, mat)

    /**
     * Given a square `n×n` matrix [mat], return true iff the matrix is diagonal, i.e. every entry off the main
     * diagonal is [zero]. Comparisons are done with [eq].
     */
    fun <A : Any> isDiagonal(
        mat: MatLike<A>,
        zero: A,
        eq: Eq<A> = Eq.default()
    ): Boolean = DenseMatKernel.isDiagonal(mat, zero, eq)

    /**
     * Given a square `n×n` matrix [mat], return the entries on the main diagonal as a vector of size `n`.
     */
    fun <A : Any> diagonal(
        mat: MatLike<A>,
    ): DenseVec<A> = DenseMatKernel.diagonal(mat)

    /**
     * Given a [zero] element, a size [n], and a function [f], create a square `n×n` matrix with:
     * - `f(i)` in position `mat[i, i]`
     * - `zero` in all other positions.
     */
    fun <A: Any> toDiagonal(
        zero : A,
        n : Int,
        f: (Int) -> A
    ): DenseMat<A> = DenseMat.tabulate(n, n) { r, c -> if (r == c) f(r) else zero }

    /**
     * Given a [zero] element and a vector [vec] of length `n`, create a square `n×n` matrix with:
     * - `vec[i]` in position `mat[i, i]`
     * - `zero` in all other positions.
     */
    fun <A : Any> toDiagonal(
        zero : A,
        vec: VecLike<A>
    ): DenseMat<A> = DenseMat.tabulate(vec.size, vec.size) { r, c -> if (r == c) vec[r] else zero }

    /**
     * Given a square `n×n` matrix [mat], calculate its [pow] power using the given [Semiring] through repeated
     * squaring.
     */
    fun <A : Any> power(
       semiring: Semiring<A>,
       mat: MatLike<A>,
       pow: Int
    ): DenseMat<A> = DenseMatKernel.power(semiring, mat, pow)
}
