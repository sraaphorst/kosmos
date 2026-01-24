package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.morphisms.GroupHomomorphism
import org.vorpal.kosmos.algebra.morphisms.GroupIsomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.algebra.structures.Semialgebra
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.MatLike

object DenseMatAlgebras {
    // ------------------------------------
    // ---------- BinOp creators ----------
    // ------------------------------------

    /**
     * Given a [Monoid] over [A], create the additive [BinOp] for `m x n` [DenseMat] over [A].
     */
    private fun <A : Any> addOp(
        monoid: Monoid<A>,
        rows: Int,
        cols: Int
    ): BinOp<DenseMat<A>> = BinOp(Symbols.PLUS) { x, y ->
        DenseMatKernel.checkSize(x, rows, cols)
        DenseMatKernel.checkSize(y, rows, cols)
        x.zipWith(y, monoid::invoke)
    }

    /**
     * Given a [Semiring] over [A], create the multiplicative [BinOp] for `n x n` [DenseMat] over [A].
     */
    private fun <A : Any> mulOp(
        semiring: Semiring<A>,
        n: Int
    ): BinOp<DenseMat<A>> = BinOp(Symbols.ASTERISK) { x, y ->
        DenseMatKernel.checkSize(x, n, n)
        DenseMatKernel.checkSize(y, n, n)
        DenseMatKernel.matMul(semiring, x, y)
    }


    // -------------------------------------------
    // ---------- One operator creators ----------
    // -------------------------------------------

    /**
     * Given an [AbelianGroup] over [A], create an additive [AbelianGroup] of `m x n` [DenseMat] over [A].
     */
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
     * Given a [CommutativeMonoid] over [A], create an additive [CommutativeMonoid] of `m x n` [DenseMat] over [A].
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
     * Given a [Semiring] over [A], create a multiplicative [Monoid] of `n x n` [DenseMat] over [A].
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


    // -------------------------------------------
    // ---------- Two operator creators ----------
    // -------------------------------------------

    /**
     * Given a [Semiring] over [A], create a [Semiring] of `n x n` [DenseMat] over [A] consisting of:
     * - An additive [CommutativeMonoid]
     * - A multiplicative [Monoid].
     */
    class DenseMatSemiring<A : Any>(
        val entries: Semiring<A>,
        dimension: Int
    ): Semiring<DenseMat<A>>, MatrixDimensionality {

        init {
            DenseKernel.checkNonnegative(dimension)
        }

        override val rows = dimension
        override val cols = dimension

        override val add: CommutativeMonoid<DenseMat<A>> = additiveCommutativeMonoid(
            monoid = entries.add,
            rows = dimension,
            cols = dimension
        )

        override val mul: Monoid<DenseMat<A>> = multiplicativeMonoid(
            semiring = entries,
            n = dimension
        )
    }


    /**
     * Given a [Ring] over [A], create a [Ring] of `n x n` [DenseMat] over [A] consisting of:
     * - An additive [AbelianGroup]
     * - A multiplicative [Monoid].
     */
    class DenseMatRing<A : Any>(
        val entries: Ring<A>,
        dimension: Int
    ): Ring<DenseMat<A>>, MatrixDimensionality {

        init {
            DenseKernel.checkNonnegative(dimension)
        }

        override val rows = dimension
        override val cols = dimension

        override val add: AbelianGroup<DenseMat<A>> = additiveAbelianGroup(
            group = entries.add,
            rows = dimension,
            cols = dimension
        )

        override val mul: Monoid<DenseMat<A>> = multiplicativeMonoid(
            semiring = entries,
            n = dimension
        )
    }


    // --------------------------------------
    // ---------- Algebra creators ----------
    // --------------------------------------

    /**
     * Given a [CommutativeSemiring] over [R] of [scalars], create an R-[Semialgebra] of `n x n` [DenseMat] over [R]
     * consisting of:
     * - An additive [CommutativeMonoid]
     * - A multiplicative [Monoid]
     * - A [LeftAction] scaling the [DenseMat].
     */
    class DenseMatSemialgebra<R : Any>(
        override val scalars: CommutativeSemiring<R>,
        dimension: Int
    ) : Semialgebra<R, DenseMat<R>>, MatrixDimensionality {

        init {
            DenseKernel.checkNonnegative(dimension)
        }

        override val rows = dimension
        override val cols = dimension

        val entries: CommutativeSemiring<R>
            get() = scalars

        override val add = additiveCommutativeMonoid(
            monoid = scalars.add,
            rows = dimension,
            cols = dimension
        )

        override val mul = multiplicativeMonoid(
            semiring = scalars,
            n = dimension
        )

        override val leftAction: LeftAction<R, DenseMat<R>> =
            LeftAction { r, m ->
                DenseMatKernel.checkSize(m, dimension, dimension)
                m.map { a -> scalars.mul(r, a) }
            }
    }


