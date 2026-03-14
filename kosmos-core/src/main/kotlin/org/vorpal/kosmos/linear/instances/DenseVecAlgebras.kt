package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Dimensionality
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.getOrElse
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.VecLike
import kotlin.require

object DenseVecAlgebras {
    /**
     * Build the additive abelian group structure on `A^n` (dense coordinate vectors),
     * given an additive abelian group structure on `A`.
     */
    class DenseVecGroup<A : Any>(
        val baseGroup: AbelianGroup<A>,
        override val dimension: Int
    ): AbelianGroup<DenseVec<A>>, Dimensionality {
        init {
            DenseKernel.checkNonnegative(dimension)
        }

        override val identity: DenseVec<A> = DenseVec.tabulate(dimension) { baseGroup.identity }

        override val op: BinOp<DenseVec<A>> = BinOp(Symbols.PLUS) { u, v ->
            DenseKernel.requireSize(u.size, dimension)
            DenseKernel.requireSize(v.size, dimension)
            u.zipWith(v, baseGroup::invoke)
        }

        override val inverse: Endo<DenseVec<A>> = Endo(Symbols.MINUS) { v: DenseVec<A> ->
            DenseKernel.requireSize(v.size, dimension)
            v.map(baseGroup.inverse::invoke)
        }
    }

    /**
     * Canonical finite-dimensional vector spaces F^n using [DenseVec] coordinates.
     */
    class DenseVectorSpace<F : Any>(
        override val scalars: Field<F>,
        override val dimension: Int
    ): FiniteVectorSpace<F, DenseVec<F>> {
        override val add: AbelianGroup<DenseVec<F>> =
            DenseVecGroup(scalars.add, dimension)

        override val leftAction: LeftAction<F, DenseVec<F>> = LeftAction { s, v ->
            DenseKernel.requireSize(v.size, dimension)
            v.map { x -> scalars.mul(s, x) }
        }
    }

    private fun <A : Any> hadamardOp(
        mul: Monoid<A>,
        size: Int
    ): BinOp<DenseVec<A>> =
        BinOp(Symbols.HADAMARD) { x, y ->
            DenseKernel.requireSize(x.size, size)
            DenseKernel.requireSize(y.size, size)
            DenseVecKernel.hadamard(mul, x, y)
        }


    fun <A : Any> hadamardMonoid(
        monoid: Monoid<A>,
        dimension: Int
    ): Monoid<DenseVec<A>> = Monoid.of(
        identity = DenseVecKernel.constantVec(monoid.identity, dimension),
        op = BinOp(Symbols.HADAMARD) { u, v ->
            DenseKernel.requireSize(u.size, dimension)
            DenseKernel.requireSize(v.size, dimension)
            DenseVecKernel.hadamard(monoid, u, v)
        }
    )


    fun <A : Any> hadamardMonoid(
        commutativeMonoid: CommutativeMonoid<A>,
        dimension: Int
    ): CommutativeMonoid<DenseVec<A>> = CommutativeMonoid.of(
        identity = DenseVecKernel.constantVec(commutativeMonoid.identity, dimension),
        op = BinOp(Symbols.HADAMARD) { u, v ->
            DenseKernel.requireSize(u.size, dimension)
            DenseKernel.requireSize(v.size, dimension)
            DenseVecKernel.hadamard(commutativeMonoid, u, v)
        }
    )

