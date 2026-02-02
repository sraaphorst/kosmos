package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.algebra.structures.Subgroup
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.algebra.structures.subgroup
import org.vorpal.kosmos.bridge.PermutationBridge.toPermMat
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.functional.datastructures.getOrElse
import org.vorpal.kosmos.functional.datastructures.map
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.linear.values.MatLike
import org.vorpal.kosmos.linear.values.PermMat
import org.vorpal.kosmos.linear.views.transposeView

object PermutationMatrixAlgebras {

    /**
     * Create a group of `n×n` [DenseMat] representing the matrix representation of the `S_n` over a given
     * [Semiring].
     *
     * Unlike [PermMatAlgebras.symmetric], it uses full [DenseMat] instead of the faster [PermMat].
     *
     * Note: the group operation here is composition (∘), implemented as reversed
     * matrix multiplication:
     * ```
     * X ∘ Y := Y * X
     * ```
     * This is chosen so that the natural map `p ↦ M_p` is a `GroupHomomorphism`
     * that aligns with `Permutation.andThen`:
     * ```
     * (p andThen q)(x) = q(p(x)).
     * ```
     *
     * If you want the usual linear-algebra group operation, use standard matrix
     * multiplication (*) on permutation matrices instead.
     */
    fun <A : Any> symmetricGroup(
        semiring: Semiring<A>,
        n: Int,
        eq: Eq<A> = Eq.default()
    ): Group<DenseMat<A>> {
        require(n >= 0) { "n must be nonnegative: $n" }

        val zero = semiring.add.identity
        val one = semiring.mul.identity

        require(!eq(zero, one)) { "Permutation matrices require 0 != 1." }

        fun requirePermMat(m: MatLike<A>) {
            require(m.rows == n) { "Expected $n rows, got ${m.rows}." }
            require(m.cols == n) { "Expected $n cols, got ${m.cols}." }
            require(DenseMatKernel.isPermutationMatrix(m, zero, one, eq)) {
                "Not a permutation matrix."
            }
        }

        return Group.of(
            identity = DenseMatKernel.identity(semiring, n),

            // Composition (∘) aligned with Permutation.andThen:
            // X ∘ Y := Y * X  (reverse standard matMul)
            op = BinOp(Symbols.OPEN_CIRCLE) { x, y ->
                requirePermMat(x)
                requirePermMat(y)
                DenseMatKernel.matMul(semiring, y, x)
            },

            // Inverse of permutation matrix is transpose.
            inverse = Endo(Symbols.INVERSE) { x ->
                requirePermMat(x)
                DenseMatKernel.copy(x.transposeView())
            }
        )
    }

    /**
     * Create the subgroup of `n×n` [DenseMat] representing the subgroup `A_n` of `S_n` over a given [Semiring].
     *
     * Unlike [PermMatAlgebras.alternating], it uses full [DenseMat] instead of the faster [PermMat].
     *
     * Note: the group operation here is composition (∘), implemented as reversed
     * matrix multiplication:
     * ```
     * X ∘ Y := Y * X
     * ```
     * This is chosen so that the natural map `p ↦ M_p` is a `GroupHomomorphism`
     * that aligns with `Permutation.andThen`:
     * ```
     * (p andThen q)(x) = q(p(x)).
     * ```
     *
     * If you want the usual linear-algebra group operation, use standard matrix
     * multiplication (*) on permutation matrices instead.
     */
    fun <A : Any> alternatingSubgroup(
        semiring: Semiring<A>,
        n: Int,
        eq: Eq<A> = Eq.default()
    ): Subgroup<DenseMat<A>> {
        val g = symmetricGroup(semiring, n, eq)

        val zero = semiring.add.identity
        val one = semiring.mul.identity

        return g.subgroup { m ->
            isEvenPermutationMatrix(m, zero, one, eq)
        }
    }

    /**
     * Returns true iff m is a permutation matrix with even parity.
     * Convention: for each column c, find unique row r with m[r,c] == 1, then p[c] = r.
     * Parity is even iff (n - numberOfCycles(p)) is even.
     */
    private fun <A : Any> isEvenPermutationMatrix(
        m: MatLike<A>,
        zero: A,
        one: A,
        eq: Eq<A>
    ): Boolean = m.toPermMat(zero, one, eq).map(PermMat::isEven).getOrElse(false)
}
