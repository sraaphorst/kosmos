package org.vorpal.kosmos.core.linear.instances

import org.vorpal.kosmos.algebra.morphisms.GroupHomomorphism
import org.vorpal.kosmos.algebra.morphisms.GroupIsomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Dimensionality
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.linear.values.DenseMat
import org.vorpal.kosmos.core.linear.values.DenseVec
import org.vorpal.kosmos.core.linear.values.MatLike
import org.vorpal.kosmos.core.linear.values.VecLike
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

/**
 * Algebraic structures over DenseMat using the [DenseMatKernel.hadamard] product.
 */
object DenseMatHadamardAlgebras {
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

    private fun <A : Any> hadamardAbelianGroup(
        add: AbelianGroup<A>,
        rows: Int,
        cols: Int
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

        override val mul: CommutativeMonoid<DenseMat<A>> =
            CommutativeMonoid.of(
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
        override val dimension: Int
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
            requireUnit(x)
            requireUnit(y)
            x.zipWith(y) { a, b -> field.mul(a, b) }
        }

        override val inverse = Endo(Symbols.INVERSE) { x: DenseVec<A> ->
            requireUnit(x)
            DenseVec.tabulate(dimension) { i -> field.reciprocal(x[i]) }
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
     * Determine if a matrix is a unit under the Hadamard operation.
     */
    fun <A : Any> isHadamardUnit(
        field: Field<A>,
        x: MatLike<A>
    ): Boolean {
        var r = 0
        while (r < x.rows) {
            var c = 0
            while (c < x.cols) {
                if (x[r, c] == field.zero) return false
                c += 1
            }
            r += 1
        }
        return true
    }

    /**
     * An isomorphism between
     * - the [AbelianGroup] of `r×c` matrices over the units of a [Field] [A] under the hadamard product
     * - the [AbelianGroup] of `rc` vectors over the units of a [Field] [A] under the hadamard product
     */
    fun <A : Any> hadamardUnitMatToVecIsomorphism(
        field: Field<A>,
        rows: Int,
        cols: Int
    ): GroupIsomorphism<DenseMat<A>, DenseVec<A>> {
        DenseKernel.checkNonnegative(rows, cols)

        val size = rows * cols

        val matUnits = DenseMatHadamardUnitGroup(
            field = field,
            rows = rows,
            cols = cols
        )

        val vecUnits = DenseVecHadamardUnitGroup(
            field = field,
            dimension = size
        )

        // mat -> vec (row-major)
        // inverse exists only for unit mats; the group op will enforce unit-ness when used.
        val forward: GroupHomomorphism<DenseMat<A>, DenseVec<A>> = GroupHomomorphism.of(
            domain = matUnits,
            codomain = vecUnits,
            map = DenseMatKernel::flattenRowMajor)

        val backward: GroupHomomorphism<DenseVec<A>, DenseMat<A>> = GroupHomomorphism.of(
            domain = vecUnits,
            codomain = matUnits,
        ) { v -> DenseMatKernel.unflattenRowMajor(v, rows, cols) }

        return GroupIsomorphism.of(forward = forward, backward = backward)
    }
}
