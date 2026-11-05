package org.vorpal.kosmos.linear.values

/**
 * A compact representation of an `n×n` permutation matrix, stored as the permutation itself: an
 * [IntArray] `p` where `p[c] = p(c)` is the image of `c ∈ [0, n)`.
 *
 * As a [MatLike], column `c` has its single 1 in row `p(c)`, so the matrix sends the basis vector
 * `e_c` to `e_p(c)`; every other entry is 0. Entries are the [Int]s 0 and 1, so a [PermMat] can be
 * passed to any matrix operation over the integers without building a [DenseMat].
 *
 * Products follow function composition: see [andThen] and [compose]. The group structures on
 * permutation matrices are in [org.vorpal.kosmos.linear.instances.PermMatAlgebras].
 *
 * Instances are made with [identity] or [of], which check that the array is a permutation.
 */
class PermMat private constructor(
    private val p: IntArray
) : MatLike<Int> {

    /**
     * The degree `n` of the permutation, which is also the number of rows and of columns.
     */
    val size: Int
        get() = p.size

    override val rows: Int
        get() = p.size

    override val cols: Int
        get() = p.size

    /**
     * The image `p(i)` of [i] under the permutation (not a matrix entry: see the two-index [get]).
     */
    operator fun get(i: Int): Int =
        p[i]

    /**
     * The matrix entry in row [r] and column [c]: 1 if `p(c) = r`, and 0 otherwise.
     *
     * @throws IndexOutOfBoundsException if either index is out of range.
     */
    override operator fun get(r: Int, c: Int): Int {
        if (r !in 0 until rows || c !in 0 until cols)
            throw IndexOutOfBoundsException("Index ($r, $c) out of bounds for a $rows × $cols matrix")
        return if (p[c] == r) 1 else 0
    }

    /**
     * The permutation that applies this one first and then [other]:
     * `(this andThen other)(c) = other(this(c))`.
     *
     * @throws IllegalArgumentException if the two permutations have different degrees
     */
    infix fun andThen(other: PermMat): PermMat {
        val n = p.size
        require(other.p.size == n) { "size mismatch" }

        val out = IntArray(n)

        var c = 0
        while (c < n) {
            out[c] = other[this[c]]
            c += 1
        }

        return PermMat(out)
    }

    /**
     * The permutation that applies [other] first and then this one:
     * `(this compose other)(c) = this(other(c))`. As matrices this is the ordinary product
     * `this · other`.
     */
    infix fun compose(other: PermMat): PermMat =
        other andThen this

    /**
     * The inverse permutation, which as a matrix is the transpose.
     */
    fun inverse(): PermMat {
        val n = p.size
        val inv = IntArray(n) { -1 }

        var c = 0
        while (c < n) {
            val r = p[c]
            require(r in 0 until n) { "invalid value p[$c] = $r" }
            require(inv[r] == -1) { "duplicate image $r" }
            inv[r] = c
            c += 1
        }

        return PermMat(inv)
    }

    /**
     * Whether the permutation is even, i.e. a product of an even number of transpositions.
     *
     * Computed from the cycle count: a permutation of `n` points with `k` cycles (fixed points
     * included) has sign `(−1)^(n − k)`.
     */
    fun isEven(): Boolean {
        val n = p.size
        val visited = BooleanArray(n)
        var cycles = 0

        var v = 0
        while (v < n) {
            if (!visited[v]) {
                cycles += 1
                var cur = v
                while (!visited[cur]) {
                    visited[cur] = true
                    cur = p[cur]
                }
            }
            v += 1
        }

        return ((n - cycles) and 1) == 0
    }

    /**
     * A copy of the underlying array `p`, with `p[c] = p(c)`.
     */
    fun toIntArray(): IntArray =
        p.copyOf()

    companion object {
        /**
         * The identity permutation of degree [n], i.e. the `n×n` identity matrix.
         *
         * @throws IllegalArgumentException if [n] is negative
         */
        fun identity(n: Int): PermMat {
            require(n >= 0) { "n must be nonnegative" }
            return PermMat(IntArray(n) { it })
        }

        /**
         * The permutation with `p(c) = p[c]`.
         *
         * If [copy] is false, [p] is used without copying, so the caller must not modify it
         * afterwards.
         *
         * @throws IllegalArgumentException if [p] is not a permutation of `[0, p.size)`
         */
        fun of(p: IntArray, copy: Boolean = true): PermMat {
            val owned =
                if (copy) p.copyOf()
                else p

            val n = owned.size
            val seen = BooleanArray(n)

            var c = 0
            while (c < n) {
                val r = owned[c]
                require(r in 0 until n) { "invalid value p[$c] = $r" }
                require(!seen[r]) { "duplicate image $r" }
                seen[r] = true
                c += 1
            }

            return PermMat(owned)
        }
    }
}
