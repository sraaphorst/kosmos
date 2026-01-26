package org.vorpal.kosmos.linear.views

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.linear.values.MatLike
import org.vorpal.kosmos.linear.values.VecLike

/**
 * View a vector as an n×1 matrix (a column matrix).
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
 * View a vector as a 1×n matrix (a row matrix).
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

/** Convenience: treat a vector as an n×1 matrix. */
fun <A : Any> VecLike<A>.asColMatView(): MatLike<A> =
    VecAsColMatView(this)

/** Convenience: treat a vector as a 1×n matrix. */
fun <A : Any> VecLike<A>.asRowMatView(): MatLike<A> =
    VecAsRowMatView(this)

/** Convenience: treat a 1×n matrix as a vector. */
fun <A : Any> MatLike<A>.asRowVecView(): VecLike<A> =
    RowMatAsVecView(this)

/** Convenience: treat an n×1 matrix as a vector. */
fun <A : Any> MatLike<A>.asColVecView(): VecLike<A> =
    ColMatAsVecView(this)