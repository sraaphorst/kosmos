package org.vorpal.kosmos.core.linear.instances

internal object DenseKernel {
    /**
     * Convenience method to check that the size of a parameter is nonnegative.
     * A description of the parameter may be passed in (should start with a capital letter) for printing error
     * messages if the check fails.
     */
    fun checkNonnegative(n: Int, description: String = "Size") {
        require(n >= 0) { "$description must be nonnegative, got: $n" }
    }

    /**
     * Convenience method to check that the size of rows and columns are nonnegative.
     */
    fun checkNonnegative(rows: Int, cols: Int) {
        checkNonnegative(rows, "Rows")
        checkNonnegative(cols, "Cols")
    }

    fun checkPositive(n: Int, description: String = "Size") {
        require(n > 0) { "$description must be positive, got: $n" }
    }

    /**
     * Require a certain size. This is useful for making sure, for example, that vectors are of the correct dimension.
     */
    fun requireSize(actual: Int, expected: Int) {
        require(actual == expected) { "Expected size $expected, got: $actual." }
    }
}
