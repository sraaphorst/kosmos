package org.vorpal.kosmos.linear.ops

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.map
import org.vorpal.kosmos.linear.instances.DenseMatKernel
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.MatLike
import org.vorpal.kosmos.linear.values.ReducedRowEchelonForm
import org.vorpal.kosmos.linear.values.RowEchelonForm
import org.vorpal.kosmos.linear.values.RowOp
import org.vorpal.kosmos.linear.values.RowReductionTrace
import org.vorpal.kosmos.linear.values.VecLike
import org.vorpal.kosmos.linear.views.transposeView
import org.vorpal.kosmos.linear.views.TransposeMode

/**
 * Operations on matrices of any shape.
 *
 * Every function accepts [MatLike], so it works on [DenseMat], on
 * [org.vorpal.kosmos.linear.values.Mat2], on [org.vorpal.kosmos.linear.values.PermMat] and on the
 * lazy views in [org.vorpal.kosmos.linear.views], and returns a freshly allocated [DenseMat] (or a
 * scalar, vector or predicate). Each function takes the weakest structure on the entries it needs:
 * a [Semiring] for sums and products, a [CommutativeRing] for determinants, a [Field] for row
 * reduction.
 *
 * Shape mismatches throw [IllegalArgumentException]. The implementations are in the internal
 * `DenseMatKernel`. The standard BLAS names for some of these are in [BLAS]; closed-form versions
 * for 2 × 2 matrices are in [FixedMatOps].
 */
object DenseMatOps {

    /**
     * Given
     * - A [Semiring] over [A]
     * - [mat1] and [mat2], two matrices of the same shape over [A]
     *
     * return their entrywise sum `mat1 + mat2`.
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
     *
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
     * Given a [Semiring] over [A] and a scalar [s], the scalar multiple `s·mat`, multiplying each
     * entry by [s] on the left.
     */
    fun <A : Any> scale(
        semiring: Semiring<A>,
        s: A,
        mat: MatLike<A>
    ): DenseMat<A> = DenseMatKernel.scale(semiring, s, mat)

    /**
     * Given a [Semiring], create the `n×n` identity matrix.
     */
    fun <A : Any> identity(
        semiring: Semiring<A>,
        n: Int
    ): DenseMat<A> = DenseMatKernel.identity(semiring, n)

    /**
     * Given a value [a], create the constant `rows×cols` matrix with every entry [a].
     */
    fun <A : Any> constMat(
        a: A,
        rows: Int,
        cols: Int
    ): DenseMat<A> = DenseMatKernel.constMat(a, rows, cols)

    /**
     * Given a [Semiring], create the `rows×cols` zero matrix (every entry the additive identity).
     */
    fun <A : Any> zero(
        semiring: Semiring<A>,
        rows: Int,
        cols: Int
    ): DenseMat<A> = constMat(semiring.add.identity, rows, cols)

    /**
     * Given a [Semiring], create the `rows×cols` all-ones matrix `J` (every entry the
     * multiplicative identity). This is not the identity matrix: see [identity].
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
        eq: Eq<A> = Eq.default()
    ): Boolean = DenseMatKernel.isHadamardUnit(field, mat, eq)

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
     * (rows are points, columns are blocks), this calculates the **block intersection matrix**.
     * - Entry `(i, j)` represents the size of the intersection between block `i` and block `j` (`|B_i ∩ B_j|`).
     * - Diagonal entries represent the block sizes (`k`).
     *
     * For the point-by-point counterpart `M Mᵀ`, see [intersectionMatrix].
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
     * The resulting matrix has dimensions `r×r` (where `r` is the number of rows in [m]).
     *
     * In the context of combinatorial design theory, if [m] is a point-block incidence matrix
     * (rows are points, columns are blocks), this calculates the **concurrence matrix** of the
     * points (despite the function's name, it is [gramMatrix] that intersects blocks).
     * - Entry `(i, j)` represents the number of blocks containing both point `i` and point `j` (`λ_ij`).
     * - Diagonal entries represent the replication numbers (`r`) for each point.
     *
     * For a balanced incomplete block design this is `(r − λ)I + λJ`.
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
     * Inflate each point (row) of the incidence matrix [mat] into [blockSize] copies, the
     * "blow up points" step of a GDD (group divisible design) construction. This replaces a
     * separate `BlockConstraintMatrix` class.
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

    /**
     * Returns true iff every entry of [mat] equals [a] under [eq]. Vacuously true for an empty
     * matrix.
     */
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
     * Given an `m×n` matrix [mat], map each entry by [f] and sum along each row in the
     * [CommutativeMonoid], returning the length `m` vector of row sums.
     */
    fun <A : Any, B : Any> rowReduce(
        add: CommutativeMonoid<B>,
        mat: MatLike<A>,
        f: (A) -> B
    ): DenseVec<B> = DenseMatKernel.rowReduce(add, mat, f)

