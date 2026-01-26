package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.relations.TotalOrder
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.VecLike

/**
 * These functions accept [VecLike] inputs and return [DenseVec] outputs.
 *
 * They comprise:
 * - hot loop plumbing
 * - unsafe constructors
 * - shape checks
 *
 * We don't necessarily want to freeze these as API or support them forever; hence we use
 * an `internal object` here and expose public wrappers with stable names in an `Operations` object or as extensions
 * and keep kernel internals internal.
 */
internal object DenseVecKernel {

    private fun <A : Any> vecOp(
        op: BinOp<A>,
        x: VecLike<A>,
        y: VecLike<A>
    ): DenseVec<A> {
        val n = x.size
        DenseKernel.requireSize(n, y.size)
        val out = arrayOfNulls<Any?>(n)

        var i = 0
        while (i < n) {
            out[i] = op(x[i], y[i])
            i += 1
        }
        return DenseVec.fromArrayUnsafe(out)
    }

    /**
     * Entrywise (Hadamard) product of two vectors over the multiplicative monoid of a [Semiring].
     */
    fun <A : Any> hadamard(
        semiring: Semiring<A>,
        x: VecLike<A>,
        y: VecLike<A>
    ): DenseVec<A> =
        vecOp(semiring.mul.op, x, y)

    /**
     * Entrywise (Hadamard) product of two vectors over a [Semigroup].
     */
    fun <A : Any> hadamard(
        semigroup: Semigroup<A>,
        x: VecLike<A>,
        y: VecLike<A>
    ): DenseVec<A> =
        vecOp(semigroup.op, x, y)


    fun <A : Any> isHadamardUnit(
        field: Field<A>,
        x: VecLike<A>
    ): Boolean {
        var r = 0
        while (r < x.size) {
            if (x[r] == field.zero) return false
            r += 1
        }
        return true
    }


    /**
     * Entrywise addition (often useful even if callers could zipWith).
     */
    fun <A : Any> add(
        semiring: Semiring<A>,
        x: VecLike<A>,
        y: VecLike<A>
    ): DenseVec<A> =
        vecOp(semiring.add.op, x, y)

    /**
     * Dot product over a semiring.
     */
    fun <A : Any> dot(
        semiring: Semiring<A>,
        x: VecLike<A>,
        y: VecLike<A>
    ): A {
        val n = x.size
        DenseKernel.requireSize(n, y.size)
        var acc = semiring.add.identity

        var i = 0
        while (i < n) {
            acc = semiring.add(acc, semiring.mul(x[i], y[i]))
            i += 1
        }

        return acc
    }

    /**
     * Scale a vector by a scalar:
     * ```
     * s ⊙ x = (s*xᵢ)
     * ```
     */
    fun <A : Any> scale(
        semiring: Semiring<A>,
        s: A,
        x: VecLike<A>
    ): DenseVec<A> {
        val n = x.size
        val out = arrayOfNulls<Any?>(n)

        var i = 0
        while (i < n) {
            out[i] = semiring.mul(s, x[i])
            i += 1
        }

        return DenseVec.fromArrayUnsafe(out)
    }

    fun <A : Any> constantVec(
        s: A,
        n: Int
    ): DenseVec<A> {
        DenseKernel.checkNonnegative(n)
        val out = arrayOfNulls<Any?>(n)
        var i = 0
        while (i < n) {
            out[i] = s
            i += 1
        }
        return DenseVec.fromArrayUnsafe(out)
    }

    fun <A : Any> oneVec(
        semiring: Semiring<A>,
        n: Int
    ): DenseVec<A> =
        constantVec(semiring.mul.identity, n)

    fun <A: Any> zeroVec(
        semiring: Semiring<A>,
        n: Int
    ): DenseVec<A> =
        constantVec(semiring.add.identity, n)

    /** Map an arbitrary VecLike into a DenseVec. */
    fun <A : Any, B : Any> map(
        x: VecLike<A>,
        f: (A) -> B
    ): DenseVec<B> {
        val n = x.size
        val out = arrayOfNulls<Any?>(n)
        var i = 0
        while (i < n) {
            out[i] = f(x[i])
            i += 1
        }
        return DenseVec.fromArrayUnsafe(out)
    }

