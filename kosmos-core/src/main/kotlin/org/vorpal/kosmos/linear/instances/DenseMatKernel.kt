package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.MatLike
import org.vorpal.kosmos.linear.values.VecLike
import org.vorpal.kosmos.linear.views.transposeView

/**
 * This comprises private code shared by:
 * - [DenseMatGroups]
 * - [DenseMatAlgebra]
 * - [DenseMatSemiring]
 * and also isolates the non-FP code to be contained in this file. We opt for standard
 * looping instead of using ranges since then no lambdas or [IntRange]s get created: using
 * a functional programming approach instead typically results in runtimes between 2 - 10x
 * longer that the loop-based approach.
 */
internal object DenseMatKernel {
    fun <A : Any> checkSize(
        x: MatLike<A>,
        rows: Int,
        cols: Int
    ) {
        require(x.rows == rows && x.cols == cols) {
            "Expected matrix of shape ${rows}${Symbols.TIMES}${cols}, got: ${x.rows}${Symbols.TIMES}${x.cols}"
        }
    }

    /**
     * Create a matrix of constant entries.
     * Used for the identity of the additive group of matrices to create J, the matrix of all ones.
     */
    fun <A : Any> constMat(
        a: A,
        rows: Int,
        cols: Int
    ): DenseMat<A> =
        DenseMat.tabulate(rows, cols) { _, _ -> a }

    /**
     * Instead of a separate `BlockConstraintMatrix` class, we can blow up points using this [pointInflation] function.
     * This is what is needed for a GDD (group divisible design) construction.
     */
    fun <R: Any> pointInflation(
        baseMatrix: DenseMat<R>,
        blockSize: Int,
        one: R
    ): DenseMat<R> {
        // Each entry gets replicated into a blockSize × blockSize block
        return DenseMat.tabulate(
            baseMatrix.rows * blockSize,
            baseMatrix.cols * blockSize
        ) { i, j ->
            baseMatrix[i / blockSize, j / blockSize]
        }
    }

    /**
     * Check to make sure a matrix is nonempty and consists of constant entries.
     * If [zero] is defined, ensure that the entries are not zero.
     *
     * Return the element comprising the matrix elements.
     */
    fun <A : Any> checkConstNonemptyMat(x: DenseMat<A>, zero: A? = null): A {
        require(x.rows != 0 && x.cols != 0) { "Matrix cannot be empty." }
        val elem = x[0, 0]
        require(elem != zero) { "Expected constant nonzero matrix, but got: $zero" }
        var r = 0
        while (r < x.rows) {
            var c = 0
            while (c < x.cols) {
                require(x[r, c] == elem) {
                    "Expected constant ${x.rows}${Symbols.TIMES}${x.cols} matrix of $elem, but found ${x[r, c]}."}
                c += 1
            }
            r += 1
        }
        return elem
    }

    fun <A: Any> identity(
        zero: A,
        one: A,
        n: Int
    ): DenseMat<A> =
        DenseMat.tabulate(n, n) { r, c ->
            if (r == c) one else zero
        }

    fun <A : Any> identity(
        semiring: Semiring<A>,
        n: Int
    ): DenseMat<A> =
        identity(semiring.add.identity, semiring.mul.identity, n)

    /**
     * Given:
     * - A matrix of shape `m×n` over [A]
     * - A matrix of shape `n×p` over [A]
     * multiply them together to get a matrix of shape `m×p` over [A].
     *
     * While using var hurts terribly, apparently, the functional implementation (i.e. using forEach on a range)
     * creates many temporary objects and as a result, is typically 2-10 times slower than this version.
     * Thus, we swallow our pride and just allow these hot loops to happen in the name of performance.
     */
    fun <A : Any> matMul(
        semiring: Semiring<A>,
        mat1: MatLike<A>,
        mat2: MatLike<A>
    ): DenseMat<A> {
        val m = mat1.rows
        val n = mat1.cols
        DenseKernel.requireSize(mat2.rows, n)
        val p = mat2.cols

        val out = arrayOfNulls<Any?>(m * p)

        var r = 0
        while (r < m) {
            var j = 0
            while (j < p) {
                var acc = semiring.add.identity

                var k = 0
                while (k < n) {
                    val term = semiring.mul(mat1[r, k], mat2[k, j])
                    acc = semiring.add(acc, term)
                    k += 1
                }

                out[r * p + j] = acc
                j += 1
            }
            r += 1
        }

        return DenseMat.fromArrayUnsafe(m, p, out)
    }