    /**
     * Given an `m×n` matrix [mat], map each entry by [f] and sum down each column in the
     * [CommutativeMonoid], returning the length `n` vector of column sums.
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
     * Given a square `n×n` matrix [mat], calculate `mat^pow` using the given [Semiring] by
     * repeated squaring. `mat^0` is the identity matrix.
     */
    fun <A : Any> power(
       semiring: Semiring<A>,
       mat: MatLike<A>,
       pow: Int
    ): DenseMat<A> = DenseMatKernel.power(semiring, mat, pow)

    /**
     * Determine if the matrix [mat] is a permutation matrix. This requires:
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
     * - [aOp] way to treat matrix [aMat] (as per [TransposeMode])
     * - `m×n` matrix [aMat] over [A]
     * - vector [xVec] over [A], of length `n` if [aOp] is [TransposeMode.Normal] and `m` if it is
     *   [TransposeMode.Transpose]
     * - constant [beta] in [A]
     * - vector [yVec] over [A], of the length of the result (`m` for `Normal`, `n` for `Transpose`)
     *
     * let `A_p` be [aMat] read as [aOp] says, and calculate the affine product (BLAS `gemv`):
     * ```
     * alpha A_p x + beta y
     * ```
     * A [Semiring] has no conjugation, so [TransposeMode.ConjugateTranspose] is rejected with an
     * [IllegalArgumentException]; use the [InvolutiveRing] overload for it.
     */
    fun <A : Any> affineMatVec(
        semiring: Semiring<A>,
        alpha: A,
        aOp: TransposeMode,
        aMat: MatLike<A>,
        xVec: VecLike<A>,
        beta: A,
        yVec: VecLike<A>
    ): DenseVec<A> = DenseMatKernel.affineMatVec(semiring, alpha, aOp, aMat, xVec, beta, yVec)

    /**
     * Given an:
     * - [InvolutiveRing] over [A]
     * - constant [alpha] in [A]
     * - [aOp] way to treat matrix [aMat] (as per [TransposeMode])
     * - `m×n` matrix [aMat] over [A]
     * - vector [xVec] over [A], of length `n` if [aOp] is [TransposeMode.Normal] and `m` otherwise
     * - constant [beta] in [A]
     * - vector [yVec] over [A], of the length of the result (`m` for `Normal`, `n` otherwise)
     *
     * let `A_p` be [aMat] read as [aOp] says, using the ring's conjugation for
     * [TransposeMode.ConjugateTranspose], and calculate the affine product (BLAS `gemv`):
     * ```
     * alpha A_p x + beta y
     * ```
     */
    fun <A : Any> affineMatVec(
        ring: InvolutiveRing<A>,
        alpha: A,
        aOp: TransposeMode,
        aMat: MatLike<A>,
        xVec: VecLike<A>,
        beta: A,
        yVec: VecLike<A>
    ): DenseVec<A> = DenseMatKernel.affineMatVec(ring, alpha, aOp, aMat, xVec, beta, yVec)