    /**
     * The multiplicative abelian group of Hadamard-units in `A^(r*c)`, for a base field [A].
     *
     * Carrier: `rc` vectors with *no zero entries*.
     *
     * Operation: Hadamard (entrywise) multiplication.
     * Identity: all-ones vector.
     * Inverse: entrywise reciprocal.
     */
    class DenseVecHadamardUnitGroup<A : Any>(
        private val field: Field<A>,
        override val dimension: Int,
    ) : AbelianGroup<DenseVec<A>>, Dimensionality {

        init {
            DenseKernel.checkNonnegative(dimension)
        }

        override val identity: DenseVec<A> = DenseVec.tabulate(dimension) { field.mul.identity }

        private fun requireUnit(v: VecLike<A>) {
            DenseKernel.requireSize(v.size, dimension)
            var i = 0
            while (i < dimension) {
                require(v[i] != field.zero) {
                    "Hadamard unit required (no zero entries). Found 0 at index $i."
                }
                i += 1
            }
        }

        override val op = BinOp(Symbols.HADAMARD) { x: DenseVec<A>, y: DenseVec<A> ->
            DenseKernel.requireSize(x.size, dimension)
            DenseKernel.requireSize(y.size, dimension)
            require(DenseVecKernel.isHadamardUnit(field, x)) { "Vector is not Hadamard-invertible." }
            require(DenseVecKernel.isHadamardUnit(field, y)) { "Vector is not Hadamard-invertible." }
            DenseVecKernel.hadamard(field.mul, x, y)
        }

        override val inverse = Endo(Symbols.INVERSE) { x: DenseVec<A> ->
            requireUnit(x)
            DenseVec.tabulate(dimension) { i -> field.reciprocal(x[i]) }
        }
    }

    private fun ellipsis(exceeded: Boolean) =
        if (exceeded) ", ${Symbols.ELLIPSIS}" else ""

    /**
     * Lift a Printable<A> into a Printable<DenseVec<A>> with strict printing.
     *
     * The optional [maxEntries] parameter limits how many entries are shown.
     * If the vector is longer, an ellipsis is appended.
     *
     * Example:
     * ```kotlin
     * val v = DenseVec.of(1.0, 2.2, -3.333, 4.0, -5.55555, 6.0)
     * println(DenseVecAlgebras.liftPrintableStrict(Printable.default<Real>(), maxEntries = Option.Some(4))(v))
     * ```
     *
     * Output:
     * ```text
     * 6 [1.0, 2.2, -3.333, 4.0, …]
     * ```
     */
    fun <A : Any> liftPrintableStrict(
        prA: Printable<A>,
        maxEntries: Option<Int> = Option.Some(12)
    ): Printable<DenseVec<A>> =
        Printable { v ->
            val limit = minOf(v.size, maxEntries.getOrElse(Int.MAX_VALUE))

            val body = (0 until limit)
                .joinToString(
                    prefix = "[",
                    postfix = "${ellipsis(v.size > limit)}]"
                ) { i ->
                    prA(v[i])
                }

            "${v.size} $body"
        }

    /**
     * Lift a Printable<A> into a Printable<DenseVec<A>> with pretty-printing.
     *
     * The optional [maxEntries] parameter limits how many entries are shown.
     * If the vector is longer, an ellipsis is appended.
     *
     * Example:
     * ```kotlin
     * val v = DenseVec.of(1.0, 2.2, -3.333, 4.0, -5.55555, 6.0)
     * println(DenseVecAlgebras.liftPrintablePretty(Printable.default<Real>(), maxEntries = Option.Some(4))(v))
     * ```
     *
     * Output:
     * ```text
     * 6-vector:
     *    [  1.0,   2.2, -3.333,   4.0, …]
     * ```
     */
    fun <A : Any> liftPrintablePretty(
        prA: Printable<A>,
        maxEntries: Option<Int> = Option.Some(12)
    ): Printable<DenseVec<A>> =
        Printable { v ->
            val limit = minOf(v.size, maxEntries.getOrElse(Int.MAX_VALUE))

            val rendered = (0 until limit).map { i -> prA(v[i]) }

            val width = rendered.maxOfOrNull { it.length } ?: 1

            val body = rendered
                .map { it.padStart(width) }
                .joinToString(
                    prefix = "   [",
                    postfix = if (v.size > limit) ", …]" else "]",
                    separator = ", "
                )

            "${v.size}-vector:\n$body"
        }

    fun <A : Any> liftEq(eqA: Eq<A>): Eq<DenseVec<A>> =
        Eq { x, y ->
            if (x.size != y.size) false
            else (0 until x.size).all { i -> eqA(x[i], y[i]) }
        }
}
