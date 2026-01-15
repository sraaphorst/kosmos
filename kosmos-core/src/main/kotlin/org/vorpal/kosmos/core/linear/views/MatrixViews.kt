package org.vorpal.kosmos.core.linear.views

import org.vorpal.kosmos.core.linear.values.MatLike
import org.vorpal.kosmos.core.linear.values.VecLike

/**
 * A lazy transpose view of a matrix.
 *
 * No data is copied. Indexing is redirected: (r, c) ↦ base(c, r).
 */
class TransposeMatView<A : Any>(
    private val base: MatLike<A>,
) : MatLike<A> {
    override val rows: Int
        get() = base.cols

    override val cols: Int
        get() = base.rows

    override fun get(r: Int, c: Int): A =
        base[c, r]
}

/**
 * A lazy view of a single row of a matrix as a vector.
 *
 * No data is copied. Indexing is redirected: i ↦ mat(row, i).
 */
class RowVecView<A : Any>(
    private val mat: MatLike<A>,
    private val row: Int,
) : VecLike<A> {
    init {
        require(row in 0 until mat.rows) { "row out of bounds: $row" }
    }

    override val size: Int
        get() = mat.cols

    override fun get(i: Int): A =
        mat[row, i]
}

/**
 * A lazy view of a single column of a matrix as a vector.
 *
 * No data is copied. Indexing is redirected: i ↦ mat(i, col).
 */
class ColVecView<A : Any>(
    private val mat: MatLike<A>,
    private val col: Int,
) : VecLike<A> {
    init {
        require(col in 0 until mat.cols) { "col out of bounds: $col" }
    }

    override val size: Int
        get() = mat.rows

    override fun get(i: Int): A =
        mat[i, col]
}

/**
 * A lazy view of a row range slice of a matrix.
 */
class RowSliceMatView<A : Any>(
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

    override operator fun get(r: Int, c: Int): A =
        mat[rowStart + r, c]
}

/**
 * A lazy view of a col range slice of a matrix.
 */
class ColSliceMatView<A : Any>(
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

    override operator fun get(r: Int, c: Int): A =
        mat[r, colStart + c]
}

/** Convenience: transpose view (no copy). */
fun <A : Any> MatLike<A>.transposeView(): MatLike<A> =
    TransposeMatView(this)

/** Convenience: view row [r] as a vector (no copy). */
fun <A : Any> MatLike<A>.rowView(r: Int): VecLike<A> =
    RowVecView(this, r)

/** Convenience: view column [c] as a vector (no copy). */
fun <A : Any> MatLike<A>.colView(c: Int): VecLike<A> =
    ColVecView(this, c)

/** Convenience: view row slice as a matrix (no copy). */
fun <A : Any> MatLike<A>.rowSliceView(rowStart: Int, rowEndExclusive: Int): MatLike<A> =
    RowSliceMatView(this, rowStart, rowEndExclusive)

/** Convenience: view col slice as a matrix (no copy). */
fun <A : Any> MatLike<A>.colSliceView(colStart: Int, colEndExclusive: Int): MatLike<A> =
    ColSliceMatView(this, colStart, colEndExclusive)

/** Convenience: view submatrix as a matrix (no copy). */
fun <A : Any> MatLike<A>.submatrixView(rowStart: Int,
                                       rowEndExclusive: Int,
                                       colStart: Int,
                                       colEndExclusive: Int): MatLike<A> =
    ColSliceMatView(RowSliceMatView(this, rowStart, rowEndExclusive), colStart, colEndExclusive)