    /**
     * Given a [CommutativeRing] over [R] of [scalars], create an R-algebra of `n x n` [DenseMat] over [R] consisting of:
     * - An additive [AbelianGroup]
     * - A multiplicative [Monoid]
     * - A [LeftAction] scaling the [DenseMat].
     */
    class DenseMatAlgebra<R : Any>(
        override val scalars: CommutativeRing<R>,
        dimension: Int
    ) : Algebra<R, DenseMat<R>>, MatrixDimensionality {

        init {
            DenseKernel.checkNonnegative(dimension)
        }

        override val rows: Int = dimension
        override val cols: Int = dimension

        override val add = additiveAbelianGroup(
            group = scalars.add,
            rows = dimension,
            cols = dimension
        )

        override val mul = multiplicativeMonoid(
            semiring = scalars,
            dimension
        )

        override val leftAction: LeftAction<R, DenseMat<R>> =
            LeftAction { r, m ->
                DenseMatKernel.checkSize(m, dimension, dimension)
                m.map { a -> scalars.mul(r, a) }
            }
    }


    // -----------------------------------------
    // ---------- Hadamard Structures ----------
    // -----------------------------------------

    // -------------------------------------------
    // ---------- One operator creators ----------
    // -------------------------------------------

    /**
     * Given an [AbelianGroup] over [A], create a multiplicative [AbelianGroup] of `m x n` [DenseMat] over [A].
     */
    private fun <A : Any> hadamardAbelianGroup(
        add: AbelianGroup<A>,
        rows: Int,
        cols: Int,
    ): AbelianGroup<DenseMat<A>> = AbelianGroup.of(
        identity = DenseMatKernel.constMat(add.identity, rows, cols),
        op = BinOp(Symbols.PLUS) { x, y ->
            DenseMatKernel.checkSize(x, rows, cols)
            DenseMatKernel.checkSize(y, rows, cols)
            x.zipWith(y, add::invoke)
        },
        inverse = Endo(Symbols.MINUS) { x ->
            DenseMatKernel.checkSize(x, rows, cols)
            x.map(add.inverse::invoke)
        }
    )


    /**
     * The semiring structure on `r×c` matrices over a base semiring [A]
     * using:
     * - addition = entrywise addition
     * - multiplication = Hadamard (entrywise) multiplication
     *
     * Additive identity is the all-zero matrix.
     * Multiplicative identity is the all-ones matrix.
     *
     * This is the direct-product semiring `A^(r*c)`.
     */
    class DenseMatHadamardSemiring<A : Any>(
        val entries: Semiring<A>,
        override val rows: Int,
        override val cols: Int,
    ) : Semiring<DenseMat<A>>, MatrixDimensionality {

        init {
            DenseKernel.checkNonnegative(rows, cols)
        }

        override val add: CommutativeMonoid<DenseMat<A>> =
            CommutativeMonoid.of(
                identity = DenseMatKernel.constMat(entries.add.identity, rows, cols),
                op = BinOp(Symbols.PLUS) { x, y ->
                    DenseMatKernel.checkSize(x, rows, cols)
                    DenseMatKernel.checkSize(y, rows, cols)
                    x.zipWith(y, entries.add::invoke)
                }
            )

        override val mul: Monoid<DenseMat<A>> =
            Monoid.of(
                identity = DenseMatKernel.constMat(entries.mul.identity, rows, cols),
                op = BinOp(Symbols.HADAMARD) { x, y ->
                    DenseMatKernel.checkSize(x, rows, cols)
                    DenseMatKernel.checkSize(y, rows, cols)
                    x.zipWith(y, entries.mul::invoke)
                }
            )
    }



    /**
     * The ring structure on r×c matrices over a base ring [A]
     * using:
     * - addition = entrywise addition (additive abelian group)
     * - multiplication = Hadamard (entrywise) multiplication
     *
     * Additive identity is the all-zero matrix.
     * Multiplicative identity is the all-ones matrix.
     *
     * This is the direct-product ring `A^(r*c)`.
     */
    class DenseMatHadamardRing<A : Any>(
        val entries: Ring<A>,
        override val rows: Int,
        override val cols: Int,
    ) : Ring<DenseMat<A>>, MatrixDimensionality {

        init {
            DenseKernel.checkNonnegative(rows, cols)
        }

        override val add: AbelianGroup<DenseMat<A>> = hadamardAbelianGroup(
            entries.add, rows, cols
        )

        override val mul: Monoid<DenseMat<A>> =
            Monoid.of(
                identity = DenseMatKernel.constMat(entries.mul.identity, rows, cols),
                op = BinOp(Symbols.HADAMARD) { x, y ->
                    DenseMatKernel.checkSize(x, rows, cols)
                    DenseMatKernel.checkSize(y, rows, cols)
                    x.zipWith(y, entries.mul::invoke)
                }
            )
    }


