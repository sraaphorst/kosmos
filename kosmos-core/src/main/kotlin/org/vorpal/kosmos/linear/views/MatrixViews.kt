package org.vorpal.kosmos.linear.views

import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.linear.values.MatLike
import org.vorpal.kosmos.linear.values.VecLike

/**
 * How an operation should read a matrix argument, as in the `trans` flags of BLAS
 * (`N`, `T`, `C`). [opView] turns a mode into the corresponding lazy view.
 */
enum class TransposeMode {
    /** Use the matrix as it is: `A`. */
    Normal,

    /** Use the transpose: `Aᵀ`, with `(Aᵀ)ᵢⱼ = Aⱼᵢ`. */
    Transpose,

    /**
     * Use the conjugate transpose (the adjoint): `A*`, with `(A*)ᵢⱼ = conj(Aⱼᵢ)`. This needs a
     * conjugation on the entries, so it is only available where one is supplied, e.g. over an
     * `InvolutiveRing`.
     */
    ConjugateTranspose
}

/**
 * A lazy view of a vector of length `n` as an `n×1` matrix (a column). No data is copied.
 *
 * @throws IllegalArgumentException from [get] if the position is out of range
 */
data class VecAsColMatView<A : Any>(
    private val v: VecLike<A>
) : MatLike<A> {
    override val rows: Int
        get() = v.size

    override val cols: Int
        get() = 1

    override fun get(r: Int, c: Int): A {
        require(r in 0 until rows) { "row out of bounds: $r" }
        require(c == 0) { "VecAsColMatView has exactly one column; got c=$c" }
        return v[r]
    }
}

/**
 * A lazy view of a vector of length `n` as a `1×n` matrix (a row). No data is copied.
 *
 * @throws IllegalArgumentException from [get] if the position is out of range
 */
data class VecAsRowMatView<A : Any>(
    private val v: VecLike<A>
) : MatLike<A> {
    override val rows: Int
        get() = 1

    override val cols: Int
        get() = v.size

    override fun get(r: Int, c: Int): A {
        require(r == 0) { "VecAsRowMatView has exactly one row; got r=$r" }
        require(c in 0 until cols) { "col out of bounds: $c" }
        return v[c]
    }
}

/**
 * A lazy transpose view of a matrix.
 *
 * No data is copied. Indexing is redirected: (r, c) ↦ base(c, r).
 */
data class TransposeMatView<A : Any>(
    private val base: MatLike<A>
): MatLike<A> {
    override val rows: Int
        get() = base.cols

    override val cols: Int
        get() = base.rows

    override fun get(r: Int, c: Int): A =
        base[c, r]
}

/**
 * A lazy view of the conjugate transpose `A*` of a matrix: `(A*)(r, c) = conj(base(c, r))`.
 *
 * No data is copied; [conj] is applied on every access.
 */
class ConjTransposeMatView<A : Any>(
    private val base: MatLike<A>,
    private val conj: Endo<A>
) : MatLike<A> {
    override val rows: Int
        get() = base.cols

    override val cols: Int
        get() = base.rows

    override fun get(r: Int, c: Int): A =
        conj(base[c, r])
}

/**
 * The view of this matrix that [op] asks for: itself, or its [transposeView].
 *
 * @throws IllegalStateException for [TransposeMode.ConjugateTranspose], which needs a conjugation:
 * use the overload that takes one
 */
fun <A : Any> MatLike<A>.opView(op: TransposeMode): MatLike<A> = when (op) {
    TransposeMode.Normal -> this
    TransposeMode.Transpose -> this.transposeView()
    TransposeMode.ConjugateTranspose -> error("ConjugateTranspose requires a conjugation Endo.")
}

/**
 * The view of this matrix that [op] asks for: itself, its [transposeView], or its conjugate
 * transpose ([ConjTransposeMatView]) with entries conjugated by [conj].
 */
fun <A : Any> MatLike<A>.opView(op: TransposeMode, conj: Endo<A>): MatLike<A> = when (op) {
    TransposeMode.Normal -> this
    TransposeMode.Transpose -> this.transposeView()
    TransposeMode.ConjugateTranspose -> ConjTransposeMatView(this, conj)
}