    /**
     * Given a:
     * - [Semiring] over [A]
     * - constant [alpha] in [A]
     * - [aOp] way to treat matrix [aMat] (as per [TransposeMode])
     * - matrix [aMat] over [A]
     * - [bOp] way to treat matrix [bMat] (as per [TransposeMode])
     * - matrix [bMat] over [A]
     * - constant [beta] in [A]
     * - matrix [cMat] over [A]
     *
     * let `A_p` and `B_p` be [aMat] and [bMat] read as [aOp] and [bOp] say, with `A_p` of shape
     * `m×k`, `B_p` of shape `k×n` and [cMat] of shape `m×n`, and calculate the affine product
     * (BLAS `gemm`):
     * ```
     * alpha A_p B_p + beta C
     * ```
     * A [Semiring] has no conjugation, so [TransposeMode.ConjugateTranspose] is rejected with an
     * [IllegalArgumentException]; use the [InvolutiveRing] overload for it.
     */
    fun <A : Any> affineMul(
        semiring: Semiring<A>,
        alpha: A,
        aOp: TransposeMode,
        aMat: MatLike<A>,
        bOp: TransposeMode,
        bMat: MatLike<A>,
        beta: A,
        cMat: MatLike<A>,
    ): DenseMat<A> = DenseMatKernel.affineMul(semiring, alpha, aOp, aMat, bOp, bMat, beta, cMat)

    /**
     * Given an:
     * - [InvolutiveRing] over [A]
     * - constant [alpha] in [A]
     * - [aOp] way to treat matrix [aMat] (as per [TransposeMode])
     * - matrix [aMat] over [A]
     * - [bOp] way to treat matrix [bMat] (as per [TransposeMode])
     * - matrix [bMat] over [A]
     * - constant [beta] in [A]
     * - matrix [cMat] over [A]
     *
     * let `A_p` and `B_p` be [aMat] and [bMat] read as [aOp] and [bOp] say, using the ring's
     * conjugation for [TransposeMode.ConjugateTranspose], with `A_p` of shape `m×k`, `B_p` of
     * shape `k×n` and [cMat] of shape `m×n`, and calculate the affine product (BLAS `gemm`):
     * ```
     * alpha A_p B_p + beta C
     * ```
     */
    fun <A : Any> affineMul(
        ring: InvolutiveRing<A>,
        alpha: A,
        aOp: TransposeMode,
        aMat: MatLike<A>,
        bOp: TransposeMode,
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
    ): DenseMat<A> = DenseMatKernel.concatDiagonal(matrices.asList(), zero)

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
     * Return true iff [mat] is lower triangular: `m_ij = 0` (under [eq]) whenever `j > i`.
     */
    fun <A : Any> isLowerTriangular(
        mat: MatLike<A>,
        zero: A,
        eq: Eq<A> = Eq.default()
    ): Boolean = DenseMatKernel.isLowerTriangular(mat, zero, eq)

    /**
     * Return true iff [mat] is upper triangular: `m_ij = 0` (under [eq]) whenever `j < i`.
     */
    fun <A : Any> isUpperTriangular(
        mat: MatLike<A>,
        zero: A,
        eq: Eq<A> = Eq.default()
    ): Boolean = DenseMatKernel.isUpperTriangular(mat, zero, eq)

    /**
     * Return true iff [mat] is square and symmetric: `m_ij = m_ji` (under [eq]) for all `i`, `j`.
     */
    fun <A : Any> isSymmetric(
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): Boolean = DenseMatKernel.isSymmetric(mat, eq)

    /**
     * Return true iff [mat1] and [mat2] have the same shape and their entries are pairwise equal
     * under [eq].
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

    /**
     * Computes the permanent using Kosmos' default exact algorithm for matrices
     * over a [Semiring].
     *
     * For a 0/1 matrix, the permanent counts the number of ways to choose exactly
     * one 1 from every row and every column.
     */
    fun <A : Any> perm(
        semiring: Semiring<A>,
        mat: MatLike<A>
    ): A = DenseMatKernel.permByPermutation(semiring, mat)

