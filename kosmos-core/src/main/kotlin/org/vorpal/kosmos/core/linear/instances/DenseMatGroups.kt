package org.vorpal.kosmos.core.linear.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.linear.values.DenseMat
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

// TODO: Should I move these into classes and define them there? Or make a DenseMatAlgebra that does all that?
// TODO: There are so many structures I could create here.
/**
 * Define an Abelian additive group of rectangular matrices.
 */
object DenseMatGroups {
    fun <A : Any> additiveAbelianGroup(
        group: AbelianGroup<A>,
        rows: Int,
        cols: Int
    ): AbelianGroup<DenseMat<A>> {
        DenseKernel.checkNonnegative(rows, cols)

        val zero = DenseMatKernel.constMat(group.identity, rows, cols)
        val add = addOp(group, rows, cols)
        val neg = Endo(Symbols.MINUS) { x: DenseMat<A> ->
            DenseMatKernel.checkSize(x, rows, cols)
            x.map(group.inverse::invoke)
        }

        return AbelianGroup.of(
            identity = zero,
            op = add,
            inverse = neg
        )
    }

    /**
     * In the case where we don't have a base abelian group (e.g. a Semiring), we can still
     * create a CommutativeMonoid in essentially the same way..
     */
    fun <A : Any> additiveCommutativeMonoid(
        monoid: CommutativeMonoid<A>,
        rows: Int,
        cols: Int
    ): CommutativeMonoid<DenseMat<A>> {
        DenseKernel.checkNonnegative(rows, cols)

        val zero = DenseMatKernel.constMat(monoid.identity, rows, cols)
        val add = addOp(monoid, rows, cols)

        return CommutativeMonoid.of(
            identity = zero,
            op = add
        )
    }

    /**
     * Given a semiring over A, we can produce a multiplicative monoid over square matrices of A.
     */
    fun <A : Any> multiplicativeMonoid(
        semiring: Semiring<A>,
        n: Int
    ): Monoid<DenseMat<A>> {
        DenseKernel.checkNonnegative(n)
        return Monoid.of(
            identity = DenseMatKernel.identity(semiring, n),
            op = mulOp(semiring, n)
        )
    }

    private fun <A : Any> addOp(
        monoid: Monoid<A>,
        rows: Int,
        cols: Int
    ): BinOp<DenseMat<A>> =
        BinOp(Symbols.PLUS) { x, y ->
            DenseMatKernel.checkSize(x, rows, cols)
            DenseMatKernel.checkSize(y, rows, cols)
            x.zipWith(y, monoid::invoke)
        }

    private fun <A : Any> mulOp(
        semiring: Semiring<A>,
        n: Int
    ): BinOp<DenseMat<A>> =
        BinOp(Symbols.ASTERISK) { x, y ->
            DenseMatKernel.checkSize(x, n, n)
            DenseMatKernel.checkSize(y, n, n)
            DenseMatKernel.matMul(semiring, x, y)
        }
}