    fun <A : Any, B : Any> foldLeft(
        x: VecLike<A>,
        initial: B,
        f: (B, A) -> B
    ): B {
        val n = x.size
        var sum = initial
        var i = 0
        while (i < n) {
            sum = f(sum, x[i])
            i += 1
        }
        return sum
    }

    fun <A : Any> reduceLeft(
        x: VecLike<A>,
        f: (A, A) -> A
    ): Option<A> {
        if (x.size == 0) return Option.None
        val n = x.size
        var sum = x[0]
        var i = 1
        while (i < n) {
            sum = f(sum, x[i])
            i += 1
        }
        return Option.Some(sum)
    }

    fun <A : Any, B : Any> sumOf(
        add: CommutativeMonoid<B>,
        x: VecLike<A>,
        f: (A) -> B
    ): B {
        var acc = add.identity
        var i = 0
        while (i < x.size) {
            acc = add(acc, f(x[i]))
            i += 1
        }
        return acc
    }

    fun <A : Any, B : Any> sumOf(
        semiring: Semiring<B>,
        x: VecLike<A>,
        f: (A) -> B
    ): B = sumOf(semiring.add, x, f)

    fun <A : Any> sum(
        add: CommutativeMonoid<A>,
        x: VecLike<A>,
    ): A {
        var acc = add.identity
        var i = 0
        while (i < x.size) {
            acc = add(acc, x[i])
            i += 1
        }
        return acc
    }

    fun <A : Any> sum(
        semiring: Semiring<A>,
        x: VecLike<A>
    ): A = sum(semiring.add, x)

    fun <A : Any> all(
        x: VecLike<A>,
        predicate: (A) -> Boolean
    ): Boolean {
        var i = 0
        while (i < x.size) {
            if (!predicate(x[i])) return false
            i += 1
        }
        return true
    }

    fun <A : Any> any(
        x: VecLike<A>,
        predicate: (A) -> Boolean
    ): Boolean {
        var i = 0
        while (i < x.size) {
            if (predicate(x[i])) return true
            i += 1
        }
        return false
    }

    fun <A : Any> argmin(
        x: VecLike<A>,
        cmp: Comparator<A>
    ): Option<Int> {
        if (x.size == 0) return Option.None
        var minIdx = 0
        var idx = 1
        while (idx < x.size) {
            if (cmp.compare(x[idx], x[minIdx]) < 0) {
                minIdx = idx
            }
            idx += 1
        }
        return Option.Some(minIdx)
    }

    fun <A : Any> argmin(
        x: VecLike<A>,
        totalOrder: TotalOrder<A>
    ): Option<Int> {
        if (x.size == 0) return Option.None
        var minIdx = 0
        var idx = 1
        while (idx < x.size) {
            if (totalOrder.lt(x[idx], x[minIdx])) {
                minIdx = idx
            }
            idx += 1
        }
        return Option.Some(minIdx)
    }

    fun <A : Any> argmax(
        x: VecLike<A>,
        cmp: Comparator<A>
    ): Option<Int> {
        if (x.size == 0) return Option.None
        var maxIdx = 0
        var idx = 1
        while (idx < x.size) {
            if (cmp.compare(x[idx], x[maxIdx]) > 0) {
                maxIdx = idx
            }
            idx += 1
        }
        return Option.Some(maxIdx)
    }

    fun <A : Any> argmax(
        x: VecLike<A>,
        totalOrder: TotalOrder<A>
    ): Option<Int> {
        if (x.size == 0) return Option.None
        var maxIdx = 0
        var idx = 1
        while (idx < x.size) {
            if (totalOrder.gt(x[idx], x[maxIdx])) {
                maxIdx = idx
            }
            idx += 1
        }
        return Option.Some(maxIdx)
    }

    fun <A : Any> outerProduct(
        semiring: Semiring<A>,
        x: VecLike<A>,
        y: VecLike<A>
    ): DenseMat<A> {
        val m = x.size
        val n = y.size
        val out = arrayOfNulls<Any?>(m * n)

        var i = 0
        while (i < m) {
            var j = 0
            while (j < n) {
                out[i * n + j] = semiring.mul(x[i], y[j])
                j += 1
            }
            i += 1
        }
        return DenseMat.fromArrayUnsafe(m, n, out)
    }
}
