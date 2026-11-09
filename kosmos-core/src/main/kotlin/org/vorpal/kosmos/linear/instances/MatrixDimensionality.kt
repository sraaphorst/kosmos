package org.vorpal.kosmos.linear.instances

/**
 * The fixed shape of the matrices a structure acts on, e.g. the `n×n` of
 * [DenseMatAlgebras.DenseMatRing].
 */
interface MatrixDimensionality {
    /** The number of rows. */
    val rows: Int

    /** The number of columns. */
    val cols: Int
}
