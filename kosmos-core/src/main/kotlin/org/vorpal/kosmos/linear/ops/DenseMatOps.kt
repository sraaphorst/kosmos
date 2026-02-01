package org.vorpal.kosmos.linear.ops

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.map
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
    fun <R : Any> pointInflation(
        mat: DenseMat<R>,
        blockSize: Int,
    ): DenseMat<R> = DenseMatKernel.pointInflation(mat, blockSize)

    /**
     * Given a matrix [mat] over [A] and a [Comparator] over [A], determine
     * the indices of the smallest element of [mat].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any> argmin(
        mat: MatLike<A>,
        comparator: Comparator<A>
    ): Option<Pair<Int, Int>> = DenseMatKernel.argmin(mat, TotalOrder.of(comparator))

    /**
     * Given a matrix [mat] over [A] and a [Comparator] over [A], determine
     * the smallest element of [mat].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any> min(
        mat: MatLike<A>,
        comparator: Comparator<A>
    ): Option<A> = argmin(mat, comparator).map { mat[it.first, it.second] }

    /**
     * Given a matrix [mat] over [A] and a [TotalOrder] over [A], determine
     * the indices of the smallest element of [mat].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any> argmin(
        mat: MatLike<A>,
        order: TotalOrder<A>
    ): Option<Pair<Int, Int>> = DenseMatKernel.argmin(mat, order)

    /**
     * Given a matrix [mat] over [A] and a [TotalOrder] over [A], determine
     * the smallest element of [mat].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any> min(
        mat: MatLike<A>,
        order: TotalOrder<A>
    ): Option<A> = argmin(mat, order).map { mat[it.first, it.second] }

    /**
     * Given a matrix [mat] over [A] and a [Comparator] over [A], determine
     * the indices of the largest element of [mat].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any> argmax(
        mat: MatLike<A>,
        comparator: Comparator<A>
    ): Option<Pair<Int, Int>> = DenseMatKernel.argmax(mat, TotalOrder.of(comparator))

    /**
     * Given a matrix [mat] over [A] and a [Comparator] over [A], determine
     * the largest element of [mat].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any> max(
        mat: MatLike<A>,
        comparator: Comparator<A>
    ): Option<A> = argmax(mat, comparator).map { mat[it.first, it.second] }

    /**
     * Given a matrix [mat] over [A] and a [TotalOrder] over [A], determine
     * the indices of the largest element of [mat].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any> argmax(
        mat: MatLike<A>,
        order: TotalOrder<A>
    ): Option<Pair<Int, Int>> = DenseMatKernel.argmax(mat, order)

    /**
     * Given a matrix [mat] over [A] and a [TotalOrder] over [A], determine
     * the largest element of [mat].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any> max(
        mat: MatLike<A>,
        order: TotalOrder<A>
    ): Option<A> = argmax(mat, order).map { mat[it.first, it.second] }

    /**
     * Given
     * - a matrix [mat] over [A]
     * - a function [f] from [A] to [B]
     * - a [Comparator] over [B]
     *
     * determine the indices of the smallest element of [mat] with respect to [B].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any, B : Any> argminBy(
        mat: MatLike<A>,
        f: (A) -> B,
        comparator: Comparator<B>
    ): Option<Pair<Int, Int>> = DenseMatKernel.argminBy(mat, f, TotalOrder.of(comparator))

    /**
     * Given
     * - a matrix [mat] over [A]
     * - a function [f] from [A] to [B]
     * - a [Comparator] over [B]
     *
     * determine the smallest element of [mat] with respect to [B].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any, B : Any> minBy(
        mat: MatLike<A>,
        f: (A) -> B,
        comparator: Comparator<B>
    ): Option<A> = argminBy(mat, f, comparator).map { mat[it.first, it.second] }

    /**
     * Given
     * - a matrix [mat] over [A]
     * - a function [f] from [A] to [B]
     * - a [TotalOrder] over [B]
     *
     * determine the indices of the smallest element of [mat] with respect to [B].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any, B : Any> argminBy(
        mat: MatLike<A>,
        f: (A) -> B,
        order: TotalOrder<B>
    ): Option<Pair<Int, Int>> = DenseMatKernel.argminBy(mat, f, order)

    /**
     * Given
     * - a matrix [mat] over [A]
     * - a function [f] from [A] to [B]
     * - a [TotalOrder] over [B]
     *
     * determine the smallest element of [mat] with respect to [B].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any, B : Any> minBy(
        mat: MatLike<A>,
        f: (A) -> B,
        order: TotalOrder<B>
    ): Option<A> = argminBy(mat, f, order).map { mat[it.first, it.second] }

    /**
     * Given
     * - a matrix [mat] over [A]
     * - a function [f] from [A] to [B]
     * - a [Comparator] over [B]
     *
     * determine the indices of the largest element of [mat] with respect to [B].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any, B : Any> argmaxBy(
        mat: MatLike<A>,
        f: (A) -> B,
        comparator: Comparator<B>
    ): Option<Pair<Int, Int>> = DenseMatKernel.argmaxBy(mat, f, TotalOrder.of(comparator))

    /**
     * Given
     * - a matrix [mat] over [A]
     * - a function [f] from [A] to [B]
     * - a [Comparator] over [B]
     *
     * determine the largest element of [mat] with respect to [B].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any, B : Any> maxBy(
        mat: MatLike<A>,
        f: (A) -> B,
        comparator: Comparator<B>
    ): Option<A> = argmaxBy(mat, f, comparator).map { mat[it.first, it.second] }

    /**
     * Given
     * - a matrix [mat] over [A]
     * - a function [f] from [A] to [B]
     * - a [TotalOrder] over [B]
     *
     * determine the indices of the largest element of [mat] with respect to [B].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any, B : Any> argmaxBy(
        mat: MatLike<A>,
        f: (A) -> B,
        order: TotalOrder<B>
    ): Option<Pair<Int, Int>> = DenseMatKernel.argmaxBy(mat, f, order)

    /**
     * Given
     * - a matrix [mat] over [A]
     * - a function [f] from [A] to [B]
     * - a [TotalOrder] over [B]
     *
     * determine the largest element of [mat] with respect to [B].
     *
     * If [mat] is empty, [Option.None] is returned.
     */
    fun <A : Any, B : Any> maxBy(
        mat: MatLike<A>,
        f: (A) -> B,
        order: TotalOrder<B>
    ): Option<A> = argmaxBy(mat, f, order).map { mat[it.first, it.second] }

    fun <A : Any> isAll(
        mat: MatLike<A>,
        a: A,
        eq: Eq<A> = Eq.default()
    ): Boolean = DenseMatKernel.isAll(mat, a, eq)

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
     * Given an `m×n` matrix [mat], calculate the sum across the rows using the [CommutativeMonoid] and return
     * the length `m` vector of sums.
     */
    fun <A : Any, B : Any> rowReduce(
        add: CommutativeMonoid<B>,
        mat: MatLike<A>,
        f: (A) -> B
    ): DenseVec<B> = DenseMatKernel.rowReduce(add, mat, f)

    /**
     * Given an `m×n` matrix [mat], calculate the sum across the cols using the [CommutativeMonoid] and return
     * the length `n` vector of sums.
     */
    fun <A : Any, B : Any> colReduce(
        add: CommutativeMonoid<B>,
        mat: MatLike<A>,
        f: (A) -> B
    ): DenseVec<B> = DenseMatKernel.colReduce(add, mat, f)

    /**
     * Given a square `n×n` matrix [mat], return the entries on the main diagonal as a vector of size `n`.
     */
    fun <A : Any> extractDiagonal(
        mat: MatLike<A>,
    ): DenseVec<A> = DenseMatKernel.extractDiagonal(mat)

    /**
     * Given a [zero] element, a size [n], and a function [f], create a square `n×n` matrix with:
     * - `f(i)` in position `mat[i, i]`
     * - `zero` in all other positions.
     */
    fun <A: Any> fromDiagonal(
        zero : A,
        n : Int,
        f: (Int) -> A
    ): DenseMat<A> = fromDiagonal(zero, n, n, f)

    /**
     * Given a [zero] element, sizes [m] and [n], and a function [f], create a rectangular `m×n` matrix
     * with:
     * - `f(i)` in position `mat[i, i]`
     * - `zero` in all other positions.
     */
    fun <A : Any> fromDiagonal(
        zero : A,
        m: Int,
        n: Int,
        f: (Int) -> A
    ): DenseMat<A> = DenseMat.tabulate(m, n) { r, c -> if (r == c) f(r) else zero }

    /**
     * Given a [zero] element and a vector [vec] of length `n`, create a square `n×n` matrix with:
     * - `vec[i]` in position `mat[i, i]`
     * - `zero` in all other positions.
     */
    fun <A : Any> fromDiagonal(
        zero : A,
        vec: VecLike<A>
    ): DenseMat<A> = fromDiagonal(zero, vec.size, vec.size, vec::get)

    /**
     * Given a square `n×n` matrix [mat], calculate its [pow] power using the given [Semiring] through repeated
     * squaring.
     */
    fun <A : Any> power(
       semiring: Semiring<A>,
       mat: MatLike<A>,
       pow: Int
    ): DenseMat<A> = DenseMatKernel.power(semiring, mat, pow)

    /**
     * Determine is the matrix [mat] is a permutation matrix. This requires:
     * - [mat] be square (i.e. `n×n`)
     * - [mat] be a binary matrix (all entries are either zero or one)
     * - Each row of [mat] sums exactly to one
     * - Each col of [mat] sums exactly to one
     */
    fun <A : Any> isPermutationMatrix(
        mat: MatLike<A>,
        zero: A,
        one: A,
        eq: Eq<A> = Eq.default()
    ): Boolean = DenseMatKernel.isPermutationMatrix(mat, zero, one, eq)

    /**
     * Determine if the matrix [mat] is a permutation matrix. This is identical to the
     * other [isPermutationMatrix] function, but accepts a [Semiring] to determine what is 0 and what is 1.
     */
    fun <A : Any> isPermutationMatrix(
        semiring: Semiring<A>,
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): Boolean = DenseMatKernel.isPermutationMatrix(semiring, mat, eq)

    /**
     * Given a:
     * - [Semiring] over [A]
     * - constant [alpha] in [A]
     * - [aOp] way to treat matrix [aMat] (as per [MatOp])
     * - `m×n` matrix [aMat] over [A]
     * - `m` or `n` sized vector [xVec] over [A] (`m` if [aOp] is [MatOp.Normal], and `n` if [aOp] is [MatOp.Trans])
     * - constant [beta] in [A]
     * - `m` or `n` sized vector [yVec] over [A] (same size as [xVec])`
     * Let:
     * - `A_p` be the matrix [aMat] be adjusted by [aOp]
     * calculate the affine product:
     * ```
     * alpha A_p x + beta y
     * ```
     * Note that since we are using a [Semiring] here, we have no `conj` function available to us, so specifying
     * [MatOp.ConjTrans] causes an error at there is no way to calculate it.
     */
    fun <A : Any> affineMatVec(
        semiring: Semiring<A>,
        alpha: A,
        aOp: MatOp,
        aMat: MatLike<A>,
        xVec: VecLike<A>,
        beta: A,
        yVec: VecLike<A>
    ): DenseVec<A> = DenseMatKernel.affineMatVec(semiring, alpha, aOp, aMat, xVec, beta, yVec)

    /**
     * Given an:
     * - [InvolutiveRing] over [A]
     * - constant [alpha] in [A]
     * - [aOp] way to treat matrix [aMat] (as per [MatOp])
     * - `m×n` matrix [aMat] over [A]
     * - `m` or `n` sized vector [xVec] over [A] (`m` if [aOp] is [MatOp.Normal], and `n` if [aOp] is otherwise)
     * - constant [beta] in [A]
     * - `m` or `n` sized vector [yVec] over [A] (same size as [xVec])`
     * Let:
     * - `A_p` be the matrix [aMat] be adjusted by [aOp]
     * calculate the affine product:
     * ```
     * alpha A_p x + beta y
     * ```
     * Note that since we are using a [Semiring] here, we have no `conj` function available to us, so specifying
     * [MatOp.ConjTrans] causes an error at there is no way to calculate it.
     */
    fun <A : Any> affineMatVec(
        ring: InvolutiveRing<A>,
        alpha: A,
        aOp: MatOp,
        aMat: MatLike<A>,
        xVec: VecLike<A>,
        beta: A,
        yVec: VecLike<A>
    ): DenseVec<A> = DenseMatKernel.affineMatVec(ring, alpha, aOp, aMat, xVec, beta, yVec)

    /**
     * Given a:
     * - [Semiring] over [A]
     * - constant [alpha] in [A]
     * - [aOp] way to treat matrix [aMat] (as per [MatOp])
     * - `m×n` matrix [aMat] over [A]
     * - [bOp] way to treat matrix [bMat] (as per [MatOp])
     * - `m×n` matrix `B` over [A]
     * - constant [beta] in [A]
     * - `m×n` matrix `C` over [A]
     * Let:
     * - `A_p` be the matrix [aMat] be adjusted by [aOp]
     * - `B_p` be the matrix [bMat] be adjusted by [bOp]
     * calculate the affine product:
     * ```
     * alpha A_p B_p + beta C
     * ```
     * Note that since we are using a [Semiring] here, we have no `conj` function available to us, so specifying
     * [MatOp.ConjTrans] causes an error at there is no way to calculate it.
     */
    fun <A : Any> affineMul(
        semiring: Semiring<A>,
        alpha: A,
        aOp: MatOp,
        aMat: MatLike<A>,
        bOp: MatOp,
        bMat: MatLike<A>,
        beta: A,
        cMat: MatLike<A>,
    ): DenseMat<A> = DenseMatKernel.affineMul(semiring, alpha, aOp, aMat, bOp, bMat, beta, cMat)

    /**
     * Given an:
     * - [InvolutiveRing] over [A]
     * - constant [alpha] in [A]
     * - [aOp] way to treat matrix [aMat] (as per [MatOp])
     * - `m×n` matrix [aMat] over [A]
     * - [bOp] way to treat matrix [bMat] (as per [MatOp])
     * - `m×n` matrix `B` over [A]
     * - constant [beta] in [A]
     * - `m×n` matrix `C` over [A]
     * Let:
     * - `A_p` be the matrix [aMat] be adjusted by [aOp]
     * - `B_p` be the matrix [bMat] be adjusted by [bOp]
     * calculate the affine product:
     * ```
     * alpha A_p B_p + beta C
     * ```
     */
    fun <A : Any> affineMul(
        ring: InvolutiveRing<A>,
        alpha: A,
        aOp: MatOp,
        aMat: MatLike<A>,
        bOp: MatOp,
        bMat: MatLike<A>,
        beta: A,
        cMat: MatLike<A>,
    ): DenseMat<A> = DenseMatKernel.affineMul(ring, alpha, aOp, aMat, bOp, bMat, beta, cMat)

    /**
     * Concatenate a variable list of matrices diagonal to one another, inserting [zero] in other positions.
     *
     * For example, concat with the following arguments:
     * [[1, 2, 3], [4, 5, 6]]
     * [[7], [8]]
     * [[9, 10]]
     * Gives:
     * 1 2 3 0 0 0
     * 4 5 6 0 0 0
     * 0 0 0 7 0 0
     * 0 0 0 8 0 0
     * 0 0 0 0 9 10
     */
    fun <A : Any> concatDiagonal(
        zero: A,
        vararg matrices: MatLike<A>
    ) = DenseMatKernel.concatDiagonal(matrices.asList(), zero)

    /**
     * Concatenate a list of matrices diagonal to one another, inserting [zero] in other positions.
     *
     * For example, concat with the following arguments:
     * [[1, 2, 3], [4, 5, 6]]
     * [[7], [8]]
     * [[9, 10]]
     * Gives:
     * 1 2 3 0 0 0
     * 4 5 6 0 0 0
     * 0 0 0 7 0 0
     * 0 0 0 8 0 0
     * 0 0 0 0 9 10
     */
    fun <A : Any> concatDiagonal(
        zero: A,
        matrices: List<MatLike<A>>,
    ): DenseMat<A> = DenseMatKernel.concatDiagonal(matrices, zero)

    /**
     * Return true iff for all j > i, m_ij = 0.
     */
    fun <A : Any> isLowerTriangular(
        mat: MatLike<A>,
        zero: A,
        eq: Eq<A> = Eq.default()
    ): Boolean = DenseMatKernel.isLowerTriangular(mat, zero, eq)

    /**
     * Return true iff for all j < i, m_ij = 0.
     */
    fun <A : Any> isUpperTriangular(
        mat: MatLike<A>,
        zero: A,
        eq: Eq<A> = Eq.default()
    ): Boolean = DenseMatKernel.isUpperTriangular(mat, zero, eq)

    /**
     * Return true iff for all i, j, m_ij = m_ji.
     */
    fun <A : Any> isSymmetric(
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): Boolean = DenseMatKernel.isSymmetric(mat, eq)

    /**
     * Return true iff mat1 and mat2 have the same size and their entries are
     * pairwise equal.
     */
    fun <A : Any> isEqual(
        mat1: MatLike<A>,
        mat2: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): Boolean = DenseMatKernel.isEqual(mat1, mat2, eq)

    /**
     * Determine if the square matrix is row diagonally dominant.
     *
     * If strict is false, this means that:
     * ```
     * mag(m_ii) >= sum_{i ≠ j} mag(m_ij)
     * ```
     * If strict is true, this means that:
     * ```
     * mag(m_ii) > sum_{i ≠ j} mag(m_ij)
     * ```
     * When mag is a norm / absolute value compatible with the scalar structure (e.g. real / complex modulus),
     * strict diagonal dominance implies the matrix is invertible.
     */
    fun <A : Any, M : Any> isRowDiagonallyDominant(
        mat: MatLike<A>,
        mag: (A) -> M,
        add: CommutativeMonoid<M>,
        order: TotalOrder<M>,
        strict: Boolean = false
    ): Boolean = DenseMatKernel.isRowDiagonallyDominant(mat, mag, add, order, strict)

    /**
     * Determine if the square matrix is strictly row diagonally dominant, i.e.:
     * ```
     * mag(m_ii) > sum_{i ≠ j} mag(m_ij)
     * ```
     * When mag is a norm / absolute value compatible with the scalar structure (e.g. real / complex modulus),
     * strict diagonal dominance implies the matrix is invertible.
     */
    fun <A : Any, M : Any> isRowDiagonallyDominantStrict(
        mat: MatLike<A>,
        mag: (A) -> M,
        add: CommutativeMonoid<M>,
        order: TotalOrder<M>
    ): Boolean = DenseMatKernel.isRowDiagonallyDominant(mat, mag, add, order, true)

    /**
     * Determine if the square matrix is col diagonally dominant.
     *
     * If strict is false, this means that:
     * ```
     * mag(m_ii) >= sum_{i ≠ j} mag(m_ji)
     * ```
     * If strict is true, this means that:
     * ```
     * mag(m_ii) > sum_{i ≠ j} mag(m_ji)
     * ```
     * When mag is a norm / absolute value compatible with the scalar structure (e.g. real / complex modulus),
     * strict diagonal dominance implies the matrix is invertible.
     */
    fun <A : Any, M : Any> isColDiagonallyDominant(
        mat: MatLike<A>,
        mag: (A) -> M,
        add: CommutativeMonoid<M>,
        order: TotalOrder<M>,
        strict: Boolean = false
    ): Boolean = DenseMatKernel.isColDiagonallyDominant(mat, mag, add, order, strict)

    /**
     * Determine if the square matrix is strictly col diagonally dominant, i.e.:
     * ```
     * mag(m_ii) > sum_{i ≠ j} mag(m_ji)
     * ```
     * When mag is a norm / absolute value compatible with the scalar structure (e.g. real / complex modulus),
     * strict diagonal dominance implies the matrix is invertible.
     */
    fun <A : Any, M : Any> isColDiagonallyDominantStrict(
        mat: MatLike<A>,
        mag: (A) -> M,
        add: CommutativeMonoid<M>,
        order: TotalOrder<M>
    ): Boolean = DenseMatKernel.isColDiagonallyDominant(mat, mag, add, order, true)

    /**
     * Make a [DenseMat] copy of [mat].
     */
    fun <A : Any> copy(
        mat: MatLike<A>
    ): DenseMat<A> = DenseMatKernel.copy(mat)
}
