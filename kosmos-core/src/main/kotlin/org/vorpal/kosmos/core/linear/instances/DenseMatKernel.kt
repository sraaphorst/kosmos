package org.vorpal.kosmos.core.linear.instances

import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.linear.values.DenseMat
import org.vorpal.kosmos.core.linear.values.DenseVec
import org.vorpal.kosmos.core.linear.values.MatLike
import org.vorpal.kosmos.core.linear.values.VecLike

/**
 * This comprises private code shared by:
 * - [DenseMatGroups]
 * - [DenseMatAlgebra]
 * - [DenseMatSemiring]
 * and also isolates the non-FP code to be contained in this file. We opt for standard
 * looping instead of using ranges since then no lambdas or [IntRange]s get created: using
 * a functional programming approach instead typically results in runtimes between 2 - 10x
 * longer that the loop-nased approach.
 */
internal object DenseMatKernel {
    fun <A : Any> checkSize(
        x: DenseMat<A>,
        rows: Int,
        cols: Int
    ) {
        require(x.rows == rows && x.cols == cols) {
            "Expected matrix of shape ${rows}${Symbols.TIMES}${cols}, got: ${x.rows}${Symbols.TIMES}${x.cols}"
        }
    }

    /**
     * Create a matrix of constant entries.
     * Used for the identity of the additive group of matrices.
     */
    fun <A : Any> constMat(
        a: A,
        rows: Int,
        cols: Int
    ): DenseMat<A> =
        DenseMat.tabulate(rows, cols) { _, _ -> a }

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
        mat2: MatLike<A>,
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
}
