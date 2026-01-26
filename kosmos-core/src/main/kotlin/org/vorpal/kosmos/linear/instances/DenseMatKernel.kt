package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.MatLike
import org.vorpal.kosmos.linear.values.VecLike
import kotlin.math.min

/**
 * Note that this file contains functions that accept [MatLike] objects but return results as [DenseMat]: hence,
 * we keep them in [DenseMatKernel].
 */

/**
 * This comprises private code shared by the classes in [DenseMatAlgebras] and DenseMatOps.
 *
 * It also isolates the non-FP code to be contained in this file. We opt for standard
 * looping instead of using ranges since then no lambdas or [IntRange]s get created: using
 * a functional programming approach instead typically results in runtimes between 2 - 10x
 * longer that the loop-based approach.
 */
internal object DenseMatKernel {
    fun <A : Any> checkSize(
        mat: MatLike<A>,
        rows: Int,
        cols: Int
    ) {
        require(mat.rows == rows && mat.cols == cols) {
            "Expected matrix of shape ${rows}${Symbols.TIMES}${cols}, got: ${mat.rows}${Symbols.TIMES}${mat.cols}"
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
     * Check to make sure a matrix is nonempty and consists of constant entries.
     * If [zero] is defined, ensure that the entries are not zero.
     *
     * Return the element comprising the matrix elements.
     */
    fun <A : Any> checkConstNonemptyMat(
        mat: MatLike<A>,
        zero: A? = null
    ): A {
        require(mat.rows != 0 && mat.cols != 0) { "Matrix cannot be empty." }
        val elem = mat[0, 0]
        require(elem != zero) { "Expected constant nonzero matrix, but got: $zero" }
        var r = 0
        while (r < mat.rows) {
            var c = 0
            while (c < mat.cols) {
                require(mat[r, c] == elem) {
                    "Expected constant ${mat.rows}${Symbols.TIMES}${mat.cols} matrix of $elem, but found ${mat[r, c]}."}
                c += 1
            }
            r += 1
        }
        return elem
    }

    fun <A : Any> identity(
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
     * - A matrix [mat1] of shape `m×n` over [A]
     * - A matrix [mat2] of shape `m×n` over [A]
     * combine them together entrywise to get a matrix of shape `m×n` over [A].
     */
    fun <A : Any> entrywise(
        semigroup: Semigroup<A>,
        mat1: MatLike<A>,
        mat2: MatLike<A>
    ): DenseMat<A> {
        val rows = mat1.rows
        val cols = mat1.cols
        checkSize(mat2, rows, cols)
        val out = arrayOfNulls<Any?>(rows * cols)

        var r = 0
        while (r < rows) {
            var c = 0
            while (c < cols) {
                out[r * cols + c] = semigroup(mat1[r, c], mat2[r, c])
                c += 1
            }
            r += 1
        }

        return DenseMat.fromArrayUnsafe(rows, cols, out)
    }


    fun <A : Any> matMul(
        semiring: Semiring<A>,
        mat1: MatLike<A>,
        mat2: MatLike<A>
    ): DenseMat<A> {
        val rows = mat1.rows
        val cols = mat1.cols
        DenseKernel.checkNonnegative(rows, cols)
        DenseKernel.requireSize(mat2.rows, cols)
        val p = mat2.cols

        val out = arrayOfNulls<Any?>(rows * p)

        var r = 0
        while (r < rows) {
            var j = 0
            while (j < p) {
                var acc = semiring.add.identity

                var k = 0
                while (k < cols) {
                    val term = semiring.mul(mat1[r, k], mat2[k, j])
                    acc = semiring.add(acc, term)
                    k += 1
                }

                out[r * p + j] = acc
                j += 1
            }
            r += 1
        }

        return DenseMat.fromArrayUnsafe(rows, p, out)
    }


    fun <A : Any> matVec(
        semiring: Semiring<A>,
        mat: MatLike<A>,
        vec: VecLike<A>
    ): DenseVec<A> {
        val rows = mat.rows
        val cols = mat.cols
        DenseKernel.requireSize(vec.size, cols)

        val out = arrayOfNulls<Any?>(rows)

        var r = 0
        while (r < rows) {
            var acc = semiring.add.identity

            var c = 0
            while (c < cols) {
                val term = semiring.mul(mat[r, c], vec[c])
                acc = semiring.add(acc, term)
                c += 1
            }
            out[r] = acc
            r += 1
        }

        return DenseVec.fromArrayUnsafe(out)
    }


    fun <A : Any> isHadamardUnit(
        field: Field<A>,
        mat: MatLike<A>,
    ): Boolean {
        var r = 0
        while (r < mat.rows) {
            var c = 0
            while (c < mat.cols) {
                if (mat[r, c] == field.zero) return false
                c += 1
            }
            r += 1
        }
        return true
    }


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
     * Negate a matrix over [A] by negating each entry using the additive [AbelianGroup] over [A].
     */
    fun <A : Any> negateEntries(
        group: AbelianGroup<A>,
        mat: MatLike<A>
    ): DenseMat<A> {
        val rows = mat.rows
        val cols = mat.cols
        val out = arrayOfNulls<Any?>(rows * cols)

        var r = 0
        while (r < rows) {
            var c = 0
            while (c < cols) {
                out[r * cols + c] = group.inverse(mat[r, c])
                c += 1
            }
            r += 1
        }

        return DenseMat.fromArrayUnsafe(rows, cols, out)
    }


    /**
     * Given a matrix, flatten it from a `r×c` matrix to a vector of length `rc` along the rows.
     */
    fun <A : Any> flattenRowMajor(
        mat: MatLike<A>
    ): DenseVec<A> {
        val rows = mat.rows
        val cols = mat.cols
        val out = arrayOfNulls<Any?>(rows * cols)

        var r = 0
        while (r < rows) {
            var c = 0
            while (c < cols) {
                out[r * cols + c] = mat[r, c]
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


    fun <R : Any> pointInflation(
        mat: DenseMat<R>,
        blockSize: Int,
    ): DenseMat<R> {
        require(blockSize > 0) { "Block size must be positive, but got: $blockSize" }

        // Each entry gets replicated into a blockSize × blockSize block.
        return DenseMat.tabulate(
            mat.rows * blockSize,
            mat.cols * blockSize
        ) { i, j ->
            mat[i / blockSize, j / blockSize]
        }
    }

    fun <A : Any> trace(
        semiring: Semiring<A>,
        mat: MatLike<A>,
    ): A {
        require(isSquare(mat)) {
            "Cannot calculate trace for non-square mat ${mat.rows}${Symbols.TIMES}${mat.cols}."
        }

        val n = mat.rows
        var t = semiring.add.identity
        var i = 0
        while (i < n) {
            t = semiring.add(t, mat[i, i])
            i += 1
        }
        return t
    }

    fun <A : Any> traceRect(
        semiring: Semiring<A>,
        mat: MatLike<A>,
    ): A {
        val n = min(mat.rows, mat.cols)
        var t = semiring.add.identity
        var i = 0
        while (i < n) {
            t = semiring.add(t, mat[i, i])
            i += 1
        }
        return t
    }

    fun <A : Any> generateTranspose(
        mat: MatLike<A>
    ): DenseMat<A> {
        val outRows = mat.cols
        val outCols = mat.rows
        val out = arrayOfNulls<Any?>(outRows * outCols)

        var r = 0
        while (r < mat.rows) {
            var c = 0
            while (c < mat.cols) {
                out[c * outCols + r] = mat[r, c]
                c += 1
            }
            r += 1
        }
        return DenseMat.fromArrayUnsafe(outRows, outCols, out)
    }

    fun <A: Any> rowSums(
        semiring: Semiring<A>,
        mat: MatLike<A>
    ): DenseVec<A> {
        val rows = mat.rows
        val cols = mat.cols
        val out = arrayOfNulls<Any?>(rows)

        var r = 0
        while (r < rows) {
            var rowSum = semiring.add.identity

            var c = 0
            while (c < cols) {
                rowSum = semiring.add(rowSum, mat[r, c])
                c += 1
            }
            out[r] = rowSum
            r += 1
        }
        return DenseVec.fromArrayUnsafe(out)
    }

    fun <A : Any> colSums(
        semiring: Semiring<A>,
        mat: MatLike<A>
    ): DenseVec<A> {
        val rows = mat.rows
        val cols = mat.cols
        val out = arrayOfNulls<Any?>(cols)

        var c = 0
        while (c < cols) {
            var colSum = semiring.add.identity

            var r = 0
            while (r < rows) {
                colSum = semiring.add(colSum, mat[r, c])
                r += 1
            }
            out[c] = colSum
            c += 1
        }
        return DenseVec.fromArrayUnsafe(out)
    }

    fun <A: Any> scale(
        semiring: Semiring<A>,
        s: A,
        mat: MatLike<A>
    ): DenseMat<A> {
        val rows = mat.rows
        val cols = mat.cols
        val out = arrayOfNulls<Any?>(rows * cols)

        var r = 0
        while (r < rows) {
            var c = 0
            while (c < cols) {
                out[r * cols + c] = semiring.mul(s, mat[r, c])
                c += 1
            }
            r += 1
        }
        return DenseMat.fromArrayUnsafe(rows, cols, out)
    }

    fun isSquare(
        mat: MatLike<*>
    ): Boolean =
        mat.rows == mat.cols

    fun <A : Any> isDiagonal(
        mat: MatLike<A>,
        zero: A,
        eq: Eq<A> = Eq.default()
    ): Boolean {
        if (!isSquare(mat)) {
            return false
        }

        val n = mat.rows
        var r = 0
        while (r < n) {
            var c = 0
            while (c < n) {
                if (r != c && !eq(mat[r, c], zero)) {
                    return false
                }
                c += 1
            }
            r += 1
        }
        return true
    }

    fun <A : Any> diagonal(
        mat: MatLike<A>,
    ): DenseVec<A> {
        require(isSquare(mat)) {
            "Matrix must be square to find diagonal, got: ${mat.rows}${Symbols.TIMES}${mat.cols}"
        }
        val n = mat.rows
        val out = arrayOfNulls<Any?>(n)
        var i = 0
        while (i < n) {
            out[i] = mat[i, i]
            i += 1
        }
        return DenseVec.fromArrayUnsafe(out)
    }

    fun <A : Any> power(
        semiring: Semiring<A>,
        mat: MatLike<A>,
        pow: Int
    ): DenseMat<A> {
        DenseKernel.checkNonnegative(pow, "pow")
        require(isSquare(mat)) {
            "Matrix must be square to exponentiate, got: ${mat.rows}${Symbols.TIMES}${mat.cols}"
        }
        val n = mat.rows
        var result = identity(semiring, n)
        var base = mat
        var exp = pow

        while (exp > 0) {
            if (exp and 1 == 1) {
                result = matMul(semiring, result, base)
            }
            base = matMul(semiring, base, base)
            exp = exp shr 1
        }

        return result
    }
}
