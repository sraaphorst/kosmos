package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.CommutativeSemigroup
import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.ops.BinOp
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
     * Entrywise (Hadamard) product of two vectors.
     */
    fun <A : Any> hadamard(
        semigroup: Semigroup<A>,
        x: VecLike<A>,
        y: VecLike<A>
    ): DenseVec<A> =
        vecOp(semigroup.op, x, y)

    /**
     * Entrywise (Hadamard) product of two vectors over a [CommutativeSemigroup].
     */
    fun <A : Any> commutativeHadamard(
        commutativeSemigroup: CommutativeSemigroup<A>,
        x: VecLike<A>,
        y: VecLike<A>
    ): DenseVec<A> =
        vecOp(commutativeSemigroup.op, x, y)

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
}
