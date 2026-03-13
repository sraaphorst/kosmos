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
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.linear.values.DenseMat
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.functional.datastructures.Option
import org.vorpal.kosmos.functional.datastructures.getOrElse
import org.vorpal.kosmos.linear.values.DenseVec
import org.vorpal.kosmos.linear.values.MatLike

object DenseMatAlgebras {
    // ------------------------------------
    // ---------- BinOp creators ----------
    // ------------------------------------

    /**
     * Given a [Monoid] over [A], create the additive [BinOp] for `m×n` [DenseMat] over [A].
     */
    private fun <A : Any> addOp(
        monoid: Monoid<A>,
        rows: Int,
        cols: Int
    ): BinOp<DenseMat<A>> = BinOp(Symbols.PLUS) { x, y ->
        DenseMatKernel.requireSize(x, rows, cols)
        DenseMatKernel.requireSize(y, rows, cols)
        DenseMatKernel.entrywise(monoid, x, y)
    }


    /**
     * Given a [Semiring] over [A], create the multiplicative [BinOp] for `n×n` [DenseMat]s over [A].
     */
    private fun <A : Any> mulOp(
        semiring: Semiring<A>,
        n: Int
    ): BinOp<DenseMat<A>> = BinOp(Symbols.ASTERISK) { x, y ->
        DenseMatKernel.requireSize(x, n, n)
        DenseMatKernel.requireSize(y, n, n)
        DenseMatKernel.matMul(semiring, x, y)
    }


    /**
     * Given an [AbelianGroup] over [A], create the inverse [Endo] for `n×n` [DenseMat]s over [A].
     */
    private fun <A : Any> negativeOp(
        group: AbelianGroup<A>,
        rows: Int,
        cols: Int
    ): Endo<DenseMat<A>> = Endo(Symbols.MINUS) { x ->
        DenseMatKernel.requireSize(x, rows, cols)
        DenseMatKernel.negateEntries(group, x)
    }


    // -------------------------------------------
    // ---------- One operator creators ----------
    // -------------------------------------------

    /**
     * Given an [AbelianGroup] over [A], create an additive [AbelianGroup] of `m×n` [DenseMat] over [A].
     */
    fun <A : Any> additiveAbelianGroup(
        group: AbelianGroup<A>,
        rows: Int,
        cols: Int
    ): AbelianGroup<DenseMat<A>> {
        DenseKernel.checkNonnegative(rows, cols)
        return AbelianGroup.of(
            identity = DenseMatKernel.constMat(group.identity, rows, cols),
            op = addOp(group, rows, cols),
            inverse = negativeOp(group, rows, cols)
        )
    }

    /**
     * Given a [CommutativeMonoid] over [A], create an additive [CommutativeMonoid] of `m×n` [DenseMat] over [A].
     */
    fun <A : Any> additiveCommutativeMonoid(
        monoid: CommutativeMonoid<A>,
        rows: Int,
        cols: Int
    ): CommutativeMonoid<DenseMat<A>> {
        DenseKernel.checkNonnegative(rows, cols)
        return CommutativeMonoid.of(
            identity = DenseMatKernel.constMat(monoid.identity, rows, cols),
            op = addOp(monoid, rows, cols)
        )
    }


    /**
     * Given a [Semiring] over [A], create a multiplicative [Monoid] of `n×n` [DenseMat] over [A].
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
     * Given a [Semiring] over [A], create a [Semiring] of `n×n` [DenseMat] over [A] consisting of:
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
     * Given a [CommutativeSemiring] over [R] of [scalars], create an R-[Semialgebra] of `n×n` [DenseMat] over [R]
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
                DenseMatKernel.requireSize(m, dimension, dimension)
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
                DenseMatKernel.requireSize(m, dimension, dimension)
                m.map { a -> scalars.mul(r, a) }
            }
    }


    // -----------------------------------------
    // ---------- Hadamard Structures ----------
    // -----------------------------------------

    private fun <A : Any> hadamardOp(
        mul: Monoid<A>,
        rows: Int,
        cols: Int
    ): BinOp<DenseMat<A>> =
        BinOp(Symbols.HADAMARD) { x, y ->
            DenseMatKernel.requireSize(x, rows, cols)
            DenseMatKernel.requireSize(y, rows, cols)
            DenseMatKernel.entrywise(mul, x, y)
        }

    /**
     * Pointwise multiplication using a [Monoid].
     */
    private fun <A : Any> hadamardMonoid(
        mul: Monoid<A>,
        rows: Int,
        cols: Int
    ): Monoid<DenseMat<A>> = Monoid.of(
        identity = DenseMatKernel.constMat(mul.identity, rows, cols),
        op = hadamardOp(mul, rows, cols)
    )


