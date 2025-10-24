package org.vorpal.kosmos.linear

import org.vorpal.kosmos.algebra.structures.Field

/**
 * A standard matrix over a given field F.
 */
class Matrix<F>(
    rows: Int,
    cols: Int,
    val field: Field<F>,
    data: List<List<F>>,
) : RMatrix<F>(rows, cols, field, data)
