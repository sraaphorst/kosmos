package org.vorpal.kosmos.linear.values

/**
 * A compact representation of a permutation matrix that implements [MatLike] so that it can be used with other
 * matrix operations, but is implemented with an [IntArray] [p] where `p[c] = p(c)`, i.e. the image of an element `c`
 * in the range `[0,n)` is `p[c]`.
 */
class PermMat private constructor(
    private val p: IntArray
) : MatLike<Int> {

    val size: Int
        get() = p.size

    override val rows: Int
        get() = p.size

    override val cols: Int
        get() = p.size

    operator fun get(r: Int): Int =
        p[r]

    override operator fun get(r: Int, c: Int): Int =
        if (p[c] == r) 1 else 0

    /** (this andThen other)(c) = other(this(c)) */
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

    /** (this compose other)(c) = this(other(c)) */
    infix fun compose(other: PermMat): PermMat =
        other andThen this

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

    fun toIntArray(): IntArray =
        p.copyOf()

    companion object {
        fun identity(n: Int): PermMat {
            require(n >= 0) { "n must be nonnegative" }
            return PermMat(IntArray(n) { it })
        }

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