    /**
     * Given a matrix of shape `m×n` and a vector of length `n` all over [A], multiply them together to
     * get a vector of length `m`.
     */
    fun <A : Any> matVec(semiring: Semiring<A>, mat: MatLike<A>, vec: VecLike<A>): DenseVec<A> {
        val m = mat.rows
        val n = mat.cols
        DenseKernel.requireSize(vec.size, n)

        val out = arrayOfNulls<Any?>(m)

        var r = 0
        while (r < m) {
            var acc = semiring.add.identity

            var c = 0
            while (c < n) {
                val term = semiring.mul(mat[r, c], vec[c])
                acc = semiring.add(acc, term)
                c += 1
            }
            out[r] = acc
            r += 1
        }

        return DenseVec.fromArrayUnsafe(out)
    }

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
    ): DenseMat<R> {
        require(mat1.rows == mat2.rows && mat1.cols == mat2.cols) {
            "Matrices must have same shape, got: ${mat1.rows}${Symbols.TIMES}${mat1.cols} and ${mat2.rows}$Symbols.TIMES}${mat2.cols}"
        }
        val m = mat1.rows
        val n = mat1.cols
        val out = arrayOfNulls<Any?>(m * n)

        var r = 0
        while (r < m) {
            var c = 0
            while (c < n) {
                out[r * n + c] = semiring.mul(mat1[r, c], mat2[r, c])
                c += 1
            }
            r += 1
        }
        return DenseMat.fromArrayUnsafe(m, n, out)
    }

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
    ): DenseMat<R> {
        val m = mat1.rows
        val n = mat1.cols
        val p = mat2.rows
        val q = mat2.cols

        val outRows = m * p
        val outCols = n * q
        val out = arrayOfNulls<Any?>(outRows * outCols)

        var i = 0
        while (i < m) {
            var j = 0
            while (j < n) {
                val aij = mat1[i, j]

                var r = 0
                while (r < p) {
                    var c = 0
                    while (c < q) {
                        val row = i * p + r
                        val col = j * q + c
                        val dest = row * outCols + col
                        out[dest] = semiring.mul(aij, mat2[r, c])
                        c += 1
                    }
                    r += 1
                }
                j += 1
            }
            i += 1
        }
        return DenseMat.fromArrayUnsafe(outRows, outCols, out)
    }

    /**
     * Given a matrix, flatten it from a `r×c` matrix to a vector of length `rc` along the rows.
     */
    fun <A : Any> flattenRowMajor(
        x: MatLike<A>
    ): DenseVec<A> {
        val rows = x.rows
        val cols = x.cols
        val out = arrayOfNulls<Any?>(rows * cols)

        var r = 0
        while (r < rows) {
            var c = 0
            while (c < cols) {
                out[r * cols + c] = x[r, c]
                c += 1
            }
            r += 1
        }

        return DenseVec.fromArrayUnsafe(out)
    }

    /**
     * Given a vector of length `rc`, unflatten it to a matrix of size `r×c`.
     */
    fun <A : Any> unflattenRowMajor(
        x: VecLike<A>,
        rows: Int,
        cols: Int
    ): DenseMat<A> {
        DenseKernel.requireSize(x.size, rows * cols)

        val out = arrayOfNulls<Any?>(rows * cols)
        var i = 0
        while (i < rows * cols) {
            out[i] = x[i]
            i += 1
        }

        return DenseMat.fromArrayUnsafe(rows, cols, out)
    }

    /**
     * Calculates the Gram matrix `G = M^T M`.
     *
     * This computes the inner products of the **columns** of [m].
     *
     * The resulting matrix is symmetric with dimensions `c×c` (where `c` is the number of columns in [m]).
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
        matMul(semiring, m.transposeView(), m)

    /**
     * Calculates the intersection matrix `B = M M^T`.
     *
     * This computes the inner products of the **rows** of [m].
     *
     * The resulting matrix is symmetric with dimensions $r \times r$ (where $r$ is the number of rows in [m]).
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
        matMul(semiring, m, m.transposeView())
}
