package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.linear.ops.MatOp
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.MatLike
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
        var r = 0
        while (r < mat.rows) {
            var c = 0
            while (c < mat.cols) {
                require(mat[r, c] == elem) {
                    "Expected constant ${mat.rows}${Symbols.TIMES}${mat.cols} matrix of $elem, but found ${mat[r, c]}."
                }
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
        requireSize(mat2, rows, cols)
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

    fun <A : Any> pointInflation(
        mat: DenseMat<A>,
        blockSize: Int,
    ): DenseMat<A> {
        require(blockSize > 0) { "Block size must be positive, but got: $blockSize" }

        // Each entry gets replicated into a blockSize × blockSize block.
        return DenseMat.tabulate(
            mat.rows * blockSize,
            mat.cols * blockSize
        ) { i, j ->
            mat[i / blockSize, j / blockSize]
        }
    }

    fun <A : Any> argmin(
        mat: MatLike<A>,
        cmp: Comparator<A>
    ): Option<Pair<Int, Int>> {
        if (mat.rows == 0 || mat.cols == 0) return Option.None
        var minRow = 0
        var minCol = 0

        var i = 0
        while (i < mat.rows) {
            var j = 0
            while (j < mat.cols) {
                if (cmp.compare(mat[i, j], mat[minRow, minCol]) < 0) {
                    minRow = i
                    minCol = j
                }
                j += 1
            }
            i += 1
        }
        return Option.Some(minRow to minCol)
    }

    fun <A : Any> argmin(
        mat: MatLike<A>,
        order: TotalOrder<A>
    ): Option<Pair<Int, Int>> =
        argmin(mat) { u, v ->
            when {
                order.lt(u, v) -> -1
                order.lt(v, u) -> 1
                else -> 0
            }
        }

    fun <A : Any> argmax(
        mat: MatLike<A>,
        cmp: Comparator<A>
    ): Option<Pair<Int, Int>> {
        if (mat.rows == 0 || mat.cols == 0) return Option.None
        var maxRow = 0
        var maxCol = 0

        var i = 0
        while (i < mat.rows) {
            var j = 0
            while (j < mat.cols) {
                if (cmp.compare(mat[i, j], mat[maxRow, maxCol]) > 0) {
                    maxRow = i
                    maxCol = j
                }
                j += 1
            }
            i += 1
        }
        return Option.Some(maxRow to maxCol)
    }

    fun <A : Any> argmax(
        mat: MatLike<A>,
        order: TotalOrder<A>
    ): Option<Pair<Int, Int>> =
        argmax(mat) { u, v ->
            when {
                order.lt(u, v) -> -1
                order.lt(v, u) -> 1
                else -> 0
            }
        }

    fun <A : Any, B : Any> argminBy(
        mat: MatLike<A>,
        f: (A) -> B,
        cmp: Comparator<B>
    ): Option<Pair<Int, Int>> {
        if (mat.rows == 0 || mat.cols == 0) return Option.None

        var minRow = 0
        var minCol = 0
        var minVal = f(mat[0, 0])

        var i = 0
        while (i < mat.rows) {
            var j = 0
            while (j < mat.cols) {
                val vij = f(mat[i, j])
                if (cmp.compare(vij, minVal) < 0) {
                    minRow = i
                    minCol = j
                    minVal = vij
                }
                j += 1
            }
            i += 1
        }
        return Option.Some(minRow to minCol)
    }

    fun <A : Any, B : Any> argminBy(
        mat: MatLike<A>,
        f: (A) -> B,
        order: TotalOrder<B>
    ): Option<Pair<Int, Int>> =
        argminBy(mat, f) { u, v ->
            when {
                order.lt(u, v) -> -1
                order.lt(v, u) -> 1
                else -> 0
            }
        }

    fun <A : Any, B : Any> argmaxBy(
        mat: MatLike<A>,
        f: (A) -> B,
        cmp: Comparator<B>
    ): Option<Pair<Int, Int>> {
        if (mat.rows == 0 || mat.cols == 0) return Option.None

        var maxRow = 0
        var maxCol = 0
        var maxVal = f(mat[0, 0])

        var i = 0
        while (i < mat.rows) {
            var j = 0
            while (j < mat.cols) {
                val vij = f(mat[i, j])
                if (cmp.compare(vij, maxVal) > 0) {
                    maxRow = i
                    maxCol = j
                    maxVal = vij
                }
                j += 1
            }
            i += 1
        }
        return Option.Some(maxRow to maxCol)
    }

    fun <A : Any, B : Any> argmaxBy(
        mat: MatLike<A>,
        f: (A) -> B,
        order: TotalOrder<B>
    ): Option<Pair<Int, Int>> =
        argmaxBy(mat, f) { u, v ->
            when {
                order.lt(u, v) -> -1
                order.lt(v, u) -> 1
                else -> 0
            }
        }

    fun <A : Any> isAll(
        mat: MatLike<A>,
        a: A,
        eq: Eq<A> = Eq.default()
    ): Boolean {
        var i = 0
        while (i < mat.rows) {
            var j = 0
            while (j < mat.cols) {
                if (!eq(mat[i, j], a)) return false
                j += 1
            }
            i += 1
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

    fun <A : Any> rowSums(
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
    ): DenseVec<A> = rowSums(semiring, mat.transposeView())

    fun <A : Any> scale(
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

    fun <A : Any, B : Any> rowReduce(
        add: CommutativeMonoid<B>,
        mat: MatLike<A>,
        f: (A) -> B
    ): DenseVec<B> {
        val out = arrayOfNulls<Any?>(mat.rows)
        var r = 0
        while (r < mat.rows) {
            var acc = add.identity
            var c = 0
            while (c < mat.cols) {
                acc = add(acc, f(mat[r, c]))
                c += 1
            }
            out[r] = acc
            r += 1
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
            var r = 0
            while (r < currMat.rows) {
                var ones = 0
                var c = 0
                while (c < currMat.cols) {
                    val entry = currMat[r, c]
                    if (!isBinary(entry)) return false
                    if (eq(entry, one)) {
                        ones += 1
                        if (ones > 1) return false
                    }
                    c += 1
                }
                if (ones != 1) return false
                r += 1
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
        var i = 0
        while (i < m) {
            val left = semiring.mul(alpha, tmp[i])
            val right = semiring.mul(beta, yVec[i])
            out[i] = semiring.add(left, right)
            i += 1
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
        var i = 0
        while (i < m) {
            val left = ring.mul(alpha, tmp[i])
            val right = ring.mul(beta, yVec[i])
            out[i] = ring.add(left, right)
            i += 1
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

        val out = arrayOfNulls<Any?>(m * n)

        var r = 0
        while (r < m) {
            var j = 0
            while (j < n) {
                var acc = semiring.add.identity

                var t = 0
                while (t < k) {
                    acc = semiring.add(acc, semiring.mul(aV[r, t], bV[t, j]))
                    t += 1
                }

                val scaledProd = semiring.mul(alpha, acc)
                val scaledC = semiring.mul(beta, cMat[r, j])
                out[r * n + j] = semiring.add(scaledProd, scaledC)

                j += 1
            }
            r += 1
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

        val out = arrayOfNulls<Any?>(m * n)

        var r = 0
        while (r < m) {
            var j = 0
            while (j < n) {
                var acc = ring.add.identity

                var t = 0
                while (t < k) {
                    acc = ring.add(acc, ring.mul(aV[r, t], bV[t, j]))
                    t += 1
                }

                val scaledProd = ring.mul(alpha, acc)
                val scaledC = ring.mul(beta, cMat[r, j])
                out[r * n + j] = ring.add(scaledProd, scaledC)

                j += 1
            }
            r += 1
        }

        return DenseMat.fromArrayUnsafe(m, n, out)
    }

    fun <A : Any> concatDiagonal(
        matrices: List<MatLike<A>>,
        zero: A
    ): DenseMat<A> {
        // Get the number of rows and columns in the final matrix.
        val totalRows = matrices.sumOf(MatLike<A>::rows)
        val totalCols = matrices.sumOf(MatLike<A>::cols)
        val out = arrayOfNulls<Any?>(totalRows * totalCols)

        // The index of the row we are filling in out.
        var outRowIdx = 0

        // The index of the matrix in matrices we are processing.
        var matIdx = 0

        // The starting row and column where matrices[matIdx] is being copied.
        var startRowIdx = 0
        var startColIdx = 0

        while (matIdx < matrices.size) {
            // We start copying matrix at startRowIdx and startColIdx
            val matrix = matrices[matIdx]
            val endRowIdx = startRowIdx + matrix.rows
            val endColIdx = startColIdx + matrix.cols

            // Fill in the row of out's outRowIdx, which contains matrix matIdx's matrixRow.
            while (outRowIdx < endRowIdx) {
                var outColIdx = 0

                val matrixRow = outRowIdx - startRowIdx
                val inRange = startColIdx..<endColIdx
                while (outColIdx < totalCols) {
                    if (outColIdx in inRange) {
                        val matrixCol = outColIdx - startColIdx
                        out[outRowIdx * totalCols + outColIdx] = matrix[matrixRow, matrixCol]
                    } else {
                        out[outRowIdx * totalCols + outColIdx] = zero
                    }
                    outColIdx += 1
                }
                outRowIdx += 1
            }

            // We have finished copying the rows corresponding to matrix matIdx.
            // Advance the row index and column index to where the next matrix will be copied.
            startRowIdx = endRowIdx
            startColIdx = endColIdx
            matIdx += 1
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

        var i = 0
        while (i < n) {
            var j = i + 1
            while (j < n) {
                if (!eq(mat[i, j], zero)) return false
                j += 1
            }
            i += 1
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

        var i = 0
        while (i < rows) {
            var j = 0
            while (j < cols) {
                if (!eq(mat1[i, j], mat2[i, j])) return false
                j += 1
            }
            i += 1
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

        return DenseMat.fromArrayUnsafe(rows, cols, out)
    }
}
