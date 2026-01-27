package org.vorpal.kosmos.linear.views

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.linear.values.MatLike
import org.vorpal.kosmos.linear.values.VecLike
import kotlin.math.min


/**
 * A lazy view of a single row of a matrix as a vector.
 *
 * No data is copied. Indexing is redirected: i ↦ mat(row, i).
 */
data class RowVecView<A : Any>(
    private val mat: MatLike<A>,
    private val row: Int,
): VecLike<A> {
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
data class ColVecView<A : Any>(
    private val mat: MatLike<A>,
    private val col: Int
): VecLike<A> {
    init {
        require(col in 0 until mat.cols) { "col out of bounds: $col" }
    }

    override val size: Int
        get() = mat.rows

    override fun get(i: Int): A =
        mat[i, col]
}

/**
 * View a 1×n matrix as a vector of length n.
 */
data class RowMatAsVecView<A : Any>(
    private val m: MatLike<A>
) : VecLike<A> {
    init {
        require(m.rows == 1) {
            "Expected 1${Symbols.TIMES}n matrix, got ${m.rows}${Symbols.TIMES}${m.cols}"
        }
    }

    override val size: Int
        get() = m.cols

    override fun get(i: Int): A =
        m[0, i]
}

/**
 * View an n×1 matrix as a vector of length n.
 */
data class ColMatAsVecView<A : Any>(
    private val m: MatLike<A>
) : VecLike<A> {
    init {
        require(m.cols == 1) {
            "Expected n${Symbols.TIMES}1 matrix, got ${m.rows}${Symbols.TIMES}${m.cols}"
        }
    }

    override val size: Int
        get() = m.rows

    override fun get(i: Int): A =
        m[i, 0]
}

data class DiagonalMatView<A : Any>(
    private val mat: MatLike<A>
): VecLike<A> {
    override val size: Int by lazy {
        min(mat.rows, mat.cols)
    }

    override operator fun get(i: Int): A {
        require(i in 0 until size) { "index out of bounds: $i" }
        return mat[i, i]
    }
}

/**
 * View a section of a vector as a slice.
 */
data class SliceVecView<A : Any>(
    private val v: VecLike<A>,
    private val start: Int,
    private val endExclusive: Int
): VecLike<A> {
    init {
        require(start in 0..v.size) { "start index out of bounds for slice: $start"}
        require(endExclusive in 0..v.size) { "endExclusive index out of bounds for slice: $endExclusive"}
        require(start <= endExclusive) { "range invalid: [$start, $endExclusive]" }
    }

    override val size: Int
        get() = endExclusive - start

    override operator fun get(i: Int): A {
        require(i in 0 until size) { "index out of bounds for slice: $i" }
        return v[i + start]
    }
}

/** Convenience: view row [r] as a vector (no copy). */
fun <A : Any> MatLike<A>.rowView(r: Int): VecLike<A> =
    RowVecView(this, r)

/** Convenience: view column [c] as a vector (no copy). */
fun <A : Any> MatLike<A>.colView(c: Int): VecLike<A> =
    ColVecView(this, c)

/** Convenience: treat a 1×n matrix as a vector. */
fun <A : Any> MatLike<A>.asRowVecView(): VecLike<A> =
    RowMatAsVecView(this)

/** Convenience: treat an n×1 matrix as a vector. */
fun <A : Any> MatLike<A>.asColVecView(): VecLike<A> =
    ColMatAsVecView(this)

/**
 * Convenience: get a slice of a vector.
 */
fun <A : Any> VecLike<A>.sliceView(start: Int, endExclusive: Int): VecLike<A> =
    SliceVecView(this, start, endExclusive)