    /**
     * The commutative ring structure on r×c matrices over a base commutative ring [A]
     * using:
     * - addition = entrywise addition (additive abelian group)
     * - multiplication = Hadamard (entrywise) multiplication
     *
     * Additive identity is the all-zero matrix.
     * Multiplicative identity is the all-ones matrix.
     *
     * This is the direct-product commutative ring `A^(r*c)`.
     */
    class DenseMatHadamardCommutativeRing<A : Any>(
        val entries: CommutativeRing<A>,
        override val rows: Int,
        override val cols: Int,
    ) : CommutativeRing<DenseMat<A>>, MatrixDimensionality {

        init {
            DenseKernel.checkNonnegative(rows, cols)
        }

        override val add: AbelianGroup<DenseMat<A>> = hadamardAbelianGroup(
            entries.add, rows, cols
        )

        override val mul: CommutativeMonoid<DenseMat<A>> = CommutativeMonoid.of(
            identity = DenseMatKernel.constMat(entries.mul.identity, rows, cols),
            op = BinOp(Symbols.HADAMARD) { x, y ->
                DenseMatKernel.checkSize(x, rows, cols)
                DenseMatKernel.checkSize(y, rows, cols)
                x.zipWith(y, entries.mul::invoke)
            }
        )
    }


    /**
     * The multiplicative abelian group of Hadamard-units in `A^(r*c)`, for a base field [A].
     *
     * Carrier: `r×c` matrices with *no zero entries*.
     *
     * Operation: Hadamard (entrywise) multiplication.
     * Identity: all-ones matrix.
     * Inverse: entrywise reciprocal.
     *
     * This is isomorphic to `(A^*)^(r*c)`, i.e. a direct product of the field’s multiplicative group.
     */
    class DenseMatHadamardUnitGroup<A : Any>(
        private val field: Field<A>,
        override val rows: Int,
        override val cols: Int,
    ) : AbelianGroup<DenseMat<A>>, MatrixDimensionality {

        init {
            DenseKernel.checkNonnegative(rows, cols)
        }

        override val identity: DenseMat<A> =
            DenseMatKernel.constMat(field.mul.identity, rows, cols)

        private fun requireUnit(x: DenseMat<A>) {
            DenseMatKernel.checkSize(x, rows, cols)

            var r = 0
            while (r < rows) {
                var c = 0
                while (c < cols) {
                    require(x[r, c] != field.zero) {
                        "Hadamard unit required (no zero entries). Found 0 at ($r, $c)."
                    }
                    c += 1
                }
                r += 1
            }
        }

        override val op: BinOp<DenseMat<A>> =
            BinOp(Symbols.HADAMARD) { x, y ->
                requireUnit(x)
                requireUnit(y)

                // Product of nonzeros in a field is nonzero, so closure holds.
                x.zipWith(y, field.mul::invoke)
            }

        override val inverse: Endo<DenseMat<A>> = Endo(Symbols.INVERSE) { x ->
            requireUnit(x)
            hadamardReciprocal(field, x, rows, cols)
        }
    }


    /**
     * Determine the `r×c` matrix inverse for the Hadamard unit group in the field over [A], which exists iff
     * the matrix has no zero entries (throws if any entry is zero).
     */
    fun <A : Any> hadamardReciprocal(
        field: Field<A>,
        x: MatLike<A>,
        rows: Int,
        cols: Int,
    ): DenseMat<A> {
        DenseMatKernel.checkSize(x, rows, cols)

        return DenseMat.tabulate(rows, cols) { r, c ->
            val a = x[r, c]
            require(a != field.zero) { "Hadamard reciprocal requires no zero entries. Found 0 at ($r, $c)." }
            field.reciprocal(a)
        }
    }


    /**
     * Since [MatLike] already carries its size, it isn't necessary to pass in unless we want to ensure that
     * a fixed size is used.
     */
    fun <A: Any> hadamardReciprocal(field: Field<A>, x: MatLike<A>): DenseMat<A> =
        hadamardReciprocal(field, x, x.rows, x.cols)


    /**
     * An isomorphism between:
     * - the [AbelianGroup] of `r×c` matrices over the units of a [Field] [A] under the hadamard product; and
     * - the [AbelianGroup] of `rc` vectors over the units of a [Field] [A] under the hadamard product.
     */
    fun <A : Any> hadamardUnitMatToVecIsomorphism(
        field: Field<A>,
        rows: Int,
        cols: Int,
    ): GroupIsomorphism<DenseMat<A>, DenseVec<A>> {
        DenseKernel.checkNonnegative(rows, cols)

        val matUnits = DenseMatHadamardUnitGroup(
            field = field,
            rows = rows,
            cols = cols
        )

        val vecUnits = DenseVecAlgebras.DenseVecHadamardUnitGroup(
            field = field,
            dimension = rows * cols
        )

        // mat -> vec (row-major)
        // inverse exists only for unit mats; the group op will enforce unit-ness when used.
        val forward: GroupHomomorphism<DenseMat<A>, DenseVec<A>> = GroupHomomorphism.of(
            domain = matUnits,
            codomain = vecUnits,
            map = DenseMatKernel::flattenRowMajor
        )

        val backward: GroupHomomorphism<DenseVec<A>, DenseMat<A>> = GroupHomomorphism.of(
            domain = vecUnits,
            codomain = matUnits,
        ) { v -> DenseMatKernel.unflattenRowMajor(v, rows, cols) }

        return GroupIsomorphism.of(forward = forward, backward = backward)
    }
}
