package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.getOrElse
import org.vorpal.kosmos.linear.ops.MatOp
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.MatLike
import org.vorpal.kosmos.linear.values.RowEchelonForm
import org.vorpal.kosmos.linear.values.VecLike
import org.vorpal.kosmos.linear.views.opView
import org.vorpal.kosmos.linear.views.transposeView
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
    /**
     * Allocator for a new matrix that ensures that (rows * cols) does not overflow an Int.
     *
     * If it overflows an int, [ArithmeticException] is thrown.
     */
    fun allocateMatrix(rows: Int, cols: Int): Array<Any?> {
        require(rows >= 0) { "rows must be nonnegative: $rows" }
        require(cols >= 0) { "cols must be nonnegative: $cols" }
        return arrayOfNulls(Math.multiplyExact(rows, cols))
    }

    /**
     * Calls required to make sure the size of `mat` is `rows×cols`.
     *
     * Throws an [IllegalArgumentException] if the check fails.
     */
    fun requireSize(
        mat: MatLike<*>,
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

        for (r in 0 until mat.rows) {
            for (c in 0 until mat.cols) {
                require(mat[r, c] == elem) {
                    "Expected constant ${mat.rows}${Symbols.TIMES}${mat.cols} matrix of $elem, but found ${mat[r, c]}."
                }
            }
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
        requireSize(mat2, rows, cols)
        val out = allocateMatrix(rows, cols)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[r * cols + c] = semigroup(mat1[r, c], mat2[r, c])
            }
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

        val out = allocateMatrix(rows, p)

        for (r in 0 until rows) {
            for (j in 0 until p) {
                var acc = semiring.add.identity
                for (k in 0 until cols) {
                    val term = semiring.mul(mat1[r, k], mat2[k, j])
                    acc = semiring.add(acc, term)
                }
                out[r * p + j] = acc
            }
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

        for (r in 0 until rows) {
            var acc = semiring.add.identity
            for (c in 0 until cols) {
                val term = semiring.mul(mat[r, c], vec[c])
                acc = semiring.add(acc, term)
            }
            out[r] = acc
        }

        return DenseVec.fromArrayUnsafe(out)
    }

    fun <A : Any> isHadamardUnit(
        field: Field<A>,
        mat: MatLike<A>,
    ): Boolean {
        for (r in 0 until mat.rows) {
            for (c in 0 until mat.cols) {
                if (mat[r, c] == field.zero) return false
            }
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

        val outRows = Math.multiplyExact(m, p)
        val outCols = Math.multiplyExact(n, q)
        val out = allocateMatrix(outRows, outCols)

        for (i in 0 until m) {
            for (j in 0 until n) {
                val aij = mat1[i, j]
                for (r in 0 until p) {
                    for (c in 0 until q) {
                        val row = i * p + r
                        val col = j * q + c
                        val dest = row * outCols + col
                        out[dest] = semiring.mul(aij, mat2[r, c])
                    }
                }
            }
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
        val out = allocateMatrix(rows, cols)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[r * cols + c] = group.inverse(mat[r, c])
            }
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
        val out = allocateMatrix(rows, cols)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[r * cols + c] = mat[r, c]
            }
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
        val size = Math.multiplyExact(rows, cols)
        DenseKernel.requireSize(x.size, size)
        val out = allocateMatrix(rows, cols)

        for (i in 0 until size) {
            out[i] = x[i]
        }

        return DenseMat.fromArrayUnsafe(rows, cols, out)
    }

    fun <A : Any> pointInflation(
        mat: DenseMat<A>,
        blockSize: Int,
    ): DenseMat<A> {
        require(blockSize > 0) { "Block size must be positive, but got: $blockSize" }

        // Each entry gets replicated into a blockSize × blockSize block.
        val outRows = Math.multiplyExact(mat.rows, blockSize)
        val outCols = Math.multiplyExact(mat.cols, blockSize)
        return DenseMat.tabulate(outRows, outCols) { i, j ->
            mat[i / blockSize, j / blockSize]
        }
    }

    fun <A : Any> argmin(
        mat: MatLike<A>,
        order: TotalOrder<A>
    ): Option<Pair<Int, Int>> {
        if (mat.rows == 0 || mat.cols == 0) return Option.None
        var minRow = 0
        var minCol = 0

        for (i in 0 until mat.rows) {
            for (j in 0 until mat.cols) {
                if (order.lt(mat[i, j], mat[minRow, minCol])) {
                    minRow = i
                    minCol = j
                }
            }
        }
        return Option.Some(minRow to minCol)
    }

    fun <A : Any> argmax(
        mat: MatLike<A>,
        order: TotalOrder<A>
    ): Option<Pair<Int, Int>> {
        if (mat.rows == 0 || mat.cols == 0) return Option.None
        var maxRow = 0
        var maxCol = 0

        for (i in 0 until mat.rows) {
            for (j in 0 until mat.cols) {
                if (order.gt(mat[i, j], mat[maxRow, maxCol])) {
                    maxRow = i
                    maxCol = j
                }
            }
        }
        return Option.Some(maxRow to maxCol)
    }

    fun <A : Any, B : Any> argminBy(
        mat: MatLike<A>,
        f: (A) -> B,
        order: TotalOrder<B>
    ): Option<Pair<Int, Int>> {
        if (mat.rows == 0 || mat.cols == 0) return Option.None

        var minRow = 0
        var minCol = 0
        var minVal = f(mat[0, 0])

        for (i in 0 until mat.rows) {
            for (j in 0 until mat.cols) {
                val vij = f(mat[i, j])
                if (order.lt(vij, minVal)) {
                    minRow = i
                    minCol = j
                    minVal = vij
                }
            }
        }
        return Option.Some(minRow to minCol)
    }

    fun <A : Any, B : Any> argmaxBy(
        mat: MatLike<A>,
        f: (A) -> B,
        order: TotalOrder<B>
    ): Option<Pair<Int, Int>> {
        if (mat.rows == 0 || mat.cols == 0) return Option.None

        var maxRow = 0
        var maxCol = 0
        var maxVal = f(mat[0, 0])

        for (i in 0 until mat.rows) {
            for (j in 0 until mat.cols) {
                val vij = f(mat[i, j])
                if (order.gt(vij, maxVal)) {
                    maxRow = i
                    maxCol = j
                    maxVal = vij
                }
            }
        }
        return Option.Some(maxRow to maxCol)
    }

    fun <A : Any> isAll(
        mat: MatLike<A>,
        a: A,
        eq: Eq<A> = Eq.default()
    ): Boolean {
        for (i in 0 until mat.rows) {
            for (j in 0 until mat.cols) {
                if (!eq(mat[i, j], a)) return false
            }
        }
        return true
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
        for (i in 0 until n) {
            t = semiring.add(t, mat[i, i])
        }
        return t
    }

    fun <A : Any> traceRect(
        semiring: Semiring<A>,
        mat: MatLike<A>,
    ): A {
        val n = min(mat.rows, mat.cols)
        var t = semiring.add.identity
        for (i in 0 until n) {
            t = semiring.add(t, mat[i, i])
        }
        return t
    }

    fun <A : Any> generateTranspose(
        mat: MatLike<A>
    ): DenseMat<A> {
        val outRows = mat.cols
        val outCols = mat.rows
        val out = allocateMatrix(outRows, outCols)

        for (r in 0 until mat.rows) {
            for (c in 0 until mat.cols) {
                out[c * outCols + r] = mat[r, c]
            }
        }
        return DenseMat.fromArrayUnsafe(outRows, outCols, out)
    }

    fun <A : Any> rowSums(
        semiring: Semiring<A>,
        mat: MatLike<A>
    ): DenseVec<A> {
        val rows = mat.rows
        val cols = mat.cols
        val out = arrayOfNulls<Any?>(rows)

        for (r in 0 until rows) {
            var rowSum = semiring.add.identity
            for (c in 0 until cols) {
                rowSum = semiring.add(rowSum, mat[r, c])
            }
            out[r] = rowSum
        }
        return DenseVec.fromArrayUnsafe(out)
    }

    fun <A : Any> colSums(
        semiring: Semiring<A>,
        mat: MatLike<A>
    ): DenseVec<A> = rowSums(semiring, mat.transposeView())

    fun <A : Any> scale(
        semiring: Semiring<A>,
        s: A,
        mat: MatLike<A>
    ): DenseMat<A> {
        val rows = mat.rows
        val cols = mat.cols
        val out = allocateMatrix(rows, cols)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[r * cols + c] = semiring.mul(s, mat[r, c])
            }
        }
        return DenseMat.fromArrayUnsafe(rows, cols, out)
    }

    fun <A : Any, B : Any> rowReduce(
        add: CommutativeMonoid<B>,
        mat: MatLike<A>,
        f: (A) -> B
    ): DenseVec<B> {
        val out = arrayOfNulls<Any?>(mat.rows)
        for (r in 0 until mat.rows) {
            var acc = add.identity
            for (c in 0 until mat.cols) {
                acc = add(acc, f(mat[r, c]))
            }
            out[r] = acc
        }
        return DenseVec.fromArrayUnsafe(out)
    }

    fun <A : Any, B : Any> colReduce(
        add: CommutativeMonoid<B>,
        mat: MatLike<A>,
        f: (A) -> B
    ): DenseVec<B> = rowReduce(add, mat.transposeView(), f)

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
        for (r in 0 until n) {
            for (c in 0 until n) {
                if (r != c && !eq(mat[r, c], zero)) {
                    return false
                }
            }
        }
        return true
    }

    fun <A : Any> extractDiagonal(
        mat: MatLike<A>,
    ): DenseVec<A> {
        require(isSquare(mat)) {
            "Matrix must be square to find diagonal, got: ${mat.rows}${Symbols.TIMES}${mat.cols}"
        }
        val n = mat.rows
        val out = arrayOfNulls<Any?>(n)
        for (i in 0 until n) {
            out[i] = mat[i, i]
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

        // Make a copy of mat so that we can make sure we are not working with a view and can use fast path assumptions.
        var base = copy(mat)
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

    /**
     * Permutation matrix check:
     * - square
     * - binary entries {0,1}
     * - exactly one 1 in each row and each column
     */
    fun <A : Any> isPermutationMatrix(
        mat: MatLike<A>,
        zero: A,
        one: A,
        eq: Eq<A> = Eq.default()
    ): Boolean {
        require(!eq(zero, one)) { "isPermutationMatrix requires zero != one" }
        fun isBinary(a: A): Boolean =
            eq(a, zero) || eq(a, one)

        fun checkRowsAllOne(currMat: MatLike<A>): Boolean {
            for (r in 0 until currMat.rows) {
                var ones = 0
                for (c in 0 until currMat.cols) {
                    val entry = currMat[r, c]
                    if (!isBinary(entry)) return false
                    if (eq(entry, one)) {
                        ones += 1
                        if (ones > 1) return false
                    }
                }
                if (ones != 1) return false
            }
            return true
        }

        if (mat.rows != mat.cols) return false
        if (!checkRowsAllOne(mat)) return false
        return checkRowsAllOne(mat.transposeView())
    }

    fun <A : Any> isPermutationMatrix(
        semiring: Semiring<A>,
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): Boolean {
        val zero = semiring.add.identity
        val one = semiring.mul.identity
        require(!eq(zero, one)) { "Permutation matrix check requires zero != one." }
        return isPermutationMatrix(mat, zero, one, eq)
    }

    fun <A: Any> affineMatVec(
        semiring: Semiring<A>,
        alpha: A,
        aOp: MatOp,
        aMat: MatLike<A>,
        xVec: VecLike<A>,
        beta: A,
        yVec: VecLike<A>
    ): DenseVec<A> {
        require(aOp != MatOp.ConjTrans) { "ConjTrans requires an involutive ring." }

        val aV = aMat.opView(aOp) // Normal or Trans
        val m = aV.rows
        val n = aV.cols

        DenseKernel.requireSize(xVec.size, n)
        DenseKernel.requireSize(yVec.size, m)

        // tmp = aV * x
        val tmp = matVec(semiring, aV, xVec)
        val out = arrayOfNulls<Any?>(m)
        for (i in 0 until m) {
            val left = semiring.mul(alpha, tmp[i])
            val right = semiring.mul(beta, yVec[i])
            out[i] = semiring.add(left, right)
        }
        return DenseVec.fromArrayUnsafe(out)
    }

    fun <A: Any> affineMatVec(
        ring: InvolutiveRing<A>,
        alpha: A,
        aOp: MatOp,
        aMat: MatLike<A>,
        xVec: VecLike<A>,
        beta: A,
        yVec: VecLike<A>
    ): DenseVec<A> {
        val aV = aMat.opView(aOp, ring.conj)
        val m = aV.rows
        val n = aV.cols

        DenseKernel.requireSize(xVec.size, n)
        DenseKernel.requireSize(yVec.size, m)

        // tmp = aV * x
        val tmp = matVec(ring, aV, xVec)
        val out = arrayOfNulls<Any?>(m)
        for (i in 0 until m) {
            val left = ring.mul(alpha, tmp[i])
            val right = ring.mul(beta, yVec[i])
            out[i] = ring.add(left, right)
        }
        return DenseVec.fromArrayUnsafe(out)// Normal/Trans/ConjTrans
    }

    fun <A : Any> affineMul(
        semiring: Semiring<A>,
        alpha: A,
        aOp: MatOp,
        aMat: MatLike<A>,
        bOp: MatOp,
        bMat: MatLike<A>,
        beta: A,
        cMat: MatLike<A>,
    ): DenseMat<A> {
        require(aOp != MatOp.ConjTrans && bOp != MatOp.ConjTrans) {
            "ConjTrans requires an involutive ring."
        }

        val aV = aMat.opView(aOp)
        val bV = bMat.opView(bOp)

        val m = aV.rows
        val k = aV.cols
        require(bV.rows == k) {
            "Shape mismatch: op(A) is ${m}${Symbols.TIMES}${k}, op(B) is ${bV.rows}${Symbols.TIMES}${bV.cols}"
        }
        val n = bV.cols
        require(cMat.rows == m && cMat.cols == n) {
            "Shape mismatch: C is ${cMat.rows}${Symbols.TIMES}${cMat.cols}, expected ${m}${Symbols.TIMES}${n}"
        }

        val out = allocateMatrix(m, n)

        for (r in 0 until m) {
            for (j in 0 until n) {
                var acc = semiring.add.identity
                for (t in 0 until k) {
                    acc = semiring.add(acc, semiring.mul(aV[r, t], bV[t, j]))
                }

                val scaledProd = semiring.mul(alpha, acc)
                val scaledC = semiring.mul(beta, cMat[r, j])
                out[r * n + j] = semiring.add(scaledProd, scaledC)
            }
        }

        return DenseMat.fromArrayUnsafe(m, n, out)
    }

    fun <A : Any> affineMul(
        ring: InvolutiveRing<A>,
        alpha: A,
        aOp: MatOp,
        aMat: MatLike<A>,
        bOp: MatOp,
        bMat: MatLike<A>,
        beta: A,
        cMat: MatLike<A>,
    ): DenseMat<A> {
        val aV = aMat.opView(aOp, ring.conj)
        val bV = bMat.opView(bOp, ring.conj)

        val m = aV.rows
        val k = aV.cols
        require(bV.rows == k) {
            "Shape mismatch: op(A) is ${m}${Symbols.TIMES}${k}, op(B) is ${bV.rows}${Symbols.TIMES}${bV.cols}"
        }
        val n = bV.cols
        require(cMat.rows == m && cMat.cols == n) {
            "Shape mismatch: C is ${cMat.rows}${Symbols.TIMES}${cMat.cols}, expected ${m}${Symbols.TIMES}${n}"
        }

        val out = allocateMatrix(m, n)

        for (r in 0 until m) {
            for (j in 0 until n) {
                var acc = ring.add.identity

                for (t in 0 until k)
                    acc = ring.add(acc, ring.mul(aV[r, t], bV[t, j]))

                val scaledProd = ring.mul(alpha, acc)
                val scaledC = ring.mul(beta, cMat[r, j])
                out[r * n + j] = ring.add(scaledProd, scaledC)
            }
        }

        return DenseMat.fromArrayUnsafe(m, n, out)
    }

    fun <A : Any> concatDiagonal(
        matrices: List<MatLike<A>>,
        zero: A
    ): DenseMat<A> {
        // Get the number of rows and columns in the final matrix.
        val totalRows = matrices.fold(0) { size, mat -> Math.addExact(size, mat.rows )}
        val totalCols = matrices.fold(0) { size, mat -> Math.addExact(size, mat.cols )}
        val out = allocateMatrix(totalRows, totalCols)

        var startRowIdx = 0
        var startColIdx = 0

        for (matIdx in matrices.indices) {
            val matrix = matrices[matIdx]
            val endRowIdx = startRowIdx + matrix.rows
            val endColIdx = startColIdx + matrix.cols
            val inRange = startColIdx..<endColIdx

            for (outRowIdx in startRowIdx until endRowIdx) {
                val matrixRow = outRowIdx - startRowIdx
                for (outColIdx in 0 until totalCols) {
                    if (outColIdx in inRange) {
                        val matrixCol = outColIdx - startColIdx
                        out[outRowIdx * totalCols + outColIdx] = matrix[matrixRow, matrixCol]
                    } else {
                        out[outRowIdx * totalCols + outColIdx] = zero
                    }
                }
            }

            startRowIdx = endRowIdx
            startColIdx = endColIdx
        }

        return DenseMat.fromArrayUnsafe(totalRows, totalCols, out)
    }

    fun <A : Any> isLowerTriangular(
        mat: MatLike<A>,
        zero: A,
        eq: Eq<A> = Eq.default()
    ): Boolean {
        if (mat.rows != mat.cols) return false
        val n = mat.rows

        for (i in 0 until n) {
            for (j in i + 1 until n) {
                if (!eq(mat[i, j], zero)) return false
            }
        }
        return true
    }

    fun <A : Any> isUpperTriangular(
        mat: MatLike<A>,
        zero: A,
        eq: Eq<A> = Eq.default()
    ): Boolean = isLowerTriangular(mat.transposeView(), zero, eq)

    fun <A : Any> isEqual(
        mat1: MatLike<A>,
        mat2: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): Boolean {
        if (mat1.rows != mat2.rows || mat1.cols != mat2.cols) return false
        val rows = mat1.rows
        val cols = mat1.cols

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (!eq(mat1[i, j], mat2[i, j])) return false
            }
        }
        return true
    }

    fun <A : Any> isSymmetric(
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ) = isEqual(mat, mat.transposeView(), eq)

    fun <A : Any> copy(
        mat: MatLike<A>
    ): DenseMat<A> {
        val rows = mat.rows
        val cols = mat.cols
        val out = allocateMatrix(rows, cols)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[r * cols + c] = mat[r, c]
            }
        }

        return DenseMat.fromArrayUnsafe(rows, cols, out)
    }

    fun <A : Any, M : Any> isRowDiagonallyDominant(
        mat: MatLike<A>,
        mag: (A) -> M,
        add: CommutativeMonoid<M>,
        order: TotalOrder<M>,
        strict: Boolean = false
    ): Boolean {
        require(isSquare(mat)) {
            "Diagonal dominance is defined for square matrices, got ${mat.rows}${Symbols.TIMES}${mat.cols}"
        }

        for (row in 0 until mat.rows) {
            var total = add.identity
            for (col in 0 until mat.cols) {
                if (col != row) total = add(total, mag(mat[row, col]))
            }

            val diag = mag(mat[row, row])
            if (strict) {
                if (!order.lt(total, diag)) return false
            } else {
                if (order.lt(diag, total)) return false
            }
        }
        return true
    }

    fun <A : Any, M : Any> isColDiagonallyDominant(
        mat: MatLike<A>,
        mag: (A) -> M,
        add: CommutativeMonoid<M>,
        order: TotalOrder<M>,
        strict: Boolean = false
    ): Boolean = isRowDiagonallyDominant(mat.transposeView(), mag, add, order, strict)

    /**
     * Calculates the permanent of a matrix over a [Semiring].
     *
     * This is the best we can do with a semiring since we do not have a negation operation on
     * the additive operator.
     */
    fun <A : Any> permByPermutation(
        ring: Semiring<A>,
        mat: MatLike<A>
    ): A {
        require(isSquare(mat)) {
            "Determinant is defined for square matrices: got ${mat.rows}${Symbols.TIMES}${mat.cols}."
        }
        val n = mat.rows
        if (n == 0) return ring.mul.identity
        if (n == 1) return mat[0, 0]
        if (n == 2) return ring.add(
            ring.mul(mat[0, 0], mat[1, 1]),
            ring.mul(mat[0, 1], mat[1, 0])
        )

        // We use an internal permutation generator here to avoid all the work of the outward facing
        // PermutationAlgorithm.permutations method. Accumulate the determinant in acc.
        var acc = ring.add.identity
        val perm = IntArray(n) { it }

        fun permutationProduct(): A {
            var prod = ring.mul.identity
            for (i in perm.indices) {
                prod = ring.mul(prod, mat[i, perm[i]])
            }
            return prod
        }

        fun addCurrentPermutationTerm() {
            acc = ring.add(acc, permutationProduct())
        }

        fun swap(i: Int, j: Int) {
            val tmp = perm[i]
            perm[i] = perm[j]
            perm[j] = tmp
        }

        fun generate(k: Int) {
            if (k == n) {
                addCurrentPermutationTerm()
                return
            }

            for (i in k until n) {
                swap(k, i)
                generate(k + 1)
                swap(k, i)
            }
        }

        generate(0)
        return acc
    }

    /**
     * Calculates the Leibniz / permutation determinant over a [CommutativeRing].
     *
     * This works for every commutative ring because it requires only addition,
     * additive inverse, multiplication, zero, and one. It is intended mainly as
     * a correctness oracle and small-matrix implementation.
     *
     * Complexity: O(n · n!), since each of the n! permutation terms multiplies n entries.
     */
    fun <A : Any> detByPermutation(
        ring: CommutativeRing<A>,
        mat: MatLike<A>
    ): A {
        require(isSquare(mat)) {
            "Permanent is defined for square matrices: got ${mat.rows}${Symbols.TIMES}${mat.cols}."
        }
        val n = mat.rows
        if (n == 0) return ring.mul.identity
        if (n == 1) return mat[0, 0]
        if (n == 2) return ring.add(
            ring.mul(mat[0, 0], mat[1, 1]),
            ring.add.inverse(ring.mul(mat[0, 1], mat[1, 0]))
        )

        // We use an internal permutation generator here to avoid all the work of the outward facing
        // PermutationAlgorithm.permutations method. Accumulate the permanent in acc.
        var acc = ring.add.identity
        val perm = IntArray(n) { it }

        fun permutationProduct(): A {
            var prod = ring.mul.identity
            for (i in perm.indices) {
                prod = ring.mul(prod, mat[i, perm[i]])
            }
            return prod
        }

        fun addCurrentPermutationTerm(even: Boolean) {
            val term = permutationProduct()
            acc =
                if (even) ring.add(acc, term)
                else ring.add(acc, ring.add.inverse(term))
        }

        fun swap(i: Int, j: Int) {
            val tmp = perm[i]
            perm[i] = perm[j]
            perm[j] = tmp
        }

        fun generate(k: Int, even: Boolean) {
            if (k == n) {
                addCurrentPermutationTerm(even)
                return
            }

            for (i in k until n) {
                swap(k, i)

                // Swapping equal positions does not change the parity of the permutation.
                generate(k + 1, if (i == k) even else !even)

                swap(k, i)
            }
        }

        generate(0, true)
        return acc
    }

    /**
     * Computes the determinant using the Bareiss fraction-free elimination algorithm.
     *
     * This requires a [CommutativeRing] plus an exact-division operation [exactDiv].
     *
     * The operation `exactDiv(a, b)` must return `q` such that:
     * ```text
     * a = b * q
     * ```
     * whenever Bareiss calls it. If division is not exact, [exactDiv] should throw.
     *
     * Complexity: O(n^3) ring operations.
     */
    fun <A : Any> detBareiss(
        ring: CommutativeRing<A>,
        exactDiv: BinOp<A>,
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): A {
        require(isSquare(mat)) {
            "Determinant (Bareiss) is defined for square matrices: got ${mat.rows}${Symbols.TIMES}${mat.cols}."
        }

        val n = mat.rows
        if (n == 0) return ring.mul.identity
        if (n == 1) return mat[0, 0]
        if (n == 2) return ring.add(
            ring.mul(mat[0, 0], mat[1, 1]),
            ring.add.inverse(ring.mul(mat[0, 1], mat[1, 0]))
        )

        val data = allocateMatrix(n, n)
        for (r in 0 until n) {
            for (c in 0 until n) {
                data[r * n + c] = mat[r, c]
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun get(row: Int, col: Int): A =
            data[row * n + col] as A

        fun set(row: Int, col: Int, value: A) {
            data[row * n + col] = value
        }

        fun swapRows(i : Int, j : Int) {
            if (i == j) return
            for (c in 0 until n) {
                val tmp = get(i, c)
                set(i, c, get(j, c))
                set(j, c, tmp)
            }
        }

        var sign = ring.mul.identity
        var previousPivot = ring.mul.identity

        for (k in 0 until (n-1)) {
            if (eq(get(k, k), ring.add.identity)) {
                var pivotRow = k + 1
                while (pivotRow < n && eq(get(pivotRow, k), ring.add.identity))
                    pivotRow++
                if (pivotRow == n)
                    return ring.add.identity

                swapRows(k, pivotRow)
                sign = ring.add.inverse(sign)
            }

            val pivot = get(k, k)

            for (i in (k+1) until n) {
                for (j in (k+1) until n) {
                    val left = ring.mul(pivot, get(i, j))
                    val right = ring.mul(get(i, k), get(k, j))
                    val numerator = ring.add(left, ring.add.inverse(right))
                    val updated =
                        if (k == 0) numerator
                        else exactDiv(numerator, previousPivot)

                    set(i, j, updated)
                }
                set(i, k, ring.add.identity)
            }

            previousPivot = pivot
        }

        return ring.mul(sign, get(n-1, n-1))
    }

    /**
     * Computes the row echelon form of the given matrix.
     *
     * The row echelon form is a matrix in which each leading entry (the first nonzero entry in a row) is to the
     * right of the leading entry of the row above it, and in this case, the leading entry is always one. All the
     * other entries in the rows below this column are zero.
     *
     * Note that we need a [Field] to perform the required row operations.
     *
     * @param field The field over which the matrix is defined.
     * @param mat The matrix of which to compute the row echelon form.
     * @param eq The equality function for the field.
     * @return The row echelon form of the given matrix.
     */
    fun <A : Any> rowEchelonForm(
        field: Field<A>,
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): RowEchelonForm<A> {
        val rows = mat.rows
        val cols = mat.cols

        // Make a mutable copy of the matrix.
        val data = allocateMatrix(rows, cols)

        @Suppress("UNCHECKED_CAST")
        fun get(row: Int, col: Int): A =
            data[row * cols + col] as A

        fun set(row: Int, col: Int, value: A) {
            data[row * cols + col] = value
        }

        for (row in 0 until rows)
            for (col in 0 until cols)
                set(row, col, mat[row, col])

        fun swapRows(i: Int, j: Int) {
            if (i == j) return

            for (k in 0 until cols) {
                val temp = get(i, k)
                set(i, k, get(j, k))
                set(j, k, temp)
            }
        }

        fun scaleRow(row: Int, factor: A, startCol: Int = 0) {
            for (k in startCol until cols)
                set(row, k, field.mul(factor, get(row, k)))
        }

        // Set dst <- dst + factor * src.
        fun addRowMultiple(src: Int, dst: Int, factor: A, startCol: Int) {
            for (k in startCol until cols)
                set(dst, k, field.add(get(dst, k), field.mul(factor, get(src, k))))
        }

        var rowSwaps = 0
        val pivots = mutableListOf<Pair<Int, Int>>()
        var currentRow = 0

        for (col in 0 until cols) {
            if (currentRow == rows) break

            var searchRow = currentRow
            while (searchRow < rows && eq(get(searchRow, col), field.zero))
                searchRow++

            if (searchRow == rows)
                continue

            if (searchRow != currentRow) {
                swapRows(currentRow, searchRow)
                rowSwaps++
            }

            val pivot = get(currentRow, col)

            if (!eq(pivot, field.one)) {
                val invPivot = field.reciprocalOption(pivot).getOrElse {
                    throw ArithmeticException("Pivot was selected as nonzero but has no reciprocal: $pivot")
                }

                scaleRow(currentRow, invPivot, col)
            }

            // Canonicalize the pivot entry. This is not a row operation for history purposes.
            set(currentRow, col, field.one)

            for (row in currentRow + 1 until rows) {
                val entry = get(row, col)

                if (!eq(entry, field.zero)) {
                    val factor = field.add.inverse(entry)

                    addRowMultiple(
                        src = currentRow,
                        dst = row,
                        factor = factor,
                        startCol = col
                    )

                    // Canonicalize the cleared entry.
                    set(row, col, field.zero)
                }
            }

            pivots.add(currentRow to col)
            currentRow++
        }

        val refMatrix = DenseMat.fromArrayUnsafe<A>(rows, cols, data)
        return RowEchelonForm(refMatrix, pivots, rowSwaps)
    }

    /**
     * Computes the reduced row echelon form of the given matrix.
     *
     * The reduced row echelon form is a matrix in which each leading entry (the first nonzero entry in a row) is to the
     * right of the leading entry of the row above it, and is one.
     * All the other entries in the rows of this column are zero.
     *
     * Note that we need a [Field] to perform the required row operations.
     *
     * @param field The field over which the matrix is defined.
     * @param mat The matrix of which to compute the reduced row echelon form.
     * @param eq The equality function for the field.
     * @return The row echelon form of the given matrix.
     */
    fun <A : Any> reducedRowEchelonForm(
        field: Field<A>,
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): RowEchelonForm<A> {
        val rows = mat.rows
        val cols = mat.cols
        val data = allocateMatrix(rows, cols)

        @Suppress("UNCHECKED_CAST")
        fun get(row: Int, col: Int): A =
            data[row * cols + col] as A

        fun set(row: Int, col: Int, value: A) {
            data[row * cols + col] = value
        }

        // Get the matrix in REF form. Then reduce rows above pivots from bottom to top, left to right.
        val ref = rowEchelonForm(field, mat, eq)

        // Set dst <- dst + factor * src.
        fun addRowMultiple(src: Int, dst: Int, factor: A, startCol: Int) {
            for (k in startCol until cols)
                set(dst, k, field.add(get(dst, k), field.mul(factor, get(src, k))))
        }

        for (row in 0 until rows)
            for (col in 0 until cols)
                set(row, col, ref.matrix[row, col])

        for ((refRow, refCol) in ref.pivots.asReversed()) {
            val pivotEntry = get(refRow, refCol)
            require(eq(pivotEntry, field.one)) {
                "The leading entry in the pivot row should be one but is: $pivotEntry"
            }

            for (row in refRow - 1 downTo 0) {
                val entry = get(row, refCol)
                if (eq(entry, field.zero)) continue

                // Since pivotEntry is one, we can reduce the row by subtracting the entry * refRow.
                addRowMultiple(
                    src = refRow,
                    dst = row,
                    factor = field.add.inverse(entry),
                    startCol = refCol
                )

                // Canonicalize the cleared entry.
                set(row, refCol, field.zero)
            }
        }

        return RowEchelonForm(DenseMat.fromArrayUnsafe(rows, cols, data), ref.pivots, ref.rowSwaps)
    }
}