    /**
     * Computes the permanent using the unsigned permutation formula.
     *
     * This is factorial-time and intended mainly for small matrices.
     */
    fun <A : Any> permByPermutation(
        semiring: Semiring<A>,
        mat: MatLike<A>
    ): A = DenseMatKernel.permByPermutation(semiring, mat)

    /**
     * Computes the determinant using Kosmos' default exact algorithm for matrices
     * over a [CommutativeRing].
     *
     * Currently, this delegates to [detByPermutation], but future versions may use
     * small unrolled formulas or other exact algorithms where appropriate.
     */
    fun <A : Any> det(
        ring: CommutativeRing<A>,
        mat: MatLike<A>
    ): A = DenseMatKernel.detByPermutation(ring, mat)

    /**
     * Computes the determinant using the Leibniz signed-permutation formula.
     *
     * This is factorial-time and intended mainly for small matrices and as a
     * correctness oracle for other determinant algorithms.
     */
    fun <A : Any> detByPermutation(
        ring: CommutativeRing<A>,
        mat: MatLike<A>
    ): A = DenseMatKernel.detByPermutation(ring, mat)

    /**
     * Computes the determinant with Bareiss's fraction-free elimination, in `O(n³)` ring
     * operations, over a [CommutativeRing] in which the needed divisions are exact (e.g. ℤ).
     *
     * Rather than requiring a [Field], the caller supplies [exactDiv], a division that is only
     * ever applied when the quotient exists in the ring: it serves as a proof of the capability.
     * Every intermediate value is itself a minor of [mat], so nothing leaves the ring. The result
     * agrees with [detByPermutation], which the tests use as an oracle.
     */
    fun <A : Any> detBareiss(
        ring: CommutativeRing<A>,
        exactDiv: BinOp<A>,
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): A = DenseMatKernel.detBareiss(ring, exactDiv, mat, eq)

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
    ): RowEchelonForm<A> =
        DenseMatKernel.rowEchelonForm(field, mat, eq, null)

    /**
     * Calculate the row echelon form of a matrix along with the row operations that were performed to obtain it.
     */
    fun <A : Any> rowEchelonFormTrace(
        field: Field<A>,
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): RowReductionTrace<A, RowEchelonForm<A>> {
        val ops = mutableListOf<RowOp<A>>()
        val form = DenseMatKernel.rowEchelonForm(field, mat, eq, ops)
        return RowReductionTrace(form, ops.toList())
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
     * @return The reduced row echelon form of the given matrix.
     */
    fun <A : Any> reducedRowEchelonForm(
        field: Field<A>,
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): ReducedRowEchelonForm<A> =
        DenseMatKernel.reducedRowEchelonForm(field, mat, eq, null)

    /**
     * Calculate the reduced row echelon form of a matrix along with the row operations that were performed to
     * obtain it.
     */
    fun <A : Any> reducedRowEchelonFormTrace(
        field: Field<A>,
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): RowReductionTrace<A, ReducedRowEchelonForm<A>> {
        val ops = mutableListOf<RowOp<A>>()
        val form = DenseMatKernel.reducedRowEchelonForm(field, mat, eq, ops)
        return RowReductionTrace(form, ops.toList())
    }

    /**
     * Calculate the rank of a matrix over a [Field].
     *
     * Note that the rank is the number of pivot columns in the row echelon form: thus, if any other
     * properties of the REF / RREF are needed, [rowEchelonForm] and [reducedRowEchelonForm] should be used instead.
     */
    fun <A : Any> rank(
        field: Field<A>,
        mat: MatLike<A>,
        eq: Eq<A> = Eq.default()
    ): Int = rowEchelonForm(field, mat, eq).rank
}