/**
 * A lazy view of the rows `[rowStart, rowEndExclusive)` of a matrix, with all its columns.
 * No data is copied.
 *
 * @throws IllegalArgumentException if the range is not inside the matrix, and from [get] if the
 * position is out of range
 */
data class RowSliceMatView<A : Any>(
    private val mat: MatLike<A>,
    private val rowStart: Int,
    private val rowEndExclusive: Int
): MatLike<A> {
    init {
        require(rowStart in 0..mat.rows) { "rowStart out of bounds: $rowStart" }
        require(rowEndExclusive in 0..mat.rows) { "rowEndExclusive out of bounds: $rowEndExclusive" }
        require(rowStart <= rowEndExclusive) { "row range invalid: [$rowStart, $rowEndExclusive)" }
    }

    override val rows: Int
        get() = rowEndExclusive - rowStart

    override val cols: Int
        get() = mat.cols

    override operator fun get(r: Int, c: Int): A {
        require(r in 0 until rows) { "row out of bounds for slice: $r" }
        require(c in 0 until cols) { "col out of bounds for slice: $c" }
        return mat[rowStart + r, c]
    }
}

/**
 * A lazy view of the columns `[colStart, colEndExclusive)` of a matrix, with all its rows.
 * No data is copied.
 *
 * @throws IllegalArgumentException if the range is not inside the matrix, and from [get] if the
 * position is out of range
 */
data class ColSliceMatView<A : Any>(
    private val mat: MatLike<A>,
    private val colStart: Int,
    private val colEndExclusive: Int
): MatLike<A> {
    init {
        require(colStart in 0..mat.cols) { "colStart out of bounds: $colStart" }
        require(colEndExclusive in 0..mat.cols) { "colEndExclusive out of bounds: $colEndExclusive" }
        require(colStart <= colEndExclusive) { "col range invalid: [$colStart, $colEndExclusive)" }
    }

    override val rows: Int
        get() = mat.rows

    override val cols: Int
        get() = colEndExclusive - colStart

    override operator fun get(r: Int, c: Int): A {
        require(r in 0 until rows) { "row out of bounds for slice: $r" }
        require(c in 0 until cols) { "col out of bounds for slice: $c" }
        return mat[r, colStart + c]
    }
}

/** A lazy [TransposeMatView] of this matrix (no copy). */
fun <A : Any> MatLike<A>.transposeView(): MatLike<A> =
    TransposeMatView(this)

/** A lazy [VecAsColMatView]: this vector as an `n×1` matrix (no copy). */
fun <A : Any> VecLike<A>.asColMatView(): MatLike<A> =
    VecAsColMatView(this)

/** A lazy [VecAsRowMatView]: this vector as a `1×n` matrix (no copy). */
fun <A : Any> VecLike<A>.asRowMatView(): MatLike<A> =
    VecAsRowMatView(this)

/** A lazy [RowSliceMatView] of the rows `[rowStart, rowEndExclusive)` (no copy). */
fun <A : Any> MatLike<A>.rowSliceView(rowStart: Int, rowEndExclusive: Int): MatLike<A> =
    RowSliceMatView(this, rowStart, rowEndExclusive)

/** A lazy [ColSliceMatView] of the columns `[colStart, colEndExclusive)` (no copy). */
fun <A : Any> MatLike<A>.colSliceView(colStart: Int, colEndExclusive: Int): MatLike<A> =
    ColSliceMatView(this, colStart, colEndExclusive)

/**
 * A lazy view of the submatrix with rows `[rowStart, rowEndExclusive)` and columns
 * `[colStart, colEndExclusive)` (no copy).
 */
fun <A : Any> MatLike<A>.submatrixView(
    rowStart: Int,
    rowEndExclusive: Int,
    colStart: Int,
    colEndExclusive: Int
): MatLike<A> =
    ColSliceMatView(
        RowSliceMatView(
            this,
            rowStart,
            rowEndExclusive
        ), colStart, colEndExclusive
    )