    /**
     * Pointwise multiplication using a [CommutativeMonoid].
     */
    private fun <A : Any> hadamardMonoid(
        mul: CommutativeMonoid<A>,
        rows: Int,
        cols: Int
    ): CommutativeMonoid<DenseMat<A>> = CommutativeMonoid.of(
        identity = DenseMatKernel.constMat(mul.identity, rows, cols),
        op = hadamardOp(mul, rows, cols)
    )


    /**
     * The [Semiring] structure on `r×c` [DenseMat] over a base semiring [A] using:
     * - an additive [CommutativeMonoid]
     * - the [hadamardMonoid] entrywise multiplication, which gives a [Monoid].
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
            additiveCommutativeMonoid(entries.add, rows, cols)

        override val mul: Monoid<DenseMat<A>> =
            hadamardMonoid(entries.mul, rows, cols)
    }


    /**
     * The [Ring] structure on `r×c` [DenseMat] over a base ring [A] using:
     * - an additive [AbelianGroup]
     * - the [hadamardMonoid] entrywise multiplication, which gives a [CommutativeMonoid].
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

        override val add: AbelianGroup<DenseMat<A>> =
            additiveAbelianGroup(entries.add, rows, cols)

        override val mul: Monoid<DenseMat<A>> =
            hadamardMonoid(entries.mul, rows, cols)
    }


    /**
     * The commutative ring structure on r×c matrices over a base commutative ring [A]
     * using:
     * - addition = entrywise addition (additive abelian group)
     * - multiplication = Hadamard (entrywise) multiplication
     *
     * Additive identity is the all-zero matrix.
     *
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

        override val add: AbelianGroup<DenseMat<A>> =
            additiveAbelianGroup(entries.add, rows, cols)

        override val mul: CommutativeMonoid<DenseMat<A>> =
            hadamardMonoid(entries.mul, rows, cols)
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
            DenseMatKernel.requireSize(x, rows, cols)
            require(DenseMatKernel.isHadamardUnit(field, x)) {
                "Hadamard unit required (no zero entries allowed)."
            }
        }

        override val op: BinOp<DenseMat<A>> =
            BinOp(Symbols.HADAMARD) { x, y ->
                requireUnit(x)
                requireUnit(y)
                DenseMatKernel.entrywise(field.mul, x, y)
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
        DenseMatKernel.requireSize(x, rows, cols)

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


    // ------------------------
    // ----- Isomorphisms -----
    // ------------------------

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

    private fun ellipsis(exceeded: Boolean) = if (exceeded) ", ${Symbols.ELLIPSIS}" else ""

    /**
     * Lift a Printable<A> into a Printable<DenseMat<A>> with strict printing.
     *
     * Furthermore, we accept a maxRows and maxCols parameter to limit the size of the printed matrix. If the matrix
     * ends up larger, we print ellipses for the remaining entries.
     *
     * If maxRows or maxCols is null, the matrix will be printed in full.
     *
     * For example, if we have the matrix:
     * ```kotlin
     * val mat = DenseMat.ofRows(
     *         listOf(
     *             listOf(1.0, 2.2, -3.333, 4.0, -5.55555, 6.0),
     *             listOf(7.7, 8.88, -9.9, 10.1010, 11.11111, 12.1),
     *             listOf(13.13, 14.1414, -15.151515, 16.161616, 17.171717, 18.181818),
     *             listOf(19.1919, 20.20202, -21.212121, 22.222222, 23.232323, 24.242424)
     *         )
     *     )
     * ```
     *
     * and we use:
     * ```kotlin
     * println(DenseMatAlgebras.liftPrintableStrict(Printable.default<Real>(), maxRows = 3, maxCols = 3)(mat))
     *```
     *
     * Example output:
     * ```
     * 4×6 [[1.0, 2.2, -3.333, 4.0, -5.55555, …], [7.7, 8.88, -9.9, 10.101, 11.11111, …], [13.13, 14.1414, -15.151515, 16.161616, 17.171717, …], …]
     * ```
     */
    fun <A : Any> liftPrintableStrict(
        prA: Printable<A>,
        maxRows: Option<Int> = Option.Some(6),
        maxCols: Option<Int> = Option.Some(6)
    ): Printable<DenseMat<A>> =
        Printable { m ->
            val rLim = maxRows.getOrElse(Int.MAX_VALUE)
            val cLim = maxCols.getOrElse(Int.MAX_VALUE)

            val body = (0 until rLim)
                .joinToString(prefix = "[", postfix = "${ellipsis(m.rows > rLim)}]") { r ->
                    (0 until cLim)
                        .joinToString(prefix = "[", postfix = "${ellipsis(m.cols > cLim)}]") { c ->
                            prA(m[r, c])
                        }
                }

            "${m.rows}${Symbols.TIMES}${m.cols} $body"
        }

    /**
     * Lift a Printable<A> into a Printable<DenseMat<A>> with pretty-printing.
     *
     * Furthermore, we accept a [maxRows] and [maxCols] parameter to limit the size of the printed matrix. If the matrix
     * ends up larger, we print ellipses for the remaining entries.
     *
     * If maxRows or maxCols is null, the matrix will be printed in full.
     *
     * For example, if we have the matrix:
     *
     * ```kotlin
     * val mat = DenseMat.ofRows(
     *         listOf(
     *             listOf(1.0, 2.2, -3.333, 4.0, -5.55555, 6.0),
     *             listOf(7.7, 8.88, -9.9, 10.1010, 11.11111, 12.1),
     *             listOf(13.13, 14.1414, -15.151515, 16.161616, 17.171717, 18.181818),
     *             listOf(19.1919, 20.20202, -21.212121, 22.222222, 23.232323, 24.242424)
     *         )
     *     )
     * ```
     *
     * and we use:
     *
     * ```kotlin
     * println(DenseMatAlgebras.liftPrintablePretty(Printable.default<Real>(), maxRows = 3, maxCols = 3)(mat))
     *```
     *
     * we get:
     * ```text
     * 4×6 matrix:
     *    [  1.0,     2.2,     -3.333, …]
     *    [  7.7,    8.88,       -9.9, …]
     *    [13.13, 14.1414, -15.151515, …]
     *    [    ⋮,       ⋮,          ⋮, ⋱]
     * ```
     */
    fun <A : Any> liftPrintablePretty(
        prA: Printable<A>,
        maxRows: Option<Int> = Option.Some(6),
        maxCols: Option<Int> = Option.Some(6)
     ): Printable<DenseMat<A>> =
        Printable { m ->
            val rLim = maxRows.getOrElse(Int.MAX_VALUE)
            val cLim = maxCols.getOrElse(Int.MAX_VALUE)

            // Pre-render all visible entries
            val rendered = (0 until rLim).map { r ->
                (0 until cLim).map { c -> prA(m[r, c]) }
            }

            // Compute column widths
            val colWidths = (0 until cLim).map { c ->
                rendered.maxOf { row -> row[c].length }
            }

            fun formatRow(cells: List<String>, suffix: String): String =
                cells.mapIndexed { c, cell -> cell.padStart(colWidths[c]) }
                    .joinToString(prefix = "   [", postfix = "$suffix]", separator = ", ")

            val ellipsisRow = colWidths.map { w -> " ".repeat(w - 1) + "⋮" }

            val rows = rendered.map { row ->
                formatRow(row, if (m.cols > cLim) ", …" else "")
            } + if (m.rows > rLim) listOf(
                formatRow(ellipsisRow, if (m.cols > cLim) ", ⋱" else "")
            ) else emptyList()

            "${m.rows}${Symbols.TIMES}${m.cols} matrix:\n" + rows.joinToString(separator = "\n")
        }

    /**
     * Lift an Eq<A> into an Eq<DenseMat<A>>.
     */
    fun <A : Any> liftEq(eqA: Eq<A>): Eq<DenseMat<A>> =
        Eq { x, y ->
            if (x.rows != y.rows) false
            else if (x.cols != y.cols) false
            else (0 until x.size).all { i -> eqA(x.flatGet(i), y.flatGet(i)) }
        }
}
